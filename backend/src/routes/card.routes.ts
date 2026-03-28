import { Router } from "express";
import { z } from "zod";
import { CardProvider, CardType } from "@prisma/client";
import { CardService } from "../services/card.service.js";
import { requireAuth } from "../middleware/auth.middleware.js";
import { sensitiveLimiter } from "../middleware/ratelimit.middleware.js";

const router = Router();

// All card routes require authentication
router.use(requireAuth);

// ────────────────────────── Validation ──────────────────────────

const createCardSchema = z.object({
  provider: z.nativeEnum(CardProvider),
  type: z.nativeEnum(CardType).default("VIRTUAL"),
  dailyLimit: z.string().optional(), // BigInt as string
});

const updateLimitSchema = z.object({
  dailyLimit: z.string().min(1),
});

const createSessionSchema = z.object({
  provider: z.nativeEnum(CardProvider),
  sessionKeyAddr: z.string().regex(/^0x[a-fA-F0-9]{40}$/),
  dailyLimit: z.string().min(1),
  perTxLimit: z.string().min(1),
  validUntil: z.string().datetime(),
  txHash: z.string().optional(),
});

// ──────────────────────── Card CRUD ─────────────────────────────

/** POST /cards — Create a new card. */
router.post("/", sensitiveLimiter, async (req, res) => {
  const parsed = createCardSchema.safeParse(req.body);
  if (!parsed.success) {
    res.status(400).json({ error: "Invalid request", details: parsed.error.flatten() });
    return;
  }

  try {
    const { provider, type, dailyLimit } = parsed.data;
    const card = await CardService.createCard(
      req.user!.sub,
      provider,
      type,
      dailyLimit ? BigInt(dailyLimit) : undefined
    );
    res.status(201).json(serializeCard(card));
  } catch (err: any) {
    res.status(500).json({ error: err.message });
  }
});

/** GET /cards — List user's cards. */
router.get("/", async (req, res) => {
  try {
    const cards = await CardService.listCards(req.user!.sub);
    res.json(cards.map(serializeCard));
  } catch (err: any) {
    res.status(500).json({ error: err.message });
  }
});

/** GET /cards/:id — Get a single card. */
router.get("/:id", async (req, res) => {
  try {
    const card = await CardService.getCard(req.params.id, req.user!.sub);
    if (!card) {
      res.status(404).json({ error: "Card not found" });
      return;
    }
    res.json(serializeCard(card));
  } catch (err: any) {
    res.status(500).json({ error: err.message });
  }
});

/** POST /cards/:id/freeze — Freeze a card. */
router.post("/:id/freeze", sensitiveLimiter, async (req, res) => {
  try {
    const card = await CardService.freezeCard(req.params.id as string, req.user!.sub);
    res.json(serializeCard(card));
  } catch (err: any) {
    res.status(400).json({ error: err.message });
  }
});

/** POST /cards/:id/unfreeze — Unfreeze a card. */
router.post("/:id/unfreeze", sensitiveLimiter, async (req, res) => {
  try {
    const card = await CardService.unfreezeCard(req.params.id as string, req.user!.sub);
    res.json(serializeCard(card));
  } catch (err: any) {
    res.status(400).json({ error: err.message });
  }
});

/** POST /cards/:id/cancel — Cancel a card permanently. */
router.post("/:id/cancel", sensitiveLimiter, async (req, res) => {
  try {
    const card = await CardService.cancelCard(req.params.id as string, req.user!.sub);
    res.json(serializeCard(card));
  } catch (err: any) {
    res.status(400).json({ error: err.message });
  }
});

/** PATCH /cards/:id/limit — Update daily spending limit. */
router.patch("/:id/limit", async (req, res) => {
  const parsed = updateLimitSchema.safeParse(req.body);
  if (!parsed.success) {
    res.status(400).json({ error: "Invalid dailyLimit" });
    return;
  }

  try {
    const card = await CardService.updateDailyLimit(
      req.params.id as string,
      req.user!.sub,
      BigInt(parsed.data.dailyLimit)
    );
    res.json(serializeCard(card));
  } catch (err: any) {
    res.status(400).json({ error: err.message });
  }
});

// ──────────────────── Card Sessions ─────────────────────────────

/** POST /cards/sessions — Create a spending session. */
router.post("/sessions", sensitiveLimiter, async (req, res) => {
  const parsed = createSessionSchema.safeParse(req.body);
  if (!parsed.success) {
    res.status(400).json({ error: "Invalid request", details: parsed.error.flatten() });
    return;
  }

  try {
    const { provider, sessionKeyAddr, dailyLimit, perTxLimit, validUntil, txHash } =
      parsed.data;
    const session = await CardService.createSession(
      req.user!.sub,
      provider,
      sessionKeyAddr,
      BigInt(dailyLimit),
      BigInt(perTxLimit),
      new Date(validUntil),
      txHash
    );
    res.status(201).json(serializeSession(session));
  } catch (err: any) {
    res.status(500).json({ error: err.message });
  }
});

/** GET /cards/sessions — List active sessions. */
router.get("/sessions/active", async (req, res) => {
  try {
    const sessions = await CardService.listActiveSessions(req.user!.sub);
    res.json(sessions.map(serializeSession));
  } catch (err: any) {
    res.status(500).json({ error: err.message });
  }
});

/** POST /cards/sessions/:id/revoke — Revoke a session. */
router.post("/sessions/:id/revoke", sensitiveLimiter, async (req, res) => {
  try {
    const session = await CardService.revokeSession(req.params.id as string, req.user!.sub);
    res.json(serializeSession(session));
  } catch (err: any) {
    res.status(400).json({ error: err.message });
  }
});

// ──────────────────── Serialization ─────────────────────────────

/** Convert BigInt fields to strings for JSON. */
function serializeCard(card: any) {
  return { ...card, dailyLimit: card.dailyLimit.toString() };
}

function serializeSession(session: any) {
  return {
    ...session,
    dailyLimit: session.dailyLimit.toString(),
    perTxLimit: session.perTxLimit.toString(),
  };
}

export default router;
