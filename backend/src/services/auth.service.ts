import { SiweMessage } from "siwe";
import jwt from "jsonwebtoken";
import crypto from "crypto";
import { ENV } from "../config/env.js";
import prisma from "./prisma.service.js";

const ACCESS_TOKEN_EXPIRY = "15m";
const REFRESH_TOKEN_EXPIRY_DAYS = 30;
const NONCE_EXPIRY_MINUTES = 10;

export interface AccessTokenPayload {
  sub: string; // userId
  wallet: string; // smartWalletAddr
  owner: string; // ownerAddr
}

export class AuthService {
  // ──────────────────────────── Nonce ────────────────────────────

  /** Generate a random nonce, store it in DB with a short TTL. */
  static async generateNonce(): Promise<string> {
    const value = crypto.randomBytes(16).toString("hex");
    const expiresAt = new Date(Date.now() + NONCE_EXPIRY_MINUTES * 60_000);

    await prisma.nonce.create({ data: { value, expiresAt } });
    return value;
  }

  /** Consume a nonce — returns true if valid, deletes it either way. */
  static async consumeNonce(value: string): Promise<boolean> {
    const nonce = await prisma.nonce.findUnique({ where: { value } });
    if (!nonce) return false;

    // Always delete after lookup (single-use)
    await prisma.nonce.delete({ where: { id: nonce.id } });

    return nonce.expiresAt > new Date();
  }

  /** Cleanup expired nonces (call periodically or via cron). */
  static async purgeExpiredNonces(): Promise<number> {
    const { count } = await prisma.nonce.deleteMany({
      where: { expiresAt: { lt: new Date() } },
    });
    return count;
  }

  // ──────────────────────────── SIWE ────────────────────────────

  /**
   * Verify a SIWE signed message.
   * Returns the parsed SIWE fields (address, chainId, nonce, etc.)
   */
  static async verifySiweMessage(
    message: string,
    signature: string
  ): Promise<SiweMessage> {
    const siweMessage = new SiweMessage(message);
    const { data: fields } = await siweMessage.verify({ signature });

    // Validate the nonce was issued by us and hasn't expired
    const nonceValid = await this.consumeNonce(fields.nonce);
    if (!nonceValid) {
      throw new Error("Invalid or expired nonce");
    }

    return fields;
  }

  // ──────────────────────────── JWT ─────────────────────────────

  /** Sign an access token (short-lived). */
  static signAccessToken(payload: AccessTokenPayload): string {
    return jwt.sign(payload, ENV.JWT_SECRET, {
      expiresIn: ACCESS_TOKEN_EXPIRY,
    });
  }

  /** Verify and decode an access token. */
  static verifyAccessToken(token: string): AccessTokenPayload {
    return jwt.verify(token, ENV.JWT_SECRET) as AccessTokenPayload;
  }

  // ────────────────────── Refresh Tokens ────────────────────────

  /**
   * Create a new refresh token family (first login / new device).
   * A "family" groups tokens so we can revoke an entire chain if reuse is detected.
   */
  static async createRefreshToken(userId: string): Promise<string> {
    const token = crypto.randomBytes(32).toString("hex");
    const family = crypto.randomUUID();
    const expiresAt = new Date(
      Date.now() + REFRESH_TOKEN_EXPIRY_DAYS * 24 * 60 * 60_000
    );

    await prisma.refreshToken.create({
      data: { token, userId, family, expiresAt },
    });

    return token;
  }

  /**
   * Rotate a refresh token: revoke the old one, issue a new one in the same family.
   * If the incoming token was already revoked → token reuse detected → revoke entire family.
   */
  static async rotateRefreshToken(
    oldToken: string
  ): Promise<{ accessToken: string; refreshToken: string }> {
    const existing = await prisma.refreshToken.findUnique({
      where: { token: oldToken },
      include: { user: true },
    });

    if (!existing) {
      throw new Error("Refresh token not found");
    }

    // Reuse detection: if already revoked, someone stole the token chain
    if (existing.revoked) {
      await prisma.refreshToken.updateMany({
        where: { family: existing.family },
        data: { revoked: true },
      });
      throw new Error("Token reuse detected — family revoked");
    }

    if (existing.expiresAt < new Date()) {
      throw new Error("Refresh token expired");
    }

    // Revoke old token
    await prisma.refreshToken.update({
      where: { id: existing.id },
      data: { revoked: true },
    });

    // Issue new token in same family
    const newToken = crypto.randomBytes(32).toString("hex");
    const expiresAt = new Date(
      Date.now() + REFRESH_TOKEN_EXPIRY_DAYS * 24 * 60 * 60_000
    );

    await prisma.refreshToken.create({
      data: {
        token: newToken,
        userId: existing.userId,
        family: existing.family,
        expiresAt,
      },
    });

    // Issue new access token
    const accessToken = this.signAccessToken({
      sub: existing.userId,
      wallet: existing.user.smartWalletAddr,
      owner: existing.user.ownerAddr,
    });

    return { accessToken, refreshToken: newToken };
  }

  /** Revoke all refresh tokens for a user (logout everywhere). */
  static async revokeAllTokens(userId: string): Promise<void> {
    await prisma.refreshToken.updateMany({
      where: { userId, revoked: false },
      data: { revoked: true },
    });
  }

  /** Revoke a single token family (logout one device). */
  static async revokeFamily(family: string): Promise<void> {
    await prisma.refreshToken.updateMany({
      where: { family },
      data: { revoked: true },
    });
  }

  /** Cleanup expired refresh tokens. */
  static async purgeExpiredRefreshTokens(): Promise<number> {
    const { count } = await prisma.refreshToken.deleteMany({
      where: { expiresAt: { lt: new Date() } },
    });
    return count;
  }
}
