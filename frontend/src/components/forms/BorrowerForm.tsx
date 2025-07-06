import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { createBorrowerSchema, type CreateBorrowerFormData, creditRatingOptions, defaultBorrowerValues } from '../../schemas/borrower';
import { borrowerApi } from '../../lib/api';
import type { ApiError, Borrower, CreateBorrowerRequest, UpdateBorrowerRequest } from '../../types/api';

interface BorrowerFormProps {
  onSuccess?: (borrower: Borrower) => void;
  onCancel?: () => void;
  editData?: Borrower; // 編集モード用のデータ
  mode?: 'create' | 'edit'; // モード指定
}

const BorrowerForm: React.FC<BorrowerFormProps> = ({ onSuccess, onCancel, editData, mode = 'create' }) => {
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);

  // 編集モード用のデフォルト値を生成
  const getInitialValues = (): CreateBorrowerFormData => {
    if (mode === 'edit' && editData) {
      return {
        name: editData.name,
        email: editData.email,
        phoneNumber: editData.phoneNumber,
        companyId: editData.companyId || '',
        creditLimit: editData.creditLimit,
        creditRating: editData.creditRating
      };
    }
    return defaultBorrowerValues;
  };

  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
  } = useForm<CreateBorrowerFormData>({
    resolver: zodResolver(createBorrowerSchema),
    defaultValues: getInitialValues(),
  });

  const onSubmit = async (data: CreateBorrowerFormData) => {
    setIsSubmitting(true);
    setSubmitError(null);

    try {
      let response;
      
      if (mode === 'edit' && editData) {
        // 編集モード: update API呼び出し
        const updateRequest: UpdateBorrowerRequest = {
          name: data.name,
          email: data.email,
          phoneNumber: data.phoneNumber,
          companyId: data.companyId,
          creditLimit: data.creditLimit,
          creditRating: data.creditRating,
          version: editData.version // 楽観的ロック用
        };
        response = await borrowerApi.update(editData.id, updateRequest);
      } else {
        // 新規作成モード: create API呼び出し
        const createRequest: CreateBorrowerRequest = {
          name: data.name,
          email: data.email,
          phoneNumber: data.phoneNumber,
          companyId: data.companyId,
          creditLimit: data.creditLimit,
          creditRating: data.creditRating
        };
        response = await borrowerApi.create(createRequest);
      }
      
      const borrower = response.data;
      reset();
      onSuccess?.(borrower);
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
          {mode === 'edit' ? 'Edit Borrower' : 'New Borrower Registration'}
        </h2>
        <p className="text-accent-400 text-sm">
          {mode === 'edit' ? 'Update the borrower information' : 'Enter the borrower\'s basic information'}
        </p>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
        {/* Company Name */}
        <div>
          <label htmlFor="name" className="block text-sm font-medium text-white mb-2">
            Company Name <span className="text-error">*</span>
          </label>
          <input
            {...register('name')}
            type="text"
            id="name"
            placeholder="Enter company name"
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

        {/* Credit Limit */}
        <div>
          <label htmlFor="creditLimit" className="block text-sm font-medium text-white mb-2">
            Credit Limit <span className="text-error">*</span>
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
            <span className="absolute right-4 top-3 text-accent-400 text-sm">JPY</span>
          </div>
          {errors.creditLimit && (
            <p className="mt-1 text-sm text-error">{errors.creditLimit.message}</p>
          )}
        </div>

        {/* Credit Rating */}
        <div>
          <label htmlFor="creditRating" className="block text-sm font-medium text-white mb-2">
            Credit Rating <span className="text-error">*</span>
          </label>
          <select
            {...register('creditRating')}
            id="creditRating"
            className="w-full px-4 py-3 bg-secondary-600 border border-secondary-500 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-accent-500 focus:border-transparent"
          >
            <option value="">Select credit rating</option>
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

export default BorrowerForm;