import { Router } from "express";
import { z } from "zod";
import { Hex } from "viem";
import { SwapService } from "../services/swap.service.js";
import { requireAuth } from "../middleware/auth.middleware.js";
import { sensitiveLimiter } from "../middleware/ratelimit.middleware.js";

const router = Router();

router.use(requireAuth);

const quoteSchema = z.object({
  chainId: z.number().int(),
  fromToken: z.string().regex(/^0x[a-fA-F0-9]{40}$/),
  toToken: z.string().regex(/^0x[a-fA-F0-9]{40}$/),
  amount: z.string().min(1),
  slippageBps: z.number().int().min(1).max(1000).default(50),
});

/** POST /swap/quote — Get a swap quote from DEX aggregator. */
router.post("/quote", async (req, res) => {
  const parsed = quoteSchema.safeParse(req.body);
  if (!parsed.success) {
    res.status(400).json({ error: "Invalid request", details: parsed.error.flatten() });
    return;
  }

  try {
    const { chainId, fromToken, toToken, amount, slippageBps } = parsed.data;
    const quote = await SwapService.getQuote(
      chainId,
      fromToken as Hex,
      toToken as Hex,
      amount,
      slippageBps
    );
    res.json(quote);
  } catch (err: any) {
    res.status(500).json({ error: err.message });
  }
});

export default router;
