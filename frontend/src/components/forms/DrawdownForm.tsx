import React, { useState, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { createDrawdownSchema, type CreateDrawdownFormData, defaultDrawdownValues, DRAWDOWN_FORM_STEPS, purposeOptions } from '../../schemas/drawdown';
import FacilitySelectForDrawdown from '../drawdown/FacilitySelectForDrawdown';
import RepaymentTerms from '../drawdown/RepaymentTerms';
import { drawdownApi, loanApi } from '../../lib/api';
import type { ApiError, Drawdown, CreateDrawdownRequest, UpdateDrawdownRequest, Facility, Loan } from '../../types/api';

interface DrawdownFormProps {
  onSuccess?: (drawdown: Drawdown) => void;
  onCancel?: () => void;
  initialData?: Drawdown;
  isEditMode?: boolean;
}

const DrawdownForm: React.FC<DrawdownFormProps> = ({ onSuccess, onCancel, initialData, isEditMode = false }) => {
  const [currentStep, setCurrentStep] = useState(1);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [selectedFacility, setSelectedFacility] = useState<Facility | null>(null);

  const {
    register,
    handleSubmit,
    formState: { errors },
    setValue,
    getValues,
    watch,
    reset,
  } = useForm<CreateDrawdownFormData>({
    resolver: zodResolver(createDrawdownSchema),
    defaultValues: defaultDrawdownValues,
    mode: 'onChange'
  });

  const watchedValues = watch();

  // Initialize form with existing data in edit mode
  useEffect(() => {
    const initializeEditForm = async () => {
      if (isEditMode && initialData) {
        // Skip facility selection step in edit mode and start from step 2
        setCurrentStep(2);
        
        // Set facility data (not editable in edit mode)
        setValue('facilityId', initialData.facilityId || 0);
        setValue('borrowerId', initialData.borrowerId || 0);
        
        // Set drawdown data
        setValue('amount', initialData.amount);
        setValue('currency', initialData.currency);
        setValue('purpose', initialData.purpose);
        setValue('drawdownDate', initialData.transactionDate);
        
        // Fetch loan data if available
        if (initialData.loanId) {
          try {
            const loanResponse = await loanApi.getById(initialData.loanId);
            const loan: Loan = loanResponse.data;
            
            // Set loan data from actual loan record
            setValue('annualInterestRate', typeof loan.annualInterestRate === 'number' ? loan.annualInterestRate : 5.0);
            setValue('repaymentPeriodMonths', loan.repaymentPeriodMonths || 12);
            setValue('repaymentCycle', loan.repaymentCycle || 'MONTHLY');
            setValue('repaymentMethod', loan.repaymentMethod || 'EQUAL_INSTALLMENT');
          } catch (error) {
            console.error('Failed to fetch loan data:', error);
            // Fallback to placeholder values
            setValue('annualInterestRate', 5.0);
            setValue('repaymentPeriodMonths', 12);
            setValue('repaymentCycle', 'MONTHLY');
            setValue('repaymentMethod', 'EQUAL_INSTALLMENT');
          }
        }
      }
    };

    initializeEditForm();
  }, [isEditMode, initialData, setValue]);

  const validateCurrentStep = () => {
    const values = getValues();
    console.log('Current step:', currentStep, 'Values:', values); // Debug log
    switch (currentStep) {
      case 1:
        return values.facilityId !== undefined;
      case 2:
        return values.amount && values.purpose && values.drawdownDate;
      case 3:
        return values.annualInterestRate && values.repaymentPeriodMonths && values.repaymentCycle && values.repaymentMethod;
      case 4:
        // Step 4 needs all required values for submission
        return values.facilityId !== undefined && 
               values.borrowerId !== undefined &&
               values.amount && 
               values.purpose && 
               values.drawdownDate &&
               values.annualInterestRate && 
               values.repaymentPeriodMonths && 
               values.repaymentCycle && 
               values.repaymentMethod;
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

  const handleFacilitySelect = (facilityId: number | undefined, facility?: Facility) => {
    setValue('facilityId', facilityId);
    setSelectedFacility(facility || null);
    
    if (facility) {
      // ファシリティの通貨を自動設定
      setValue('currency', facility.currency);
      
      // シンジケートからborrowerId を自動取得する必要があります
      // 今回は簡易実装として、facilityのsyndicateIdから推測します
      // 実際の実装では、syndicateApi.getById でborrowerId を取得する必要があります
      // 仮にborrowerId=1として設定
      setValue('borrowerId', 1);
    }
  };

  const onSubmit = async (data: CreateDrawdownFormData) => {
    console.log('Form submitted with data:', data); // Debug log
    console.log('Edit mode:', isEditMode, 'Initial data:', initialData); // Debug log
    
    setIsSubmitting(true);
    setSubmitError(null);

    try {
      let response;
      
      if (isEditMode && initialData) {
        // Update existing drawdown
        const updateRequest: UpdateDrawdownRequest = {
          amount: data.amount,
          currency: data.currency,
          purpose: data.purpose,
          annualInterestRate: data.annualInterestRate,
          drawdownDate: data.drawdownDate,
          repaymentPeriodMonths: data.repaymentPeriodMonths,
          repaymentCycle: data.repaymentCycle,
          repaymentMethod: data.repaymentMethod,
          version: initialData.version,
        };
        
        response = await drawdownApi.update(initialData.id, updateRequest);
      } else {
        // Create new drawdown
        const createRequest: CreateDrawdownRequest = {
          facilityId: data.facilityId,
          borrowerId: data.borrowerId,
          amount: data.amount,
          currency: data.currency,
          purpose: data.purpose,
          annualInterestRate: data.annualInterestRate,
          drawdownDate: data.drawdownDate,
          repaymentPeriodMonths: data.repaymentPeriodMonths,
          repaymentCycle: data.repaymentCycle,
          repaymentMethod: data.repaymentMethod,
        };
        
        response = await drawdownApi.create(createRequest);
      }
      
      const updatedDrawdown = response.data;
      
      if (!isEditMode) {
        reset();
        setCurrentStep(1);
        setSelectedFacility(null);
      }
      
      onSuccess?.(updatedDrawdown);
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

  const getAvailableAmount = () => {
    return selectedFacility?.commitment || 0;
  };

  const renderStepContent = () => {
    switch (currentStep) {
      case 1:
        return (
          <div className="space-y-6">
            <FacilitySelectForDrawdown
              value={watchedValues.facilityId}
              onChange={handleFacilitySelect}
              error={errors.facilityId?.message}
            />
          </div>
        );

      case 2:
        return (
          <div className="space-y-6">
            <div>
              <h3 className="text-lg font-semibold text-white mb-2">Drawdown Basic Information</h3>
              <p className="text-accent-400 text-sm mb-6">Set the amount, purpose, and execution date for the drawdown</p>
              
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                {/* Amount */}
                <div>
                  <label htmlFor="amount" className="block text-sm font-medium text-white mb-2">
                    Drawdown Amount <span className="text-error">*</span>
                  </label>
                  <input
                    {...register('amount', { valueAsNumber: true })}
                    type="number"
                    id="amount"
                    placeholder="e.g., 1000000"
                    max={getAvailableAmount()}
                    className="w-full px-4 py-3 bg-secondary-600 border border-secondary-500 rounded-lg text-white placeholder:text-accent-400 focus:outline-none focus:ring-2 focus:ring-accent-500 focus:border-transparent"
                  />
                  {errors.amount && (
                    <p className="mt-1 text-sm text-error">{errors.amount.message}</p>
                  )}
                  {selectedFacility && (
                    <p className="mt-1 text-xs text-accent-400">
                      Available Amount: {formatCurrency(getAvailableAmount())}
                    </p>
                  )}
                </div>

                {/* Drawdown Date */}
                <div>
                  <label htmlFor="drawdownDate" className="block text-sm font-medium text-white mb-2">
                    Drawdown Execution Date <span className="text-error">*</span>
                  </label>
                  <input
                    {...register('drawdownDate')}
                    type="date"
                    id="drawdownDate"
                    min={new Date().toISOString().split('T')[0]}
                    className="w-full px-4 py-3 bg-secondary-600 border border-secondary-500 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-accent-500 focus:border-transparent"
                  />
                  {errors.drawdownDate && (
                    <p className="mt-1 text-sm text-error">{errors.drawdownDate.message}</p>
                  )}
                </div>
              </div>

              {/* Purpose */}
              <div className="mt-6">
                <label htmlFor="purpose" className="block text-sm font-medium text-white mb-2">
                  Drawdown Purpose <span className="text-error">*</span>
                </label>
                <div className="grid grid-cols-3 md:grid-cols-6 gap-2 mb-3">
                  {purposeOptions.map((option) => (
                    <button
                      key={option.value}
                      type="button"
                      onClick={() => setValue('purpose', option.value)}
                      className={`px-3 py-2 text-sm rounded-lg border transition-colors ${
                        watchedValues.purpose === option.value
                          ? 'bg-accent-500 border-accent-500 text-white'
                          : 'bg-secondary-600 border-secondary-500 text-accent-400 hover:border-accent-500'
                      }`}
                    >
                      {option.label}
                    </button>
                  ))}
                </div>
                <input
                  {...register('purpose')}
                  type="text"
                  id="purpose"
                  placeholder="Enter detailed purpose"
                  className="w-full px-4 py-3 bg-secondary-600 border border-secondary-500 rounded-lg text-white placeholder:text-accent-400 focus:outline-none focus:ring-2 focus:ring-accent-500 focus:border-transparent"
                />
                {errors.purpose && (
                  <p className="mt-1 text-sm text-error">{errors.purpose.message}</p>
                )}
              </div>
            </div>
          </div>
        );

      case 3:
        return (
          <div className="space-y-6">
            <RepaymentTerms
              annualInterestRate={watchedValues.annualInterestRate || 0}
              repaymentPeriodMonths={watchedValues.repaymentPeriodMonths || 0}
              repaymentCycle={watchedValues.repaymentCycle || 'MONTHLY'}
              repaymentMethod={watchedValues.repaymentMethod || 'EQUAL_INSTALLMENT'}
              onInterestRateChange={(rate) => setValue('annualInterestRate', rate)}
              onPeriodChange={(months) => setValue('repaymentPeriodMonths', months)}
              onCycleChange={(cycle) => setValue('repaymentCycle', cycle)}
              onMethodChange={(method) => setValue('repaymentMethod', method)}
              currency={watchedValues.currency || 'USD'}
              amount={watchedValues.amount || 0}
              errors={{
                annualInterestRate: errors.annualInterestRate?.message,
                repaymentPeriodMonths: errors.repaymentPeriodMonths?.message,
                repaymentCycle: errors.repaymentCycle?.message,
                repaymentMethod: errors.repaymentMethod?.message,
              }}
            />
          </div>
        );

      case 4:
        return (
          <div className="space-y-6">
            <div>
              <h3 className="text-lg font-semibold text-white mb-2">Confirmation & Execution</h3>
              <p className="text-accent-400 text-sm mb-6">内容を確認して、ドローダウンを実行してください</p>
              
              <div className="bg-secondary-600 rounded-lg p-6 space-y-4">
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <div className="text-sm text-accent-400">Facility</div>
                    <div className="text-white font-medium">#{watchedValues.facilityId}</div>
                  </div>
                  <div>
                    <div className="text-sm text-accent-400">Drawdown Amount</div>
                    <div className="text-white font-medium text-lg">
                      {watchedValues.amount ? formatCurrency(watchedValues.amount) : 'Not set'}
                    </div>
                  </div>
                </div>
                
                <div>
                  <div className="text-sm text-accent-400">Purpose</div>
                  <div className="text-white font-medium">{watchedValues.purpose}</div>
                </div>
                
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <div className="text-sm text-accent-400">Execution Date</div>
                    <div className="text-white font-medium">{watchedValues.drawdownDate}</div>
                  </div>
                  <div>
                    <div className="text-sm text-accent-400">Annual Rate</div>
                    <div className="text-white font-medium">
                      {((watchedValues.annualInterestRate || 0) * 100).toFixed(2)}%
                    </div>
                  </div>
                </div>
                
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <div className="text-sm text-accent-400">Repayment Period</div>
                    <div className="text-white font-medium">
                      {watchedValues.repaymentPeriodMonths} months
                    </div>
                  </div>
                  <div>
                    <div className="text-sm text-accent-400">Repayment Method</div>
                    <div className="text-white font-medium">{watchedValues.repaymentMethod}</div>
                  </div>
                </div>
              </div>

              {selectedFacility && (
                <div className="bg-warning/10 border border-warning/30 rounded-lg p-4">
                  <div className="flex items-start gap-3">
                    <svg className="w-5 h-5 text-warning flex-shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.664-.833-2.464 0L3.34 16.5c-.77.833.192 2.5 1.732 2.5z" />
                    </svg>
                    <div>
                      <div className="text-warning font-medium text-sm">Important Notice</div>
                      <div className="text-accent-400 text-sm mt-1">
                        After drawdown execution, the facility will become fixed (FIXED status) and cannot be modified.
                      </div>
                    </div>
                  </div>
                </div>
              )}
            </div>
          </div>
        );

      default:
        return null;
    }
  };

  const isStepValid = validateCurrentStep();
  
  // Debug info in development
  console.log('Step valid:', isStepValid, 'Current step:', currentStep, 'Is submitting:', isSubmitting);

  return (
    <div className="bg-primary-900 border border-secondary-500 rounded-xl p-6">
      {/* Step Indicator */}
      <div className="mb-8">
        <div className="flex items-center justify-between">
          {DRAWDOWN_FORM_STEPS
            .filter(step => !isEditMode || step.id !== 1) // Skip Step 1 in edit mode
            .map((step, index, filteredSteps) => (
            <div key={step.id} className="flex items-center">
              <div className={`w-8 h-8 rounded-full flex items-center justify-center text-sm font-medium ${
                currentStep === step.id
                  ? 'bg-accent-500 text-white'
                  : currentStep > step.id || (isEditMode && step.id === 1)
                  ? 'bg-success text-white'
                  : 'bg-secondary-600 text-accent-400'
              }`}>
                {currentStep > step.id || (isEditMode && step.id === 1) ? (
                  <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
                    <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                  </svg>
                ) : (
                  step.id
                )}
              </div>
              
              {index < filteredSteps.length - 1 && (
                <div className={`w-16 h-1 mx-2 ${
                  currentStep > step.id ? 'bg-success' : 'bg-secondary-600'
                }`} />
              )}
            </div>
          ))}
        </div>
        
        <div className="mt-4">
          <h2 className="text-xl font-bold text-white">
            {DRAWDOWN_FORM_STEPS.find(step => step.id === currentStep)?.title || 'Unknown Step'}
          </h2>
          <p className="text-accent-400 text-sm">
            {DRAWDOWN_FORM_STEPS.find(step => step.id === currentStep)?.description || ''}
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
          {currentStep > (isEditMode ? 2 : 1) && (
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
{isSubmitting 
                ? (isEditMode ? 'Updating Drawdown...' : 'Executing Drawdown...') 
                : (isEditMode ? 'Update Drawdown' : 'Execute Drawdown')}
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

export default DrawdownForm;