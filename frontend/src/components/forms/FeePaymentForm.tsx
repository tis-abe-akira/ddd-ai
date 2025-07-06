import React, { useState, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { feePaymentApi, facilityApi, investorApi } from '../../lib/api';
import { 
  FEE_TYPE_LABELS, 
  FEE_TYPE_DESCRIPTIONS, 
  RECIPIENT_TYPE_LABELS,
  getFeeTypeOptions,
  getRecipientTypeOptions,
  getFeeTypeRecipientRestrictions,
  validateFeeTypeRecipient,
  validateFeeCalculation,
  getFeeTypeColor
} from '../../lib/feeTypes';
import type { 
  FeePayment, 
  CreateFeePaymentRequest, 
  FeeType, 
  RecipientType,
  Facility,
  Investor,
  ApiError 
} from '../../types/api';

// Form validation schema
const feePaymentSchema = z.object({
  facilityId: z.number().min(1, 'Facility selection is required'),
  feeType: z.enum([
    'MANAGEMENT_FEE',
    'ARRANGEMENT_FEE', 
    'COMMITMENT_FEE',
    'TRANSACTION_FEE',
    'LATE_FEE',
    'AGENT_FEE',
    'OTHER_FEE'
  ], { required_error: 'Fee type is required' }),
  recipientType: z.enum(['BANK', 'INVESTOR'], { required_error: 'Recipient type is required' }),
  recipientId: z.number().min(1, 'Recipient selection is required'),
  feeAmount: z.number().min(0.01, 'Fee amount must be greater than 0'),
  calculationBase: z.number().min(0, 'Calculation base must be 0 or greater'),
  feeRate: z.number().min(0, 'Fee rate must be 0 or greater'),
  currency: z.string().min(1, 'Currency is required'),
  feeDate: z.string().min(1, 'Fee date is required'),
  description: z.string().min(1, 'Description is required'),
}).refine((data) => {
  // Validate fee type and recipient type combination
  return validateFeeTypeRecipient(data.feeType, data.recipientType);
}, {
  message: 'Invalid recipient type for this fee type',
  path: ['recipientType']
}).refine((data) => {
  // Validate fee calculation consistency
  if (data.calculationBase > 0 && data.feeRate > 0) {
    return validateFeeCalculation(data.calculationBase, data.feeRate, data.feeAmount);
  }
  return true;
}, {
  message: 'Fee calculation mismatch: calculationBase × feeRate should equal feeAmount',
  path: ['feeAmount']
});

type FeePaymentFormData = z.infer<typeof feePaymentSchema>;

const defaultValues: FeePaymentFormData = {
  facilityId: 0,
  feeType: 'MANAGEMENT_FEE',
  recipientType: 'BANK',
  recipientId: 0,
  feeAmount: 0,
  calculationBase: 0,
  feeRate: 0,
  currency: 'USD',
  feeDate: new Date().toISOString().split('T')[0],
  description: '',
};

interface FeePaymentFormProps {
  onSuccess?: (feePayment: FeePayment) => void;
  onCancel?: () => void;
}

const FeePaymentForm: React.FC<FeePaymentFormProps> = ({ onSuccess, onCancel }) => {
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [facilities, setFacilities] = useState<Facility[]>([]);
  const [investors, setInvestors] = useState<Investor[]>([]);
  const [isLoadingFacilities, setIsLoadingFacilities] = useState(false);
  const [isLoadingInvestors, setIsLoadingInvestors] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
    setValue,
    watch,
    reset,
  } = useForm<FeePaymentFormData>({
    resolver: zodResolver(feePaymentSchema),
    defaultValues,
    mode: 'onChange'
  });

  const watchedValues = watch();

  // Load facilities on component mount
  useEffect(() => {
    const loadFacilities = async () => {
      setIsLoadingFacilities(true);
      try {
        const response = await facilityApi.getAll();
        setFacilities(response.data.content);
      } catch (error) {
        console.error('Failed to load facilities:', error);
      } finally {
        setIsLoadingFacilities(false);
      }
    };
    loadFacilities();
  }, []);

  // Load investors when recipient type is INVESTOR
  useEffect(() => {
    const loadInvestors = async () => {
      if (watchedValues.recipientType === 'INVESTOR') {
        setIsLoadingInvestors(true);
        try {
          const response = await investorApi.getAll();
          setInvestors(response.data.content);
        } catch (error) {
          console.error('Failed to load investors:', error);
        } finally {
          setIsLoadingInvestors(false);
        }
      }
    };
    loadInvestors();
  }, [watchedValues.recipientType]);

  // Auto-calculate fee amount when base and rate change
  useEffect(() => {
    if (watchedValues.calculationBase > 0 && watchedValues.feeRate > 0) {
      const calculatedAmount = watchedValues.calculationBase * (watchedValues.feeRate / 100);
      setValue('feeAmount', Number(calculatedAmount.toFixed(2)));
    }
  }, [watchedValues.calculationBase, watchedValues.feeRate, setValue]);

  // Update currency when facility changes
  useEffect(() => {
    if (watchedValues.facilityId > 0) {
      const selectedFacility = facilities.find(f => f.id === watchedValues.facilityId);
      if (selectedFacility) {
        setValue('currency', selectedFacility.currency);
      }
    }
  }, [watchedValues.facilityId, facilities, setValue]);

  // Reset recipient when fee type changes
  useEffect(() => {
    const allowedRecipients = getFeeTypeRecipientRestrictions(watchedValues.feeType);
    if (allowedRecipients.length === 1) {
      setValue('recipientType', allowedRecipients[0]);
    }
    setValue('recipientId', 0); // Reset recipient selection
  }, [watchedValues.feeType, setValue]);

  const onSubmit = async (data: FeePaymentFormData) => {
    setIsSubmitting(true);
    setSubmitError(null);

    try {
      const createRequest: CreateFeePaymentRequest = {
        facilityId: data.facilityId,
        feeType: data.feeType,
        recipientType: data.recipientType,
        recipientId: data.recipientId,
        feeAmount: data.feeAmount,
        calculationBase: data.calculationBase,
        feeRate: data.feeRate,
        currency: data.currency,
        feeDate: data.feeDate,
        description: data.description,
      };

      const response = await feePaymentApi.create(createRequest);
      const feePayment = response.data;

      reset();
      onSuccess?.(feePayment);
    } catch (error) {
      const apiError = error as ApiError;
      setSubmitError(apiError.message || 'An error occurred while creating fee payment');
    } finally {
      setIsSubmitting(false);
    }
  };

  const formatCurrency = (amount: number, currency: string = 'USD') => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: currency,
      minimumFractionDigits: 0,
    }).format(amount);
  };

  const getSelectedFacility = () => {
    return facilities.find(f => f.id === watchedValues.facilityId);
  };

  const getAllowedRecipientTypes = () => {
    return getFeeTypeRecipientRestrictions(watchedValues.feeType);
  };

  const getRecipientOptions = () => {
    if (watchedValues.recipientType === 'INVESTOR') {
      return investors.map(investor => ({
        value: investor.id,
        label: `${investor.name} (${investor.investorType})`,
      }));
    }
    return []; // For BANK type, we'll need to implement bank selection
  };

  const feeTypeOptions = getFeeTypeOptions();
  const recipientTypeOptions = getRecipientTypeOptions()
    .filter(option => getAllowedRecipientTypes().includes(option.value));

  return (
    <div className="bg-primary-900 border border-secondary-500 rounded-xl p-6">
      <div className="mb-6">
        <h2 className="text-xl font-bold text-white mb-2">Create Fee Payment</h2>
        <p className="text-accent-400 text-sm">
          Specify fee type, amount, and recipient to create a fee payment
        </p>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
        {/* Facility Selection */}
        <div>
          <label htmlFor="facilityId" className="block text-sm font-medium text-white mb-2">
            Facility <span className="text-error">*</span>
          </label>
          <select
            {...register('facilityId', { valueAsNumber: true })}
            id="facilityId"
            className="w-full px-4 py-3 bg-secondary-600 border border-secondary-500 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-accent-500 focus:border-transparent"
          >
            <option value={0}>Select a facility</option>
            {facilities.map(facility => (
              <option key={facility.id} value={facility.id}>
                #{facility.id} - {facility.currency} {formatCurrency(facility.commitment, facility.currency)}
              </option>
            ))}
          </select>
          {errors.facilityId && (
            <p className="mt-1 text-sm text-error">{errors.facilityId.message}</p>
          )}
          {isLoadingFacilities && (
            <p className="mt-1 text-sm text-accent-400">Loading facilities...</p>
          )}
        </div>

        {/* Fee Type Selection */}
        <div>
          <label htmlFor="feeType" className="block text-sm font-medium text-white mb-2">
            Fee Type <span className="text-error">*</span>
          </label>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
            {feeTypeOptions.map(option => (
              <div
                key={option.value}
                className={`p-3 border rounded-lg cursor-pointer transition-all ${
                  watchedValues.feeType === option.value
                    ? 'border-accent-500 bg-accent-500/10'
                    : 'border-secondary-500 hover:border-accent-500/50'
                }`}
                onClick={() => setValue('feeType', option.value)}
              >
                <div className="flex items-center gap-3">
                  <input
                    type="radio"
                    {...register('feeType')}
                    value={option.value}
                    checked={watchedValues.feeType === option.value}
                    className="text-accent-500 focus:ring-accent-500"
                    readOnly
                  />
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2">
                      <span className="text-white font-medium text-sm">{option.label}</span>
                      <span className={`px-2 py-1 text-xs rounded-full ${getFeeTypeColor(option.value)}`}>
                        {option.value}
                      </span>
                    </div>
                    <p className="text-accent-400 text-xs mt-1 line-clamp-2">
                      {option.description}
                    </p>
                  </div>
                </div>
              </div>
            ))}
          </div>
          {errors.feeType && (
            <p className="mt-1 text-sm text-error">{errors.feeType.message}</p>
          )}
        </div>

        {/* Recipient Type and Selection */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div>
            <label htmlFor="recipientType" className="block text-sm font-medium text-white mb-2">
              Recipient Type <span className="text-error">*</span>
            </label>
            <select
              {...register('recipientType')}
              id="recipientType"
              className="w-full px-4 py-3 bg-secondary-600 border border-secondary-500 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-accent-500 focus:border-transparent"
            >
              {recipientTypeOptions.map(option => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
            {errors.recipientType && (
              <p className="mt-1 text-sm text-error">{errors.recipientType.message}</p>
            )}
          </div>

          <div>
            <label htmlFor="recipientId" className="block text-sm font-medium text-white mb-2">
              Recipient <span className="text-error">*</span>
            </label>
            <select
              {...register('recipientId', { valueAsNumber: true })}
              id="recipientId"
              className="w-full px-4 py-3 bg-secondary-600 border border-secondary-500 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-accent-500 focus:border-transparent"
            >
              <option value={0}>Select recipient</option>
              {getRecipientOptions().map(option => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
            {errors.recipientId && (
              <p className="mt-1 text-sm text-error">{errors.recipientId.message}</p>
            )}
            {isLoadingInvestors && watchedValues.recipientType === 'INVESTOR' && (
              <p className="mt-1 text-sm text-accent-400">Loading investors...</p>
            )}
          </div>
        </div>

        {/* Fee Calculation */}
        <div className="bg-secondary-600 rounded-lg p-4">
          <h3 className="text-white font-medium mb-4">Fee Calculation</h3>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div>
              <label htmlFor="calculationBase" className="block text-sm font-medium text-white mb-2">
                Calculation Base
              </label>
              <input
                {...register('calculationBase', { valueAsNumber: true })}
                type="number"
                id="calculationBase"
                step="0.01"
                placeholder="e.g., 1000000"
                className="w-full px-4 py-3 bg-secondary-700 border border-secondary-500 rounded-lg text-white placeholder:text-accent-400 focus:outline-none focus:ring-2 focus:ring-accent-500 focus:border-transparent"
              />
              {errors.calculationBase && (
                <p className="mt-1 text-sm text-error">{errors.calculationBase.message}</p>
              )}
            </div>

            <div>
              <label htmlFor="feeRate" className="block text-sm font-medium text-white mb-2">
                Fee Rate (%)
              </label>
              <input
                {...register('feeRate', { valueAsNumber: true })}
                type="number"
                id="feeRate"
                step="0.01"
                placeholder="e.g., 1.5"
                className="w-full px-4 py-3 bg-secondary-700 border border-secondary-500 rounded-lg text-white placeholder:text-accent-400 focus:outline-none focus:ring-2 focus:ring-accent-500 focus:border-transparent"
              />
              {errors.feeRate && (
                <p className="mt-1 text-sm text-error">{errors.feeRate.message}</p>
              )}
            </div>

            <div>
              <label htmlFor="feeAmount" className="block text-sm font-medium text-white mb-2">
                Fee Amount <span className="text-error">*</span>
              </label>
              <input
                {...register('feeAmount', { valueAsNumber: true })}
                type="number"
                id="feeAmount"
                step="0.01"
                placeholder="e.g., 15000"
                className="w-full px-4 py-3 bg-secondary-700 border border-secondary-500 rounded-lg text-white placeholder:text-accent-400 focus:outline-none focus:ring-2 focus:ring-accent-500 focus:border-transparent"
              />
              {errors.feeAmount && (
                <p className="mt-1 text-sm text-error">{errors.feeAmount.message}</p>
              )}
            </div>
          </div>
          {watchedValues.calculationBase > 0 && watchedValues.feeRate > 0 && (
            <div className="mt-3 p-3 bg-accent-500/10 border border-accent-500/30 rounded-lg">
              <div className="text-sm text-accent-400">
                Calculated: {formatCurrency(watchedValues.calculationBase * (watchedValues.feeRate / 100), watchedValues.currency)}
              </div>
            </div>
          )}
        </div>

        {/* Fee Date and Description */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div>
            <label htmlFor="feeDate" className="block text-sm font-medium text-white mb-2">
              Fee Date <span className="text-error">*</span>
            </label>
            <input
              {...register('feeDate')}
              type="date"
              id="feeDate"
              max={new Date().toISOString().split('T')[0]}
              className="w-full px-4 py-3 bg-secondary-600 border border-secondary-500 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-accent-500 focus:border-transparent"
            />
            {errors.feeDate && (
              <p className="mt-1 text-sm text-error">{errors.feeDate.message}</p>
            )}
          </div>

          <div>
            <label htmlFor="currency" className="block text-sm font-medium text-white mb-2">
              Currency <span className="text-error">*</span>
            </label>
            <input
              {...register('currency')}
              type="text"
              id="currency"
              className="w-full px-4 py-3 bg-secondary-600 border border-secondary-500 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-accent-500 focus:border-transparent"
              readOnly
            />
            {errors.currency && (
              <p className="mt-1 text-sm text-error">{errors.currency.message}</p>
            )}
          </div>
        </div>

        {/* Description */}
        <div>
          <label htmlFor="description" className="block text-sm font-medium text-white mb-2">
            Description <span className="text-error">*</span>
          </label>
          <textarea
            {...register('description')}
            id="description"
            rows={3}
            placeholder="Enter detailed description of the fee"
            className="w-full px-4 py-3 bg-secondary-600 border border-secondary-500 rounded-lg text-white placeholder:text-accent-400 focus:outline-none focus:ring-2 focus:ring-accent-500 focus:border-transparent"
          />
          {errors.description && (
            <p className="mt-1 text-sm text-error">{errors.description.message}</p>
          )}
        </div>

        {/* Submit Error */}
        {submitError && (
          <div className="p-3 bg-error/10 border border-error/20 rounded-lg">
            <p className="text-error text-sm">{submitError}</p>
          </div>
        )}

        {/* Form Actions */}
        <div className="flex gap-4">
          <button
            type="submit"
            disabled={isSubmitting}
            className="flex-1 bg-accent-500 hover:bg-accent-400 disabled:opacity-50 disabled:cursor-not-allowed text-white font-semibold py-3 px-6 rounded-lg transition-colors duration-200"
          >
            {isSubmitting ? 'Creating...' : 'Create Fee Payment'}
          </button>
          
          {onCancel && (
            <button
              type="button"
              onClick={onCancel}
              className="bg-secondary-600 hover:bg-secondary-500 text-white font-semibold py-3 px-6 rounded-lg transition-colors duration-200"
            >
              Cancel
            </button>
          )}
        </div>
      </form>
    </div>
  );
};

export default FeePaymentForm;