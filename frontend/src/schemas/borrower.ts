import { z } from 'zod';
import type { CreditRating } from '../types/api';

// Credit Rating選択肢
export const creditRatingOptions: CreditRating[] = [
  'AAA', 'AA', 'A', 'BBB', 'BB', 'B', 'CCC', 'CC', 'C', 'D'
];

// Borrower作成用Zodスキーマ
export const createBorrowerSchema = z.object({
  name: z
    .string()
    .min(1, 'Company name is required')
    .max(100, 'Company name must be 100 characters or less'),
  
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
  
  creditLimit: z
    .number({
      required_error: 'Credit limit is required',
      invalid_type_error: 'Credit limit must be a number'
    })
    .positive('Credit limit must be positive')
    .max(1000000000, 'Credit limit must be 1 billion or less'),
  
  creditRating: z
    .enum(['AAA', 'AA', 'A', 'BBB', 'BB', 'B', 'CCC', 'CC', 'C', 'D'], {
      required_error: 'Please select credit rating',
      invalid_type_error: 'Please select a valid credit rating'
    })
});

// TypeScriptの型を自動生成
export type CreateBorrowerFormData = z.infer<typeof createBorrowerSchema>;

// フォームのデフォルト値
export const defaultBorrowerValues: Partial<CreateBorrowerFormData> = {
  name: '',
  email: '',
  phoneNumber: '',
  companyId: '',
  creditLimit: undefined,
  creditRating: undefined,
};