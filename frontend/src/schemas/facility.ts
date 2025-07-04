import { z } from 'zod';

// SharePie用のスキーマ
export const sharePieSchema = z.object({
  investorId: z
    .number({
      required_error: 'Please select an investor',
      invalid_type_error: 'Please select a valid investor'
    })
    .positive('Please select a valid investor'),
  
  share: z
    .number({
      required_error: 'Please enter share percentage',
      invalid_type_error: 'Share percentage must be a number'
    })
    .min(0.01, 'Share percentage must be at least 1%')
    .max(1.0, 'Share percentage must be at most 100%')
});

// Facility作成用Zodスキーマ
export const createFacilitySchema = z.object({
  syndicateId: z
    .number({
      required_error: 'Please select a syndicate',
      invalid_type_error: 'Please select a valid syndicate'
    })
    .positive('Please select a valid syndicate'),
  
  commitment: z
    .number({
      required_error: 'Please enter facility amount',
      invalid_type_error: 'Facility amount must be a number'
    })
    .positive('Facility amount must be positive')
    .max(100000000000, 'Facility amount must be 100 billion or less'),
  
  currency: z
    .string()
    .min(1, 'Please select a currency')
    .max(3, 'Currency code must be 3 characters or less'),
  
  startDate: z
    .string()
    .min(1, 'Please select start date')
    .refine((date) => {
      const parsed = new Date(date);
      return !isNaN(parsed.getTime());
    }, 'Please enter a valid date'),
  
  endDate: z
    .string()
    .min(1, 'Please select end date')
    .refine((date) => {
      const parsed = new Date(date);
      return !isNaN(parsed.getTime());
    }, 'Please enter a valid date'),
  
  interestTerms: z
    .string()
    .min(1, 'Please enter interest terms')
    .max(200, 'Interest terms must be 200 characters or less'),
  
  sharePies: z
    .array(sharePieSchema)
    .min(1, 'At least one investor share must be set')
    .max(20, 'Maximum 20 investor shares can be set')
    .refine((sharePies) => {
      // 合計が100%（1.0）になることを検証
      const total = sharePies.reduce((sum, pie) => sum + pie.share, 0);
      return Math.abs(total - 1.0) < 0.0001; // 浮動小数点の誤差を考慮
    }, 'Total of all investor shares must be 100%')
    .refine((sharePies) => {
      // 同じ投資家IDが重複していないことを検証
      const investorIds = sharePies.map(pie => pie.investorId);
      return new Set(investorIds).size === investorIds.length;
    }, 'The same investor is set multiple times')
}).refine((data) => {
  // 開始日が終了日より前であることを検証
  const startDate = new Date(data.startDate);
  const endDate = new Date(data.endDate);
  return startDate < endDate;
}, {
  message: 'Start date must be before end date',
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
  { value: 'USD', label: 'USD (US Dollar)' },
  { value: 'JPY', label: 'JPY (Japanese Yen)' },
  { value: 'EUR', label: 'EUR (Euro)' },
  { value: 'GBP', label: 'GBP (British Pound)' },
  { value: 'CHF', label: 'CHF (Swiss Franc)' },
] as const;

// ステップ定義
export const FACILITY_FORM_STEPS = [
  { id: 1, title: 'Syndicate Selection', description: 'Select the syndicate to create the facility' },
  { id: 2, title: 'Basic Information', description: 'Enter basic facility information' },
  { id: 3, title: 'Share Allocation', description: 'Set investor share percentages' },
  { id: 4, title: 'Confirmation', description: 'Review and create facility' }
] as const;

export type FacilityFormStep = typeof FACILITY_FORM_STEPS[number]['id'];