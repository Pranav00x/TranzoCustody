import { Router } from "express";
import { z } from "zod";
import { Hex } from "viem";
import { AuthService } from "../services/auth.service.js";
import { WalletService } from "../services/wallet.service.js";
import { requireAuth } from "../middleware/auth.middleware.js";
import prisma from "../services/prisma.service.js";
import { EmailService } from "../services/email.service.js";

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

const oauthSignupSchema = z.object({
  email: z.string().email(),
  googleId: z.string().optional(),
  publicKey: z.string().optional(),
  ownerAddr: z.string().regex(/^0x[a-fA-F0-9]{40}$/),
  chainId: z.number().int().positive(),
  emailVerified: z.boolean().optional(),
});

const googleLoginSchema = z.object({
  idToken: z.string().min(1),
  ownerAddr: z.string().regex(/^0x[a-fA-F0-9]{40}$/),
  chainId: z.number().int().positive(),
});

const sendOtpSchema = z.object({
  email: z.string().email(),
});

const verifyOtpSchema = z.object({
  email: z.string().email(),
  otp: z.string().length(6),
});

// ─────────────────────── POST /auth/signup ───────────────────────

/**
 * Standard signup using email, password, and owner address.
 * Generates a counterfactual (CREATE2) address for the user's smart wallet.
 */
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

    // Send welcome email (non-blocking)
    EmailService.sendWelcomeEmail(user.email, user.smartWalletAddr).catch(
      (err) => console.error("Failed to send welcome email:", err)
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

// ────────────────────── POST /auth/oauth-signup ──────────────────

/**
 * Advanced signup for OAuth and WebAuthn (Passkeys).
 * Links social identities to a deterministic smart wallet address.
 */
router.post("/oauth-signup", async (req, res) => {
  const parsed = oauthSignupSchema.safeParse(req.body);
  if (!parsed.success) {
    res
      .status(400)
      .json({ error: "Invalid request body", details: parsed.error.flatten() });
    return;
  }

  const { email, googleId, publicKey, ownerAddr, chainId, emailVerified } =
    parsed.data;

  if (!googleId && !publicKey) {
    res.status(400).json({ error: "googleId or publicKey is required" });
    return;
  }

  try {
    const smartWalletAddr = await WalletService.computeCounterfactualAddress(
      ownerAddr.toLowerCase() as Hex,
      1,
      chainId
    );

    const { user, isNewUser } = await AuthService.signupWithOAuth({
      email,
      googleId,
      publicKey,
      ownerAddr,
      smartWalletAddr,
      chainId,
      emailVerified,
    });

    if (isNewUser) {
      EmailService.sendWelcomeEmail(user.email, user.smartWalletAddr).catch(
        (err) => console.error("Failed to send welcome email:", err)
      );
    }

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

// ────────────────────── POST /auth/google-login ──────────────────

/**
 * Specialized endpoint for Google One-Tap or standard Google Sign-In.
 * Verifies the Google ID Token and logs in (or creates) the user.
 */
router.post("/google-login", async (req, res) => {
  const parsed = googleLoginSchema.safeParse(req.body);
  if (!parsed.success) {
    res
      .status(400)
      .json({ error: "Invalid request body", details: parsed.error.flatten() });
    return;
  }

  const { idToken, ownerAddr, chainId } = parsed.data;

  try {
    const payload = await AuthService.verifyGoogleIdToken(idToken);
    if (!payload || !payload.email || !payload.sub) {
      res.status(401).json({ error: "Invalid Google token payload" });
      return;
    }

    const smartWalletAddr = await WalletService.computeCounterfactualAddress(
      ownerAddr.toLowerCase() as Hex,
      1,
      chainId
    );

    const { user, isNewUser } = await AuthService.signupWithOAuth({
      email: payload.email,
      googleId: payload.sub,
      ownerAddr,
      smartWalletAddr,
      chainId,
      emailVerified: payload.email_verified,
    });

    if (isNewUser) {
      EmailService.sendWelcomeEmail(user.email, user.smartWalletAddr).catch(
        (err) => console.error("Failed to send welcome email:", err)
      );
    }

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

// ────────────────────── POST /auth/otp-send ───────────────────

/**
 * Triggers a 6-digit verification code to the requested email.
 * Ideal for passwordless flows or email verification steps.
 */
router.post("/otp-send", async (req, res) => {
  const parsed = sendOtpSchema.safeParse(req.body);
  if (!parsed.success) {
    res.status(400).json({ error: "Invalid email" });
    return;
  }

  try {
    await AuthService.sendAuthOTP(parsed.data.email);
    res.json({ message: "If an account exists, an OTP has been sent." });
  } catch (err: any) {
    res.status(500).json({ error: "Failed to send OTP" });
  }
});

// ────────────────────── POST /auth/otp-verify ─────────────────

/**
 * Validates a previously sent OTP.
 * Can be used as a standalone verification or part of a multi-step login.
 */
router.post("/otp-verify", async (req, res) => {
  const parsed = verifyOtpSchema.safeParse(req.body);
  if (!parsed.success) {
    res.status(400).json({ error: "Invalid input" });
    return;
  }

  try {
    const valid = await AuthService.verifyOTP(
      parsed.data.email,
      parsed.data.otp
    );
    if (!valid) {
      res.status(401).json({ error: "Invalid or expired OTP" });
      return;
    }

    // On success, we could mark the email as verified or return a temporary verification token
    res.json({ message: "OTP verified successfully" });
  } catch (err: any) {
    res.status(500).json({ error: "Verification failed" });
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

    await EmailService.sendPasswordReset(parsed.data.email, token);

    res.json({ message: "If an account exists, a reset link has been sent." });
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
