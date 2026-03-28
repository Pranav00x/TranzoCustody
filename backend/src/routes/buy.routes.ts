import { Router } from "express";
import { z } from "zod";
import { OnrampService } from "../services/onramp.service.js";
import { requireAuth } from "../middleware/auth.middleware.js";
import { sensitiveLimiter } from "../middleware/ratelimit.middleware.js";

const router = Router();

router.use(requireAuth);

const quoteSchema = z.object({
  fiatAmount: z.number().positive(),
  fiatCurrency: z.string().min(3).max(3).default("USD"),
  cryptoToken: z.string().min(1),
  chainId: z.number().int(),
});

const sessionSchema = z.object({
  fiatAmount: z.number().positive(),
  fiatCurrency: z.string().min(3).max(3).default("USD"),
  cryptoToken: z.string().min(1),
  chainId: z.number().int(),
  paymentMethod: z.enum(["card", "bank_transfer"]).default("card"),
});

/** POST /buy/quote — Get a fiat → crypto buy quote. */
router.post("/quote", async (req, res) => {
  const parsed = quoteSchema.safeParse(req.body);
  if (!parsed.success) {
    res.status(400).json({ error: "Invalid request", details: parsed.error.flatten() });
    return;
  }

  try {
    const { fiatAmount, fiatCurrency, cryptoToken, chainId } = parsed.data;
    const quote = await OnrampService.getQuote(fiatAmount, fiatCurrency, cryptoToken, chainId);
    res.json(quote);
  } catch (err: any) {
    res.status(500).json({ error: err.message });
  }
});

/** POST /buy/session — Create a payment session (returns widget URL). */
router.post("/session", sensitiveLimiter, async (req, res) => {
  const parsed = sessionSchema.safeParse(req.body);
  if (!parsed.success) {
    res.status(400).json({ error: "Invalid request", details: parsed.error.flatten() });
    return;
  }

  try {
    const { fiatAmount, fiatCurrency, cryptoToken, chainId, paymentMethod } = parsed.data;
    const session = await OnrampService.createSession(
      req.user!.wallet,
      fiatAmount,
      fiatCurrency,
      cryptoToken,
      chainId,
      paymentMethod
    );
    res.json(session);
  } catch (err: any) {
    res.status(500).json({ error: err.message });
  }
});

export default router;
