import apiClient, { handleApiError } from './client';
import type { RoundResponse, RoundCreateRequest, RoundUpdateRequest } from './types';

export const roundApi = {
  /**
   * 정산 라운드 생성
   */
  create: async (gatheringId: number, request: RoundCreateRequest): Promise<RoundResponse> => {
    try {
      const response = await apiClient.post<RoundResponse>(
        `/api/gatherings/${gatheringId}/rounds`,
        request
      );
      return response.data;
    } catch (error) {
      handleApiError(error);
    }
  },

  /**
   * 모임의 모든 라운드 조회
   */
  findByGatheringId: async (gatheringId: number): Promise<RoundResponse[]> => {
    try {
      const response = await apiClient.get<RoundResponse[]>(
        `/api/gatherings/${gatheringId}/rounds`
      );
      return response.data;
    } catch (error) {
      handleApiError(error);
    }
  },

  /**
   * 특정 라운드 조회
   */
  findById: async (id: number): Promise<RoundResponse> => {
    try {
      const response = await apiClient.get<RoundResponse>(`/api/rounds/${id}`);
      return response.data;
    } catch (error) {
      handleApiError(error);
    }
  },

  /**
   * 라운드 수정
   */
  update: async (id: number, request: RoundUpdateRequest): Promise<RoundResponse> => {
    try {
      const response = await apiClient.patch<RoundResponse>(`/api/rounds/${id}`, request);
      return response.data;
    } catch (error) {
      handleApiError(error);
    }
  },

  /**
   * 라운드 삭제
   */
  delete: async (id: number): Promise<void> => {
    try {
      await apiClient.delete(`/api/rounds/${id}`);
    } catch (error) {
      handleApiError(error);
    }
  },

  /**
   * 영수증 이미지 업로드
   */
  uploadImage: async (id: number, file: File): Promise<RoundResponse> => {
    try {
      const formData = new FormData();
      formData.append('file', file);

      const response = await apiClient.post<RoundResponse>(`/api/rounds/${id}/image`, formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });
      return response.data;
    } catch (error) {
      handleApiError(error);
    }
  },
};

export default roundApi;
