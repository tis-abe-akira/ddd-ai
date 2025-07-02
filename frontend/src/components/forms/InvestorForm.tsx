import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { createInvestorSchema, type CreateInvestorFormData, investorTypeOptions, investorTypeLabels, defaultInvestorValues } from '../../schemas/investor';
import { investorApi } from '../../lib/api';
import type { ApiError } from '../../types/api';

interface InvestorFormProps {
  onSuccess?: (investor: any) => void;
  onCancel?: () => void;
}

const InvestorForm: React.FC<InvestorFormProps> = ({ onSuccess, onCancel }) => {
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
  } = useForm<CreateInvestorFormData>({
    resolver: zodResolver(createInvestorSchema),
    defaultValues: defaultInvestorValues,
  });

  const onSubmit = async (data: CreateInvestorFormData) => {
    setIsSubmitting(true);
    setSubmitError(null);

    try {
      const response = await investorApi.create(data);
      const investor = response.data;
      
      reset();
      onSuccess?.(investor);
    } catch (error) {
      const apiError = error as ApiError;
      setSubmitError(apiError.message || 'エラーが発生しました');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="bg-primary-900 border border-secondary-500 rounded-xl p-6">
      <div className="mb-6">
        <h2 className="text-xl font-bold text-white mb-2">新規投資家登録</h2>
        <p className="text-accent-400 text-sm">投資家の基本情報を入力してください</p>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
        {/* Investor Name */}
        <div>
          <label htmlFor="name" className="block text-sm font-medium text-white mb-2">
            投資家名 <span className="text-error">*</span>
          </label>
          <input
            {...register('name')}
            type="text"
            id="name"
            placeholder="投資家名を入力してください"
            className="w-full px-4 py-3 bg-secondary-600 border border-secondary-500 rounded-lg text-white placeholder:text-accent-400 focus:outline-none focus:ring-2 focus:ring-accent-500 focus:border-transparent"
          />
          {errors.name && (
            <p className="mt-1 text-sm text-error">{errors.name.message}</p>
          )}
        </div>

        {/* Email */}
        <div>
          <label htmlFor="email" className="block text-sm font-medium text-white mb-2">
            メールアドレス <span className="text-error">*</span>
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
            電話番号 <span className="text-error">*</span>
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
            会社ID（任意）
          </label>
          <input
            {...register('companyId')}
            type="text"
            id="companyId"
            placeholder="COMP001（任意）"
            className="w-full px-4 py-3 bg-secondary-600 border border-secondary-500 rounded-lg text-white placeholder:text-accent-400 focus:outline-none focus:ring-2 focus:ring-accent-500 focus:border-transparent"
          />
          {errors.companyId && (
            <p className="mt-1 text-sm text-error">{errors.companyId.message}</p>
          )}
        </div>

        {/* Investment Capacity */}
        <div>
          <label htmlFor="investmentCapacity" className="block text-sm font-medium text-white mb-2">
            投資能力 <span className="text-error">*</span>
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
            <span className="absolute right-4 top-3 text-accent-400 text-sm">円</span>
          </div>
          {errors.investmentCapacity && (
            <p className="mt-1 text-sm text-error">{errors.investmentCapacity.message}</p>
          )}
        </div>

        {/* Investor Type */}
        <div>
          <label htmlFor="investorType" className="block text-sm font-medium text-white mb-2">
            投資家タイプ <span className="text-error">*</span>
          </label>
          <select
            {...register('investorType')}
            id="investorType"
            className="w-full px-4 py-3 bg-secondary-600 border border-secondary-500 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-accent-500 focus:border-transparent"
          >
            <option value="">タイプを選択してください</option>
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
            {isSubmitting ? '登録中...' : '登録する'}
          </button>
          
          {onCancel && (
            <button
              type="button"
              onClick={onCancel}
              className="flex-1 bg-secondary-600 hover:bg-secondary-500 text-white font-semibold py-3 px-6 rounded-lg transition-colors duration-200"
            >
              キャンセル
            </button>
          )}
        </div>
      </form>
    </div>
  );
};

export default InvestorForm;