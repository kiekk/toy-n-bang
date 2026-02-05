import apiClient, { handleApiError } from './client';
import type { CalculationResponse } from './types';

export const calculationApi = {
  /**
   * 모임 정산 계산
   */
  calculate: async (gatheringId: number): Promise<CalculationResponse> => {
    try {
      const response = await apiClient.get<CalculationResponse>(
        `/api/gatherings/${gatheringId}/calculate`
      );
      return response.data;
    } catch (error) {
      handleApiError(error);
    }
  },
};

export default calculationApi;
