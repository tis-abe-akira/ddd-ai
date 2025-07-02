import { z } from 'zod';

// SharePie用のスキーマ
export const sharePieSchema = z.object({
  investorId: z
    .number({
      required_error: '投資家を選択してください',
      invalid_type_error: '有効な投資家を選択してください'
    })
    .positive('有効な投資家を選択してください'),
  
  share: z
    .number({
      required_error: '持分比率を入力してください',
      invalid_type_error: '持分比率は数値で入力してください'
    })
    .min(0.01, '持分比率は1%以上である必要があります')
    .max(1.0, '持分比率は100%以下である必要があります')
});

// Facility作成用Zodスキーマ
export const createFacilitySchema = z.object({
  syndicateId: z
    .number({
      required_error: 'シンジケートを選択してください',
      invalid_type_error: '有効なシンジケートを選択してください'
    })
    .positive('有効なシンジケートを選択してください'),
  
  commitment: z
    .number({
      required_error: '融資枠を入力してください',
      invalid_type_error: '融資枠は数値で入力してください'
    })
    .positive('融資枠は正の数である必要があります')
    .max(100000000000, '融資枠は1000億以下で入力してください'),
  
  currency: z
    .string()
    .min(1, '通貨を選択してください')
    .max(3, '通貨コードは3文字以内で入力してください'),
  
  startDate: z
    .string()
    .min(1, '開始日を選択してください')
    .refine((date) => {
      const parsed = new Date(date);
      return !isNaN(parsed.getTime());
    }, '有効な日付を入力してください'),
  
  endDate: z
    .string()
    .min(1, '終了日を選択してください')
    .refine((date) => {
      const parsed = new Date(date);
      return !isNaN(parsed.getTime());
    }, '有効な日付を入力してください'),
  
  interestTerms: z
    .string()
    .min(1, '金利条件を入力してください')
    .max(200, '金利条件は200文字以内で入力してください'),
  
  sharePies: z
    .array(sharePieSchema)
    .min(1, '最低1名の投資家持分を設定してください')
    .max(20, '投資家持分は最大20名まで設定できます')
    .refine((sharePies) => {
      // 合計が100%（1.0）になることを検証
      const total = sharePies.reduce((sum, pie) => sum + pie.share, 0);
      return Math.abs(total - 1.0) < 0.0001; // 浮動小数点の誤差を考慮
    }, 'すべての投資家持分の合計は100%である必要があります')
    .refine((sharePies) => {
      // 同じ投資家IDが重複していないことを検証
      const investorIds = sharePies.map(pie => pie.investorId);
      return new Set(investorIds).size === investorIds.length;
    }, '同じ投資家が重複して設定されています')
}).refine((data) => {
  // 開始日が終了日より前であることを検証
  const startDate = new Date(data.startDate);
  const endDate = new Date(data.endDate);
  return startDate < endDate;
}, {
  message: '開始日は終了日より前である必要があります',
  path: ['endDate']
});

// TypeScriptの型を自動生成
export type CreateFacilityFormData = z.infer<typeof createFacilitySchema>;
export type SharePieFormData = z.infer<typeof sharePieSchema>;

// フォームのデフォルト値
export const defaultFacilityValues: Partial<CreateFacilityFormData> = {
  syndicateId: undefined,
  commitment: undefined,
  currency: 'USD',
  startDate: '',
  endDate: '',
  interestTerms: '',
  sharePies: [],
};

// 通貨オプション
export const currencyOptions = [
  { value: 'USD', label: 'USD (米ドル)' },
  { value: 'JPY', label: 'JPY (日本円)' },
  { value: 'EUR', label: 'EUR (ユーロ)' },
  { value: 'GBP', label: 'GBP (英ポンド)' },
  { value: 'CHF', label: 'CHF (スイスフラン)' },
] as const;

// ステップ定義
export const FACILITY_FORM_STEPS = [
  { id: 1, title: 'シンジケート選択', description: '融資枠を作成するシンジケートを選択' },
  { id: 2, title: '基本情報', description: '融資枠の基本情報を入力' },
  { id: 3, title: '持分配分', description: '投資家の持分比率を設定' },
  { id: 4, title: '確認', description: '内容を確認してファシリティを組成' }
] as const;

export type FacilityFormStep = typeof FACILITY_FORM_STEPS[number]['id'];