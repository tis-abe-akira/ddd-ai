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

// API Response Types
export interface ApiResponse<T> {
  data: T;
  message?: string;
  status: number;
}

export interface ApiError {
  message: string;
  status: number;
  errors?: Record<string, string[]>;
}