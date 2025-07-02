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
    .min(1, '会社名は必須です')
    .max(100, '会社名は100文字以内で入力してください'),
  
  email: z
    .string()
    .min(1, 'メールアドレスは必須です')
    .email('有効なメールアドレスを入力してください'),
  
  phoneNumber: z
    .string()
    .min(1, '電話番号は必須です')
    .regex(/^[\d\-\+\(\)\s]+$/, '有効な電話番号を入力してください'),
  
  companyId: z
    .string()
    .optional(),
  
  creditLimit: z
    .number({
      required_error: '信用限度額は必須です',
      invalid_type_error: '信用限度額は数値で入力してください'
    })
    .positive('信用限度額は正の数である必要があります')
    .max(1000000000, '信用限度額は10億以下で入力してください'),
  
  creditRating: z
    .enum(['AAA', 'AA', 'A', 'BBB', 'BB', 'B', 'CCC', 'CC', 'C', 'D'], {
      required_error: '信用格付けを選択してください',
      invalid_type_error: '有効な信用格付けを選択してください'
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