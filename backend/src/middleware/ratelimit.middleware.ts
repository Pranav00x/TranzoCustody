import rateLimit from "express-rate-limit";

/** General API rate limit: 100 requests per minute per IP. */
export const generalLimiter = rateLimit({
  windowMs: 60_000,
  max: 100,
  standardHeaders: true,
  legacyHeaders: false,
  message: { error: "Too many requests, slow down" },
});

/** Stricter limit for auth endpoints: 20 requests per minute per IP. */
export const authLimiter = rateLimit({
  windowMs: 60_000,
  max: 20,
  standardHeaders: true,
  legacyHeaders: false,
  message: { error: "Too many auth attempts, try again later" },
});

/** Tight limit for sensitive operations (send tx, card actions): 10 per minute. */
export const sensitiveLimiter = rateLimit({
  windowMs: 60_000,
  max: 10,
  standardHeaders: true,
  legacyHeaders: false,
  message: { error: "Rate limit exceeded for this operation" },
});
