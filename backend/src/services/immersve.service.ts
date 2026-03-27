import axios from "axios";
import { ENV } from "../config/env.js";

export class ImmersveService {
  private static api = axios.create({
    baseURL: ENV.IMMERSVE_API_URL,
    headers: {
      "x-api-key": ENV.IMMERSVE_API_KEY,
    },
  });

  static async issueVirtualCard(userId: string, fundingChannelId: string) {
    const response = await this.api.post("/cards/virtual", {
      userId,
      fundingChannelId,
    });
    return response.data;
  }

  static async getCards(userId: string) {
    const response = await this.api.get(`/users/${userId}/cards`);
    return response.data;
  }

  static async freezeCard(cardId: string) {
    const response = await this.api.post(`/cards/${cardId}/freeze`);
    return response.data;
  }

  static async unfreezeCard(cardId: string) {
      const response = await this.api.post(`/cards/${cardId}/unfreeze`);
      return response.data;
  }

  static async getTransactions(cardId: string) {
      const response = await this.api.get(`/cards/${cardId}/transactions`);
      return response.data;
  }
}
