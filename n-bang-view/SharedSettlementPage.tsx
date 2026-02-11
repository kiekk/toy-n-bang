import React, { useState, useEffect } from 'react';
import { shareApi } from './services/api/shareApi';
import type { SharedSettlementResponse } from './services/api/types';
import { Users, ArrowRightLeft, AlertCircle, Loader2, ChevronRight, Receipt, UserMinus } from 'lucide-react';
import { GATHERING_TYPES } from './types';

interface SharedSettlementPageProps {
  uuid: string;
}

const SharedSettlementPage: React.FC<SharedSettlementPageProps> = ({ uuid }) => {
  const [data, setData] = useState<SharedSettlementResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const result = await shareApi.getSharedSettlement(uuid);
        setData(result);
      } catch (err: any) {
        if (err?.statusCode === 410) {
          setError('이 공유 링크는 만료되었습니다.');
        } else if (err?.statusCode === 404) {
          setError('존재하지 않는 공유 링크입니다.');
        } else {
          setError('정산 결과를 불러오는데 실패했습니다.');
        }
      } finally {
        setIsLoading(false);
      }
    };
    fetchData();
  }, [uuid]);

  if (isLoading) {
    return (
      <div className="min-h-screen bg-slate-50 flex items-center justify-center">
        <div className="text-center">
          <Loader2 className="w-12 h-12 text-indigo-600 animate-spin mx-auto mb-4" />
          <p className="text-slate-500 font-bold">정산 결과를 불러오는 중...</p>
        </div>
      </div>
    );
  }

  if (error || !data) {
    return (
      <div className="min-h-screen bg-slate-50 flex items-center justify-center p-4">
        <div className="text-center bg-white rounded-3xl p-10 shadow-sm border border-slate-200 max-w-md w-full">
          <AlertCircle className="w-16 h-16 text-red-400 mx-auto mb-4" />
          <h1 className="text-2xl font-black text-slate-900 mb-2">{error || '오류가 발생했습니다.'}</h1>
          <p className="text-slate-500 mb-6">공유 링크가 유효하지 않거나 만료되었습니다.</p>
          <a href="/" className="inline-block bg-indigo-600 text-white px-8 py-3 rounded-2xl font-black hover:bg-indigo-700 transition-all">
            N빵 홈으로 이동
          </a>
        </div>
      </div>
    );
  }

  const typeInfo = GATHERING_TYPES.find(gt => gt.type === data.gatheringType);

  return (
    <div className="min-h-screen bg-slate-50">
      <div className="max-w-4xl mx-auto p-4 sm:p-6 md:p-12">
        {/* Header */}
        <header className="text-center mb-8 sm:mb-16">
          <div className="bg-indigo-100 text-indigo-700 text-[11px] font-black px-5 py-2 rounded-full inline-block uppercase tracking-[0.3em] mb-4 sm:mb-6">
            공유된 정산 결과
          </div>
          <h1 className="text-3xl sm:text-6xl font-black text-slate-900 mb-3 sm:mb-4 tracking-tighter">{data.gatheringName}</h1>
          {typeInfo && (
            <span className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-slate-100 text-sm font-bold text-slate-600 mb-4">
              {typeInfo.icon} {typeInfo.label}
            </span>
          )}
          <p className="text-slate-500 text-sm sm:text-lg font-medium">
            총 {data.totalAmount.toLocaleString()}원, 정확하게 나누었습니다.
          </p>
        </header>

        {/* Content Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6 sm:gap-10">
          {/* Member Summary */}
          <section className="bg-white rounded-3xl sm:rounded-[56px] shadow-sm border border-slate-200 p-6 sm:p-12">
            <h3 className="text-xl sm:text-2xl font-black mb-6 sm:mb-10 flex items-center gap-3 sm:gap-4">
              <Users className="text-indigo-600" size={28} /> 멤버별 요약
            </h3>
            <div className="space-y-4 sm:space-y-6">
              {data.balances.map(b => (
                <div key={b.participantId} className="flex items-start justify-between gap-3 sm:gap-4 p-4 sm:p-7 bg-slate-50 rounded-2xl sm:rounded-[32px] border border-transparent hover:border-indigo-100 hover:bg-white transition-all shadow-sm">
                  <div className="min-w-0">
                    <p className="font-black text-slate-800 text-base sm:text-xl truncate">{b.name}</p>
                    <p className="text-[11px] text-slate-400 font-bold uppercase tracking-widest mt-2 sm:mt-5 whitespace-nowrap">지불 {b.totalPaid.toLocaleString()}원</p>
                  </div>
                  <p className={`text-lg sm:text-2xl font-black whitespace-nowrap flex-shrink-0 ${b.netBalance > 0 ? 'text-green-600' : b.netBalance < 0 ? 'text-red-500' : 'text-slate-400'}`}>
                    {b.netBalance > 0 ? '+' : ''}{Math.round(b.netBalance).toLocaleString()}원
                  </p>
                </div>
              ))}
            </div>
          </section>

          {/* Transfer Guide */}
          <section className="bg-white rounded-3xl sm:rounded-[56px] shadow-sm border border-slate-200 p-6 sm:p-12 flex flex-col">
            <h3 className="text-xl sm:text-2xl font-black mb-6 sm:mb-10 flex items-center gap-3 sm:gap-4">
              <ArrowRightLeft className="text-indigo-600" size={28} /> 송금 가이드
            </h3>
            <div className="space-y-4 sm:space-y-6 flex-1">
              {data.debts.map((d, i) => (
                <div key={i} className="p-4 sm:p-6 bg-indigo-50/40 border-indigo-50 border-2 rounded-2xl sm:rounded-[32px] shadow-sm">
                  <div className="flex items-center justify-center gap-4 sm:gap-6 mb-3 sm:mb-4">
                    <div className="flex flex-col items-center">
                      <span className="text-[10px] font-bold text-slate-400 uppercase tracking-widest mb-1">보낼 사람</span>
                      <span className="font-black text-slate-700 text-base sm:text-lg">{d.from}</span>
                    </div>
                    <ChevronRight className="text-indigo-400 flex-shrink-0 mt-4" size={20} />
                    <div className="flex flex-col items-center">
                      <span className="text-[10px] font-bold text-slate-400 uppercase tracking-widest mb-1">받는 사람</span>
                      <span className="font-black text-slate-700 text-base sm:text-lg">{d.to}</span>
                    </div>
                  </div>
                  <div className="text-xl sm:text-2xl font-black text-center text-indigo-600">
                    {Math.round(d.amount).toLocaleString()}원
                  </div>
                </div>
              ))}
            </div>
          </section>
        </div>

        {/* Expense History */}
        {data.rounds.length > 0 && (
          <section className="bg-white rounded-3xl sm:rounded-[56px] shadow-sm border border-slate-200 p-6 sm:p-12 mt-6 sm:mt-10">
            <h3 className="text-xl sm:text-2xl font-black mb-6 sm:mb-10 flex items-center gap-3 sm:gap-4">
              <Receipt className="text-indigo-600" size={28} /> 지출 내역
            </h3>
            <div className="space-y-4 sm:space-y-6">
              {data.rounds.map(round => (
                <div key={round.id} className="p-4 sm:p-7 bg-slate-50 rounded-2xl sm:rounded-[32px] border border-transparent hover:border-indigo-100 hover:bg-white transition-all shadow-sm">
                  <div className="flex items-start justify-between gap-3 sm:gap-4">
                    <div className="min-w-0">
                      <p className="font-black text-slate-800 text-base sm:text-xl">{round.title}</p>
                      <p className="text-[11px] text-slate-400 font-bold uppercase tracking-widest mt-2 sm:mt-3">결제 {round.payerName}</p>
                    </div>
                    <p className="text-lg sm:text-2xl font-black text-indigo-600 whitespace-nowrap flex-shrink-0">
                      {round.amount.toLocaleString()}원
                    </p>
                  </div>
                  {round.exclusions.length > 0 && (
                    <div className="mt-3 sm:mt-4 pt-3 sm:pt-4 border-t border-slate-100 flex flex-wrap items-center gap-2">
                      <UserMinus size={14} className="text-orange-500" />
                      <span className="text-[10px] font-black text-orange-400 uppercase tracking-widest">제외</span>
                      {round.exclusions.map(e => (
                        <span key={e.id} className="bg-orange-100 text-orange-600 px-2 py-1 rounded-lg text-xs font-bold">
                          {e.participantName}
                        </span>
                      ))}
                    </div>
                  )}
                </div>
              ))}
            </div>
          </section>
        )}

        {/* Footer */}
        <footer className="text-center mt-10 sm:mt-16 pb-10">
          <p className="text-xs text-slate-400 font-bold">
            이 링크는 {new Date(data.expiresAt).toLocaleString('ko-KR', { month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit' })}까지 유효합니다.
          </p>
          <a href="/" className="inline-block mt-4 text-indigo-600 font-black text-sm hover:underline">
            N빵으로 나도 정산하기
          </a>
        </footer>
      </div>
    </div>
  );
};

export default SharedSettlementPage;
