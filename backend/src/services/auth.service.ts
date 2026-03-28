import jwt from "jsonwebtoken";
import crypto from "crypto";
import { ENV } from "../config/env.js";
import prisma from "./prisma.service.js";

const ACCESS_TOKEN_EXPIRY = "15m";
const REFRESH_TOKEN_EXPIRY_DAYS = 30;
const RESET_TOKEN_EXPIRY_MINUTES = 15;

// bcrypt-like hashing using Node's built-in scrypt (no extra deps)
const SCRYPT_KEYLEN = 64;
const SCRYPT_COST = 16384;
const SCRYPT_BLOCK_SIZE = 8;
const SCRYPT_PARALLELIZATION = 1;

export interface AccessTokenPayload {
  sub: string; // userId
  wallet: string; // smartWalletAddr
  owner: string; // ownerAddr
}

export class AuthService {
  // ──────────────────────── Password Hashing ──────────────────────

  static async hashPassword(password: string): Promise<string> {
    const salt = crypto.randomBytes(16).toString("hex");
    return new Promise((resolve, reject) => {
      crypto.scrypt(
        password,
        salt,
        SCRYPT_KEYLEN,
        { N: SCRYPT_COST, r: SCRYPT_BLOCK_SIZE, p: SCRYPT_PARALLELIZATION },
        (err, derivedKey) => {
          if (err) reject(err);
          else resolve(`${salt}:${derivedKey.toString("hex")}`);
        }
      );
    });
  }

  static async verifyPassword(
    password: string,
    hash: string
  ): Promise<boolean> {
    const [salt, key] = hash.split(":");
    return new Promise((resolve, reject) => {
      crypto.scrypt(
        password,
        salt,
        SCRYPT_KEYLEN,
        { N: SCRYPT_COST, r: SCRYPT_BLOCK_SIZE, p: SCRYPT_PARALLELIZATION },
        (err, derivedKey) => {
          if (err) reject(err);
          else
            resolve(
              crypto.timingSafeEqual(
                Buffer.from(key, "hex"),
                derivedKey
              )
            );
        }
      );
    });
  }

  // ──────────────────────── Signup / Login ─────────────────────────

  static async signup(
    email: string,
    password: string,
    ownerAddr: string,
    smartWalletAddr: string,
    chainId: number
  ) {
    const existing = await prisma.user.findUnique({ where: { email } });
    if (existing) {
      throw new Error("Unable to create account");
    }

    const passwordHash = await this.hashPassword(password);

    const user = await prisma.user.create({
      data: {
        email: email.toLowerCase().trim(),
        passwordHash,
        ownerAddr: ownerAddr.toLowerCase(),
        smartWalletAddr: smartWalletAddr.toLowerCase(),
        chainId,
      },
    });

    return user;
  }

  static async login(email: string, password: string) {
    const user = await prisma.user.findUnique({
      where: { email: email.toLowerCase().trim() },
    });
    if (!user) {
      throw new Error("Invalid email or password");
    }

    const valid = await this.verifyPassword(password, user.passwordHash);
    if (!valid) {
      throw new Error("Invalid email or password");
    }

    return user;
  }

  // ──────────────────── Password Reset Tokens ─────────────────────

  static async generatePasswordResetToken(email: string): Promise<string> {
    const user = await prisma.user.findUnique({
      where: { email: email.toLowerCase().trim() },
    });
    // Always return a token-shaped string to prevent email enumeration
    if (!user) return crypto.randomBytes(32).toString("hex");

    const token = crypto.randomBytes(32).toString("hex");
    const tokenHash = crypto.createHash("sha256").update(token).digest("hex");
    const expiresAt = new Date(
      Date.now() + RESET_TOKEN_EXPIRY_MINUTES * 60_000
    );

    await prisma.passwordResetToken.create({
      data: {
        tokenHash,
        userId: user.id,
        type: "PASSWORD_RESET",
        expiresAt,
      },
    });

    return token;
  }

  static async resetPassword(
    token: string,
    newPassword: string
  ): Promise<void> {
    const tokenHash = crypto.createHash("sha256").update(token).digest("hex");
    const record = await prisma.passwordResetToken.findUnique({
      where: { tokenHash },
    });

    if (!record || record.used || record.expiresAt < new Date()) {
      throw new Error("Invalid or expired reset token");
    }

    const passwordHash = await this.hashPassword(newPassword);

    await prisma.$transaction([
      prisma.user.update({
        where: { id: record.userId },
        data: { passwordHash },
      }),
      prisma.passwordResetToken.update({
        where: { id: record.id },
        data: { used: true },
      }),
      // Revoke all sessions on password reset
      prisma.refreshToken.updateMany({
        where: { userId: record.userId, revoked: false },
        data: { revoked: true },
      }),
    ]);
  }

  // ──────────────────────────── JWT ─────────────────────────────

  static signAccessToken(payload: AccessTokenPayload): string {
    return jwt.sign(payload, ENV.JWT_SECRET, {
      expiresIn: ACCESS_TOKEN_EXPIRY,
    });
  }

  static verifyAccessToken(token: string): AccessTokenPayload {
    return jwt.verify(token, ENV.JWT_SECRET) as AccessTokenPayload;
  }

  // ────────────────────── Refresh Tokens ────────────────────────

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

    await prisma.refreshToken.update({
      where: { id: existing.id },
      data: { revoked: true },
    });

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

    const accessToken = this.signAccessToken({
      sub: existing.userId,
      wallet: existing.user.smartWalletAddr,
      owner: existing.user.ownerAddr,
    });

    return { accessToken, refreshToken: newToken };
  }

  static async revokeAllTokens(userId: string): Promise<void> {
    await prisma.refreshToken.updateMany({
      where: { userId, revoked: false },
      data: { revoked: true },
    });
  }

  static async revokeFamily(family: string): Promise<void> {
    await prisma.refreshToken.updateMany({
      where: { family },
      data: { revoked: true },
    });
  }

  static async purgeExpiredRefreshTokens(): Promise<number> {
    const { count } = await prisma.refreshToken.deleteMany({
      where: { expiresAt: { lt: new Date() } },
    });
    return count;
  }
}
