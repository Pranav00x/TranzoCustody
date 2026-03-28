import axios from "axios";

interface TokenPrice {
  id: string;
  symbol: string;
  usd: number;
  usd_24h_change: number | null;
}

interface PortfolioToken {
  symbol: string;
  name: string;
  contractAddress: string | null;
  balance: string;
  decimals: number;
  price: number;
  value: number;
  change24h: number;
}

interface Portfolio {
  totalValue: number;
  change24h: number;
  tokens: PortfolioToken[];
}

// Simple in-memory price cache (5 min TTL)
const priceCache = new Map<string, { prices: TokenPrice[]; expiresAt: number }>();
const CACHE_TTL_MS = 5 * 60_000;

export class PortfolioService {
  /**
   * Fetch token prices from CoinGecko (free API, cached).
   */
  static async getTokenPrices(
    coinIds: string[] = ["ethereum", "usd-coin", "dai", "wrapped-bitcoin"]
  ): Promise<TokenPrice[]> {
    const key = coinIds.sort().join(",");
    const cached = priceCache.get(key);
    if (cached && cached.expiresAt > Date.now()) {
      return cached.prices;
    }

    try {
      const { data } = await axios.get<Record<string, { usd?: number; usd_24h_change?: number }>>(
        "https://api.coingecko.com/api/v3/simple/price",
        {
          params: {
            ids: key,
            vs_currencies: "usd",
            include_24hr_change: "true",
          },
          timeout: 10_000,
        }
      );

      const prices: TokenPrice[] = Object.entries(data).map(
        ([id, val]: [string, any]) => ({
          id,
          symbol: id,
          usd: val.usd ?? 0,
          usd_24h_change: val.usd_24h_change ?? null,
        })
      );

      priceCache.set(key, { prices, expiresAt: Date.now() + CACHE_TTL_MS });
      return prices;
    } catch {
      // Return stale cache on error
      return cached?.prices ?? [];
    }
  }

  /**
   * Build a portfolio summary from on-chain balances + prices.
   * In production, balances should come from BalanceService.
   */
  static async getPortfolio(
    balances: { symbol: string; name: string; contractAddress: string | null; balance: string; decimals: number }[]
  ): Promise<Portfolio> {
    const symbolToCoinId: Record<string, string> = {
      ETH: "ethereum",
      USDC: "usd-coin",
      DAI: "dai",
      WBTC: "wrapped-bitcoin",
      WETH: "ethereum",
      cbETH: "coinbase-wrapped-staked-eth",
    };

    const coinIds = balances
      .map((b) => symbolToCoinId[b.symbol])
      .filter(Boolean) as string[];

    const prices = await this.getTokenPrices(
      coinIds.length > 0 ? coinIds : undefined
    );

    const priceMap = new Map(prices.map((p) => [p.id, p]));

    const tokens: PortfolioToken[] = balances.map((b) => {
      const coinId = symbolToCoinId[b.symbol];
      const priceData = coinId ? priceMap.get(coinId) : undefined;
      const price = priceData?.usd ?? 0;
      const rawBalance = Number(b.balance) / 10 ** b.decimals;
      const value = rawBalance * price;

      return {
        symbol: b.symbol,
        name: b.name,
        contractAddress: b.contractAddress,
        balance: b.balance,
        decimals: b.decimals,
        price,
        value,
        change24h: priceData?.usd_24h_change ?? 0,
      };
    });

    const totalValue = tokens.reduce((sum, t) => sum + t.value, 0);
    const weightedChange =
      totalValue > 0
        ? tokens.reduce((sum, t) => sum + t.change24h * (t.value / totalValue), 0)
        : 0;

    return {
      totalValue,
      change24h: weightedChange,
      tokens,
    };
  }
}
