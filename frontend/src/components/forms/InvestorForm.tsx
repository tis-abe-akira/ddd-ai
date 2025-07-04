import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { createInvestorSchema, type CreateInvestorFormData, investorTypeOptions, investorTypeLabels, defaultInvestorValues } from '../../schemas/investor';
import { investorApi } from '../../lib/api';
import type { ApiError, Investor, CreateInvestorRequest, UpdateInvestorRequest } from '../../types/api';

interface InvestorFormProps {
  onSuccess?: (investor: Investor) => void;
  onCancel?: () => void;
  editData?: Investor; // 編集モード用のデータ
  mode?: 'create' | 'edit'; // モード指定
}

const InvestorForm: React.FC<InvestorFormProps> = ({ onSuccess, onCancel, editData, mode = 'create' }) => {
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);

  // 編集モード用のデフォルト値を生成
  const getInitialValues = (): CreateInvestorFormData => {
    if (mode === 'edit' && editData) {
      return {
        name: editData.name,
        email: editData.email,
        phoneNumber: editData.phoneNumber,
        companyId: editData.companyId || '',
        investmentCapacity: editData.investmentCapacity,
        investorType: editData.investorType
      };
    }
    return defaultInvestorValues;
  };

  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
  } = useForm<CreateInvestorFormData>({
    resolver: zodResolver(createInvestorSchema),
    defaultValues: getInitialValues(),
  });

  const onSubmit = async (data: CreateInvestorFormData) => {
    setIsSubmitting(true);
    setSubmitError(null);

    try {
      let response;
      
      if (mode === 'edit' && editData) {
        // 編集モード: update API呼び出し
        const updateRequest: UpdateInvestorRequest = {
          name: data.name,
          email: data.email,
          phoneNumber: data.phoneNumber,
          companyId: data.companyId,
          investmentCapacity: data.investmentCapacity,
          investorType: data.investorType,
          version: editData.version // 楽観的ロック用
        };
        response = await investorApi.update(editData.id, updateRequest);
      } else {
        // 新規作成モード: create API呼び出し
        const createRequest: CreateInvestorRequest = {
          name: data.name,
          email: data.email,
          phoneNumber: data.phoneNumber,
          companyId: data.companyId,
          investmentCapacity: data.investmentCapacity,
          investorType: data.investorType
        };
        response = await investorApi.create(createRequest);
      }
      
      const investor = response.data;
      reset();
      onSuccess?.(investor);
    } catch (error) {
      const apiError = error as ApiError;
      setSubmitError(apiError.message || 'An error occurred');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="bg-primary-900 border border-secondary-500 rounded-xl p-6">
      <div className="mb-6">
        <h2 className="text-xl font-bold text-white mb-2">
          {mode === 'edit' ? 'Edit Investor' : 'New Investor Registration'}
        </h2>
        <p className="text-accent-400 text-sm">
          {mode === 'edit' ? 'Update the investor information' : 'Enter the investor\'s basic information'}
        </p>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
        {/* Investor Name */}
        <div>
          <label htmlFor="name" className="block text-sm font-medium text-white mb-2">
            Investor Name <span className="text-error">*</span>
          </label>
          <input
            {...register('name')}
            type="text"
            id="name"
            placeholder="Enter investor name"
            className="w-full px-4 py-3 bg-secondary-600 border border-secondary-500 rounded-lg text-white placeholder:text-accent-400 focus:outline-none focus:ring-2 focus:ring-accent-500 focus:border-transparent"
          />
          {errors.name && (
            <p className="mt-1 text-sm text-error">{errors.name.message}</p>
          )}
        </div>

        {/* Email */}
        <div>
          <label htmlFor="email" className="block text-sm font-medium text-white mb-2">
            Email Address <span className="text-error">*</span>
          </label>
          <input
            {...register('email')}
            type="email"
            id="email"
            placeholder="example@company.com"
            className="w-full px-4 py-3 bg-secondary-600 border border-secondary-500 rounded-lg text-white placeholder:text-accent-400 focus:outline-none focus:ring-2 focus:ring-accent-500 focus:border-transparent"
          />
          {errors.email && (
            <p className="mt-1 text-sm text-error">{errors.email.message}</p>
          )}
        </div>

        {/* Phone Number */}
        <div>
          <label htmlFor="phoneNumber" className="block text-sm font-medium text-white mb-2">
            Phone Number <span className="text-error">*</span>
          </label>
          <input
            {...register('phoneNumber')}
            type="tel"
            id="phoneNumber"
            placeholder="03-1234-5678"
            className="w-full px-4 py-3 bg-secondary-600 border border-secondary-500 rounded-lg text-white placeholder:text-accent-400 focus:outline-none focus:ring-2 focus:ring-accent-500 focus:border-transparent"
          />
          {errors.phoneNumber && (
            <p className="mt-1 text-sm text-error">{errors.phoneNumber.message}</p>
          )}
        </div>

        {/* Company ID */}
        <div>
          <label htmlFor="companyId" className="block text-sm font-medium text-white mb-2">
            Company ID (Optional)
          </label>
          <input
            {...register('companyId')}
            type="text"
            id="companyId"
            placeholder="COMP001 (optional)"
            className="w-full px-4 py-3 bg-secondary-600 border border-secondary-500 rounded-lg text-white placeholder:text-accent-400 focus:outline-none focus:ring-2 focus:ring-accent-500 focus:border-transparent"
          />
          {errors.companyId && (
            <p className="mt-1 text-sm text-error">{errors.companyId.message}</p>
          )}
        </div>

        {/* Investment Capacity */}
        <div>
          <label htmlFor="investmentCapacity" className="block text-sm font-medium text-white mb-2">
            Investment Capacity <span className="text-error">*</span>
          </label>
          <div className="relative">
            <input
              {...register('investmentCapacity', {
                valueAsNumber: true,
              })}
              type="number"
              id="investmentCapacity"
              placeholder="1000000000"
              min="1"
              max="10000000000"
              step="1"
              className="w-full px-4 py-3 bg-secondary-600 border border-secondary-500 rounded-lg text-white placeholder:text-accent-400 focus:outline-none focus:ring-2 focus:ring-accent-500 focus:border-transparent"
            />
            <span className="absolute right-4 top-3 text-accent-400 text-sm">JPY</span>
          </div>
          {errors.investmentCapacity && (
            <p className="mt-1 text-sm text-error">{errors.investmentCapacity.message}</p>
          )}
        </div>

        {/* Investor Type */}
        <div>
          <label htmlFor="investorType" className="block text-sm font-medium text-white mb-2">
            Investor Type <span className="text-error">*</span>
          </label>
          <select
            {...register('investorType')}
            id="investorType"
            className="w-full px-4 py-3 bg-secondary-600 border border-secondary-500 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-accent-500 focus:border-transparent"
          >
            <option value="">Select investor type</option>
            {investorTypeOptions.map((type) => (
              <option key={type} value={type} className="bg-secondary-600 text-white">
                {investorTypeLabels[type]}
              </option>
            ))}
          </select>
          {errors.investorType && (
            <p className="mt-1 text-sm text-error">{errors.investorType.message}</p>
          )}
        </div>

        {/* Submit Error */}
        {submitError && (
          <div className="p-3 bg-error/10 border border-error/20 rounded-lg">
            <p className="text-error text-sm">{submitError}</p>
          </div>
        )}

        {/* Form Actions */}
        <div className="flex gap-4 pt-4">
          <button
            type="submit"
            disabled={isSubmitting}
            className="flex-1 bg-accent-500 hover:bg-accent-400 disabled:opacity-50 disabled:cursor-not-allowed text-white font-semibold py-3 px-6 rounded-lg transition-colors duration-200"
          >
            {isSubmitting 
              ? (mode === 'edit' ? 'Updating...' : 'Registering...') 
              : (mode === 'edit' ? 'Update' : 'Register')
            }
          </button>
          
          {onCancel && (
            <button
              type="button"
              onClick={onCancel}
              className="flex-1 bg-secondary-600 hover:bg-secondary-500 text-white font-semibold py-3 px-6 rounded-lg transition-colors duration-200"
            >
              Cancel
            </button>
          )}
        </div>
      </form>
    </div>
  );
};

export default InvestorForm;