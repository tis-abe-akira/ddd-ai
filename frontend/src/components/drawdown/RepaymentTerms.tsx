import React from 'react';
import { repaymentMethodOptions, repaymentCycleOptions } from '../../schemas/drawdown';
import type { RepaymentMethod } from '../../types/api';

interface RepaymentTermsProps {
  annualInterestRate: number;
  repaymentPeriodMonths: number;
  repaymentCycle: string;
  repaymentMethod: RepaymentMethod;
  onInterestRateChange: (rate: number) => void;
  onPeriodChange: (months: number) => void;
  onCycleChange: (cycle: string) => void;
  onMethodChange: (method: RepaymentMethod) => void;
  currency: string;
  amount: number;
  errors?: {
    annualInterestRate?: string;
    repaymentPeriodMonths?: string;
    repaymentCycle?: string;
    repaymentMethod?: string;
  };
}

const RepaymentTerms: React.FC<RepaymentTermsProps> = ({
  annualInterestRate,
  repaymentPeriodMonths,
  repaymentCycle,
  repaymentMethod,
  onInterestRateChange,
  onPeriodChange,
  onCycleChange,
  onMethodChange,
  currency,
  amount,
  errors
}) => {
  // 返済金額の概算計算
  const calculateEstimatedPayment = () => {
    if (!amount || !annualInterestRate || !repaymentPeriodMonths) {
      return { monthlyPayment: 0, totalPayment: 0, totalInterest: 0 };
    }

    const monthlyRate = annualInterestRate / 12;
    
    if (repaymentMethod === 'BULLET_PAYMENT') {
      // バレット返済：満期に一括返済
      const totalInterest = amount * annualInterestRate * (repaymentPeriodMonths / 12);
      return {
        monthlyPayment: 0,
        totalPayment: amount + totalInterest,
        totalInterest
      };
    } else {
      // 元利均等返済
      if (monthlyRate === 0) {
        const monthlyPayment = amount / repaymentPeriodMonths;
        return {
          monthlyPayment,
          totalPayment: amount,
          totalInterest: 0
        };
      }
      
      const monthlyPayment = amount * (monthlyRate * Math.pow(1 + monthlyRate, repaymentPeriodMonths)) / 
                            (Math.pow(1 + monthlyRate, repaymentPeriodMonths) - 1);
      const totalPayment = monthlyPayment * repaymentPeriodMonths;
      const totalInterest = totalPayment - amount;
      
      return {
        monthlyPayment,
        totalPayment,
        totalInterest
      };
    }
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('ja-JP', {
      style: 'currency',
      currency: currency || 'USD',
      minimumFractionDigits: 0,
    }).format(amount);
  };

  const formatPercentage = (rate: number) => {
    return `${(rate * 100).toFixed(2)}%`;
  };

  const estimation = calculateEstimatedPayment();

  return (
    <div className="space-y-6">
      <div>
        <h3 className="text-lg font-semibold text-white mb-2">返済条件</h3>
        <p className="text-accent-400 text-sm mb-6">金利条件と返済スケジュールを設定してください</p>
        
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {/* Annual Interest Rate */}
          <div>
            <label className="block text-sm font-medium text-white mb-2">
              年利 <span className="text-error">*</span>
            </label>
            <div className="relative">
              <input
                type="number"
                value={annualInterestRate * 100}
                onChange={(e) => onInterestRateChange(parseFloat(e.target.value) / 100 || 0)}
                placeholder="例: 2.5"
                step="0.01"
                min="0"
                max="100"
                className="w-full px-4 py-3 pr-8 bg-secondary-600 border border-secondary-500 rounded-lg text-white placeholder:text-accent-400 focus:outline-none focus:ring-2 focus:ring-accent-500 focus:border-transparent"
              />
              <span className="absolute right-3 top-1/2 transform -translate-y-1/2 text-accent-400 text-sm">%</span>
            </div>
            {errors?.annualInterestRate && (
              <p className="mt-1 text-sm text-error">{errors.annualInterestRate}</p>
            )}
            <p className="mt-1 text-xs text-accent-400">例: 2.5% = 年利2.5%</p>
          </div>

          {/* Repayment Period */}
          <div>
            <label className="block text-sm font-medium text-white mb-2">
              返済期間 <span className="text-error">*</span>
            </label>
            <div className="relative">
              <input
                type="number"
                value={repaymentPeriodMonths}
                onChange={(e) => onPeriodChange(parseInt(e.target.value) || 0)}
                placeholder="例: 12"
                min="1"
                max="600"
                className="w-full px-4 py-3 pr-12 bg-secondary-600 border border-secondary-500 rounded-lg text-white placeholder:text-accent-400 focus:outline-none focus:ring-2 focus:ring-accent-500 focus:border-transparent"
              />
              <span className="absolute right-3 top-1/2 transform -translate-y-1/2 text-accent-400 text-sm">ヶ月</span>
            </div>
            {errors?.repaymentPeriodMonths && (
              <p className="mt-1 text-sm text-error">{errors.repaymentPeriodMonths}</p>
            )}
            <p className="mt-1 text-xs text-accent-400">
              {repaymentPeriodMonths > 0 && `約${Math.round(repaymentPeriodMonths / 12 * 10) / 10}年`}
            </p>
          </div>

          {/* Repayment Cycle */}
          <div>
            <label className="block text-sm font-medium text-white mb-2">
              返済サイクル <span className="text-error">*</span>
            </label>
            <select
              value={repaymentCycle}
              onChange={(e) => onCycleChange(e.target.value)}
              className="w-full px-4 py-3 bg-secondary-600 border border-secondary-500 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-accent-500 focus:border-transparent"
            >
              {repaymentCycleOptions.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label} - {option.description}
                </option>
              ))}
            </select>
            {errors?.repaymentCycle && (
              <p className="mt-1 text-sm text-error">{errors.repaymentCycle}</p>
            )}
          </div>

          {/* Repayment Method */}
          <div>
            <label className="block text-sm font-medium text-white mb-2">
              返済方法 <span className="text-error">*</span>
            </label>
            <select
              value={repaymentMethod}
              onChange={(e) => onMethodChange(e.target.value as RepaymentMethod)}
              className="w-full px-4 py-3 bg-secondary-600 border border-secondary-500 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-accent-500 focus:border-transparent"
            >
              {repaymentMethodOptions.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
            {errors?.repaymentMethod && (
              <p className="mt-1 text-sm text-error">{errors.repaymentMethod}</p>
            )}
            <p className="mt-1 text-xs text-accent-400">
              {repaymentMethodOptions.find(opt => opt.value === repaymentMethod)?.description}
            </p>
          </div>
        </div>
      </div>

      {/* Payment Estimation */}
      {amount > 0 && annualInterestRate > 0 && repaymentPeriodMonths > 0 && (
        <div className="bg-secondary-600 rounded-lg p-6">
          <h4 className="text-white font-medium mb-4 flex items-center gap-2">
            <svg className="w-5 h-5 text-accent-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 7h6m0 10v-3m-3 3h.01M9 17h.01M9 14h.01M12 14h.01M15 11h.01M12 11h.01M9 11h.01M7 21h10a2 2 0 002-2V5a2 2 0 00-2-2H7a2 2 0 00-2 2v14a2 2 0 002 2z" />
            </svg>
            返済シミュレーション
          </h4>
          
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="bg-primary-900 rounded-lg p-4">
              <div className="text-accent-400 text-sm">
                {repaymentMethod === 'BULLET_PAYMENT' ? '最終返済額' : '月次返済額'}
              </div>
              <div className="text-white text-xl font-bold">
                {repaymentMethod === 'BULLET_PAYMENT' 
                  ? formatCurrency(estimation.totalPayment)
                  : formatCurrency(estimation.monthlyPayment)
                }
              </div>
            </div>
            
            <div className="bg-primary-900 rounded-lg p-4">
              <div className="text-accent-400 text-sm">総返済額</div>
              <div className="text-white text-xl font-bold">
                {formatCurrency(estimation.totalPayment)}
              </div>
            </div>
            
            <div className="bg-primary-900 rounded-lg p-4">
              <div className="text-accent-400 text-sm">総利息額</div>
              <div className="text-white text-xl font-bold">
                {formatCurrency(estimation.totalInterest)}
              </div>
            </div>
          </div>

          <div className="mt-4 pt-4 border-t border-secondary-500">
            <div className="flex justify-between text-sm">
              <span className="text-accent-400">実効年利</span>
              <span className="text-white font-medium">{formatPercentage(annualInterestRate)}</span>
            </div>
            <div className="flex justify-between text-sm mt-2">
              <span className="text-accent-400">返済期間</span>
              <span className="text-white font-medium">
                {repaymentPeriodMonths}ヶ月 ({Math.round(repaymentPeriodMonths / 12 * 10) / 10}年)
              </span>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default RepaymentTerms;