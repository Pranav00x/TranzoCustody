import { Router } from "express";
import { z } from "zod";
import { Hex } from "viem";
import { AuthService } from "../services/auth.service.js";
import { WalletService } from "../services/wallet.service.js";
import { requireAuth } from "../middleware/auth.middleware.js";
import prisma from "../services/prisma.service.js";

const router = Router();

// ────────────────────────── Validation ──────────────────────────

const verifySchema = z.object({
  message: z.string().min(1),
  signature: z.string().regex(/^0x[a-fA-F0-9]+$/),
});

const refreshSchema = z.object({
  refreshToken: z.string().min(1),
});

// ─────────────────────── GET /auth/nonce ─────────────────────────

/**
 * Returns a fresh nonce for SIWE message construction.
 * The nonce is single-use and expires in 10 minutes.
 */
router.get("/nonce", async (_req, res) => {
  try {
    const nonce = await AuthService.generateNonce();
    res.json({ nonce });
  } catch (err: any) {
    res.status(500).json({ error: err.message });
  }
});

// ─────────────────────── POST /auth/verify ──────────────────────

/**
 * Verify a SIWE signed message.
 * - Validates the signature against the Ethereum address
 * - Consumes the nonce (single-use)
 * - Finds or creates the user
 * - Returns access + refresh tokens
 */
router.post("/verify", async (req, res) => {
  const parsed = verifySchema.safeParse(req.body);
  if (!parsed.success) {
    res.status(400).json({ error: "Invalid request body", details: parsed.error.flatten() });
    return;
  }

  const { message, signature } = parsed.data;

  try {
    // 1. Verify SIWE signature + consume nonce
    const siweFields = await AuthService.verifySiweMessage(message, signature);
    const ownerAddr = siweFields.address.toLowerCase();
    const chainId = siweFields.chainId;

    // 2. Find or create user
    let user = await prisma.user.findUnique({
      where: { ownerAddr },
    });

    if (!user) {
      // Compute counterfactual smart wallet address
      const smartWalletAddr = await WalletService.computeCounterfactualAddress(
        ownerAddr as Hex,
        1, // default salt
        chainId
      );

      user = await prisma.user.create({
        data: {
          ownerAddr,
          smartWalletAddr,
          chainId,
        },
      });
    }

    // 3. Issue tokens
    const accessToken = AuthService.signAccessToken({
      sub: user.id,
      wallet: user.smartWalletAddr,
      owner: user.ownerAddr,
    });
    const refreshToken = await AuthService.createRefreshToken(user.id);

    res.json({
      accessToken,
      refreshToken,
      user: {
        id: user.id,
        smartWalletAddr: user.smartWalletAddr,
        ownerAddr: user.ownerAddr,
        chainId: user.chainId,
      },
    });
  } catch (err: any) {
    res.status(401).json({ error: err.message });
  }
});

// ─────────────────────── POST /auth/refresh ─────────────────────

/**
 * Rotate a refresh token → new access + refresh token pair.
 * Implements token reuse detection (revokes entire family if reuse found).
 */
router.post("/refresh", async (req, res) => {
  const parsed = refreshSchema.safeParse(req.body);
  if (!parsed.success) {
    res.status(400).json({ error: "Invalid request body" });
    return;
  }

  try {
    const tokens = await AuthService.rotateRefreshToken(parsed.data.refreshToken);
    res.json(tokens);
  } catch (err: any) {
    res.status(401).json({ error: err.message });
  }
});

// ─────────────────────── POST /auth/logout ──────────────────────

/**
 * Revoke all refresh tokens for the authenticated user (logout everywhere).
 * Requires a valid access token.
 */
router.post("/logout", requireAuth, async (req, res) => {
  try {
    await AuthService.revokeAllTokens(req.user!.sub);
    res.json({ message: "Logged out from all sessions" });
  } catch (err: any) {
    res.status(500).json({ error: err.message });
  }
});

export default router;
