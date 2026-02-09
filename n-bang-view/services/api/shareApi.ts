import apiClient, { handleApiError } from './client';
import axios from 'axios';
import type { SharedSettlementLinkResponse, SharedSettlementResponse } from './types';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

export const shareApi = {
  createShareLink: async (gatheringId: number): Promise<SharedSettlementLinkResponse> => {
    try {
      const response = await apiClient.post<SharedSettlementLinkResponse>(
        `/api/gatherings/${gatheringId}/share`
      );
      return response.data;
    } catch (error) {
      handleApiError(error);
    }
  },

  getSharedSettlement: async (uuid: string): Promise<SharedSettlementResponse> => {
    try {
      const response = await axios.get<SharedSettlementResponse>(
        `${API_BASE_URL}/api/shared/${uuid}`
      );
      return response.data;
    } catch (error) {
      handleApiError(error);
    }
  },
};
