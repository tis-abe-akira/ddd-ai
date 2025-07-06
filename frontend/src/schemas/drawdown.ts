import { z } from 'zod';

// Zod schema for drawdown creation
export const createDrawdownSchema = z.object({
  facilityId: z
    .number({
      required_error: 'Please select a facility',
      invalid_type_error: 'Please select a valid facility'
    })
    .positive('Please select a valid facility'),
  
  borrowerId: z
    .number({
      required_error: 'Please select a borrower',
      invalid_type_error: 'Please select a valid borrower'
    })
    .positive('Please select a valid borrower'),
  
  amount: z
    .number({
      required_error: 'Please enter drawdown amount',
      invalid_type_error: 'Drawdown amount must be a number'
    })
    .positive('Drawdown amount must be positive')
    .max(100000000000, 'Drawdown amount must be 100 billion or less'),
  
  currency: z
    .string()
    .min(1, 'Please select a currency')
    .max(3, 'Currency code must be 3 characters or less'),
  
  purpose: z
    .string()
    .min(1, 'Please enter drawdown purpose')
    .max(500, 'Purpose must be 500 characters or less'),
  
  drawdownDate: z
    .string()
    .min(1, 'Please select drawdown execution date')
    .refine((date) => {
      const parsed = new Date(date);
      return !isNaN(parsed.getTime());
    }, 'Please enter a valid date')
    .refine((date) => {
      const parsed = new Date(date);
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      return parsed >= today;
    }, 'Drawdown execution date must be today or later'),
  
  annualInterestRate: z
    .number({
      required_error: 'Please enter annual interest rate',
      invalid_type_error: 'Annual interest rate must be a number'
    })
    .min(0, 'Annual interest rate must be 0% or higher')
    .max(1, 'Annual interest rate must be 100% or lower'),
  
  repaymentPeriodMonths: z
    .number({
      required_error: 'Please enter repayment period',
      invalid_type_error: 'Repayment period must be a number'
    })
    .int('Repayment period must be an integer')
    .min(1, 'Repayment period must be at least 1 month')
    .max(600, 'Repayment period must be 600 months or less'),
  
  repaymentCycle: z
    .string()
    .min(1, 'Please select repayment cycle'),
  
  repaymentMethod: z
    .enum(['EQUAL_INSTALLMENT', 'BULLET_PAYMENT'], {
      required_error: 'Please select repayment method',
      invalid_type_error: 'Please select a valid repayment method'
    })
});

// Auto-generated TypeScript type
export type CreateDrawdownFormData = z.infer<typeof createDrawdownSchema>;

// Default form values
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

// Repayment method options
export const repaymentMethodOptions = [
  { value: 'EQUAL_INSTALLMENT', label: 'Equal Installment', description: 'Fixed monthly payment' },
  { value: 'BULLET_PAYMENT', label: 'Bullet Payment', description: 'Lump sum payment at maturity' },
] as const;

// Repayment cycle options
export const repaymentCycleOptions = [
  { value: 'MONTHLY', label: 'Monthly', description: 'Monthly payment' },
  { value: 'QUARTERLY', label: 'Quarterly', description: 'Payment every 3 months' },
  { value: 'SEMI_ANNUALLY', label: 'Semi-Annually', description: 'Payment every 6 months' },
  { value: 'ANNUALLY', label: 'Annually', description: 'Annual payment' },
] as const;

// Drawdown purpose options
export const purposeOptions = [
  { value: 'Working Capital', label: 'Working Capital' },
  { value: 'Capital Investment', label: 'Capital Investment' },
  { value: 'M&A Funding', label: 'M&A Funding' },
  { value: 'Refinancing', label: 'Refinancing' },
  { value: 'Business Expansion', label: 'Business Expansion' },
  { value: 'Other', label: 'Other' },
] as const;

// Step definitions
export const DRAWDOWN_FORM_STEPS = [
  { id: 1, title: 'Facility Selection', description: 'Select facility to execute drawdown' },
  { id: 2, title: 'Drawdown Information', description: 'Set amount, purpose, and execution date' },
  { id: 3, title: 'Repayment Terms', description: 'Set interest rate, period, and repayment method' },
  { id: 4, title: 'Confirmation & Execution', description: 'Review and execute drawdown' }
] as const;

export type DrawdownFormStep = typeof DRAWDOWN_FORM_STEPS[number]['id'];