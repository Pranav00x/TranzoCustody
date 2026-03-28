import { Router } from "express";
import { z } from "zod";
import { StreamService } from "../services/stream.service.js";
import { requireAuth } from "../middleware/auth.middleware.js";
import { sensitiveLimiter } from "../middleware/ratelimit.middleware.js";

const router = Router();

router.use(requireAuth);

// ────────────────────────── Validation ──────────────────────────

const createStreamSchema = z.object({
  recipientAddr: z.string().regex(/^0x[a-fA-F0-9]{40}$/),
  token: z.string().min(1),
  totalAmount: z.string().min(1), // BigInt as string
  startTime: z.string().datetime(),
  endTime: z.string().datetime(),
  txHash: z.string().optional(),
  onChainStreamId: z.number().int().optional(),
});

// ──────────────────────── Routes ────────────────────────────────

/** POST /streams — Create a new token stream. */
router.post("/", sensitiveLimiter, async (req, res) => {
  const parsed = createStreamSchema.safeParse(req.body);
  if (!parsed.success) {
    res.status(400).json({ error: "Invalid request", details: parsed.error.flatten() });
    return;
  }

  try {
    const { recipientAddr, token, totalAmount, startTime, endTime, txHash, onChainStreamId } =
      parsed.data;
    const stream = await StreamService.createStream(
      req.user!.sub,
      recipientAddr,
      token,
      BigInt(totalAmount),
      new Date(startTime),
      new Date(endTime),
      txHash,
      onChainStreamId
    );
    res.status(201).json(serializeStream(stream));
  } catch (err: any) {
    res.status(400).json({ error: err.message });
  }
});

/** GET /streams/sent — List streams the user is sending. */
router.get("/sent", async (req, res) => {
  try {
    const streams = await StreamService.listSentStreams(req.user!.sub);
    res.json(streams.map(serializeStream));
  } catch (err: any) {
    res.status(500).json({ error: err.message });
  }
});

/** GET /streams/received — List streams where user's wallet is the recipient. */
router.get("/received", async (req, res) => {
  try {
    const streams = await StreamService.listReceivedStreams(req.user!.wallet);
    res.json(streams.map(serializeStream));
  } catch (err: any) {
    res.status(500).json({ error: err.message });
  }
});

/** GET /streams/:id — Get a single stream. */
router.get("/:id", async (req, res) => {
  try {
    const stream = await StreamService.getStream(req.params.id as string, req.user!.sub);
    if (!stream) {
      res.status(404).json({ error: "Stream not found" });
      return;
    }
    res.json(serializeStream(stream));
  } catch (err: any) {
    res.status(500).json({ error: err.message });
  }
});

/** POST /streams/:id/cancel — Cancel a stream. */
router.post("/:id/cancel", sensitiveLimiter, async (req, res) => {
  try {
    const stream = await StreamService.cancelStream(req.params.id as string, req.user!.sub);
    res.json(serializeStream(stream));
  } catch (err: any) {
    res.status(400).json({ error: err.message });
  }
});

/** POST /streams/:id/pause — Pause a stream. */
router.post("/:id/pause", sensitiveLimiter, async (req, res) => {
  try {
    const stream = await StreamService.pauseStream(req.params.id as string, req.user!.sub);
    res.json(serializeStream(stream));
  } catch (err: any) {
    res.status(400).json({ error: err.message });
  }
});

/** POST /streams/:id/resume — Resume a paused stream. */
router.post("/:id/resume", sensitiveLimiter, async (req, res) => {
  try {
    const stream = await StreamService.resumeStream(req.params.id as string, req.user!.sub);
    res.json(serializeStream(stream));
  } catch (err: any) {
    res.status(400).json({ error: err.message });
  }
});

// ──────────────────── Serialization ─────────────────────────────

function serializeStream(stream: any) {
  return {
    ...stream,
    totalAmount: stream.totalAmount.toString(),
    withdrawn: stream.withdrawn.toString(),
  };
}

export default router;
