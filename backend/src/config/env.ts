import { z } from "zod";
import dotenv from "dotenv";

dotenv.config();

const ethAddress = z.string().regex(/^0x[a-fA-F0-9]{40}$/);
const ethPrivateKey = z.string().regex(/^0x[a-fA-F0-9]{64}$/);

/** Strict config when NODE_ENV=production (deployed services). */
const productionEnvSchema = z.object({
  PORT: z.string().default("3000"),
  NODE_ENV: z.enum(["development", "production", "test"]).default("production"),
  DATABASE_URL: z.string().min(1),
  JWT_SECRET: z.string().min(1),
  REDIS_URL: z.string().min(1),

  POLYGON_RPC_URL: z.string().url(),
  BASE_RPC_URL: z.string().url().optional(),
  POLYGON_AMOY_RPC_URL: z.string().url().optional(),
  BASE_SEPOLIA_RPC_URL: z.string().url().optional(),

  PIMLICO_API_KEY: z.string().min(1),

  FACTORY_ADDRESS: ethAddress,
  PAYMASTER_ADDRESS: ethAddress,
  DRIPPER_ADDRESS: ethAddress,

  PAYMASTER_SIGNER_PRIVATE_KEY: z.string().startsWith("0x"),

  IMMERSVE_API_URL: z.string().url(),
  IMMERSVE_API_KEY: z.string().min(1),
  IMMERSVE_WEBHOOK_SECRET: z.string().min(1),

  REAP_API_URL: z.string().url().optional(),
  REAP_API_KEY: z.string().optional(),
});

/**
 * Local / CI: run `/health` and `/wallet/*` with only Postgres + a few vars.
 * Paymaster, bundler, Immersve, etc. get safe placeholders until you configure them.
 */
const developmentEnvSchema = z.object({
  PORT: z.string().default("3000"),
  NODE_ENV: z.enum(["development", "production", "test"]).default("development"),
  DATABASE_URL: z.string().min(1),

  JWT_SECRET: z
    .string()
    .min(1)
    .default("development-only-change-me-before-any-real-deployment-min-32"),

  REDIS_URL: z.string().default("redis://127.0.0.1:6379"),

  POLYGON_RPC_URL: z.string().url().optional(),
  BASE_RPC_URL: z.string().url().optional(),
  POLYGON_AMOY_RPC_URL: z
    .string()
    .url()
    .default("https://rpc-amoy.polygon.technology"),
  BASE_SEPOLIA_RPC_URL: z.string().url().optional(),

  PIMLICO_API_KEY: z.string().default("dev-placeholder"),

  FACTORY_ADDRESS: ethAddress.default(
    "0x1b41BbeDAAeDAf82E9D4Bc25dB3DB6144eEbC4E6"
  ),
  PAYMASTER_ADDRESS: ethAddress.default(
    "0x0000000000000000000000000000000000000000"
  ),
  DRIPPER_ADDRESS: ethAddress.default(
    "0x0000000000000000000000000000000000000000"
  ),

  /** Default: first Anvil test key — replace for real paymaster signing. */
  PAYMASTER_SIGNER_PRIVATE_KEY: ethPrivateKey.default(
    "0xac0974bec39a17e36ba4a6b4d238ff944bacb478cbed5efcae784d7bf4f2ff80"
  ),

  IMMERSVE_API_URL: z.string().url().default("https://api.example.com"),
  IMMERSVE_API_KEY: z.string().default("dev"),
  IMMERSVE_WEBHOOK_SECRET: z.string().default("dev"),

  REAP_API_URL: z.string().url().optional(),
  REAP_API_KEY: z.string().optional(),
});

const useProductionSchema = process.env.NODE_ENV === "production";

export const ENV = useProductionSchema
  ? productionEnvSchema.parse(process.env)
  : developmentEnvSchema.parse(process.env);

export type Env = typeof ENV;
