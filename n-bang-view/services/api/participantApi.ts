import apiClient, { handleApiError } from './client';
import type { ParticipantResponse, ParticipantCreateRequest } from './types';

export const participantApi = {
  /**
   * 모임에 참여자 추가
   */
  addToGathering: async (
    gatheringId: number,
    request: ParticipantCreateRequest
  ): Promise<ParticipantResponse> => {
    try {
      const response = await apiClient.post<ParticipantResponse>(
        `/api/gatherings/${gatheringId}/participants`,
        request
      );
      return response.data;
    } catch (error) {
      handleApiError(error);
    }
  },

  /**
   * 모임의 참여자 목록 조회
   */
  findByGatheringId: async (gatheringId: number): Promise<ParticipantResponse[]> => {
    try {
      const response = await apiClient.get<ParticipantResponse[]>(
        `/api/gatherings/${gatheringId}/participants`
      );
      return response.data;
    } catch (error) {
      handleApiError(error);
    }
  },

  /**
   * 참여자 삭제
   */
  delete: async (id: number): Promise<void> => {
    try {
      await apiClient.delete(`/api/participants/${id}`);
    } catch (error) {
      handleApiError(error);
    }
  },
};

export default participantApi;
