import type { FeeType, RecipientType } from '../types/api';

// Fee type display names and business rules
export const FEE_TYPE_LABELS: Record<FeeType, string> = {
  MANAGEMENT_FEE: 'Management Fee',
  ARRANGEMENT_FEE: 'Arrangement Fee',
  COMMITMENT_FEE: 'Commitment Fee',
  TRANSACTION_FEE: 'Transaction Fee',
  LATE_FEE: 'Late Fee',
  AGENT_FEE: 'Agent Fee',
  OTHER_FEE: 'Other Fee',
};

// Fee type descriptions
export const FEE_TYPE_DESCRIPTIONS: Record<FeeType, string> = {
  MANAGEMENT_FEE: 'Loan management and operation fees (Lead Bank revenue)',
  ARRANGEMENT_FEE: 'Loan arrangement and structuring fees (Lead Bank revenue)',
  COMMITMENT_FEE: 'Commitment maintenance fees (Investor distribution)',
  TRANSACTION_FEE: 'Transaction execution fees (Bank revenue)',
  LATE_FEE: 'Payment delay penalties (Investor distribution)',
  AGENT_FEE: 'Agent services fees (Agent Bank revenue)',
  OTHER_FEE: 'Other fees (configurable)',
};

// Fee type recipient restrictions (supporting new RecipientType)
export const getFeeTypeRecipientRestrictions = (feeType: FeeType): RecipientType[] => {
  switch (feeType) {
    case 'MANAGEMENT_FEE':
    case 'ARRANGEMENT_FEE':
      return ['LEAD_BANK'];
    case 'TRANSACTION_FEE':
    case 'AGENT_FEE':
      return ['AGENT_BANK'];
    case 'COMMITMENT_FEE':
    case 'LATE_FEE':
      return ['AUTO_DISTRIBUTE'];
    case 'OTHER_FEE':
      return ['INVESTOR'];
    default:
      return ['INVESTOR'];
  }
};

// Check if fee type requires investor distribution
export const requiresInvestorDistribution = (feeType: FeeType): boolean => {
  return getFeeTypeRecipientRestrictions(feeType).includes('AUTO_DISTRIBUTE');
};

// Check if fee type is bank revenue
export const isBankRevenue = (feeType: FeeType): boolean => {
  return ['MANAGEMENT_FEE', 'ARRANGEMENT_FEE', 'TRANSACTION_FEE', 'AGENT_FEE'].includes(feeType);
};

// Recipient type display names
export const RECIPIENT_TYPE_LABELS: Record<RecipientType, string> = {
  LEAD_BANK: 'Lead Bank (Auto)',
  AGENT_BANK: 'Agent Bank',
  INVESTOR: 'Investor',
  AUTO_DISTRIBUTE: 'Auto Distribution',
};

// Fee type validation
export const validateFeeTypeRecipient = (feeType: FeeType, recipientType: RecipientType): boolean => {
  const allowedRecipients = getFeeTypeRecipientRestrictions(feeType);
  return allowedRecipients.includes(recipientType);
};

// Fee calculation validation
export const validateFeeCalculation = (
  calculationBase: number,
  feeRate: number,
  feeAmount: number,
  tolerance: number = 0.01
): boolean => {
  const expectedAmount = calculationBase * (feeRate / 100);
  return Math.abs(expectedAmount - feeAmount) <= tolerance;
};

// Fee type color coding (for UI)
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

// Fee type priority (for sorting)
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

// Fee type options (for forms)
export const getFeeTypeOptions = () => {
  return Object.entries(FEE_TYPE_LABELS).map(([value, label]) => ({
    value: value as FeeType,
    label,
    description: FEE_TYPE_DESCRIPTIONS[value as FeeType],
  }));
};

// Recipient type options (for forms)
export const getRecipientTypeOptions = () => {
  return Object.entries(RECIPIENT_TYPE_LABELS).map(([value, label]) => ({
    value: value as RecipientType,
    label,
  }));
};

// Default fee rates by fee type
export const getFeeTypeDefaultRate = (feeType: FeeType): number => {
  const defaultRates: Record<FeeType, number> = {
    MANAGEMENT_FEE: 1.5,
    ARRANGEMENT_FEE: 2.0,
    COMMITMENT_FEE: 0.5,
    TRANSACTION_FEE: 0.1,
    LATE_FEE: 5.0,
    AGENT_FEE: 1.0,
    OTHER_FEE: 0.0,
  };
  return defaultRates[feeType] || 0.0;
};

// Check if recipient selection is required
export const requiresRecipientSelection = (feeType: FeeType): boolean => {
  const recipientType = getFeeTypeRecipientRestrictions(feeType)[0];
  return recipientType === 'AGENT_BANK' || recipientType === 'INVESTOR';
};

// Check if automatic recipient determination is possible
export const isRecipientAutomatic = (feeType: FeeType): boolean => {
  const recipientType = getFeeTypeRecipientRestrictions(feeType)[0];
  return recipientType === 'LEAD_BANK' || recipientType === 'AUTO_DISTRIBUTE';
};