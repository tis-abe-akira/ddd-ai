import { z } from 'zod';

// Syndicate作成用Zodスキーマ
export const createSyndicateSchema = z.object({
  name: z
    .string()
    .min(1, 'シンジケート名は必須です')
    .max(100, 'シンジケート名は100文字以内で入力してください'),
  
  borrowerId: z
    .number({
      required_error: '借り手を選択してください',
      invalid_type_error: '有効な借り手を選択してください'
    })
    .positive('有効な借り手を選択してください'),
  
  leadBankId: z
    .number({
      required_error: 'リードバンクを選択してください',
      invalid_type_error: '有効なリードバンクを選択してください'
    })
    .positive('有効なリードバンクを選択してください'),
  
  memberInvestorIds: z
    .array(z.number().positive())
    .min(1, '最低1名のメンバー投資家を選択してください')
    .max(10, 'メンバー投資家は最大10名まで選択できます')
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
  { id: 1, title: '基本情報', description: 'シンジケート名を入力' },
  { id: 2, title: '借り手選択', description: '融資を受ける借り手を選択' },
  { id: 3, title: 'リードバンク', description: 'リードバンクを選択' },
  { id: 4, title: 'メンバー選択', description: 'メンバー投資家を選択' },
  { id: 5, title: '確認', description: '内容を確認して組成' }
] as const;

export type SyndicateFormStep = typeof SYNDICATE_FORM_STEPS[number]['id'];