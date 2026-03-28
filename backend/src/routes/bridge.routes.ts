import { Router } from "express";
import { z } from "zod";
import { Hex } from "viem";
import { BridgeService } from "../services/bridge.service.js";
import { requireAuth } from "../middleware/auth.middleware.js";

const router = Router();

router.use(requireAuth);

const quoteSchema = z.object({
  fromChainId: z.number().int(),
  toChainId: z.number().int(),
  fromToken: z.string().regex(/^0x[a-fA-F0-9]{40}$/),
  toToken: z.string().regex(/^0x[a-fA-F0-9]{40}$/),
  amount: z.string().min(1),
});

/** POST /bridge/quote — Get a cross-chain bridge quote. */
router.post("/quote", async (req, res) => {
  const parsed = quoteSchema.safeParse(req.body);
  if (!parsed.success) {
    res.status(400).json({ error: "Invalid request", details: parsed.error.flatten() });
    return;
  }

  try {
    const { fromChainId, toChainId, fromToken, toToken, amount } = parsed.data;
    const quote = await BridgeService.getQuote(
      fromChainId,
      toChainId,
      fromToken as Hex,
      toToken as Hex,
      amount,
      req.user!.wallet as Hex
    );
    res.json(quote);
  } catch (err: any) {
    res.status(500).json({ error: err.message });
  }
});

export default router;
