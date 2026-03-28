import { Request, Response, NextFunction } from "express";
import { AuthService, AccessTokenPayload } from "../services/auth.service.js";

/**
 * Extends Express Request to carry the authenticated user payload.
 */
declare global {
  namespace Express {
    interface Request {
      user?: AccessTokenPayload;
    }
  }
}

/**
 * Require a valid JWT access token in the Authorization header.
 * Attaches `req.user` with { sub, wallet, owner } on success.
 */
export function requireAuth(req: Request, res: Response, next: NextFunction) {
  const header = req.headers.authorization;
  if (!header?.startsWith("Bearer ")) {
    res.status(401).json({ error: "Missing or malformed Authorization header" });
    return;
  }

  const token = header.slice(7);

  try {
    req.user = AuthService.verifyAccessToken(token);
    next();
  } catch (err: any) {
    if (err.name === "TokenExpiredError") {
      res.status(401).json({ error: "Access token expired" });
      return;
    }
    res.status(401).json({ error: "Invalid access token" });
  }
}

/**
 * Optional auth — attaches req.user if a valid token is present,
 * but does NOT reject the request if missing.
 */
export function optionalAuth(req: Request, res: Response, next: NextFunction) {
  const header = req.headers.authorization;
  if (!header?.startsWith("Bearer ")) {
    return next();
  }

  try {
    req.user = AuthService.verifyAccessToken(header.slice(7));
  } catch {
    // Silently ignore invalid tokens for optional auth
  }

  next();
}
