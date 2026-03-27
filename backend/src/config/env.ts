import { z } from "zod";
import dotenv from "dotenv";

dotenv.config();

const envSchema = z.object({
  PORT: z.string().default("3000"),
  NODE_ENV: z.enum(["development", "production", "test"]).default("development"),
  DATABASE_URL: z.string(),
  JWT_SECRET: z.string(),
  REDIS_URL: z.string(),

  // RPCs
  POLYGON_RPC_URL: z.string().url(),
  BASE_RPC_URL: z.string().url().optional(),
  POLYGON_AMOY_RPC_URL: z.string().url().optional(),
  BASE_SEPOLIA_RPC_URL: z.string().url().optional(),

  // Bundler
  PIMLICO_API_KEY: z.string(),

  // Contract Addresses
  FACTORY_ADDRESS: z.string(),
  PAYMASTER_ADDRESS: z.string(),
  DRIPPER_ADDRESS: z.string(),

  // Paymaster
  PAYMASTER_SIGNER_PRIVATE_KEY: z.string().startsWith("0x"),

  // Immersve
  IMMERSVE_API_URL: z.string().url(),
  IMMERSVE_API_KEY: z.string(),
  IMMERSVE_WEBHOOK_SECRET: z.string(),

  // Reap
  REAP_API_URL: z.string().url().optional(),
  REAP_API_KEY: z.string().optional(),
});

export const ENV = envSchema.parse(process.env);
