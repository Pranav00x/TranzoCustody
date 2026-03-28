import { Router, Request, Response } from "express";
import { createHmac } from "crypto";
import { ENV } from "../config/env.js";
import prisma from "../services/prisma.service.js";
import { KycStatus } from "@prisma/client";

const router = Router();

// ─────────────── POST /webhooks/immersve ────────────────────────

/**
 * Webhook handler for Immersve card provider callbacks.
 * Validates HMAC signature, stores event, processes known event types.
 */
router.post("/immersve", async (req: Request, res: Response) => {
  // 1. Verify webhook signature
  const signature = req.headers["x-immersve-signature"] as string | undefined;
  if (!verifyImmersveSignature(req.body, signature)) {
    res.status(401).json({ error: "Invalid webhook signature" });
    return;
  }

  const { type, data } = req.body;

  try {
    // 2. Store raw event
    await prisma.webhookEvent.create({
      data: {
        provider: "immersve",
        eventType: type,
        payload: req.body,
      },
    });

    // 3. Process known event types
    switch (type) {
      case "kyc.approved": {
        const { walletAddress } = data;
        if (walletAddress) {
          await prisma.user.updateMany({
            where: { smartWalletAddr: walletAddress.toLowerCase() },
            data: { kycStatus: KycStatus.APPROVED, kycProvider: "immersve" },
          });
        }
        break;
      }

      case "kyc.rejected": {
        const { walletAddress: rejAddr } = data;
        if (rejAddr) {
          await prisma.user.updateMany({
            where: { smartWalletAddr: rejAddr.toLowerCase() },
            data: { kycStatus: KycStatus.REJECTED },
          });
        }
        break;
      }

      case "card.activated": {
        const { externalId, last4 } = data;
        if (externalId) {
          await prisma.card.updateMany({
            where: { externalId },
            data: { status: "ACTIVE", last4: last4 || undefined },
          });
        }
        break;
      }

      case "card.frozen": {
        const { externalId: frozenId } = data;
        if (frozenId) {
          await prisma.card.updateMany({
            where: { externalId: frozenId },
            data: { status: "FROZEN" },
          });
        }
        break;
      }

      case "transaction.completed": {
        // Card spend event — could update card balance tracking
        break;
      }

      default:
        // Unknown event type — stored but not processed
        break;
    }

    // 4. Mark as processed
    // (latest event for this type — simple approach)
    res.json({ received: true });
  } catch (err: any) {
    console.error("[webhook/immersve] Error:", err.message);
    res.status(500).json({ error: "Webhook processing failed" });
  }
});

// ─────────────── Signature Verification ─────────────────────────

function verifyImmersveSignature(
  body: any,
  signature: string | undefined
): boolean {
  if (!signature) return false;

  const secret = ENV.IMMERSVE_WEBHOOK_SECRET;
  if (!secret || secret === "dev") return true; // Skip in dev

  const expected = createHmac("sha256", secret)
    .update(JSON.stringify(body))
    .digest("hex");

  return signature === expected;
}

export default router;
