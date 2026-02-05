import { useState, useCallback, useEffect } from 'react';
import { gatheringApi, participantApi, roundApi, calculationApi } from '../services/api';
import type { Gathering, Participant, SettlementRound, Exclusion, GatheringType } from '../types';
import {
  mapGatheringFromApi,
  mapGatheringToCreateRequest,
  mapRoundToCreateRequest,
  mapRoundToUpdateRequest,
} from '../utils/mappers';

interface UseGatheringsReturn {
  gatherings: Gathering[];
  isLoading: boolean;
  error: string | null;
  // Gathering operations
  fetchGatherings: () => Promise<void>;
  createGathering: (name: string, type: GatheringType, startDate: string, endDate: string) => Promise<Gathering | null>;
  deleteGathering: (id: string) => Promise<boolean>;
  // Participant operations
  addParticipant: (gatheringId: string, name: string) => Promise<Participant | null>;
  removeParticipant: (gatheringId: string, participantId: string) => Promise<boolean>;
  // Round operations
  addRound: (gatheringId: string, title: string, amount: number, payerId: string, exclusions?: Exclusion[]) => Promise<SettlementRound | null>;
  updateRound: (gatheringId: string, roundId: string, title: string, amount: number, payerId: string, exclusions?: Exclusion[]) => Promise<SettlementRound | null>;
  deleteRound: (gatheringId: string, roundId: string) => Promise<boolean>;
  // Local state update (for optimistic updates)
  updateLocalGathering: (id: string, updates: Partial<Gathering>) => void;
  refreshGathering: (id: string) => Promise<Gathering | null>;
}

export const useGatherings = (isAuthenticated: boolean): UseGatheringsReturn => {
  const [gatherings, setGatherings] = useState<Gathering[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Fetch all gatherings
  const fetchGatherings = useCallback(async () => {
    if (!isAuthenticated) return;

    setIsLoading(true);
    setError(null);
    try {
      const response = await gatheringApi.findAll();
      const mapped = response.map(mapGatheringFromApi);
      setGatherings(mapped);
    } catch (err) {
      setError('모임 목록을 불러오는데 실패했습니다.');
      console.error('Failed to fetch gatherings:', err);
    } finally {
      setIsLoading(false);
    }
  }, [isAuthenticated]);

  // Fetch single gathering
  const refreshGathering = useCallback(async (id: string): Promise<Gathering | null> => {
    try {
      const response = await gatheringApi.findById(Number(id));
      const mapped = mapGatheringFromApi(response);
      setGatherings(prev => prev.map(g => g.id === id ? mapped : g));
      return mapped;
    } catch (err) {
      console.error('Failed to refresh gathering:', err);
      return null;
    }
  }, []);

  // Create gathering
  const createGathering = useCallback(async (
    name: string,
    type: GatheringType,
    startDate: string,
    endDate: string
  ): Promise<Gathering | null> => {
    try {
      const request = mapGatheringToCreateRequest(name, type, startDate, endDate);
      const response = await gatheringApi.create(request);
      const mapped = mapGatheringFromApi(response);
      setGatherings(prev => [mapped, ...prev]);
      return mapped;
    } catch (err) {
      setError('모임 생성에 실패했습니다.');
      console.error('Failed to create gathering:', err);
      return null;
    }
  }, []);

  // Delete gathering
  const deleteGathering = useCallback(async (id: string): Promise<boolean> => {
    try {
      await gatheringApi.delete(Number(id));
      setGatherings(prev => prev.filter(g => g.id !== id));
      return true;
    } catch (err) {
      setError('모임 삭제에 실패했습니다.');
      console.error('Failed to delete gathering:', err);
      return false;
    }
  }, []);

  // Add participant
  const addParticipant = useCallback(async (
    gatheringId: string,
    name: string
  ): Promise<Participant | null> => {
    try {
      const response = await participantApi.addToGathering(Number(gatheringId), { name });
      const newParticipant: Participant = {
        id: String(response.id),
        name: response.name,
      };

      // Update local state
      setGatherings(prev => prev.map(g => {
        if (g.id !== gatheringId) return g;
        return {
          ...g,
          participants: [...g.participants, newParticipant],
        };
      }));

      return newParticipant;
    } catch (err) {
      setError('참여자 추가에 실패했습니다.');
      console.error('Failed to add participant:', err);
      return null;
    }
  }, []);

  // Remove participant
  const removeParticipant = useCallback(async (
    gatheringId: string,
    participantId: string
  ): Promise<boolean> => {
    try {
      await participantApi.delete(Number(participantId));

      // Update local state
      setGatherings(prev => prev.map(g => {
        if (g.id !== gatheringId) return g;
        const updatedParticipants = g.participants.filter(p => p.id !== participantId);
        const updatedRounds = g.rounds.map(r => ({
          ...r,
          payerId: r.payerId === participantId ? (updatedParticipants[0]?.id || '') : r.payerId,
          excluded: r.excluded.filter(e => e.participantId !== participantId),
        }));
        return { ...g, participants: updatedParticipants, rounds: updatedRounds };
      }));

      return true;
    } catch (err) {
      setError('참여자 삭제에 실패했습니다.');
      console.error('Failed to remove participant:', err);
      return false;
    }
  }, []);

  // Add round
  const addRound = useCallback(async (
    gatheringId: string,
    title: string,
    amount: number,
    payerId: string,
    exclusions?: Exclusion[]
  ): Promise<SettlementRound | null> => {
    try {
      const request = mapRoundToCreateRequest(title, amount, Number(payerId), exclusions);
      const response = await roundApi.create(Number(gatheringId), request);

      const newRound: SettlementRound = {
        id: String(response.id),
        title: response.title,
        amount: response.amount,
        payerId: String(response.payerId),
        excluded: response.exclusions.map(e => ({
          participantId: String(e.participantId),
          reason: e.reason,
        })),
        receiptImage: response.receiptImageUrl || undefined,
      };

      // Update local state
      setGatherings(prev => prev.map(g => {
        if (g.id !== gatheringId) return g;
        return { ...g, rounds: [...g.rounds, newRound] };
      }));

      return newRound;
    } catch (err) {
      setError('지출 내역 추가에 실패했습니다.');
      console.error('Failed to add round:', err);
      return null;
    }
  }, []);

  // Update round
  const updateRound = useCallback(async (
    gatheringId: string,
    roundId: string,
    title: string,
    amount: number,
    payerId: string,
    exclusions?: Exclusion[]
  ): Promise<SettlementRound | null> => {
    try {
      const request = mapRoundToUpdateRequest(title, amount, Number(payerId), exclusions);
      const response = await roundApi.update(Number(roundId), request);

      const updatedRound: SettlementRound = {
        id: String(response.id),
        title: response.title,
        amount: response.amount,
        payerId: String(response.payerId),
        excluded: response.exclusions.map(e => ({
          participantId: String(e.participantId),
          reason: e.reason,
        })),
        receiptImage: response.receiptImageUrl || undefined,
      };

      // Update local state
      setGatherings(prev => prev.map(g => {
        if (g.id !== gatheringId) return g;
        return {
          ...g,
          rounds: g.rounds.map(r => r.id === roundId ? updatedRound : r),
        };
      }));

      return updatedRound;
    } catch (err) {
      setError('지출 내역 수정에 실패했습니다.');
      console.error('Failed to update round:', err);
      return null;
    }
  }, []);

  // Delete round
  const deleteRound = useCallback(async (
    gatheringId: string,
    roundId: string
  ): Promise<boolean> => {
    try {
      await roundApi.delete(Number(roundId));

      // Update local state
      setGatherings(prev => prev.map(g => {
        if (g.id !== gatheringId) return g;
        return { ...g, rounds: g.rounds.filter(r => r.id !== roundId) };
      }));

      return true;
    } catch (err) {
      setError('지출 내역 삭제에 실패했습니다.');
      console.error('Failed to delete round:', err);
      return false;
    }
  }, []);

  // Update local gathering (for optimistic updates)
  const updateLocalGathering = useCallback((id: string, updates: Partial<Gathering>) => {
    setGatherings(prev => prev.map(g => g.id === id ? { ...g, ...updates } : g));
  }, []);

  // Fetch gatherings on mount when authenticated
  useEffect(() => {
    if (isAuthenticated) {
      fetchGatherings();
    } else {
      setGatherings([]);
    }
  }, [isAuthenticated, fetchGatherings]);

  return {
    gatherings,
    isLoading,
    error,
    fetchGatherings,
    createGathering,
    deleteGathering,
    addParticipant,
    removeParticipant,
    addRound,
    updateRound,
    deleteRound,
    updateLocalGathering,
    refreshGathering,
  };
};

export default useGatherings;
