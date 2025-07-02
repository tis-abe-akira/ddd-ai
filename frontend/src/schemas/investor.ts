import { z } from 'zod';
import type { InvestorType } from '../types/api';

// Investor Type選択肢
export const investorTypeOptions: InvestorType[] = [
  'LEAD_BANK', 'BANK', 'INSURANCE', 'FUND', 'CORPORATE', 
  'INDIVIDUAL', 'GOVERNMENT', 'PENSION', 'SOVEREIGN_FUND', 
  'CREDIT_UNION', 'OTHER'
];

// Investor Type表示名マッピング
export const investorTypeLabels: Record<InvestorType, string> = {
  'LEAD_BANK': 'Lead Bank',
  'BANK': 'Bank',
  'INSURANCE': 'Insurance Company',
  'FUND': 'Fund',
  'CORPORATE': 'Corporate',
  'INDIVIDUAL': 'Individual',
  'GOVERNMENT': 'Government Agency',
  'PENSION': 'Pension Fund',
  'SOVEREIGN_FUND': 'Sovereign Fund',
  'CREDIT_UNION': 'Credit Union',
  'OTHER': 'Other'
};

// Investor作成用Zodスキーマ
export const createInvestorSchema = z.object({
  name: z
    .string()
    .min(1, 'Investor name is required')
    .max(100, 'Investor name must be 100 characters or less'),
  
  email: z
    .string()
    .min(1, 'Email address is required')
    .email('Please enter a valid email address'),
  
  phoneNumber: z
    .string()
    .min(1, 'Phone number is required')
    .regex(/^[\d\-\+\(\)\s]+$/, 'Please enter a valid phone number'),
  
  companyId: z
    .string()
    .optional(),
  
  investmentCapacity: z
    .number({
      required_error: 'Investment capacity is required',
      invalid_type_error: 'Investment capacity must be a number'
    })
    .positive('Investment capacity must be positive')
    .max(10000000000, 'Investment capacity must be 10 billion or less'),
  
  investorType: z
    .enum(['LEAD_BANK', 'BANK', 'INSURANCE', 'FUND', 'CORPORATE', 'INDIVIDUAL', 'GOVERNMENT', 'PENSION', 'SOVEREIGN_FUND', 'CREDIT_UNION', 'OTHER'], {
      required_error: 'Please select investor type',
      invalid_type_error: 'Please select a valid investor type'
    })
});

// TypeScriptの型を自動生成
export type CreateInvestorFormData = z.infer<typeof createInvestorSchema>;

// フォームのデフォルト値
export const defaultInvestorValues: Partial<CreateInvestorFormData> = {
  name: '',
  email: '',
  phoneNumber: '',
  companyId: '',
  investmentCapacity: undefined,
  investorType: undefined,
};