import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { createSyndicateSchema, type CreateSyndicateFormData, defaultSyndicateValues, SYNDICATE_FORM_STEPS } from '../../schemas/syndicate';
import BorrowerSelect from '../syndicate/BorrowerSelect';
import InvestorCards from '../syndicate/InvestorCards';
import { syndicateApi } from '../../lib/api';
import type { ApiError, Syndicate, Borrower, Investor, CreateSyndicateRequest } from '../../types/api';

interface SyndicateFormProps {
  onSuccess?: (syndicate: Syndicate) => void;
  onCancel?: () => void;
}

const SyndicateForm: React.FC<SyndicateFormProps> = ({ onSuccess, onCancel }) => {
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
  } = useForm<CreateSyndicateFormData>({
    resolver: zodResolver(createSyndicateSchema),
    defaultValues: defaultSyndicateValues,
    mode: 'onChange'
  });

  const watchedValues = watch();

  const validateCurrentStep = () => {
    const values = getValues();
    switch (currentStep) {
      case 1:
        return values.name && values.name.trim().length > 0;
      case 2:
        return values.borrowerId !== undefined;
      case 3:
        return values.leadBankId !== undefined;
      case 4:
        return values.memberInvestorIds && values.memberInvestorIds.length > 0;
      default:
        return true;
    }
  };

  const nextStep = () => {
    if (validateCurrentStep() && currentStep < 5) {
      setCurrentStep(currentStep + 1);
    }
  };

  const prevStep = () => {
    if (currentStep > 1) {
      setCurrentStep(currentStep - 1);
    }
  };

  const onSubmit = async (data: CreateSyndicateFormData) => {
    setIsSubmitting(true);
    setSubmitError(null);

    try {
      const syndicateRequest: CreateSyndicateRequest = {
        name: data.name,
        leadBankId: data.leadBankId,
        borrowerId: data.borrowerId,
        memberInvestorIds: data.memberInvestorIds,
      };
      
      const response = await syndicateApi.create(syndicateRequest);
      const newSyndicate = response.data;
      
      reset();
      setCurrentStep(1);
      onSuccess?.(newSyndicate);
    } catch (error) {
      const apiError = error as ApiError;
      setSubmitError(apiError.message || 'An error occurred');
    } finally {
      setIsSubmitting(false);
    }
  };

  const renderStepContent = () => {
    switch (currentStep) {
      case 1:
        return (
          <div className="space-y-6">
            <div>
              <h3 className="text-lg font-semibold text-white mb-2">Basic Information</h3>
              <p className="text-accent-400 text-sm mb-6">Enter the syndicate name</p>
              
              <div>
                <label htmlFor="name" className="block text-sm font-medium text-white mb-2">
                  Syndicate Name <span className="text-error">*</span>
                </label>
                <input
                  {...register('name')}
                  type="text"
                  id="name"
                  placeholder="e.g., ABC Syndicate 2025"
                  className="w-full px-4 py-3 bg-secondary-600 border border-secondary-500 rounded-lg text-white placeholder:text-accent-400 focus:outline-none focus:ring-2 focus:ring-accent-500 focus:border-transparent"
                />
                {errors.name && (
                  <p className="mt-1 text-sm text-error">{errors.name.message}</p>
                )}
              </div>
            </div>
          </div>
        );

      case 2:
        return (
          <div className="space-y-6">
            <div>
              <h3 className="text-lg font-semibold text-white mb-2">Borrower Selection</h3>
              <p className="text-accent-400 text-sm mb-6">Select the borrower who will receive the financing</p>
              
              <BorrowerSelect
                value={watchedValues.borrowerId}
                onChange={(borrowerId) => setValue('borrowerId', borrowerId)}
                error={errors.borrowerId?.message}
              />
            </div>
          </div>
        );

      case 3:
        return (
          <div className="space-y-6">
            <div>
              <h3 className="text-lg font-semibold text-white mb-2">Lead Bank Selection</h3>
              <p className="text-accent-400 text-sm mb-6">Select the lead bank for the syndicate</p>
              
              <InvestorCards
                mode="lead-bank"
                selectedLeadBank={watchedValues.leadBankId}
                onLeadBankChange={(leadBankId) => {
                  setValue('leadBankId', leadBankId);
                  // Automatically add lead bank to member investors
                  const currentMembers = watchedValues.memberInvestorIds || [];
                  if (!currentMembers.includes(leadBankId)) {
                    setValue('memberInvestorIds', [...currentMembers, leadBankId]);
                  }
                }}
                error={errors.leadBankId?.message}
              />
            </div>
          </div>
        );

      case 4:
        return (
          <div className="space-y-6">
            <div>
              <h3 className="text-lg font-semibold text-white mb-2">Member Investor Selection</h3>
              <p className="text-accent-400 text-sm mb-6">Select member investors for the syndicate</p>
              
              <InvestorCards
                mode="members"
                selectedMembers={watchedValues.memberInvestorIds}
                onMembersChange={(memberIds) => setValue('memberInvestorIds', memberIds)}
                error={errors.memberInvestorIds?.message}
              />
            </div>
          </div>
        );

      case 5:
        return (
          <div className="space-y-6">
            <div>
              <h3 className="text-lg font-semibold text-white mb-2">Confirmation</h3>
              <p className="text-accent-400 text-sm mb-6">Review the details and create the syndicate</p>
              
              <div className="bg-secondary-600 rounded-lg p-6 space-y-4">
                <div>
                  <div className="text-sm text-accent-400">Syndicate Name</div>
                  <div className="text-white font-medium">{watchedValues.name}</div>
                </div>
                
                <div>
                  <div className="text-sm text-accent-400">Borrower ID</div>
                  <div className="text-white font-medium">{watchedValues.borrowerId}</div>
                </div>
                
                <div>
                  <div className="text-sm text-accent-400">Lead Bank ID</div>
                  <div className="text-white font-medium">{watchedValues.leadBankId}</div>
                </div>
                
                <div>
                  <div className="text-sm text-accent-400">Member Investor IDs</div>
                  <div className="text-white font-medium">
                    {watchedValues.memberInvestorIds?.join(', ') || 'None'}
                  </div>
                  <div className="text-accent-400 text-xs mt-1">
                    Total: {watchedValues.memberInvestorIds?.length || 0} investors
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
          {SYNDICATE_FORM_STEPS.map((step, index) => (
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
              
              {index < SYNDICATE_FORM_STEPS.length - 1 && (
                <div className={`w-16 h-1 mx-2 ${
                  currentStep > step.id ? 'bg-success' : 'bg-secondary-600'
                }`} />
              )}
            </div>
          ))}
        </div>
        
        <div className="mt-4">
          <h2 className="text-xl font-bold text-white">
            {SYNDICATE_FORM_STEPS[currentStep - 1].title}
          </h2>
          <p className="text-accent-400 text-sm">
            {SYNDICATE_FORM_STEPS[currentStep - 1].description}
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
          
          {currentStep < 5 ? (
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
              {isSubmitting ? 'Creating Syndicate...' : 'Create Syndicate'}
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

export default SyndicateForm;