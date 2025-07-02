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
  isActive: boolean;
}

export interface Syndicate {
  id: number;
  name: string;
  leadBankId: number;
  borrowerId: number;
  memberInvestorIds: number[];
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

export interface CreateSyndicateRequest {
  name: string;
  leadBankId: number;
  borrowerId: number;
  memberInvestorIds: number[];
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