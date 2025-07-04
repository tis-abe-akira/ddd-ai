import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { createFacilitySchema, type CreateFacilityFormData, defaultFacilityValues, FACILITY_FORM_STEPS, currencyOptions } from '../../schemas/facility';
import SyndicateSelect from '../facility/SyndicateSelect';
import SharePieAllocation from '../facility/SharePieAllocation';
import { facilityApi } from '../../lib/api';
import type { ApiError, Facility, CreateFacilityRequest } from '../../types/api';

interface FacilityFormProps {
  onSuccess?: (facility: Facility) => void;
  onCancel?: () => void;
}

const FacilityForm: React.FC<FacilityFormProps> = ({ onSuccess, onCancel }) => {
  const [currentStep, setCurrentStep] = useState(1);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    formState: { errors },
    setValue,
    getValues,
    watch,
    reset,
  } = useForm<CreateFacilityFormData>({
    resolver: zodResolver(createFacilitySchema),
    defaultValues: defaultFacilityValues,
    mode: 'onChange'
  });

  const watchedValues = watch();

  const validateCurrentStep = () => {
    const values = getValues();
    switch (currentStep) {
      case 1:
        return values.syndicateId !== undefined;
      case 2:
        return values.commitment && values.currency && values.startDate && values.endDate && values.interestTerms;
      case 3:
        return values.sharePies && values.sharePies.length > 0;
      default:
        return true;
    }
  };

  const nextStep = () => {
    if (validateCurrentStep() && currentStep < 4) {
      setCurrentStep(currentStep + 1);
    }
  };

  const prevStep = () => {
    if (currentStep > 1) {
      setCurrentStep(currentStep - 1);
    }
  };

  const onSubmit = async (data: CreateFacilityFormData) => {
    setIsSubmitting(true);
    setSubmitError(null);

    try {
      const facilityRequest: CreateFacilityRequest = {
        syndicateId: data.syndicateId,
        commitment: data.commitment,
        currency: data.currency,
        startDate: data.startDate,
        endDate: data.endDate,
        interestTerms: data.interestTerms,
        sharePies: data.sharePies.map(pie => ({
          investorId: pie.investorId,
          share: pie.share
        }))
      };
      
      const response = await facilityApi.create(facilityRequest);
      const newFacility = response.data;
      
      reset();
      setCurrentStep(1);
      onSuccess?.(newFacility);
    } catch (error) {
      const apiError = error as ApiError;
      setSubmitError(apiError.message || 'An error occurred');
    } finally {
      setIsSubmitting(false);
    }
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('ja-JP', {
      style: 'currency',
      currency: watchedValues.currency || 'USD',
      minimumFractionDigits: 0,
    }).format(amount);
  };

  const renderStepContent = () => {
    switch (currentStep) {
      case 1:
        return (
          <div className="space-y-6">
            <div>
              <h3 className="text-lg font-semibold text-white mb-2">Syndicate Selection</h3>
              <p className="text-accent-400 text-sm mb-6">Select the syndicate for this facility</p>
              
              <SyndicateSelect
                value={watchedValues.syndicateId}
                onChange={(syndicateId) => setValue('syndicateId', syndicateId)}
                error={errors.syndicateId?.message}
              />
            </div>
          </div>
        );

      case 2:
        return (
          <div className="space-y-6">
            <div>
              <h3 className="text-lg font-semibold text-white mb-2">Facility Basic Information</h3>
              <p className="text-accent-400 text-sm mb-6">Set the basic conditions for the credit facility</p>
              
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                {/* Commitment Amount */}
                <div>
                  <label htmlFor="commitment" className="block text-sm font-medium text-white mb-2">
                    Facility Amount <span className="text-error">*</span>
                  </label>
                  <input
                    {...register('commitment', { valueAsNumber: true })}
                    type="number"
                    id="commitment"
                    placeholder="e.g., 5000000"
                    className="w-full px-4 py-3 bg-secondary-600 border border-secondary-500 rounded-lg text-white placeholder:text-accent-400 focus:outline-none focus:ring-2 focus:ring-accent-500 focus:border-transparent"
                  />
                  {errors.commitment && (
                    <p className="mt-1 text-sm text-error">{errors.commitment.message}</p>
                  )}
                </div>

                {/* Currency */}
                <div>
                  <label htmlFor="currency" className="block text-sm font-medium text-white mb-2">
                    Currency <span className="text-error">*</span>
                  </label>
                  <select
                    {...register('currency')}
                    id="currency"
                    className="w-full px-4 py-3 bg-secondary-600 border border-secondary-500 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-accent-500 focus:border-transparent"
                  >
                    {currencyOptions.map((option) => (
                      <option key={option.value} value={option.value}>
                        {option.label}
                      </option>
                    ))}
                  </select>
                  {errors.currency && (
                    <p className="mt-1 text-sm text-error">{errors.currency.message}</p>
                  )}
                </div>

                {/* Start Date */}
                <div>
                  <label htmlFor="startDate" className="block text-sm font-medium text-white mb-2">
                    Start Date <span className="text-error">*</span>
                  </label>
                  <input
                    {...register('startDate')}
                    type="date"
                    id="startDate"
                    className="w-full px-4 py-3 bg-secondary-600 border border-secondary-500 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-accent-500 focus:border-transparent"
                  />
                  {errors.startDate && (
                    <p className="mt-1 text-sm text-error">{errors.startDate.message}</p>
                  )}
                </div>

                {/* End Date */}
                <div>
                  <label htmlFor="endDate" className="block text-sm font-medium text-white mb-2">
                    End Date <span className="text-error">*</span>
                  </label>
                  <input
                    {...register('endDate')}
                    type="date"
                    id="endDate"
                    className="w-full px-4 py-3 bg-secondary-600 border border-secondary-500 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-accent-500 focus:border-transparent"
                  />
                  {errors.endDate && (
                    <p className="mt-1 text-sm text-error">{errors.endDate.message}</p>
                  )}
                </div>
              </div>

              {/* Interest Terms */}
              <div className="mt-6">
                <label htmlFor="interestTerms" className="block text-sm font-medium text-white mb-2">
                  Interest Terms <span className="text-error">*</span>
                </label>
                <input
                  {...register('interestTerms')}
                  type="text"
                  id="interestTerms"
                  placeholder="e.g., LIBOR + 2%"
                  className="w-full px-4 py-3 bg-secondary-600 border border-secondary-500 rounded-lg text-white placeholder:text-accent-400 focus:outline-none focus:ring-2 focus:ring-accent-500 focus:border-transparent"
                />
                {errors.interestTerms && (
                  <p className="mt-1 text-sm text-error">{errors.interestTerms.message}</p>
                )}
              </div>
            </div>
          </div>
        );

      case 3:
        return (
          <div className="space-y-6">
            <div>
              <h3 className="text-lg font-semibold text-white mb-2">Investor Share Allocation</h3>
              <p className="text-accent-400 text-sm mb-6">Set the share percentage for each investor (total 100%)</p>
              
              <SharePieAllocation
                value={watchedValues.sharePies || []}
                onChange={(sharePies) => setValue('sharePies', sharePies)}
                syndicateId={watchedValues.syndicateId}
                error={errors.sharePies?.message}
              />
            </div>
          </div>
        );

      case 4:
        return (
          <div className="space-y-6">
            <div>
              <h3 className="text-lg font-semibold text-white mb-2">Confirmation</h3>
              <p className="text-accent-400 text-sm mb-6">Review the details and create the facility</p>
              
              <div className="bg-secondary-600 rounded-lg p-6 space-y-4">
                <div>
                  <div className="text-sm text-accent-400">Syndicate ID</div>
                  <div className="text-white font-medium">{watchedValues.syndicateId}</div>
                </div>
                
                <div>
                  <div className="text-sm text-accent-400">Facility Amount</div>
                  <div className="text-white font-medium text-lg">
                    {watchedValues.commitment ? formatCurrency(watchedValues.commitment) : 'Not set'}
                  </div>
                </div>
                
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <div className="text-sm text-accent-400">Start Date</div>
                    <div className="text-white font-medium">{watchedValues.startDate}</div>
                  </div>
                  <div>
                    <div className="text-sm text-accent-400">End Date</div>
                    <div className="text-white font-medium">{watchedValues.endDate}</div>
                  </div>
                </div>
                
                <div>
                  <div className="text-sm text-accent-400">Interest Terms</div>
                  <div className="text-white font-medium">{watchedValues.interestTerms}</div>
                </div>
                
                <div>
                  <div className="text-sm text-accent-400">Investor Share Allocation</div>
                  <div className="space-y-2 mt-2">
                    {watchedValues.sharePies?.map((pie, index) => (
                      <div key={index} className="flex justify-between items-center bg-primary-900 rounded p-2">
                        <span className="text-white">Investor ID: {pie.investorId}</span>
                        <span className="text-accent-500 font-medium">{Math.round(pie.share * 100)}%</span>
                      </div>
                    )) || []}
                    <div className="border-t border-secondary-500 pt-2 flex justify-between items-center font-bold">
                      <span className="text-white">Total</span>
                      <span className="text-success">
                        {Math.round((watchedValues.sharePies?.reduce((sum, pie) => sum + pie.share, 0) || 0) * 100)}%
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        );

      default:
        return null;
    }
  };

  const isStepValid = validateCurrentStep();

  return (
    <div className="bg-primary-900 border border-secondary-500 rounded-xl p-6">
      {/* Step Indicator */}
      <div className="mb-8">
        <div className="flex items-center justify-between">
          {FACILITY_FORM_STEPS.map((step, index) => (
            <div key={step.id} className="flex items-center">
              <div className={`w-8 h-8 rounded-full flex items-center justify-center text-sm font-medium ${
                currentStep === step.id
                  ? 'bg-accent-500 text-white'
                  : currentStep > step.id
                  ? 'bg-success text-white'
                  : 'bg-secondary-600 text-accent-400'
              }`}>
                {currentStep > step.id ? (
                  <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
                    <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                  </svg>
                ) : (
                  step.id
                )}
              </div>
              
              {index < FACILITY_FORM_STEPS.length - 1 && (
                <div className={`w-16 h-1 mx-2 ${
                  currentStep > step.id ? 'bg-success' : 'bg-secondary-600'
                }`} />
              )}
            </div>
          ))}
        </div>
        
        <div className="mt-4">
          <h2 className="text-xl font-bold text-white">
            {FACILITY_FORM_STEPS[currentStep - 1].title}
          </h2>
          <p className="text-accent-400 text-sm">
            {FACILITY_FORM_STEPS[currentStep - 1].description}
          </p>
        </div>
      </div>

      <form onSubmit={handleSubmit(onSubmit)}>
        {/* Step Content */}
        <div className="mb-8">
          {renderStepContent()}
        </div>

        {/* Submit Error */}
        {submitError && (
          <div className="mb-6 p-3 bg-error/10 border border-error/20 rounded-lg">
            <p className="text-error text-sm">{submitError}</p>
          </div>
        )}

        {/* Navigation Buttons */}
        <div className="flex gap-4">
          {currentStep > 1 && (
            <button
              type="button"
              onClick={prevStep}
              className="flex-1 bg-secondary-600 hover:bg-secondary-500 text-white font-semibold py-3 px-6 rounded-lg transition-colors duration-200"
            >
              Back
            </button>
          )}
          
          {currentStep < 4 ? (
            <button
              type="button"
              onClick={nextStep}
              disabled={!isStepValid}
              className="flex-1 bg-accent-500 hover:bg-accent-400 disabled:opacity-50 disabled:cursor-not-allowed text-white font-semibold py-3 px-6 rounded-lg transition-colors duration-200"
            >
              Next
            </button>
          ) : (
            <button
              type="submit"
              disabled={isSubmitting || !isStepValid}
              className="flex-1 bg-success hover:bg-success/80 disabled:opacity-50 disabled:cursor-not-allowed text-white font-semibold py-3 px-6 rounded-lg transition-colors duration-200"
            >
              {isSubmitting ? 'Creating Facility...' : 'Create Facility'}
            </button>
          )}
          
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

export default FacilityForm;