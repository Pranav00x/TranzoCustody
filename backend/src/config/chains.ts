export const CHAINS = {
  137: {
    name: "Polygon PoS",
    rpcUrl: process.env.POLYGON_RPC_URL,
    explorer: "https://polygonscan.com",
    entryPoint: "0x0000000071727De22E5E9d8BAf0edAc6f37da032",
  },
  8453: {
    name: "Base",
    rpcUrl: process.env.BASE_RPC_URL,
    explorer: "https://basescan.org",
    entryPoint: "0x0000000071727De22E5E9d8BAf0edAc6f37da032",
  },
  80002: {
    name: "Polygon Amoy",
    rpcUrl: process.env.POLYGON_AMOY_RPC_URL,
    explorer: "https://amoy.polygonscan.com",
    entryPoint: "0x0000000071727De22E5E9d8BAf0edAc6f37da032",
  },
  84532: {
     name: "Base Sepolia",
     rpcUrl: process.env.BASE_SEPOLIA_RPC_URL,
     explorer: "https://sepolia.basescan.org",
     entryPoint: "0x0000000071727De22E5E9d8BAf0edAc6f37da032",
  }
} as const;

export type ChainId = keyof typeof CHAINS;
