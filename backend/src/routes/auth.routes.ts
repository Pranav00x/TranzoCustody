import { Router } from "express";
import { z } from "zod";
import { Hex } from "viem";
import { AuthService } from "../services/auth.service.js";
import { WalletService } from "../services/wallet.service.js";
import { requireAuth } from "../middleware/auth.middleware.js";
import prisma from "../services/prisma.service.js";

const router = Router();

// ────────────────────────── Validation ──────────────────────────

const signupSchema = z.object({
  email: z.string().email(),
  password: z.string().min(8, "Password must be at least 8 characters"),
  ownerAddr: z.string().regex(/^0x[a-fA-F0-9]{40}$/),
  chainId: z.number().int().positive(),
});

const loginSchema = z.object({
  email: z.string().email(),
  password: z.string().min(1),
});

const refreshSchema = z.object({
  refreshToken: z.string().min(1),
});

const forgotPasswordSchema = z.object({
  email: z.string().email(),
});

const resetPasswordSchema = z.object({
  token: z.string().min(1),
  newPassword: z.string().min(8, "Password must be at least 8 characters"),
});

// ─────────────────────── POST /auth/signup ───────────────────────

router.post("/signup", async (req, res) => {
  const parsed = signupSchema.safeParse(req.body);
  if (!parsed.success) {
    res
      .status(400)
      .json({ error: "Invalid request body", details: parsed.error.flatten() });
    return;
  }

  const { email, password, ownerAddr, chainId } = parsed.data;

  try {
    // Compute counterfactual smart wallet address
    const smartWalletAddr = await WalletService.computeCounterfactualAddress(
      ownerAddr.toLowerCase() as Hex,
      1,
      chainId
    );

    const user = await AuthService.signup(
      email,
      password,
      ownerAddr,
      smartWalletAddr,
      chainId
    );

    // Issue tokens
    const accessToken = AuthService.signAccessToken({
      sub: user.id,
      wallet: user.smartWalletAddr,
      owner: user.ownerAddr,
    });
    const refreshToken = await AuthService.createRefreshToken(user.id);

    res.status(201).json({
      accessToken,
      refreshToken,
      user: {
        id: user.id,
        email: user.email,
        smartWalletAddr: user.smartWalletAddr,
        ownerAddr: user.ownerAddr,
        chainId: user.chainId,
      },
    });
  } catch (err: any) {
    res.status(400).json({ error: err.message });
  }
});

// ─────────────────────── POST /auth/login ────────────────────────

router.post("/login", async (req, res) => {
  const parsed = loginSchema.safeParse(req.body);
  if (!parsed.success) {
    res.status(400).json({ error: "Invalid request body" });
    return;
  }

  const { email, password } = parsed.data;

  try {
    const user = await AuthService.login(email, password);

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
        email: user.email,
        smartWalletAddr: user.smartWalletAddr,
        ownerAddr: user.ownerAddr,
        chainId: user.chainId,
      },
    });
  } catch (err: any) {
    res.status(401).json({ error: err.message });
  }
});

// ────────────────── POST /auth/forgot-password ──────────────────

router.post("/forgot-password", async (req, res) => {
  const parsed = forgotPasswordSchema.safeParse(req.body);
  if (!parsed.success) {
    res.status(400).json({ error: "Invalid email" });
    return;
  }

  try {
    const token = await AuthService.generatePasswordResetToken(
      parsed.data.email
    );

    // TODO: Send email with reset link containing `token`
    // For now, return token directly (dev only — remove in production)
    res.json({
      message: "If an account exists, a reset link has been sent.",
      // Remove this in production:
      _devToken: token,
    });
  } catch (err: any) {
    // Always return success to prevent email enumeration
    res.json({ message: "If an account exists, a reset link has been sent." });
  }
});

// ─────────────────── POST /auth/reset-password ──────────────────

router.post("/reset-password", async (req, res) => {
  const parsed = resetPasswordSchema.safeParse(req.body);
  if (!parsed.success) {
    res.status(400).json({ error: "Invalid request body" });
    return;
  }

  try {
    await AuthService.resetPassword(parsed.data.token, parsed.data.newPassword);
    res.json({ message: "Password has been reset. Please log in again." });
  } catch (err: any) {
    res.status(400).json({ error: err.message });
  }
});

// ─────────────────────── POST /auth/refresh ─────────────────────

router.post("/refresh", async (req, res) => {
  const parsed = refreshSchema.safeParse(req.body);
  if (!parsed.success) {
    res.status(400).json({ error: "Invalid request body" });
    return;
  }

  try {
    const tokens = await AuthService.rotateRefreshToken(
      parsed.data.refreshToken
    );
    res.json(tokens);
  } catch (err: any) {
    res.status(401).json({ error: err.message });
  }
});

// ─────────────────────── POST /auth/logout ──────────────────────

router.post("/logout", requireAuth, async (req, res) => {
  try {
    await AuthService.revokeAllTokens(req.user!.sub);
    res.json({ message: "Logged out from all sessions" });
  } catch (err: any) {
    res.status(500).json({ error: err.message });
  }
});

export default router;
