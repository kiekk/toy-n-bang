
import { SettlementRound, Participant, UserBalance, DebtLink } from '../types.ts';

export const calculateBalances = (
  participants: Participant[],
  rounds: SettlementRound[]
): UserBalance[] => {
  const balances: Record<string, { paid: number; owed: number }> = {};

  participants.forEach((p) => {
    balances[p.id] = { paid: 0, owed: 0 };
  });

  rounds.forEach((round) => {
    if (balances[round.payerId]) {
      balances[round.payerId].paid += round.amount;
    }

    const excludedIds = (round.excluded || []).map((e) => e.participantId);
    const inclusionList = participants.filter((p) => !excludedIds.includes(p.id));

    if (inclusionList.length > 0) {
      const perPerson = round.amount / inclusionList.length;
      inclusionList.forEach((p) => {
        balances[p.id].owed += perPerson;
      });
    }
  });

  return participants.map((p) => ({
    participantId: p.id,
    name: p.name,
    totalPaid: balances[p.id].paid,
    totalOwed: balances[p.id].owed,
    netBalance: balances[p.id].paid - balances[p.id].owed,
  }));
};

export const resolveDebts = (balances: UserBalance[]): DebtLink[] => {
  const creditors = balances
    .filter((b) => b.netBalance > 0.01)
    .map((b) => ({ ...b }))
    .sort((a, b) => b.netBalance - a.netBalance);
    
  const debtors = balances
    .filter((b) => b.netBalance < -0.01)
    .map((b) => ({ ...b }))
    .sort((a, b) => a.netBalance - b.netBalance);

  const debts: DebtLink[] = [];

  let i = 0; // debtor index
  let j = 0; // creditor index

  while (i < debtors.length && j < creditors.length) {
    const debtor = debtors[i];
    const creditor = creditors[j];

    const amount = Math.min(Math.abs(debtor.netBalance), creditor.netBalance);
    
    debts.push({
      from: debtor.name,
      to: creditor.name,
      toParticipantId: creditor.participantId,
      amount: amount,
    });

    debtor.netBalance += amount;
    creditor.netBalance -= amount;

    if (Math.abs(debtor.netBalance) < 0.01) i++;
    if (Math.abs(creditor.netBalance) < 0.01) j++;
  }

  return debts;
};
