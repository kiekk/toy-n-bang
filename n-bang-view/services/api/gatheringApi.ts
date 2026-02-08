import apiClient, { handleApiError } from './client';
import type {
  GatheringResponse,
  GatheringCreateRequest,
  GatheringUpdateRequest,
} from './types';

const GATHERING_BASE = '/api/gatherings';

export const gatheringApi = {
  /**
   * 모임 생성
   */
  create: async (request: GatheringCreateRequest): Promise<GatheringResponse> => {
    try {
      const response = await apiClient.post<GatheringResponse>(GATHERING_BASE, request);
      return response.data;
    } catch (error) {
      handleApiError(error);
    }
  },

  /**
   * 사용자의 모든 모임 조회
   */
  findAll: async (): Promise<GatheringResponse[]> => {
    try {
      const response = await apiClient.get<GatheringResponse[]>(GATHERING_BASE);
      return response.data;
    } catch (error) {
      handleApiError(error);
    }
  },

  /**
   * 특정 모임 조회
   */
  findById: async (id: number): Promise<GatheringResponse> => {
    try {
      const response = await apiClient.get<GatheringResponse>(`${GATHERING_BASE}/${id}`);
      return response.data;
    } catch (error) {
      handleApiError(error);
    }
  },

  /**
   * 모임 수정
   */
  update: async (id: number, request: GatheringUpdateRequest): Promise<GatheringResponse> => {
    try {
      const response = await apiClient.patch<GatheringResponse>(
        `${GATHERING_BASE}/${id}`,
        request
      );
      return response.data;
    } catch (error) {
      handleApiError(error);
    }
  },

  /**
   * 모임 삭제
   */
  delete: async (id: number): Promise<void> => {
    try {
      await apiClient.delete(`${GATHERING_BASE}/${id}`);
    } catch (error) {
      handleApiError(error);
    }
  },
};

export default gatheringApi;
