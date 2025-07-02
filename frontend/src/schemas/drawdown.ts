import { z } from 'zod';

// Drawdown作成用Zodスキーマ
export const createDrawdownSchema = z.object({
  facilityId: z
    .number({
      required_error: 'ファシリティを選択してください',
      invalid_type_error: '有効なファシリティを選択してください'
    })
    .positive('有効なファシリティを選択してください'),
  
  borrowerId: z
    .number({
      required_error: '借り手を選択してください',
      invalid_type_error: '有効な借り手を選択してください'
    })
    .positive('有効な借り手を選択してください'),
  
  amount: z
    .number({
      required_error: 'ドローダウン金額を入力してください',
      invalid_type_error: 'ドローダウン金額は数値で入力してください'
    })
    .positive('ドローダウン金額は正の数である必要があります')
    .max(100000000000, 'ドローダウン金額は1000億以下で入力してください'),
  
  currency: z
    .string()
    .min(1, '通貨を選択してください')
    .max(3, '通貨コードは3文字以内で入力してください'),
  
  purpose: z
    .string()
    .min(1, 'ドローダウンの目的を入力してください')
    .max(500, '目的は500文字以内で入力してください'),
  
  drawdownDate: z
    .string()
    .min(1, 'ドローダウン実行日を選択してください')
    .refine((date) => {
      const parsed = new Date(date);
      return !isNaN(parsed.getTime());
    }, '有効な日付を入力してください')
    .refine((date) => {
      const parsed = new Date(date);
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      return parsed >= today;
    }, 'ドローダウン実行日は今日以降の日付を選択してください'),
  
  annualInterestRate: z
    .number({
      required_error: '年利を入力してください',
      invalid_type_error: '年利は数値で入力してください'
    })
    .min(0, '年利は0%以上である必要があります')
    .max(1, '年利は100%以下である必要があります'),
  
  repaymentPeriodMonths: z
    .number({
      required_error: '返済期間を入力してください',
      invalid_type_error: '返済期間は数値で入力してください'
    })
    .int('返済期間は整数で入力してください')
    .min(1, '返済期間は1ヶ月以上である必要があります')
    .max(600, '返済期間は600ヶ月以下である必要があります'),
  
  repaymentCycle: z
    .string()
    .min(1, '返済サイクルを選択してください'),
  
  repaymentMethod: z
    .enum(['EQUAL_INSTALLMENT', 'BULLET', 'INTEREST_ONLY'], {
      required_error: '返済方法を選択してください',
      invalid_type_error: '有効な返済方法を選択してください'
    })
});

// TypeScriptの型を自動生成
export type CreateDrawdownFormData = z.infer<typeof createDrawdownSchema>;

// フォームのデフォルト値
export const defaultDrawdownValues: Partial<CreateDrawdownFormData> = {
  facilityId: undefined,
  borrowerId: undefined,
  amount: undefined,
  currency: 'USD',
  purpose: '',
  drawdownDate: '',
  annualInterestRate: 0.025, // 2.5%
  repaymentPeriodMonths: 12,
  repaymentCycle: 'MONTHLY',
  repaymentMethod: 'EQUAL_INSTALLMENT',
};

// 返済方法オプション
export const repaymentMethodOptions = [
  { value: 'EQUAL_INSTALLMENT', label: '元利均等返済', description: '毎月一定額の返済' },
  { value: 'BULLET', label: 'バレット返済', description: '満期一括返済' },
  { value: 'INTEREST_ONLY', label: '利息のみ返済', description: '期中は利息のみ、満期に元本返済' },
] as const;

// 返済サイクルオプション
export const repaymentCycleOptions = [
  { value: 'MONTHLY', label: '月次', description: '毎月返済' },
  { value: 'QUARTERLY', label: '四半期', description: '3ヶ月毎の返済' },
  { value: 'SEMI_ANNUALLY', label: '半年', description: '6ヶ月毎の返済' },
  { value: 'ANNUALLY', label: '年次', description: '年1回の返済' },
] as const;

// ドローダウンの目的オプション
export const purposeOptions = [
  { value: '運転資金', label: '運転資金' },
  { value: '設備投資', label: '設備投資' },
  { value: 'M&A資金', label: 'M&A資金' },
  { value: '借換資金', label: '借換資金' },
  { value: '事業拡大', label: '事業拡大' },
  { value: 'その他', label: 'その他' },
] as const;

// ステップ定義
export const DRAWDOWN_FORM_STEPS = [
  { id: 1, title: 'ファシリティ選択', description: 'ドローダウンを実行するファシリティを選択' },
  { id: 2, title: 'ドローダウン基本情報', description: '金額・目的・実行日を設定' },
  { id: 3, title: '返済条件', description: '金利・期間・返済方法を設定' },
  { id: 4, title: '確認・実行', description: '内容を確認してドローダウンを実行' }
] as const;

export type DrawdownFormStep = typeof DRAWDOWN_FORM_STEPS[number]['id'];