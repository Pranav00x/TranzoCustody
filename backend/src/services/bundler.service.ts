import { createPimlicoClient } from "permissionless/clients/pimlico";
import { createPublicClient, http, Hex } from "viem";
import { polygon, polygonAmoy, base, baseSepolia } from "viem/chains";
import { ENV } from "../config/env.js";
import { CHAINS } from "../config/chains.js";

const viemChains = {
  137: polygon,
  80002: polygonAmoy,
  8453: base,
  84532: baseSepolia,
} as const;

export class BundlerService {
  private static getBundlerUrl(chainId: number) {
    return `https://api.pimlico.io/v2/${chainId}/rpc?apikey=${ENV.PIMLICO_API_KEY}`;
  }

  static getClient(chainId: keyof typeof viemChains) {
    const transport = http(this.getBundlerUrl(chainId));
    
    return createPimlicoClient({
      chain: viemChains[chainId],
      transport,
      entryPoint: {
        address: CHAINS[chainId].entryPoint as Hex,
        version: "0.7",
      },
    });
  }

  static async estimateUserOperationGas(chainId: number, userOp: any) {
    const client = this.getClient(chainId as any);
    return (client as any).estimateUserOperationGas(userOp);
  }

  static async sendUserOperation(chainId: number, userOp: any) {
    const client = this.getClient(chainId as any);
    return (client as any).sendUserOperation(userOp);
  }

  static async getUserOperationReceipt(chainId: number, hash: Hex) {
    const client = this.getClient(chainId as any);
    return (client as any).getUserOperationReceipt({ hash });
  }
}
