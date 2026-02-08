import { SettlementRound, Participant, DebtLink } from "../types.ts";

export const generateKakaoMessage = (
  gatheringName: string,
  participants: Participant[],
  rounds: SettlementRound[],
  debts: DebtLink[]
): string => {
  const totalAmount = rounds.reduce((sum, r) => sum + r.amount, 0);

  let message = `🧾 [${gatheringName}] 정산 안내\n\n`;

  // 총 지출 요약
  message += `💰 총 지출: ${totalAmount.toLocaleString()}원\n`;
  message += `👥 참여자: ${participants.map(p => p.name).join(', ')}\n\n`;

  // 지출 내역
  message += `📋 지출 내역\n`;
  message += `${'─'.repeat(20)}\n`;
  rounds.forEach(r => {
    const payer = participants.find(p => p.id === r.payerId);
    const excludedNames = r.excluded
      ?.map(e => {
        const p = participants.find(p => p.id === e.participantId);
        return p ? `${p.name}(${e.reason})` : null;
      })
      .filter(Boolean)
      .join(', ');

    message += `• ${r.title}: ${r.amount.toLocaleString()}원 (${payer?.name || '?'} 결제)`;
    if (excludedNames) {
      message += ` [제외: ${excludedNames}]`;
    }
    message += '\n';
  });

  // 송금 안내
  if (debts.length > 0) {
    message += `\n💸 송금 안내\n`;
    message += `${'─'.repeat(20)}\n`;
    debts.forEach(d => {
      const receiver = participants.find(p => p.id === d.toParticipantId);
      message += `${d.from} ➡️ ${d.to}: ${Math.round(d.amount).toLocaleString()}원\n`;
      if (receiver?.bankName && receiver?.accountNumber) {
        message += `   └ ${receiver.bankName} ${receiver.accountNumber}\n`;
      }
    });
  } else {
    message += `\n✅ 정산 완료! 추가 송금이 필요 없습니다.\n`;
  }

  return message;
};
