// Backend API Types (Spring Bootと連携)

export interface Company {
  id: number;
  companyName: string;
  registrationNumber: string;
  industry: string;
  country: string;
  address: string;
}

export interface Borrower {
  id: number;
  name: string;
  email: string;
  phoneNumber: string;
  companyId: string;
  creditLimit: number;
  creditRating: CreditRating;
  currentFacilityAmount: number; // 既存Facility総額（動的計算）
  status: BorrowerStatus;
}

export interface Investor {
  id: number;
  name: string;
  email: string;
  phoneNumber: string;
  companyId: string | null;
  investmentCapacity: number;
  currentInvestmentAmount: number;
  investorType: InvestorType;
  status: InvestorStatus;
}

export interface Syndicate {
  id: number;
  name: string;
  leadBankId: number;
  borrowerId: number;
  memberInvestorIds: number[];
  status: SyndicateStatus;
  createdAt: string;
  updatedAt: string;
  version: number;
}

export type SyndicateStatus = 'DRAFT' | 'ACTIVE' | 'COMPLETED';

export interface SyndicateDetail {
  id: number;
  name: string;
  borrowerId: number;
  borrowerName: string;
  leadBankId: number;
  leadBankName: string;
  memberInvestorIds: number[];
  memberInvestorNames: string[];
  status: SyndicateStatus;
  createdAt: string;
  updatedAt: string;
  version: number;
}

// Enums (バックエンドと同じ定義)
export type CreditRating = 'AAA' | 'AA' | 'A' | 'BBB' | 'BB' | 'B' | 'CCC' | 'CC' | 'C' | 'D';

export type InvestorType = 
  | 'LEAD_BANK' 
  | 'BANK' 
  | 'INSURANCE' 
  | 'FUND' 
  | 'CORPORATE' 
  | 'INDIVIDUAL' 
  | 'GOVERNMENT' 
  | 'PENSION' 
  | 'SOVEREIGN_FUND' 
  | 'CREDIT_UNION' 
  | 'OTHER';

export type FacilityStatus = 'DRAFT' | 'ACTIVE' | 'COMPLETED';

export type LoanStatus = 'DRAFT' | 'ACTIVE' | 'OVERDUE' | 'COMPLETED';

export type BorrowerStatus = 'DRAFT' | 'ACTIVE';

export type InvestorStatus = 'DRAFT' | 'ACTIVE';

export type RepaymentMethod = 'EQUAL_INSTALLMENT' | 'BULLET_PAYMENT';

export type RepaymentCycle = 'MONTHLY' | 'QUARTERLY' | 'SEMI_ANNUALLY' | 'ANNUALLY';

export type Industry = 
  | 'FINANCE' 
  | 'MANUFACTURING' 
  | 'IT' 
  | 'RETAIL' 
  | 'ENERGY' 
  | 'TRANSPORTATION' 
  | 'HEALTHCARE' 
  | 'CONSTRUCTION' 
  | 'AGRICULTURE' 
  | 'OTHER';

export type Country = 
  | 'JAPAN' 
  | 'USA' 
  | 'UK' 
  | 'GERMANY' 
  | 'FRANCE' 
  | 'CHINA' 
  | 'INDIA' 
  | 'AUSTRALIA' 
  | 'CANADA' 
  | 'OTHER';

// API Request Types
export interface CreateBorrowerRequest {
  name: string;
  email: string;
  phoneNumber: string;
  companyId: string;
  creditLimit: number;
  creditRating: CreditRating;
}

export interface UpdateBorrowerRequest {
  name: string;
  email: string;
  phoneNumber: string;
  companyId: string;
  creditLimit: number;
  creditRating: CreditRating;
  version: number;
}

export interface CreateCompanyRequest {
  companyName: string;
  registrationNumber: string;
  industry: Industry;
  country: Country;
  address: string;
}

export interface CreateInvestorRequest {
  name: string;
  email: string;
  phoneNumber: string;
  companyId?: string;
  investmentCapacity: number;
  investorType: InvestorType;
}

export interface UpdateInvestorRequest {
  name: string;
  email: string;
  phoneNumber: string;
  companyId?: string;
  investmentCapacity: number;
  investorType: InvestorType;
  version: number;
}

export interface CreateSyndicateRequest {
  name: string;
  leadBankId: number;
  borrowerId: number;
  memberInvestorIds: number[];
}

export interface UpdateSyndicateRequest {
  name: string;
  leadBankId: number;
  borrowerId: number;
  memberInvestorIds: number[];
  version: number;
}

export interface Facility {
  id: number;
  syndicateId: number;
  commitment: number;
  currency: string;
  startDate: string;
  endDate: string;
  interestTerms: string;
  sharePies: SharePie[];
  status: FacilityStatus;
  createdAt: string;
  updatedAt: string;
  version: number;
}

export interface SharePie {
  id: number;
  investorId: number;
  share: number; // 0.0-1.0 (例: 0.4 = 40%)
  facilityId?: number;
  createdAt: string;
  updatedAt: string;
}

export interface CreateFacilityRequest {
  syndicateId: number;
  commitment: number;
  currency: string;
  startDate: string;
  endDate: string;
  interestTerms: string;
  sharePies: CreateSharePieRequest[];
}

export interface CreateSharePieRequest {
  investorId: number;
  share: number;
}

export interface UpdateFacilityRequest {
  syndicateId: number;
  commitment?: number;
  currency?: string;
  startDate?: string;
  endDate?: string;
  interestTerms?: string;
  sharePies?: CreateSharePieRequest[];
  version: number;
}

export interface Loan {
  id: number;
  facilityId: number;
  borrowerId: number;
  principalAmount: any; // MoneyオブジェクトまたはnumberとしてJSONシリアライズされる可能性
  outstandingBalance: any; // MoneyオブジェクトまたはnumberとしてJSONシリアライズされる可能性
  currency: string;
  annualInterestRate: any; // PercentageオブジェクトまたはnumberとしてJSONシリアライズされる可能性
  drawdownDate: string;
  repaymentPeriodMonths: number;
  repaymentCycle: string;
  repaymentMethod: RepaymentMethod;
  status: LoanStatus;
  createdAt: string;
  updatedAt: string;
  version: number;
}

export interface Drawdown {
  id: number;
  loanId: number;
  amount: number;
  currency: string;
  purpose: string;
  transactionDate: string;
  status: TransactionStatus;
  amountPies: AmountPie[];
  createdAt: string;
  updatedAt: string;
  version: number;
}

export type TransactionStatus = 'DRAFT' | 'ACTIVE' | 'COMPLETED' | 'FAILED' | 'CANCELLED' | 'REFUNDED';

export interface AmountPie {
  id: number;
  investorId: number;
  amount: number;
  drawdownId?: number;
  createdAt: string;
  updatedAt: string;
}

export interface CreateDrawdownRequest {
  facilityId: number;
  borrowerId: number;
  amount: number;
  currency: string;
  purpose: string;
  annualInterestRate: number;
  drawdownDate: string;
  repaymentPeriodMonths: number;
  repaymentCycle: string;
  repaymentMethod: RepaymentMethod;
  amountPies?: CreateAmountPieRequest[];
}

export interface UpdateDrawdownRequest {
  amount: number;
  currency: string;
  purpose: string;
  annualInterestRate: number;
  drawdownDate: string;
  repaymentPeriodMonths: number;
  repaymentCycle: string;
  repaymentMethod: RepaymentMethod;
  version: number;
  amountPies?: CreateAmountPieRequest[];
}

export interface CreateAmountPieRequest {
  investorId: number;
  amount: number;
}

// Payment関連の型定義
export interface Payment {
  id: number;
  loanId: number;
  paymentDate: string;
  principalAmount: number;
  interestAmount: number;
  totalAmount: number;
  currency: string;
  paymentDistributions: PaymentDistribution[];
  createdAt: string;
  updatedAt: string;
  version: number;
}

export interface PaymentDetail {
  id: number;
  paymentNumber: number;
  principalPayment: number;
  interestPayment: number;
  dueDate: string;
  remainingBalance: number;
  paymentStatus: PaymentStatus;
  actualPaymentDate?: string;
  paymentId?: number;
  createdAt: string;
  updatedAt: string;
}

export type PaymentStatus = 'PENDING' | 'PAID' | 'OVERDUE';

export interface PaymentDistribution {
  id: number;
  paymentId: number;
  investorId: number;
  principalAmount: number;
  interestAmount: number;
  totalAmount: number;
  createdAt: string;
  updatedAt: string;
}

export interface CreatePaymentRequest {
  loanId: number;
  paymentDate: string;
  principalAmount: number;
  interestAmount: number;
  currency: string;
}

// API Response Types
export interface ApiResponse<T> {
  data: T;
  message?: string;
  status: number;
}

export interface PageResponse<T> {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
    sort: {
      sorted: boolean;
      unsorted: boolean;
      empty: boolean;
    };
    offset: number;
    paged: boolean;
    unpaged: boolean;
  };
  totalElements: number;
  totalPages: number;
  last: boolean;
  size: number;
  number: number;
  sort: {
    sorted: boolean;
    unsorted: boolean;
    empty: boolean;
  };
  first: boolean;
  numberOfElements: number;
  empty: boolean;
}

export interface ApiError {
  message: string;
  status: number;
  errors?: Record<string, string[]>;
}

// Fee Payment関連の型定義
export type FeeType = 
  | 'MANAGEMENT_FEE'
  | 'ARRANGEMENT_FEE'
  | 'COMMITMENT_FEE'
  | 'TRANSACTION_FEE'
  | 'LATE_FEE'
  | 'AGENT_FEE'
  | 'OTHER_FEE';

export type RecipientType = 'LEAD_BANK' | 'AGENT_BANK' | 'INVESTOR' | 'AUTO_DISTRIBUTE';

export interface FeePayment {
  id: number;
  feeType: FeeType;
  feeAmount: number;
  currency: string;
  feeDate: string;
  description: string;
  recipientType: RecipientType;
  recipientId: number;
  calculationBase: number;
  feeRate: number;
  facilityId: number;
  feeDistributions: FeeDistribution[];
  status: TransactionStatus;
  createdAt: string;
  updatedAt: string;
  version: number;
}

export interface FeeDistribution {
  id: number;
  feePaymentId: number;
  recipientType: RecipientType;
  recipientId: number;
  distributionAmount: number;
  distributionRatio: number;
  currency: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateFeePaymentRequest {
  feeType: FeeType;
  feeAmount: number;
  currency: string;
  feeDate: string;
  description: string;
  recipientType: RecipientType;
  recipientId: number;
  calculationBase: number;
  feeRate: number;
  facilityId: number;
  borrowerId: number;
}

export interface FeePaymentStatistics {
  totalFeePayments: number;
  totalFeeAmount: number;
  feePaymentCountsByType: Record<FeeType, number>;
  currency: string;
}