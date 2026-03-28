import { Request, Response, NextFunction } from "express";
import { ZodError } from "zod";

/**
 * Global error handler — catches unhandled errors from routes.
 * Must be registered AFTER all routes (4-arg signature tells Express it's an error handler).
 */
export function errorHandler(
  err: Error,
  _req: Request,
  res: Response,
  _next: NextFunction
) {
  // Zod validation errors → 400
  if (err instanceof ZodError) {
    res.status(400).json({
      error: "Validation error",
      details: err.flatten(),
    });
    return;
  }

  // Known operational errors with a status code
  if ("statusCode" in err && typeof (err as any).statusCode === "number") {
    res.status((err as any).statusCode).json({ error: err.message });
    return;
  }

  // Unexpected errors → 500
  console.error("[ERROR]", err);
  res.status(500).json({ error: "Internal server error" });
}

/**
 * Catch async route errors and forward them to the error handler.
 * Wraps an async route handler so thrown errors hit errorHandler.
 */
export function asyncHandler(
  fn: (req: Request, res: Response, next: NextFunction) => Promise<any>
) {
  return (req: Request, res: Response, next: NextFunction) => {
    fn(req, res, next).catch(next);
  };
}
