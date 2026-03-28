import { Router } from "express";
import { z } from "zod";
import { KycStatus } from "@prisma/client";
import { requireAuth } from "../middleware/auth.middleware.js";
import prisma from "../services/prisma.service.js";

const router = Router();

router.use(requireAuth);

// ─────────────────────── GET /kyc/status ────────────────────────

/** Get the authenticated user's KYC status. */
router.get("/status", async (req, res) => {
  try {
    const user = await prisma.user.findUnique({
      where: { id: req.user!.sub },
      select: { kycStatus: true, kycProvider: true },
    });

    if (!user) {
      res.status(404).json({ error: "User not found" });
      return;
    }

    res.json({
      status: user.kycStatus,
      provider: user.kycProvider,
    });
  } catch (err: any) {
    res.status(500).json({ error: err.message });
  }
});

// ─────────────────────── POST /kyc/start ────────────────────────

const startKycSchema = z.object({
  provider: z.string().min(1).default("immersve"),
});

/**
 * Initiate KYC verification.
 * In production, this would call the KYC provider API and return a verification URL/session.
 * For now, it sets the user's status to PENDING.
 */
router.post("/start", async (req, res) => {
  const parsed = startKycSchema.safeParse(req.body);
  if (!parsed.success) {
    res.status(400).json({ error: "Invalid request" });
    return;
  }

  try {
    const user = await prisma.user.findUnique({
      where: { id: req.user!.sub },
    });

    if (!user) {
      res.status(404).json({ error: "User not found" });
      return;
    }

    if (user.kycStatus === "APPROVED") {
      res.json({ status: "APPROVED", message: "KYC already approved" });
      return;
    }

    const updated = await prisma.user.update({
      where: { id: req.user!.sub },
      data: {
        kycStatus: KycStatus.PENDING,
        kycProvider: parsed.data.provider,
      },
    });

    // TODO: Call actual KYC provider API (Immersve/Sumsub/etc.)
    // and return the verification session URL

    res.json({
      status: updated.kycStatus,
      provider: updated.kycProvider,
      // verificationUrl: "https://verify.provider.com/session/xxx"
    });
  } catch (err: any) {
    res.status(500).json({ error: err.message });
  }
});

export default router;
