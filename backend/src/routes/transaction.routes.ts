import { Router } from "express";
import { z } from "zod";
import { requireAuth } from "../middleware/auth.middleware.js";
import { TransactionService } from "../services/transaction.service.js";

const router = Router();

const createTxSchema = z.object({
  txHash: z.string().regex(/^0x[a-fA-F0-9]{64}$/),
  chainId: z.number().int().positive(),
  from: z.string().regex(/^0x[a-fA-F0-9]{40}$/),
  to: z.string().regex(/^0x[a-fA-F0-9]{40}$/),
  value: z.string(),
  token: z.string().optional(),
  tokenAddress: z.string().optional(),
  type: z.enum(["SEND", "RECEIVE", "SWAP", "BRIDGE", "CONTRACT_CALL", "USER_OP"]),
});

const historyQuery = z.object({
  page: z.coerce.number().int().positive().optional(),
  limit: z.coerce.number().int().positive().max(100).optional(),
  chain: z.coerce.number().int().positive().optional(),
});

/**
 * POST /transactions
 * Record a new transaction.
 */
router.post("/", requireAuth, async (req, res) => {
  const parsed = createTxSchema.safeParse(req.body);
  if (!parsed.success) {
    res.status(400).json({ error: "Invalid request body", details: parsed.error.flatten() });
    return;
  }

  try {
    const tx = await TransactionService.create({
      userId: req.user!.sub,
      ...parsed.data,
    });
    res.status(201).json({ transaction: tx });
  } catch (err: any) {
    res.status(500).json({ error: err.message });
  }
});

/**
 * GET /transactions?page=1&limit=20&chain=8453
 * Get transaction history for the authenticated user.
 */
router.get("/", requireAuth, async (req, res) => {
  const parsed = historyQuery.safeParse(req.query);
  if (!parsed.success) {
    res.status(400).json({ error: "Invalid query parameters" });
    return;
  }

  try {
    const result = await TransactionService.getHistory(req.user!.sub, {
      page: parsed.data.page,
      limit: parsed.data.limit,
      chainId: parsed.data.chain,
    });
    res.json(result);
  } catch (err: any) {
    res.status(500).json({ error: err.message });
  }
});

/**
 * GET /transactions/:id
 * Get a single transaction by ID.
 */
router.get("/:id", requireAuth, async (req, res) => {
  try {
    const tx = await TransactionService.getById(req.params.id as string, req.user!.sub);
    if (!tx) {
      res.status(404).json({ error: "Transaction not found" });
      return;
    }
    res.json({ transaction: tx });
  } catch (err: any) {
    res.status(500).json({ error: err.message });
  }
});

export default router;
