import { Hex, createPublicClient, http } from "viem";
import { ENV } from "../config/env.js";
import { CHAINS } from "../config/chains.js";

export class WalletService {
  /**
   * Predicts the CREATE2 address of a user's smart account (factory `getAddress`).
   */
  static async computeCounterfactualAddress(owner: Hex, salt: number, chainId: number) {
    const id = chainId as keyof typeof CHAINS;
    const chain = CHAINS[id];
    const rpcUrl = chain?.rpcUrl;
    if (!rpcUrl || typeof rpcUrl !== "string") {
      throw new Error(
        `No RPC URL for chainId ${chainId}. Set the matching env var (e.g. POLYGON_AMOY_RPC_URL for 80002).`
      );
    }
    const client = createPublicClient({ transport: http(rpcUrl) });
    const FACTORY_ABI = [
      {
        name: "getAddress",
        type: "function",
        inputs: [
          { type: "address", name: "owner" },
          { type: "uint256", name: "salt" },
        ],
        outputs: [{ type: "address" }],
      },
    ] as const;

    return client.readContract({
      address: ENV.FACTORY_ADDRESS as Hex,
      abi: FACTORY_ABI,
      functionName: "getAddress",
      args: [owner, BigInt(salt)],
    }) as Promise<Hex>;
  }

  /**
   * Checks if an address is already deployed on a specific chain.
   */
  static async isDeployed(chainId: keyof typeof CHAINS, address: Hex) {
     const client = createPublicClient({ transport: http(CHAINS[chainId].rpcUrl) });
     const code = await client.getBytecode({ address });
     return code !== undefined && code !== "0x";
  }
}
