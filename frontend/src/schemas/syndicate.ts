import { z } from 'zod';

// Syndicate作成用Zodスキーマ
export const createSyndicateSchema = z.object({
  name: z
    .string()
    .min(1, 'Syndicate name is required')
    .max(100, 'Syndicate name must be 100 characters or less'),
  
  borrowerId: z
    .number({
      required_error: 'Please select a borrower',
      invalid_type_error: 'Please select a valid borrower'
    })
    .positive('Please select a valid borrower'),
  
  leadBankId: z
    .number({
      required_error: 'Please select a lead bank',
      invalid_type_error: 'Please select a valid lead bank'
    })
    .positive('Please select a valid lead bank'),
  
  memberInvestorIds: z
    .array(z.number().positive())
    .min(1, 'At least one member investor must be selected')
    .max(10, 'Maximum 10 member investors can be selected')
});

// TypeScriptの型を自動生成
export type CreateSyndicateFormData = z.infer<typeof createSyndicateSchema>;

// フォームのデフォルト値
export const defaultSyndicateValues: Partial<CreateSyndicateFormData> = {
  name: '',
  borrowerId: undefined,
  leadBankId: undefined,
  memberInvestorIds: [],
};

// ステップ定義
export const SYNDICATE_FORM_STEPS = [
  { id: 1, title: 'Basic Information', description: 'Enter the syndicate name' },
  { id: 2, title: 'Borrower Selection', description: 'Select the borrower for financing' },
  { id: 3, title: 'Lead Bank', description: 'Select the lead bank' },
  { id: 4, title: 'Member Selection', description: 'Select member investors' },
  { id: 5, title: 'Confirmation', description: 'Review and create syndicate' }
] as const;

export type SyndicateFormStep = typeof SYNDICATE_FORM_STEPS[number]['id'];