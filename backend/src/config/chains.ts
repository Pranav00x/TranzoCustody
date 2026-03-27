import { ENV } from "./env.js";

export const CHAINS = {
  137: {
    name: "Polygon PoS",
    rpcUrl: ENV.POLYGON_RPC_URL ?? "",
    explorer: "https://polygonscan.com",
    entryPoint: "0x0000000071727De22E5E9d8BAf0edAc6f37da032",
  },
  8453: {
    name: "Base",
    rpcUrl: ENV.BASE_RPC_URL ?? "",
    explorer: "https://basescan.org",
    entryPoint: "0x0000000071727De22E5E9d8BAf0edAc6f37da032",
  },
  80002: {
    name: "Polygon Amoy",
    rpcUrl: ENV.POLYGON_AMOY_RPC_URL ?? "",
    explorer: "https://amoy.polygonscan.com",
    entryPoint: "0x0000000071727De22E5E9d8BAf0edAc6f37da032",
  },
  84532: {
    name: "Base Sepolia",
    rpcUrl: ENV.BASE_SEPOLIA_RPC_URL ?? "",
    explorer: "https://sepolia.basescan.org",
    entryPoint: "0x0000000071727De22E5E9d8BAf0edAc6f37da032",
  },
} as const;

export type ChainId = keyof typeof CHAINS;
