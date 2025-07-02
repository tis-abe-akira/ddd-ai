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
  'LEAD_BANK': 'リードバンク',
  'BANK': '銀行',
  'INSURANCE': '保険会社',
  'FUND': 'ファンド',
  'CORPORATE': '法人',
  'INDIVIDUAL': '個人',
  'GOVERNMENT': '政府機関',
  'PENSION': '年金基金',
  'SOVEREIGN_FUND': 'ソブリンファンド',
  'CREDIT_UNION': '信用組合',
  'OTHER': 'その他'
};

// Investor作成用Zodスキーマ
export const createInvestorSchema = z.object({
  name: z
    .string()
    .min(1, '投資家名は必須です')
    .max(100, '投資家名は100文字以内で入力してください'),
  
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
  
  investmentCapacity: z
    .number({
      required_error: '投資能力は必須です',
      invalid_type_error: '投資能力は数値で入力してください'
    })
    .positive('投資能力は正の数である必要があります')
    .max(10000000000, '投資能力は100億以下で入力してください'),
  
  investorType: z
    .enum(['LEAD_BANK', 'BANK', 'INSURANCE', 'FUND', 'CORPORATE', 'INDIVIDUAL', 'GOVERNMENT', 'PENSION', 'SOVEREIGN_FUND', 'CREDIT_UNION', 'OTHER'], {
      required_error: '投資家タイプを選択してください',
      invalid_type_error: '有効な投資家タイプを選択してください'
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