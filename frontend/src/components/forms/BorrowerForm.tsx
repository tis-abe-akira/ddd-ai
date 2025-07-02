import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { createBorrowerSchema, type CreateBorrowerFormData, creditRatingOptions, defaultBorrowerValues } from '../../schemas/borrower';
import { borrowerApi } from '../../lib/api';
import type { ApiError } from '../../types/api';

interface BorrowerFormProps {
  onSuccess?: (borrower: any) => void;
  onCancel?: () => void;
}

const BorrowerForm: React.FC<BorrowerFormProps> = ({ onSuccess, onCancel }) => {
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
  } = useForm<CreateBorrowerFormData>({
    resolver: zodResolver(createBorrowerSchema),
    defaultValues: defaultBorrowerValues,
  });

  const onSubmit = async (data: CreateBorrowerFormData) => {
    setIsSubmitting(true);
    setSubmitError(null);

    try {
      const response = await borrowerApi.create(data);
      const borrower = response.data;
      
      reset();
      onSuccess?.(borrower);
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
        <h2 className="text-xl font-bold text-white mb-2">新規借り手登録</h2>
        <p className="text-accent-400 text-sm">借り手の基本情報を入力してください</p>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
        {/* Company Name */}
        <div>
          <label htmlFor="name" className="block text-sm font-medium text-white mb-2">
            会社名 <span className="text-error">*</span>
          </label>
          <input
            {...register('name')}
            type="text"
            id="name"
            placeholder="会社名を入力してください"
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

        {/* Credit Limit */}
        <div>
          <label htmlFor="creditLimit" className="block text-sm font-medium text-white mb-2">
            信用限度額 <span className="text-error">*</span>
          </label>
          <div className="relative">
            <input
              {...register('creditLimit', {
                valueAsNumber: true,
              })}
              type="number"
              id="creditLimit"
              placeholder="100000000"
              min="1"
              max="1000000000"
              step="1"
              className="w-full px-4 py-3 bg-secondary-600 border border-secondary-500 rounded-lg text-white placeholder:text-accent-400 focus:outline-none focus:ring-2 focus:ring-accent-500 focus:border-transparent"
            />
            <span className="absolute right-4 top-3 text-accent-400 text-sm">円</span>
          </div>
          {errors.creditLimit && (
            <p className="mt-1 text-sm text-error">{errors.creditLimit.message}</p>
          )}
        </div>

        {/* Credit Rating */}
        <div>
          <label htmlFor="creditRating" className="block text-sm font-medium text-white mb-2">
            信用格付け <span className="text-error">*</span>
          </label>
          <select
            {...register('creditRating')}
            id="creditRating"
            className="w-full px-4 py-3 bg-secondary-600 border border-secondary-500 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-accent-500 focus:border-transparent"
          >
            <option value="">格付けを選択してください</option>
            {creditRatingOptions.map((rating) => (
              <option key={rating} value={rating} className="bg-secondary-600 text-white">
                {rating}
              </option>
            ))}
          </select>
          {errors.creditRating && (
            <p className="mt-1 text-sm text-error">{errors.creditRating.message}</p>
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
            className="flex-1 bg-accent-500 hover:bg-accent-400 disabled:opacity-50 disabled:cursor-not-allowed text-primary-900 font-semibold py-3 px-6 rounded-lg transition-colors duration-200"
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

export default BorrowerForm;