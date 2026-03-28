import { createPublicClient, http, Hex, formatEther, formatUnits } from "viem";
import { CHAINS, ChainId } from "../config/chains.js";

/** Well-known ERC-20 tokens per chain (extend as needed). */
const KNOWN_TOKENS: Record<number, { address: Hex; symbol: string; decimals: number }[]> = {
  80002: [
    { address: "0x41E94Eb019C0762f9Bfcf9Fb1E58725BfB0e7582", symbol: "USDC", decimals: 6 },
    { address: "0xcab78B14Ef5E3aE710DE64724C8831a8B1880544", symbol: "USDT", decimals: 6 },
  ],
  137: [
    { address: "0x3c499c542cEF5E3811e1192ce70d8cC03d5c3359", symbol: "USDC", decimals: 6 },
    { address: "0xc2132D05D31c914a87C6611C10748AEb04B58e8F", symbol: "USDT", decimals: 6 },
    { address: "0x7ceB23fD6bC0adD59E62ac25578270cFf1b9f619", symbol: "WETH", decimals: 18 },
  ],
  8453: [
    { address: "0x833589fCD6eDb6E08f4c7C32D4f71b54bdA02913", symbol: "USDC", decimals: 6 },
    { address: "0x4200000000000000000000000000000000000006", symbol: "WETH", decimals: 18 },
  ],
  84532: [
    { address: "0x036CbD53842c5426634e7929541eC2318f3dCF7e", symbol: "USDC", decimals: 6 },
  ],
};

const ERC20_BALANCE_ABI = [
  {
    name: "balanceOf",
    type: "function",
    inputs: [{ type: "address", name: "account" }],
    outputs: [{ type: "uint256" }],
    stateMutability: "view",
  },
] as const;

export interface TokenBalance {
  symbol: string;
  balance: string;
  rawBalance: string;
  decimals: number;
  contractAddress: string | null;
}

export class BalanceService {
  /**
   * Fetch native + ERC-20 balances for a wallet on a given chain.
   */
  static async getBalances(address: Hex, chainId: number): Promise<TokenBalance[]> {
    const id = chainId as ChainId;
    const chain = CHAINS[id];
    if (!chain?.rpcUrl) {
      throw new Error(`No RPC configured for chainId ${chainId}`);
    }

    const client = createPublicClient({ transport: http(chain.rpcUrl) });
    const balances: TokenBalance[] = [];

    // Native balance
    const nativeRaw = await client.getBalance({ address });
    balances.push({
      symbol: chainId === 137 || chainId === 80002 ? "MATIC" : "ETH",
      balance: formatEther(nativeRaw),
      rawBalance: nativeRaw.toString(),
      decimals: 18,
      contractAddress: null,
    });

    // ERC-20 balances
    const tokens = KNOWN_TOKENS[chainId] ?? [];
    const erc20Results = await Promise.allSettled(
      tokens.map((token) =>
        client.readContract({
          address: token.address,
          abi: ERC20_BALANCE_ABI,
          functionName: "balanceOf",
          args: [address],
        })
      )
    );

    for (let i = 0; i < tokens.length; i++) {
      const result = erc20Results[i];
      if (result.status === "fulfilled") {
        const raw = result.value as bigint;
        balances.push({
          symbol: tokens[i].symbol,
          balance: formatUnits(raw, tokens[i].decimals),
          rawBalance: raw.toString(),
          decimals: tokens[i].decimals,
          contractAddress: tokens[i].address,
        });
      }
    }

    return balances;
  }

  /**
   * Fetch token prices from CoinGecko free API.
   * Returns a map of symbol → { usd, usd_24h_change }.
   */
  static async getPrices(
    symbols: string[]
  ): Promise<Record<string, { usd: number; change24h: number }>> {
    // Map our symbols to CoinGecko IDs
    const symbolToGeckoId: Record<string, string> = {
      ETH: "ethereum",
      MATIC: "matic-network",
      WETH: "ethereum",
      USDC: "usd-coin",
      USDT: "tether",
      DAI: "dai",
      WBTC: "wrapped-bitcoin",
    };

    const geckoIds = [
      ...new Set(symbols.map((s) => symbolToGeckoId[s.toUpperCase()]).filter(Boolean)),
    ];

    if (geckoIds.length === 0) return {};

    const url = `https://api.coingecko.com/api/v3/simple/price?ids=${geckoIds.join(",")}&vs_currencies=usd&include_24hr_change=true`;

    const response = await fetch(url);
    if (!response.ok) {
      throw new Error(`CoinGecko API error: ${response.status}`);
    }

    const data = (await response.json()) as Record<
      string,
      { usd?: number; usd_24h_change?: number }
    >;

    // Map back to our symbols
    const result: Record<string, { usd: number; change24h: number }> = {};
    for (const symbol of symbols) {
      const geckoId = symbolToGeckoId[symbol.toUpperCase()];
      if (geckoId && data[geckoId]) {
        result[symbol.toUpperCase()] = {
          usd: data[geckoId].usd ?? 0,
          change24h: data[geckoId].usd_24h_change ?? 0,
        };
      }
    }

    return result;
  }
}
