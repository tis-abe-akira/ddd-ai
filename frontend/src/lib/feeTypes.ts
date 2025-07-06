import type { FeeType, RecipientType } from '../types/api';

// 手数料タイプの表示名とビジネスルール
export const FEE_TYPE_LABELS: Record<FeeType, string> = {
  MANAGEMENT_FEE: '管理手数料',
  ARRANGEMENT_FEE: 'アレンジメント手数料',
  COMMITMENT_FEE: 'コミットメント手数料',
  TRANSACTION_FEE: '取引手数料',
  LATE_FEE: '遅延手数料',
  AGENT_FEE: 'エージェント手数料',
  OTHER_FEE: 'その他手数料',
};

// 手数料タイプの説明
export const FEE_TYPE_DESCRIPTIONS: Record<FeeType, string> = {
  MANAGEMENT_FEE: 'ローン管理・運営に関する手数料（リードバンク収益）',
  ARRANGEMENT_FEE: 'ローン組成・アレンジメントに関する手数料（リードバンク収益）',
  COMMITMENT_FEE: 'コミットメント維持に関する手数料（投資家配分）',
  TRANSACTION_FEE: '取引実行に関する手数料（バンク収益）',
  LATE_FEE: '支払い遅延に関する手数料（投資家配分）',
  AGENT_FEE: 'エージェント業務に関する手数料（エージェントバンク収益）',
  OTHER_FEE: 'その他の手数料（設定可能）',
};

// 手数料タイプ別の受取人制限
export const getFeeTypeRecipientRestrictions = (feeType: FeeType): RecipientType[] => {
  switch (feeType) {
    case 'MANAGEMENT_FEE':
    case 'ARRANGEMENT_FEE':
    case 'TRANSACTION_FEE':
    case 'AGENT_FEE':
      return ['BANK'];
    case 'COMMITMENT_FEE':
    case 'LATE_FEE':
      return ['INVESTOR'];
    case 'OTHER_FEE':
      return ['BANK', 'INVESTOR'];
    default:
      return ['BANK', 'INVESTOR'];
  }
};

// 手数料タイプが投資家配分を必要とするかチェック
export const requiresInvestorDistribution = (feeType: FeeType): boolean => {
  return feeType === 'COMMITMENT_FEE' || feeType === 'LATE_FEE';
};

// 手数料タイプがバンク収益かチェック
export const isBankRevenue = (feeType: FeeType): boolean => {
  return ['MANAGEMENT_FEE', 'ARRANGEMENT_FEE', 'TRANSACTION_FEE', 'AGENT_FEE'].includes(feeType);
};

// 受取人タイプの表示名
export const RECIPIENT_TYPE_LABELS: Record<RecipientType, string> = {
  BANK: '銀行',
  INVESTOR: '投資家',
};

// 手数料タイプのバリデーション
export const validateFeeTypeRecipient = (feeType: FeeType, recipientType: RecipientType): boolean => {
  const allowedRecipients = getFeeTypeRecipientRestrictions(feeType);
  return allowedRecipients.includes(recipientType);
};

// 手数料計算の検証
export const validateFeeCalculation = (
  calculationBase: number,
  feeRate: number,
  feeAmount: number,
  tolerance: number = 0.01
): boolean => {
  const expectedAmount = calculationBase * (feeRate / 100);
  return Math.abs(expectedAmount - feeAmount) <= tolerance;
};

// 手数料タイプの色分け（UI用）
export const getFeeTypeColor = (feeType: FeeType): string => {
  switch (feeType) {
    case 'MANAGEMENT_FEE':
      return 'bg-blue-100 text-blue-800';
    case 'ARRANGEMENT_FEE':
      return 'bg-green-100 text-green-800';
    case 'COMMITMENT_FEE':
      return 'bg-yellow-100 text-yellow-800';
    case 'TRANSACTION_FEE':
      return 'bg-purple-100 text-purple-800';
    case 'LATE_FEE':
      return 'bg-red-100 text-red-800';
    case 'AGENT_FEE':
      return 'bg-indigo-100 text-indigo-800';
    case 'OTHER_FEE':
      return 'bg-gray-100 text-gray-800';
    default:
      return 'bg-gray-100 text-gray-800';
  }
};

// 手数料タイプの優先度（ソート用）
export const getFeeTypePriority = (feeType: FeeType): number => {
  const priorities: Record<FeeType, number> = {
    MANAGEMENT_FEE: 1,
    ARRANGEMENT_FEE: 2,
    COMMITMENT_FEE: 3,
    TRANSACTION_FEE: 4,
    LATE_FEE: 5,
    AGENT_FEE: 6,
    OTHER_FEE: 7,
  };
  return priorities[feeType] || 999;
};

// 手数料タイプの選択肢（フォーム用）
export const getFeeTypeOptions = () => {
  return Object.entries(FEE_TYPE_LABELS).map(([value, label]) => ({
    value: value as FeeType,
    label,
    description: FEE_TYPE_DESCRIPTIONS[value as FeeType],
  }));
};

// 受取人タイプの選択肢（フォーム用）
export const getRecipientTypeOptions = () => {
  return Object.entries(RECIPIENT_TYPE_LABELS).map(([value, label]) => ({
    value: value as RecipientType,
    label,
  }));
};