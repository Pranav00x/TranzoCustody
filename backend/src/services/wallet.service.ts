import { encodeFunctionData, keccak256, toHex, encodePacked, Hex, getAddress, createPublicClient, http } from "viem";
import { ENV } from "../config/env.js";
import { CHAINS } from "../config/chains.js";

export class WalletService {
  /**
   * Predicts the CREATE2 address of a user's smart account.
   */
  static async computeCounterfactualAddress(owner: Hex, salt: number) {
     // This logic should closely match the TranzoAccountFactory.getAddress
     // but we can also fetch it from an on-chain call or static computation.
     const client = createPublicClient({ transport: http(CHAINS[137].rpcUrl) });
     const FACTORY_ABI = [
       {
         name: "getAddress",
         type: "function",
         inputs: [
           { type: "address", name: "owner" },
           { type: "uint256", name: "salt" }
         ],
         outputs: [{ type: "address" }]
       }
     ];

     return client.readContract({
       address: ENV.FACTORY_ADDRESS as Hex,
       abi: FACTORY_ABI,
       functionName: "getAddress",
       args: [owner, salt]
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
