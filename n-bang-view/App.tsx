
import React, { useState, useMemo, useEffect } from 'react';
import {
  Users,
  Receipt,
  Plus,
  Trash2,
  ChevronRight,
  Calculator,
  UserMinus,
  ArrowRightLeft,
  Copy,
  Check,
  ChevronLeft,
  Calendar as CalendarIcon,
  Layers,
  ChevronRight as ChevronRightIcon,
  ChevronLeft as ChevronLeftIcon,
  Clock,
  CalendarDays,
  CreditCard,
  Edit2,
  Zap,
  ShieldCheck,
  LogOut,
  AlertCircle,
  Loader2
} from 'lucide-react';
import { Participant, SettlementRound, DebtLink, Gathering, Exclusion, GatheringType, GATHERING_TYPES } from './types.ts';
import { calculateBalances, resolveDebts } from './utils/calculator.ts';
import { generateKakaoMessage } from './services/messageService.ts';
import { useAuth } from './contexts/AuthContext.tsx';
import { useToast } from './contexts/ToastContext.tsx';
import { useGatherings } from './hooks';

// Vibrant, accessible color palette for gathering timeline bars
const GATHERING_COLORS = [
  'bg-indigo-500', 
  'bg-rose-500', 
  'bg-amber-500', 
  'bg-emerald-500', 
  'bg-violet-500', 
  'bg-sky-500', 
  'bg-fuchsia-500', 
  'bg-orange-500',
  'bg-teal-500'
];

const App: React.FC = () => {
  // Auth State (from AuthContext)
  const { user, isLoading: isAuthLoading, isAuthenticated, login, logout } = useAuth();

  // Toast notifications
  const { showToast } = useToast();

  // Data State (from API)
  const {
    gatherings,
    isLoading: isDataLoading,
    error: dataError,
    createGathering: apiCreateGathering,
    deleteGathering: apiDeleteGathering,
    addParticipant: apiAddParticipant,
    removeParticipant: apiRemoveParticipant,
    addRound: apiAddRound,
    updateRound: apiUpdateRound,
    deleteRound: apiDeleteRound,
    updateLocalGathering,
    refreshGathering,
  } = useGatherings(isAuthenticated);

  // Local State (directory for bank info - stays local)
  const [directory, setDirectory] = useState<Record<string, { bankName?: string, accountNumber?: string }>>(() => {
    const saved = localStorage.getItem('nbang_directory');
    return saved ? JSON.parse(saved) : {};
  });

  const [activeGatheringId, setActiveGatheringId] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<'participants' | 'rounds' | 'result'>('participants');

  // UI State
  const [isCreatingGathering, setIsCreatingGathering] = useState(false);
  const [newGatheringName, setNewGatheringName] = useState('');
  const [newGatheringType, setNewGatheringType] = useState<GatheringType>('MEETING');
  const [startDateStr, setStartDateStr] = useState(new Date().toISOString().split('T')[0]);
  const [endDateStr, setEndDateStr] = useState(new Date().toISOString().split('T')[0]);

  // Validation State
  const [nameError, setNameError] = useState('');
  const [dateError, setDateError] = useState('');
  
  const [newParticipantName, setNewParticipantName] = useState('');
  const [kakaoMessage, setKakaoMessage] = useState('');
  const [showCopyFeedback, setShowCopyFeedback] = useState(false);

  const [isImportModalOpen, setIsImportModalOpen] = useState(false);
  const [importText, setImportText] = useState('');
  const [isImporting, setIsImporting] = useState(false);

  const [editingParticipantId, setEditingParticipantId] = useState<string | null>(null);
  const [editParticipantName, setEditParticipantName] = useState('');
  const [editBankName, setEditBankName] = useState('');
  const [editAccountNum, setEditAccountNum] = useState('');

  const [viewDate, setViewDate] = useState(new Date());
  const [selectedDate, setSelectedDate] = useState<string | null>(new Date().toDateString());

  // 버림 단위 (1 = 버림 없음, 10, 100, 1000, 10000)
  const [roundingUnit, setRoundingUnit] = useState<number>(1);

  // Persistence (only directory - gatherings are now on server)
  useEffect(() => {
    localStorage.setItem('nbang_directory', JSON.stringify(directory));
  }, [directory]);

  // Show toast when data error occurs
  useEffect(() => {
    if (dataError) {
      showToast('error', dataError);
    }
  }, [dataError, showToast]);

  // Apply directory bank info to participants when gatherings change
  useEffect(() => {
    if (!gatherings.length || !Object.keys(directory).length) return;

    gatherings.forEach(gathering => {
      const updatedParticipants = gathering.participants.map(p => {
        const dirInfo = directory[p.name];
        if (dirInfo && (!p.bankName || !p.accountNumber)) {
          return { ...p, bankName: dirInfo.bankName, accountNumber: dirInfo.accountNumber };
        }
        return p;
      });

      // Only update if there are changes
      const hasChanges = updatedParticipants.some((p, i) =>
        p.bankName !== gathering.participants[i].bankName ||
        p.accountNumber !== gathering.participants[i].accountNumber
      );

      if (hasChanges) {
        updateLocalGathering(gathering.id, { participants: updatedParticipants });
      }
    });
  }, [gatherings, directory, updateLocalGathering]);

  // Auth Handlers
  const handleLogout = () => {
    if (confirm('로그아웃 하시겠습니까?')) {
      logout();
      setActiveGatheringId(null);
    }
  };

  // Gathering Logic
  const createGathering = async () => {
    // Reset errors
    setNameError('');
    setDateError('');

    // Validate name
    if (!newGatheringName.trim()) {
      setNameError('모임 이름을 입력해주세요.');
      return;
    }
    if (newGatheringName.trim().length < 2) {
      setNameError('모임 이름은 최소 2자 이상이어야 합니다.');
      return;
    }

    // Validate dates
    if (!startDateStr || !endDateStr) {
      setDateError('날짜를 선택해주세요.');
      return;
    }
    if (new Date(endDateStr) < new Date(startDateStr)) {
      setDateError('종료일은 시작일 이후여야 합니다.');
      return;
    }

    const newG = await apiCreateGathering(newGatheringName.trim(), newGatheringType, startDateStr, endDateStr);
    if (newG) {
      setActiveGatheringId(newG.id);
      setNewGatheringName('');
      setNewGatheringType('MEETING');
      setNameError('');
      setDateError('');
      setIsCreatingGathering(false);
      setActiveTab('participants');
      showToast('success', `'${newG.name}' 모임이 생성되었습니다.`);
    }
  };

  const deleteGathering = async (id: string, e?: React.MouseEvent) => {
    e?.stopPropagation();
    if (confirm('이 모임의 모든 정산 기록이 영구적으로 삭제됩니다. 계속하시겠습니까?')) {
      const success = await apiDeleteGathering(id);
      if (success) {
        if (activeGatheringId === id) {
          setActiveGatheringId(null);
        }
        showToast('success', '모임이 삭제되었습니다.');
      }
    }
  };

  const activeGathering = useMemo(() => 
    gatherings.find(g => g.id === activeGatheringId) || null
  , [gatherings, activeGatheringId]);

  // Calendar Helpers
  const calendarDays = useMemo(() => {
    const year = viewDate.getFullYear();
    const month = viewDate.getMonth();
    const days = [];
    const firstDay = new Date(year, month, 1).getDay();
    const totalDays = new Date(year, month + 1, 0).getDate();
    for (let i = 0; i < firstDay; i++) days.push(null);
    for (let i = 1; i <= totalDays; i++) days.push(new Date(year, month, i));
    return days;
  }, [viewDate]);

  const isDateInGathering = (date: Date, gathering: Gathering) => {
    const d = new Date(date); d.setHours(0, 0, 0, 0);
    const start = new Date(gathering.startDate); start.setHours(0, 0, 0, 0);
    const end = new Date(gathering.endDate); end.setHours(0, 0, 0, 0);
    return d >= start && d <= end;
  };

  const selectedDayGatherings = useMemo(() => {
    if (!selectedDate) return [];
    const date = new Date(selectedDate);
    return gatherings.filter(g => isDateInGathering(date, g));
  }, [selectedDate, gatherings]);

  // Participant Handlers
  const addParticipant = async (nameArg?: string) => {
    const name = (nameArg || newParticipantName).trim();
    if (!name || !activeGatheringId) return;

    const newParticipant = await apiAddParticipant(activeGatheringId, name);
    if (newParticipant) {
      // Refresh gathering from server to ensure latest state with all participants
      await refreshGathering(activeGatheringId);
      setNewParticipantName('');
      showToast('success', `${name}님이 추가되었습니다.`);
    }
  };

  const removeParticipant = async (id: string) => {
    if (!activeGatheringId) return;
    const participant = activeGathering?.participants.find(p => p.id === id);
    const success = await apiRemoveParticipant(activeGatheringId, id);
    if (success && participant) {
      showToast('success', `${participant.name}님이 제거되었습니다.`);
    }
  };

  const saveParticipantInfo = () => {
    if (!editingParticipantId || !activeGatheringId || !activeGathering) return;
    const participant = activeGathering.participants.find(p => p.id === editingParticipantId);
    if (!participant) return;

    const newName = editParticipantName.trim() || participant.name;
    const bName = editBankName.trim();
    const aNum = editAccountNum.trim();

    // Save to directory (local storage) with new name
    setDirectory(prev => ({ ...prev, [newName]: { bankName: bName, accountNumber: aNum } }));

    // Update local state including name
    updateLocalGathering(activeGatheringId, {
      participants: activeGathering.participants.map(p =>
        p.id === editingParticipantId ? { ...p, name: newName, bankName: bName, accountNumber: aNum } : p
      ),
    });
    setEditingParticipantId(null);
  };

  const handleQuickImport = async () => {
    if (!importText.trim() || !activeGatheringId) return;
    setIsImporting(true);
    try {
      // 간단한 텍스트 파싱: 줄바꿈이나 쉼표로 구분된 이름 추출
      const lines = importText.split(/[\n,]+/).map(s => s.trim()).filter(Boolean);

      for (const line of lines) {
        // "이름 은행 계좌번호" 또는 "이름" 형식 파싱
        const parts = line.split(/\s+/);
        const name = parts[0];
        const bankName = parts.length >= 3 ? parts[1] : undefined;
        const accountNumber = parts.length >= 3 ? parts.slice(2).join('') : undefined;

        if (name) {
          const newParticipant = await apiAddParticipant(activeGatheringId, name);
          if (newParticipant && (bankName || accountNumber)) {
            setDirectory(prev => ({
              ...prev,
              [name]: { bankName, accountNumber }
            }));
          }
        }
      }

      await refreshGathering(activeGatheringId);
      setIsImportModalOpen(false);
      setImportText('');
      showToast('success', `${lines.length}명의 참여자가 추가되었습니다.`);
    } catch (error) {
      showToast('error', '참여자 추가에 실패했습니다.');
    } finally {
      setIsImporting(false);
    }
  };

  // Round Handlers
  const handleAddRound = async () => {
    if (!activeGatheringId || !activeGathering) return;
    const title = `${activeGathering.rounds.length + 1}차 정산`;
    const payerId = activeGathering.participants[0]?.id || '';
    if (!payerId) {
      showToast('warning', '먼저 참여자를 추가해주세요.');
      return;
    }
    const result = await apiAddRound(activeGatheringId, title, 0, payerId, []);
    if (result) {
      showToast('success', '지출 내역이 추가되었습니다.');
    }
  };

  const handleDeleteRound = async (roundId: string) => {
    if (!activeGatheringId) return;
    const success = await apiDeleteRound(activeGatheringId, roundId);
    if (success) {
      showToast('success', '지출 내역이 삭제되었습니다.');
    }
  };

  const handleUpdateRound = (roundId: string, updates: Partial<SettlementRound>) => {
    if (!activeGatheringId || !activeGathering) return;
    const round = activeGathering.rounds.find(r => r.id === roundId);
    if (!round) return;

    // Optimistic local update first
    const updatedRounds = activeGathering.rounds.map(r =>
      r.id === roundId ? { ...r, ...updates } : r
    );
    updateLocalGathering(activeGatheringId, { rounds: updatedRounds });
  };

  // API update for rounds - accepts optional updates to ensure latest values are sent
  const saveRoundToServer = async (roundId: string, updates?: Partial<SettlementRound>) => {
    if (!activeGatheringId || !activeGathering) return;
    const round = activeGathering.rounds.find(r => r.id === roundId);
    if (!round) return;

    // Merge updates to get the latest values
    const updatedRound = updates ? { ...round, ...updates } : round;

    await apiUpdateRound(
      activeGatheringId,
      roundId,
      updatedRound.title,
      updatedRound.amount,
      updatedRound.payerId,
      updatedRound.excluded || []
    );
  };

  const calculationResult = useMemo(() => {
    if (!activeGathering) return { balances: [], debts: [], totalSpent: 0 };
    const b = calculateBalances(activeGathering.participants, activeGathering.rounds);
    const d = resolveDebts(b);
    const total = activeGathering.rounds.reduce((sum, r) => sum + r.amount, 0);

    // 버림 단위 적용
    const roundedDebts = roundingUnit > 1
      ? d.map(debt => ({
          ...debt,
          amount: Math.floor(debt.amount / roundingUnit) * roundingUnit
        })).filter(debt => debt.amount > 0)
      : d;

    return { balances: b, debts: roundedDebts, totalSpent: total };
  }, [activeGathering, roundingUnit]);

  const copyToClipboard = (textToCopy?: string) => {
    const text = textToCopy || kakaoMessage || calculationResult.debts.map(d => `${d.from} -> ${d.to}: ${Math.round(d.amount).toLocaleString()}원`).join('\n');
    navigator.clipboard.writeText(text);
    setShowCopyFeedback(true); setTimeout(() => setShowCopyFeedback(false), 2000);
  };

  // Loading View
  if (isAuthLoading) {
    return (
      <div className="min-h-screen bg-indigo-600 flex flex-col items-center justify-center p-6">
        <div className="flex flex-col items-center gap-4">
          <Loader2 size={48} className="text-white animate-spin" />
          <p className="text-white font-medium">로그인 중...</p>
        </div>
      </div>
    );
  }

  // Login View
  if (!isAuthenticated) {
    return (
      <div className="min-h-screen bg-indigo-600 flex flex-col items-center justify-center p-6 animate-in fade-in duration-700">
        <div className="max-w-md w-full bg-white rounded-[48px] p-10 shadow-2xl space-y-10 text-center">
          <div className="flex flex-col items-center gap-4">
            <div className="w-20 h-20 bg-indigo-600 rounded-[28px] flex items-center justify-center text-white shadow-lg rotate-3">
              <Layers size={40} />
            </div>
            <h1 className="text-4xl font-black text-slate-900 tracking-tight">N-Bang</h1>
            <p className="text-slate-500 font-medium leading-relaxed px-4">더 이상의 머리 아픈 정산은 그만.<br/>N빵으로 쉽게, 정확하게</p>
          </div>
          <div className="space-y-4">
            <button onClick={() => login('kakao')} className="w-full bg-[#FEE500] text-[#191919] rounded-2xl py-4 font-black flex items-center justify-center gap-3 hover:opacity-90 transition-all shadow-md active:scale-95">
              <div className="w-6 h-6 bg-[#191919] rounded-full flex items-center justify-center"><span className="text-[10px] text-[#FEE500] font-black">K</span></div>
              카카오로 계속하기
            </button>
            <button onClick={() => login('google')} className="w-full bg-white text-slate-700 border border-slate-200 rounded-2xl py-4 font-black flex items-center justify-center gap-3 hover:bg-slate-50 transition-all shadow-md active:scale-95">
              <svg className="w-5 h-5" viewBox="0 0 48 48"><path fill="#EA4335" d="M24 9.5c3.54 0 6.71 1.22 9.21 3.6l6.85-6.85C35.9 2.38 30.47 0 24 0 14.62 0 6.51 5.38 2.56 13.22l7.98 6.19C12.43 13.72 17.74 9.5 24 9.5z"></path><path fill="#4285F4" d="M46.98 24.55c0-1.57-.15-3.13-.38-4.55H24v9.02h12.94c-.58 2.96-2.26 5.48-4.78 7.18l7.73 6c4.51-4.18 7.09-10.36 7.09-17.65z"></path><path fill="#FBBC05" d="M10.53 28.59c-.48-1.45-.76-2.99-.76-4.59s.27-3.14.76-4.59l-7.98-6.19C.92 16.46 0 20.12 0 24s.92 7.54 2.56 10.78l7.97-6.19z"></path><path fill="#34A853" d="M24 48c6.48 0 11.93-2.13 15.89-5.81l-7.73-6c-2.15 1.45-4.92 2.3-8.16 2.3-6.26 0-11.57-4.22-13.47-9.91l-7.98 6.19C6.51 42.62 14.62 48 24 48z"></path></svg>
              Google로 계속하기
            </button>
          </div>
          <div className="pt-6 border-t border-slate-50">
            <div className="flex items-center justify-center gap-2 text-slate-300 text-[10px] font-bold uppercase tracking-widest"><ShieldCheck size={14} /> 100% Secure & Private</div>
          </div>
        </div>
      </div>
    );
  }

  // Home View
  if (!activeGatheringId) {
    return (
      <div className="min-h-screen bg-slate-50 p-6 md:p-12 max-w-5xl mx-auto space-y-12 animate-in fade-in duration-500 pb-32">
        {/* Error Banner */}
        {dataError && (
          <div className="bg-red-50 border border-red-200 rounded-2xl p-4 flex items-center gap-3 text-red-600">
            <AlertCircle size={20} />
            <span className="font-medium">{dataError}</span>
          </div>
        )}

        {/* Loading Overlay */}
        {isDataLoading && (
          <div className="fixed inset-0 bg-white/80 backdrop-blur-sm flex items-center justify-center z-50">
            <div className="flex flex-col items-center gap-4">
              <Loader2 size={48} className="text-indigo-600 animate-spin" />
              <p className="text-slate-600 font-medium">데이터 로딩 중...</p>
            </div>
          </div>
        )}

        <header className="flex flex-wrap items-center justify-between gap-6">
          <div className="flex items-center gap-4">
            <div className="w-14 h-14 bg-white rounded-2xl shadow-sm border border-slate-200 flex items-center justify-center overflow-hidden">
              {user?.profileImage ? (
                <img src={user.profileImage} alt="profile" className="w-full h-full object-cover" />
              ) : (
                <div className="w-full h-full bg-indigo-100 flex items-center justify-center text-indigo-600 font-black text-xl">
                  {user?.nickname?.charAt(0) || '?'}
                </div>
              )}
            </div>
            <div>
              <h2 className="text-xl font-black text-slate-900 tracking-tight">반가워요, {user?.nickname}님!</h2>
              <button onClick={handleLogout} className="text-xs font-bold text-slate-400 hover:text-red-500 transition-colors flex items-center gap-1 mt-0.5">로그아웃 <LogOut size={12} /></button>
            </div>
          </div>
          <button onClick={() => setIsCreatingGathering(true)} className="bg-indigo-600 hover:bg-indigo-700 text-white rounded-2xl px-8 py-4 font-black flex items-center gap-2 transition-all shadow-lg shadow-indigo-200 active:scale-95"><Plus size={24} /> 새 모임 시작하기</button>
        </header>

        {/* Improved Calendar with Timeline Bars */}
        <section className="bg-white rounded-[48px] border border-slate-200 p-8 md:p-12 shadow-sm relative overflow-hidden">
          <div className="flex items-center justify-between mb-12">
            <h2 className="text-2xl font-black text-slate-800 flex items-center gap-3"><CalendarIcon className="text-indigo-600" size={32} /> 정산 캘린더</h2>
            <div className="flex items-center gap-4 bg-slate-50 p-2 rounded-2xl border border-slate-100 shadow-inner">
              <button onClick={() => setViewDate(new Date(viewDate.getFullYear(), viewDate.getMonth() - 1))} className="p-2 hover:bg-white hover:shadow-sm rounded-xl transition-all"><ChevronLeftIcon size={20} /></button>
              <span className="font-black text-slate-700 min-w-[120px] text-center tracking-tight">{viewDate.getFullYear()}년 {viewDate.getMonth() + 1}월</span>
              <button onClick={() => setViewDate(new Date(viewDate.getFullYear(), viewDate.getMonth() + 1))} className="p-2 hover:bg-white hover:shadow-sm rounded-xl transition-all"><ChevronRightIcon size={20} /></button>
            </div>
          </div>
          
          <div className="grid grid-cols-7 gap-y-1 gap-x-1">
            {['SUN', 'MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT'].map(d => (<div key={d} className="text-center text-[10px] font-black text-slate-300 uppercase tracking-[0.2em] pb-6">{d}</div>))}
            {calendarDays.map((date, i) => {
              if (!date) return <div key={`empty-${i}`} className="h-28" />;
              const isToday = date.toDateString() === new Date().toDateString();
              const isSelected = selectedDate === date.toDateString();
              const dayGatherings = gatherings.filter(g => isDateInGathering(date, g));
              
              return (
                <div key={date.toDateString()} className="relative h-28 border border-slate-50/50 flex flex-col pt-2 overflow-hidden">
                  <div className="px-2 mb-1 flex justify-between items-start">
                    <button 
                      onClick={() => setSelectedDate(date.toDateString())} 
                      className={`w-7 h-7 rounded-lg transition-all flex items-center justify-center font-black text-xs ${isSelected ? 'bg-indigo-600 text-white shadow-lg scale-110' : isToday ? 'bg-indigo-50 border border-indigo-200 text-indigo-600' : 'text-slate-400 hover:bg-slate-50'}`}
                    >
                      {date.getDate()}
                    </button>
                  </div>
                  <div className="flex-1 space-y-1 overflow-hidden pb-1 px-1">
                    {dayGatherings.map((g, idx) => {
                      const startDate = new Date(g.startDate); startDate.setHours(0,0,0,0);
                      const endDate = new Date(g.endDate); endDate.setHours(0,0,0,0);
                      const isStart = date.getTime() === startDate.getTime();
                      const isEnd = date.getTime() === endDate.getTime();
                      const isFirstDayOfWeek = date.getDay() === 0;
                      const isLastDayOfWeek = date.getDay() === 6;
                      
                      // Use assigned gathering color or default to indigo
                      const barColor = g.color || 'bg-indigo-500';
                      
                      return (
                        <div 
                          key={g.id} 
                          onClick={() => { setActiveGatheringId(g.id); setActiveTab('participants'); }}
                          className={`
                            h-5 text-[9px] font-black text-white px-2 flex items-center truncate cursor-pointer transition-all hover:brightness-110
                            ${isStart || isFirstDayOfWeek ? 'rounded-l-md ml-0.5' : ''}
                            ${isEnd || isLastDayOfWeek ? 'rounded-r-md mr-0.5' : ''}
                            ${barColor} shadow-sm opacity-90
                          `}
                        >
                          {(isStart || isFirstDayOfWeek) && <span className="truncate">{g.name}</span>}
                        </div>
                      );
                    })}
                  </div>
                </div>
              );
            })}
          </div>
        </section>

        {/* Selected Date Details */}
        {selectedDate && (
           <div className="animate-in fade-in slide-in-from-bottom-4 duration-500">
              <h3 className="text-xl font-black text-slate-800 mb-8 flex items-center justify-between">
                <span className="flex items-center gap-2"><Clock className="text-slate-400" /> {new Date(selectedDate).toLocaleDateString('ko-KR', { year: 'numeric', month: 'long', day: 'numeric' })} 일정</span>
                {gatherings.length > 0 && <span className="text-xs font-bold text-slate-400">총 {gatherings.length}개의 모임 관리 중</span>}
              </h3>
             {selectedDayGatherings.length > 0 ? (
               <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                 {selectedDayGatherings.map(g => (
                   <div key={g.id} onClick={() => { setActiveGatheringId(g.id); setActiveTab('participants'); }} className="group bg-white p-7 rounded-[32px] border border-slate-200 cursor-pointer hover:border-indigo-400 hover:shadow-2xl transition-all relative overflow-hidden">
                     <button onClick={(e) => deleteGathering(g.id, e)} className="absolute top-4 right-4 p-2 text-slate-200 hover:text-red-500 opacity-0 group-hover:opacity-100 transition-all z-10"><Trash2 size={18} /></button>
                     <h4 className="font-black text-slate-800 text-lg mb-1 truncate pr-8">{g.name}</h4>
                     <p className="text-[10px] text-slate-400 font-bold mb-6 flex items-center gap-1 uppercase tracking-widest"><CalendarDays size={12}/> {new Date(g.startDate).toLocaleDateString()} 시작</p>
                     <div className="flex items-center justify-between pt-4 border-t border-slate-50">
                       <div className="flex -space-x-2">
                         {g.participants.slice(0, 3).map(p => <div key={p.id} className="w-8 h-8 rounded-full bg-indigo-50 border-2 border-white flex items-center justify-center text-[10px] font-black text-indigo-500">{p.name.charAt(0)}</div>)}
                         {g.participants.length > 3 && <div className="w-8 h-8 rounded-full bg-slate-50 border-2 border-white flex items-center justify-center text-[10px] font-black text-slate-400">+{g.participants.length - 3}</div>}
                       </div>
                       <p className={`text-sm font-black ${g.color?.replace('bg-', 'text-') || 'text-indigo-600'}`}>{g.rounds.reduce((s, r) => s + r.amount, 0).toLocaleString()}원</p>
                     </div>
                   </div>
                 ))}
               </div>
             ) : (
              <div className="py-24 text-center bg-white rounded-[40px] border-2 border-dashed border-slate-100 shadow-inner flex flex-col items-center justify-center gap-4 group">
                <div className="w-16 h-16 bg-slate-50 rounded-full flex items-center justify-center text-slate-200 group-hover:text-indigo-200 transition-colors"><CalendarIcon size={32} /></div>
                <p className="text-slate-300 font-black">이 날짜에는 등록된 일정이 없습니다.</p>
                <button onClick={() => setIsCreatingGathering(true)} className="text-indigo-600 text-sm font-bold underline-offset-4 hover:underline">첫 모임 추가하기</button>
              </div>
             )}
           </div>
        )}

        {/* Modal: Create Gathering with Adjusted Button Widths */}
        {isCreatingGathering && (
          <div className="fixed inset-0 bg-slate-900/60 backdrop-blur-md z-[100] flex items-center justify-center p-4">
            <div className="bg-white rounded-[48px] p-10 w-full max-w-md shadow-2xl animate-in zoom-in-95 duration-200">
              <h2 className="text-3xl font-black mb-8 text-slate-900 tracking-tight">새로운 정산 시작</h2>
              <div className="space-y-6 mb-12">
                <div className="space-y-2">
                  <label className="text-[11px] font-black text-slate-400 uppercase tracking-[0.2em] px-1">모임 타입</label>
                  <div className="grid grid-cols-4 gap-2">
                    {GATHERING_TYPES.map((gt) => (
                      <button
                        key={gt.type}
                        type="button"
                        onClick={() => setNewGatheringType(gt.type)}
                        className={`flex flex-col items-center gap-1 p-3 rounded-2xl border-2 transition-all ${
                          newGatheringType === gt.type
                            ? 'border-indigo-500 bg-indigo-50'
                            : 'border-slate-100 bg-slate-50 hover:border-slate-200'
                        }`}
                      >
                        <span className="text-2xl">{gt.icon}</span>
                        <span className={`text-xs font-bold ${newGatheringType === gt.type ? 'text-indigo-600' : 'text-slate-500'}`}>{gt.label}</span>
                      </button>
                    ))}
                  </div>
                </div>
                <div className="space-y-2">
                  <label className="text-[11px] font-black text-slate-400 uppercase tracking-[0.2em] px-1">모임 이름</label>
                  <input
                    type="text"
                    value={newGatheringName}
                    onChange={(e) => { setNewGatheringName(e.target.value); setNameError(''); }}
                    placeholder="예: 제주도 여행, 송년회"
                    className={`w-full bg-slate-50 border rounded-2xl px-6 py-4 focus:outline-none focus:ring-4 text-lg font-bold ${
                      nameError
                        ? 'border-red-400 focus:ring-red-500/10 focus:border-red-500'
                        : 'border-slate-200 focus:ring-indigo-500/10 focus:border-indigo-500'
                    }`}
                  />
                  {nameError && (
                    <p className="text-red-500 text-xs font-bold px-1 flex items-center gap-1">
                      <AlertCircle size={12} /> {nameError}
                    </p>
                  )}
                </div>
                <div className="space-y-2">
                  <div className="grid grid-cols-2 gap-4">
                    <div className="space-y-2">
                      <label className="text-[11px] font-black text-slate-400 uppercase tracking-[0.2em] px-1">시작일</label>
                      <input
                        type="date"
                        value={startDateStr}
                        max="9999-12-31"
                        onChange={(e) => { setStartDateStr(e.target.value); setDateError(''); }}
                        className={`w-full bg-slate-50 border rounded-2xl px-5 py-4 font-bold text-sm ${
                          dateError ? 'border-red-400' : 'border-slate-200'
                        }`}
                      />
                    </div>
                    <div className="space-y-2">
                      <label className="text-[11px] font-black text-slate-400 uppercase tracking-[0.2em] px-1">종료일</label>
                      <input
                        type="date"
                        value={endDateStr}
                        max="9999-12-31"
                        onChange={(e) => { setEndDateStr(e.target.value); setDateError(''); }}
                        className={`w-full bg-slate-50 border rounded-2xl px-5 py-4 font-bold text-sm ${
                          dateError ? 'border-red-400' : 'border-slate-200'
                        }`}
                      />
                    </div>
                  </div>
                  {dateError && (
                    <p className="text-red-500 text-xs font-bold px-1 flex items-center gap-1">
                      <AlertCircle size={12} /> {dateError}
                    </p>
                  )}
                </div>
              </div>
              <div className="flex gap-4">
                {/* Wider Create Button (2/3) and Smaller Cancel Button (1/3) */}
                <button onClick={() => setIsCreatingGathering(false)} className="w-1/3 bg-slate-100 text-slate-500 py-5 rounded-[24px] font-black hover:bg-slate-200 transition-all active:scale-95 shadow-sm">취소</button>
                <button onClick={createGathering} className="w-2/3 bg-indigo-600 text-white py-5 rounded-[24px] font-black hover:bg-indigo-700 shadow-xl shadow-indigo-100 transition-all active:scale-95">모임 생성</button>
              </div>
            </div>
          </div>
        )}
      </div>
    );
  }

  // Active Gathering Detail View (Remains same but handles the active gathering state)
  return (
    <div className="min-h-screen pb-24 lg:pb-0 lg:pl-72 bg-slate-50">
      <nav className="hidden lg:flex flex-col fixed left-0 top-0 bottom-0 w-72 bg-white border-r border-slate-200 p-8 z-10">
        <button onClick={() => setActiveGatheringId(null)} className="flex items-center gap-2 text-slate-400 hover:text-indigo-600 font-black mb-10 transition-colors text-sm"><ChevronLeft size={20} /> 전체 목록</button>
        <div className="mb-12">
          <h1 className="text-2xl font-black text-slate-900 leading-tight mb-2 truncate pr-2">{activeGathering.name}</h1>
          <div className="flex items-center gap-2 text-[10px] font-black text-slate-400 uppercase tracking-widest"><CalendarIcon size={12} /> {new Date(activeGathering.startDate).toLocaleDateString()} ~</div>
        </div>
        <div className="space-y-3 flex-1">
          <NavItem active={activeTab === 'participants'} onClick={() => setActiveTab('participants')} icon={<Users size={22} />} label="참여자" count={activeGathering.participants.length} />
          <NavItem active={activeTab === 'rounds'} onClick={() => setActiveTab('rounds')} icon={<Receipt size={22} />} label="지출 내역" count={activeGathering.rounds.length} />
          <NavItem active={activeTab === 'result'} onClick={() => setActiveTab('result')} icon={<ArrowRightLeft size={22} />} label="정산 결과" />
        </div>
        <div className="mt-auto pt-8 border-t border-slate-100 space-y-4">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-slate-100 overflow-hidden">
              {user?.profileImage ? (
                <img src={user.profileImage} alt="me" className="w-full h-full object-cover" />
              ) : (
                <div className="w-full h-full bg-indigo-100 flex items-center justify-center text-indigo-600 font-black">
                  {user?.nickname?.charAt(0) || '?'}
                </div>
              )}
            </div>
            <div className="overflow-hidden"><p className="text-sm font-black text-slate-900 truncate">{user?.nickname}</p></div>
          </div>
          <div className={`${activeGathering.color || 'bg-indigo-600'} rounded-[28px] p-6 text-white shadow-2xl shadow-indigo-100`}>
            <p className="text-[10px] font-black text-white/70 uppercase tracking-[0.2em] mb-1">총 지출 금액</p>
            <p className="text-3xl font-black">{calculationResult.totalSpent.toLocaleString()}<span className="text-base ml-1 opacity-80">원</span></p>
          </div>
          <button onClick={(e) => deleteGathering(activeGathering.id, e)} className="w-full py-3 rounded-2xl text-[11px] font-black text-red-300 hover:text-red-500 hover:bg-red-50 transition-all flex items-center justify-center gap-2 uppercase tracking-widest"><Trash2 size={14}/> 모임 삭제</button>
        </div>
      </nav>

      <main className="max-w-4xl mx-auto p-6 md:p-12">
        {activeTab === 'participants' && (
          <div className="animate-in fade-in slide-in-from-bottom-8 duration-700">
            <header className="mb-10 flex flex-wrap items-center justify-between gap-4">
              <div><h2 className="text-3xl font-black text-slate-900 tracking-tight">참여자 관리</h2><p className="text-slate-500 font-medium">정산 멤버들의 정보를 관리하세요.</p></div>
              <button onClick={() => setIsImportModalOpen(true)} className="flex items-center gap-2 bg-white text-indigo-600 px-6 py-4 rounded-[20px] font-black hover:bg-indigo-50 transition-all text-sm shadow-sm border border-slate-200 active:scale-95"><Zap size={20} className="fill-indigo-600" /> 빠른 가져오기</button>
            </header>
            <section className="bg-white rounded-[48px] shadow-sm border border-slate-200 p-10 mb-8">
              <div className="flex gap-4 mb-12">
                <input type="text" value={newParticipantName} onChange={(e) => setNewParticipantName(e.target.value)} onKeyDown={(e) => e.key === 'Enter' && addParticipant()} placeholder="멤버 이름을 입력하세요" className="flex-1 bg-slate-50 border border-slate-200 rounded-2xl px-6 py-4 focus:outline-none focus:ring-4 focus:ring-indigo-500/10 font-black" />
                <button onClick={() => addParticipant()} className={`${activeGathering.color || 'bg-indigo-600'} text-white rounded-2xl px-12 py-4 font-black hover:brightness-110 shadow-xl shadow-indigo-100 transition-all active:scale-95`}>추가</button>
              </div>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                {activeGathering.participants.map((p) => (
                  <div key={p.id} className="bg-slate-50 p-7 rounded-[32px] border border-transparent hover:border-indigo-100 hover:bg-white transition-all group flex flex-col justify-between shadow-sm">
                    <div className="flex items-center justify-between mb-6">
                      <div className="flex items-center gap-4"><div className={`w-14 h-14 rounded-[20px] ${activeGathering.color?.replace('bg-', 'bg-opacity-10 text-') || 'bg-indigo-50 text-indigo-600'} flex items-center justify-center font-black text-xl shadow-inner`}>{p.name.charAt(0)}</div><span className="font-black text-slate-800 text-lg">{p.name}</span></div>
                      <div className="flex gap-1"><button onClick={() => { setEditingParticipantId(p.id); setEditParticipantName(p.name); setEditBankName(p.bankName || ''); setEditAccountNum(p.accountNumber || ''); }} className="text-slate-300 hover:text-indigo-600 p-2 transition-colors"><Edit2 size={22} /></button><button onClick={() => removeParticipant(p.id)} className="text-slate-200 hover:text-red-500 p-2 transition-colors"><Trash2 size={22} /></button></div>
                    </div>
                    <div className="flex items-center gap-3 px-5 py-4 bg-white rounded-2xl border border-slate-100 group-hover:border-indigo-50 shadow-inner">
                      <CreditCard size={18} className="text-indigo-300" />
                      <span className="text-xs font-black text-slate-400 truncate">{p.bankName ? `${p.bankName} ${p.accountNumber}` : '계좌 정보를 등록해 주세요.'}</span>
                    </div>
                  </div>
                ))}
              </div>
            </section>
            <button onClick={() => setActiveTab('rounds')} className="w-full mt-12 bg-slate-900 text-white rounded-[32px] py-7 font-black text-xl flex items-center justify-center gap-3 hover:bg-slate-800 transition-all shadow-2xl active:scale-[0.98]">지출 내역 작성하러 가기 <ChevronRight size={32} /></button>
          </div>
        )}

        {activeTab === 'rounds' && (
          <div className="animate-in fade-in slide-in-from-bottom-8 duration-700 space-y-10">
             <header className="flex items-center justify-between mb-2">
               <div><h2 className="text-3xl font-black text-slate-900 tracking-tight">지출 내역</h2><p className="text-slate-500 font-medium">언제, 누가, 얼마를 썼는지 기록하세요.</p></div>
               <button onClick={handleAddRound} className={`${activeGathering.color || 'bg-indigo-600'} text-white rounded-[20px] px-8 py-4 font-black flex items-center gap-2 shadow-xl active:scale-95`}><Plus size={24} /> 지출 추가</button>
             </header>
             <div className="space-y-8">
                {activeGathering.rounds.map(round => (
                  <div key={round.id} className="bg-white rounded-[48px] shadow-sm border border-slate-200 p-10 hover:shadow-2xl transition-all">
                    {/* 첫 번째 행: 항목 이름 + 결제 멤버 + 금액 + 삭제 버튼 */}
                    <div className="flex flex-wrap items-center gap-4 mb-6">
                      <input
                        type="text"
                        value={round.title}
                        onChange={(e) => handleUpdateRound(round.id, { title: e.target.value })}
                        onBlur={() => saveRoundToServer(round.id)}
                        className="flex-1 min-w-[150px] font-black text-2xl bg-transparent border-none p-0 focus:ring-0 placeholder:text-slate-200"
                        placeholder="항목 이름 (예: 1차 고기집)"
                      />
                      <div className="flex items-center gap-3 bg-slate-50 rounded-[20px] px-5 py-3 border border-slate-100 shadow-sm">
                        <span className="text-[10px] font-black text-slate-400 uppercase tracking-widest">결제</span>
                        {activeGathering.participants.length === 0 ? (
                          <span className="text-red-500 text-xs font-bold">참여자 필요</span>
                        ) : (
                          <select
                            value={round.payerId}
                            onChange={(e) => {
                              const newPayerId = e.target.value;
                              handleUpdateRound(round.id, { payerId: newPayerId });
                              saveRoundToServer(round.id, { payerId: newPayerId });
                            }}
                            className="bg-transparent border-none p-0 font-black text-slate-800 focus:ring-0 cursor-pointer text-sm"
                          >
                            {activeGathering.participants.map(p => (
                              <option key={p.id} value={p.id}>{p.name}</option>
                            ))}
                          </select>
                        )}
                      </div>
                      <div className="flex items-center gap-3 bg-slate-50 px-5 py-3 rounded-[20px] border border-slate-100 shadow-inner">
                        <input
                          type="number"
                          value={round.amount === 0 ? '' : round.amount}
                          onChange={(e) => handleUpdateRound(round.id, { amount: Number(e.target.value) })}
                          onBlur={() => saveRoundToServer(round.id)}
                          className={`w-28 font-black text-right ${activeGathering.color?.replace('bg-', 'text-') || 'text-indigo-600'} bg-transparent border-none p-0 focus:ring-0 text-xl`}
                          placeholder="0"
                        />
                        <span className="font-black text-slate-400 text-sm">원</span>
                      </div>
                      <button onClick={() => handleDeleteRound(round.id)} className="p-2 text-slate-200 hover:text-red-500 transition-colors"><Trash2 size={24} /></button>
                    </div>
                    {/* 두 번째 행: 제외 인원 선택 */}
                    {activeGathering.participants.length > 0 && (
                      <div className="flex flex-wrap items-center gap-3 pt-6 border-t border-slate-100">
                        <div className="flex items-center gap-2">
                          <UserMinus size={16} className="text-orange-500" />
                          <span className="text-[10px] font-black text-orange-400 uppercase tracking-widest">제외</span>
                        </div>
                        <div className="flex flex-wrap gap-2">
                          {activeGathering.participants
                            .map(p => {
                              const excludedList = round.excluded || [];
                              const isExcluded = excludedList.some(e => e.participantId === p.id);
                              return (
                                <button
                                  key={p.id}
                                  type="button"
                                  onClick={() => {
                                    const currentExcluded = round.excluded || [];
                                    const newExcluded = isExcluded
                                      ? currentExcluded.filter(e => e.participantId !== p.id)
                                      : [...currentExcluded, { participantId: p.id, reason: '미참여' }];
                                    handleUpdateRound(round.id, { excluded: newExcluded });
                                    saveRoundToServer(round.id, { excluded: newExcluded });
                                  }}
                                  className={`px-3 py-1.5 rounded-full text-xs font-black transition-all ${
                                    isExcluded
                                      ? 'bg-orange-500 text-white shadow-md'
                                      : 'bg-white text-slate-400 border border-slate-200 hover:border-orange-300 hover:text-orange-500'
                                  }`}
                                >
                                  {p.name}
                                </button>
                              );
                            })}
                        </div>
                      </div>
                    )}
                    {/* 제외된 인원 표시 */}
                    {round.excluded && round.excluded.length > 0 && (
                      <div className="mt-4 flex items-center gap-2 text-xs text-orange-500 font-medium">
                        <span>제외된 인원:</span>
                        {round.excluded.map(e => {
                          const participant = activeGathering.participants.find(p => p.id === e.participantId);
                          return participant ? (
                            <span key={e.participantId} className="bg-orange-100 text-orange-600 px-2 py-1 rounded-lg font-bold">
                              {participant.name}
                            </span>
                          ) : null;
                        })}
                      </div>
                    )}
                  </div>
                ))}
             </div>
             <button onClick={() => setActiveTab('result')} className={`w-full ${activeGathering.color || 'bg-indigo-600'} text-white rounded-[32px] py-7 font-black text-xl hover:brightness-110 shadow-2xl transition-all active:scale-[0.98]`}>정산 결과 산출하기</button>
          </div>
        )}

        {activeTab === 'result' && (
           <div className="animate-in fade-in slide-in-from-bottom-8 duration-700 space-y-16">
             <header className="text-center">
               <div className={`${activeGathering.color?.replace('bg-', 'bg-opacity-20 text-') || 'bg-indigo-100 text-indigo-700'} text-[11px] font-black px-5 py-2 rounded-full inline-block uppercase tracking-[0.3em] mb-6`}>Complete</div>
               <h2 className="text-6xl font-black text-slate-900 mb-6 tracking-tighter">정산 준비 완료!</h2>
               <p className="text-slate-500 text-lg font-medium">총 {calculationResult.totalSpent.toLocaleString()}원, 정확하게 나누었습니다.</p>
             </header>

             <div className="grid grid-cols-1 md:grid-cols-2 gap-10">
                <section className="bg-white rounded-[56px] shadow-sm border border-slate-200 p-12">
                  <h3 className="text-2xl font-black mb-10 flex items-center gap-4"><Users className={`${activeGathering.color?.replace('bg-', 'text-') || 'text-indigo-600'}`} size={36} /> 멤버별 요약</h3>
                  <div className="space-y-6">
                    {calculationResult.balances.map(b => (
                      <div key={b.participantId} className="flex items-start justify-between gap-4 p-7 bg-slate-50 rounded-[32px] border border-transparent hover:border-indigo-100 hover:bg-white transition-all shadow-sm">
                        <div className="min-w-0"><p className="font-black text-slate-800 text-xl truncate">{b.name}</p><p className="text-[11px] text-slate-400 font-bold uppercase tracking-widest mt-5 whitespace-nowrap">지불 {b.totalPaid.toLocaleString()}원</p></div>
                        <p className={`text-2xl font-black whitespace-nowrap flex-shrink-0 ${b.netBalance > 0 ? 'text-green-600' : b.netBalance < 0 ? 'text-red-500' : 'text-slate-400'}`}>{b.netBalance > 0 ? '+' : ''}{Math.round(b.netBalance).toLocaleString()}원</p>
                      </div>
                    ))}
                  </div>
                </section>
                <section className="bg-white rounded-[56px] shadow-sm border border-slate-200 p-12 flex flex-col">
                   <h3 className="text-2xl font-black mb-6 flex items-center gap-4"><ArrowRightLeft className={`${activeGathering.color?.replace('bg-', 'text-') || 'text-indigo-600'}`} size={36} /> 송금 가이드</h3>
                   {/* 버림 단위 선택 */}
                   <div className="flex items-center gap-3 mb-8 p-4 bg-slate-50 rounded-2xl">
                     <span className="text-xs font-black text-slate-400 uppercase tracking-widest">버림 단위</span>
                     <select
                       value={roundingUnit}
                       onChange={(e) => setRoundingUnit(Number(e.target.value))}
                       className="bg-white border border-slate-200 rounded-xl px-4 py-2 font-bold text-sm text-slate-700 focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 cursor-pointer"
                     >
                       <option value={1}>없음</option>
                       <option value={10}>10원</option>
                       <option value={100}>100원</option>
                       <option value={1000}>1,000원</option>
                       <option value={10000}>10,000원</option>
                     </select>
                   </div>
                   <div className="space-y-6 flex-1">
                     {calculationResult.debts.map((d, i) => {
                       const receiver = activeGathering.participants.find(p => p.id === d.toParticipantId);
                       return (
                         <div key={i} className={`p-6 ${activeGathering.color?.replace('bg-', 'bg-opacity-5 border-opacity-20 border-') || 'bg-indigo-50/40 border-indigo-50'} border-2 rounded-[32px] group hover:border-opacity-50 transition-all shadow-sm`}>
                           <div className="flex items-center justify-center gap-6 mb-4">
                             <div className="flex flex-col items-center">
                               <span className="text-[10px] font-bold text-slate-400 uppercase tracking-widest mb-1">보낼 사람</span>
                               <span className="font-black text-slate-700 text-lg">{d.from}</span>
                             </div>
                             <ChevronRightIcon className={`${activeGathering.color?.replace('bg-', 'text-opacity-60 text-') || 'text-indigo-400'} flex-shrink-0 mt-4`} size={20} />
                             <div className="flex flex-col items-center">
                               <span className="text-[10px] font-bold text-slate-400 uppercase tracking-widest mb-1">받는 사람</span>
                               <span className="font-black text-slate-700 text-lg">{d.to}</span>
                             </div>
                           </div>
                           <div className={`text-2xl font-black text-center ${activeGathering.color?.replace('bg-', 'text-') || 'text-indigo-600'}`}>{Math.round(d.amount).toLocaleString()}원</div>
                           {receiver?.bankName && (
                             <button onClick={() => copyToClipboard(`${receiver.bankName} ${receiver.accountNumber}`)} className="w-full mt-4 flex items-center justify-between px-5 py-3 bg-white rounded-2xl text-xs font-black text-slate-600 hover:border-indigo-400 border border-slate-100 transition-all shadow-sm active:scale-95">
                               <div className="flex items-center gap-3"><CreditCard size={18} className="text-indigo-400" /> <span>{receiver.bankName} {receiver.accountNumber}</span></div>
                               <Copy size={16} className="text-slate-200 group-hover:text-indigo-400" />
                             </button>
                           )}
                         </div>
                       );
                     })}
                   </div>
                </section>
             </div>

             <div className="space-y-8 pb-10">
               <button onClick={() => {
                  const message = generateKakaoMessage(activeGathering.name, activeGathering.participants, activeGathering.rounds, calculationResult.debts);
                  setKakaoMessage(message);
               }} className={`w-full ${activeGathering.color || 'bg-indigo-600'} text-white rounded-[40px] py-10 font-black text-3xl flex items-center justify-center gap-5 hover:brightness-110 shadow-2xl transition-all active:scale-[0.98]`}>
                 <Copy size={40} />
                 카카오톡 공유 메시지 생성
               </button>

               {kakaoMessage && (
                 <div className={`${activeGathering.color || 'bg-indigo-600'} p-14 rounded-[72px] text-white shadow-2xl shadow-indigo-100 animate-in slide-in-from-bottom-10 duration-700`}>
                   <p className="whitespace-pre-wrap leading-relaxed mb-12 text-lg font-medium text-white/90 bg-white/10 p-6 rounded-3xl">{kakaoMessage}</p>
                   <div className="flex flex-wrap gap-6">
                    <button onClick={() => copyToClipboard(kakaoMessage)} className="bg-white text-indigo-600 px-12 py-6 rounded-[28px] font-black flex items-center gap-4 hover:bg-indigo-50 shadow-2xl transition-all active:scale-95">
                      {showCopyFeedback ? <Check size={24}/> : <Copy size={24}/>} {showCopyFeedback ? '복사 완료!' : '메시지 복사하기'}
                    </button>
                    <button onClick={() => setKakaoMessage('')} className="bg-white/20 text-white px-10 py-6 rounded-[28px] font-black hover:bg-white/30 transition-all border border-white/20">닫기</button>
                   </div>
                 </div>
               )}
             </div>
           </div>
        )}
      </main>

      {/* Mobile Nav */}
      <div className="lg:hidden fixed bottom-0 left-0 right-0 bg-white border-t border-slate-100 flex items-center justify-around py-6 z-50 rounded-t-[48px] shadow-[0_-10px_40px_-15px_rgba(0,0,0,0.1)] px-8">
        <MobileNavItem active={activeTab === 'participants'} onClick={() => setActiveTab('participants')} icon={<Users size={32} />} label="참여자" />
        <MobileNavItem active={activeTab === 'rounds'} onClick={() => setActiveTab('rounds')} icon={<Receipt size={32} />} label="지출" />
        <MobileNavItem active={activeTab === 'result'} onClick={() => setActiveTab('result')} icon={<Calculator size={32} />} label="결과" />
      </div>

      {/* Modal: Edit Participant */}
      {editingParticipantId && (
        <div className="fixed inset-0 bg-slate-900/60 backdrop-blur-md z-[100] flex items-center justify-center p-4">
          <div className="bg-white rounded-[48px] p-10 w-full max-w-md shadow-2xl animate-in zoom-in-95 duration-200">
            <h2 className="text-3xl font-black mb-10 flex items-center gap-4 text-slate-900"><Edit2 className="text-indigo-600" size={36} /> 참여자 정보 수정</h2>
            <div className="space-y-6 mb-12">
              <div className="space-y-2">
                <label className="text-[11px] font-black text-slate-400 uppercase tracking-widest px-1">이름</label>
                <input type="text" value={editParticipantName} onChange={(e) => setEditParticipantName(e.target.value)} placeholder="참여자 이름" className="w-full bg-slate-50 border border-slate-200 rounded-2xl px-6 py-4 font-bold text-lg" />
              </div>
              <div className="space-y-2">
                <label className="text-[11px] font-black text-slate-400 uppercase tracking-widest px-1">은행 또는 앱</label>
                <input type="text" value={editBankName} onChange={(e) => setEditBankName(e.target.value)} placeholder="예: 카카오페이, 국민은행" className="w-full bg-slate-50 border border-slate-200 rounded-2xl px-6 py-4 font-bold text-lg" />
              </div>
              <div className="space-y-2">
                <label className="text-[11px] font-black text-slate-400 uppercase tracking-widest px-1">계좌번호</label>
                <input type="text" value={editAccountNum} onChange={(e) => setEditAccountNum(e.target.value)} placeholder="000-0000-0000" className="w-full bg-slate-50 border border-slate-200 rounded-2xl px-6 py-4 font-bold text-lg" />
              </div>
            </div>
            <div className="flex gap-4">
              <button onClick={() => setEditingParticipantId(null)} className="w-1/3 bg-slate-100 text-slate-500 py-5 rounded-[24px] font-black active:scale-95">취소</button>
              <button onClick={saveParticipantInfo} className="w-2/3 bg-indigo-600 text-white py-5 rounded-[24px] font-black hover:bg-indigo-700 shadow-xl shadow-indigo-100 active:scale-95">저장하기</button>
            </div>
          </div>
        </div>
      )}

      {/* Modal: Quick Import */}
      {isImportModalOpen && (
        <div className="fixed inset-0 bg-slate-900/60 backdrop-blur-md z-[100] flex items-center justify-center p-4">
          <div className="bg-white rounded-[56px] p-12 w-full max-w-xl shadow-2xl animate-in zoom-in-95 duration-200">
            <div className="flex items-center gap-4 mb-6 text-indigo-600"><Zap size={40} className="fill-indigo-600" /><h2 className="text-4xl font-black tracking-tight">빠른 추가</h2></div>
            <p className="text-slate-500 font-medium mb-10 leading-relaxed text-lg pr-4">참여자 목록을 붙여넣으세요.<br/>줄바꿈 또는 쉼표로 구분해주세요.</p>
            <textarea value={importText} onChange={(e) => setImportText(e.target.value)} placeholder="예:&#10;김철수 국민 123-45&#10;이영희 신한 678-90&#10;박민수" className="w-full h-56 bg-slate-50 border border-slate-200 rounded-[32px] p-8 focus:outline-none focus:ring-4 focus:ring-indigo-500/10 mb-10 text-lg font-medium leading-relaxed shadow-inner" />
            <div className="flex gap-5">
              <button onClick={() => setIsImportModalOpen(false)} className="w-1/3 bg-slate-100 text-slate-500 py-6 rounded-[28px] font-black hover:bg-slate-200 transition-all active:scale-95">취소</button>
              <button onClick={handleQuickImport} disabled={isImporting || !importText.trim()} className="w-2/3 bg-indigo-600 text-white py-6 rounded-[28px] font-black flex items-center justify-center gap-3 hover:bg-indigo-700 shadow-2xl shadow-indigo-100 transition-all active:scale-95 disabled:opacity-50">
                {isImporting ? <Loader2 className="animate-spin" size={24}/> : <Zap size={24}/>} {isImporting ? '추가 중...' : '추가하기'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

const NavItem: React.FC<{ active: boolean; onClick: () => void; icon: React.ReactNode; label: string; count?: number }> = ({ active, onClick, icon, label, count }) => (
  <button onClick={onClick} className={`w-full flex items-center gap-5 px-7 py-6 rounded-[28px] font-black transition-all ${active ? 'bg-indigo-600 text-white shadow-2xl shadow-indigo-200 scale-105' : 'text-slate-400 hover:bg-slate-50 hover:text-slate-600'}`}>
    {icon} <span className="text-lg">{label}</span>
    {count !== undefined && <span className={`ml-auto text-[10px] font-black px-3 py-1.5 rounded-full ${active ? 'bg-white/20 text-white' : 'bg-slate-100 text-slate-500'}`}>{count}</span>}
  </button>
);

const MobileNavItem: React.FC<{ active: boolean; onClick: () => void; icon: React.ReactNode; label: string }> = ({ active, onClick, icon, label }) => (
  <button onClick={onClick} className={`flex flex-col items-center gap-2 flex-1 transition-all ${active ? 'text-indigo-600 scale-110' : 'text-slate-300'}`}>
    {icon} <span className="text-[12px] font-black uppercase tracking-[0.2em]">{label}</span>
  </button>
);

export default App;
