import React, { useState } from 'react';
import { 
  FEE_TYPE_LABELS, 
  RECIPIENT_TYPE_LABELS, 
  getFeeTypeColor,
  requiresInvestorDistribution 
} from '../../lib/feeTypes';
import type { 
  FeePayment, 
  FeeDistribution, 
  TransactionStatus,
  FeeType,
  RecipientType 
} from '../../types/api';

interface FeePaymentTableProps {
  feePayments: FeePayment[];
  onViewDetails?: (feePayment: FeePayment) => void;
  isLoading?: boolean;
}

const FeePaymentTable: React.FC<FeePaymentTableProps> = ({
  feePayments,
  onViewDetails,
  isLoading = false
}) => {
  const [expandedRows, setExpandedRows] = useState<Set<number>>(new Set());

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
  };

  const formatCurrency = (amount: number, currency: string = 'USD') => {
    return new Intl.NumberFormat('ja-JP', {
      style: 'currency',
      currency: currency,
      minimumFractionDigits: 0,
    }).format(amount);
  };

  const getStatusColor = (status: TransactionStatus) => {
    switch (status) {
      case 'PENDING':
        return 'bg-warning/20 text-warning';
      case 'PROCESSING':
        return 'bg-accent-500/20 text-accent-500';
      case 'COMPLETED':
        return 'bg-success/20 text-success';
      case 'FAILED':
        return 'bg-error/20 text-error';
      case 'CANCELLED':
        return 'bg-secondary-500/20 text-secondary-500';
      case 'REFUNDED':
        return 'bg-purple-500/20 text-purple-500';
      default:
        return 'bg-secondary-600 text-accent-400';
    }
  };

  const getStatusLabel = (status: TransactionStatus) => {
    switch (status) {
      case 'PENDING':
        return '保留中';
      case 'PROCESSING':
        return '処理中';
      case 'COMPLETED':
        return '完了';
      case 'FAILED':
        return '失敗';
      case 'CANCELLED':
        return 'キャンセル';
      case 'REFUNDED':
        return '返金済み';
      default:
        return status;
    }
  };

  const toggleRowExpansion = (feePaymentId: number) => {
    setExpandedRows(prev => {
      const newSet = new Set(prev);
      if (newSet.has(feePaymentId)) {
        newSet.delete(feePaymentId);
      } else {
        newSet.add(feePaymentId);
      }
      return newSet;
    });
  };

  const hasDistributions = (feePayment: FeePayment) => {
    return feePayment.feeDistributions && feePayment.feeDistributions.length > 0;
  };

  const getTotalDistributionAmount = (distributions: FeeDistribution[]) => {
    return distributions.reduce((total, dist) => total + dist.distributionAmount, 0);
  };

  if (isLoading) {
    return (
      <div className="bg-primary-900 border border-secondary-500 rounded-xl p-6">
        <div className="flex items-center justify-center py-12">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-accent-500"></div>
          <span className="ml-3 text-accent-400">Loading fee payments...</span>
        </div>
      </div>
    );
  }

  if (feePayments.length === 0) {
    return (
      <div className="bg-primary-900 border border-secondary-500 rounded-xl p-6">
        <h3 className="text-white font-semibold mb-4">手数料支払い履歴</h3>
        <div className="text-center py-8">
          <div className="text-accent-400 text-lg mb-2">手数料支払いがありません</div>
          <div className="text-accent-400 text-sm">手数料支払いが作成されると、ここに表示されます</div>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-primary-900 border border-secondary-500 rounded-xl p-6">
      <h3 className="text-white font-semibold mb-4">
        手数料支払い履歴 ({feePayments.length} 件)
      </h3>
      
      <div className="overflow-x-auto">
        <table className="w-full">
          <thead>
            <tr className="border-b border-secondary-500">
              <th className="px-4 py-3 text-left text-xs font-medium text-accent-400 uppercase tracking-wider">
                ID / Type
              </th>
              <th className="px-4 py-3 text-left text-xs font-medium text-accent-400 uppercase tracking-wider">
                Fee Date
              </th>
              <th className="px-4 py-3 text-right text-xs font-medium text-accent-400 uppercase tracking-wider">
                Amount
              </th>
              <th className="px-4 py-3 text-left text-xs font-medium text-accent-400 uppercase tracking-wider">
                Recipient
              </th>
              <th className="px-4 py-3 text-center text-xs font-medium text-accent-400 uppercase tracking-wider">
                Status
              </th>
              <th className="px-4 py-3 text-left text-xs font-medium text-accent-400 uppercase tracking-wider">
                Description
              </th>
              <th className="px-4 py-3 text-center text-xs font-medium text-accent-400 uppercase tracking-wider">
                Actions
              </th>
            </tr>
          </thead>
          <tbody className="divide-y divide-secondary-500">
            {feePayments.map((feePayment) => (
              <React.Fragment key={feePayment.id}>
                <tr className="hover:bg-secondary-600/50 transition-colors">
                  <td className="px-4 py-4">
                    <div className="flex flex-col">
                      <div className="text-white font-medium">#{feePayment.id}</div>
                      <div className="flex items-center gap-2 mt-1">
                        <span className={`px-2 py-1 text-xs rounded-full ${getFeeTypeColor(feePayment.feeType)}`}>
                          {FEE_TYPE_LABELS[feePayment.feeType]}
                        </span>
                      </div>
                    </div>
                  </td>
                  <td className="px-4 py-4 text-white">
                    {formatDate(feePayment.feeDate)}
                  </td>
                  <td className="px-4 py-4 text-right">
                    <div className="text-white font-bold text-lg">
                      {formatCurrency(feePayment.feeAmount, feePayment.currency)}
                    </div>
                    {feePayment.calculationBase > 0 && feePayment.feeRate > 0 && (
                      <div className="text-accent-400 text-xs">
                        {formatCurrency(feePayment.calculationBase, feePayment.currency)} × {feePayment.feeRate}%
                      </div>
                    )}
                  </td>
                  <td className="px-4 py-4">
                    <div className="text-white">
                      {RECIPIENT_TYPE_LABELS[feePayment.recipientType]} #{feePayment.recipientId}
                    </div>
                    {hasDistributions(feePayment) && (
                      <div className="text-accent-400 text-xs mt-1">
                        {feePayment.feeDistributions.length} distributions
                      </div>
                    )}
                  </td>
                  <td className="px-4 py-4 text-center">
                    <span 
                      className={`inline-flex px-2 py-1 text-xs font-medium rounded-full ${getStatusColor(feePayment.status)}`}
                    >
                      {getStatusLabel(feePayment.status)}
                    </span>
                  </td>
                  <td className="px-4 py-4">
                    <div className="text-white text-sm max-w-xs truncate" title={feePayment.description}>
                      {feePayment.description}
                    </div>
                  </td>
                  <td className="px-4 py-4 text-center">
                    <div className="flex items-center justify-center gap-2">
                      {onViewDetails && (
                        <button
                          onClick={() => onViewDetails(feePayment)}
                          className="bg-accent-500 hover:bg-accent-400 text-white font-medium py-1 px-3 rounded-lg transition-colors duration-200 text-sm"
                        >
                          詳細
                        </button>
                      )}
                      {hasDistributions(feePayment) && (
                        <button
                          onClick={() => toggleRowExpansion(feePayment.id)}
                          className="bg-secondary-600 hover:bg-secondary-500 text-white font-medium py-1 px-2 rounded-lg transition-colors duration-200 text-sm"
                          title={expandedRows.has(feePayment.id) ? '配分を隠す' : '配分を表示'}
                        >
                          {expandedRows.has(feePayment.id) ? (
                            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                            </svg>
                          ) : (
                            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                            </svg>
                          )}
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
                
                {/* Expanded row for fee distributions */}
                {expandedRows.has(feePayment.id) && hasDistributions(feePayment) && (
                  <tr>
                    <td colSpan={7} className="px-4 py-4 bg-secondary-600/30">
                      <div className="ml-8">
                        <h4 className="text-white font-medium mb-3">手数料配分詳細</h4>
                        <div className="bg-secondary-700 rounded-lg p-4">
                          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                            {feePayment.feeDistributions.map((distribution, index) => (
                              <div 
                                key={distribution.id} 
                                className="bg-secondary-600 rounded-lg p-3 border border-secondary-500"
                              >
                                <div className="flex justify-between items-start mb-2">
                                  <div className="text-accent-400 text-xs">
                                    Distribution #{index + 1}
                                  </div>
                                  <div className="text-white text-sm font-medium">
                                    {formatCurrency(distribution.distributionAmount, distribution.currency)}
                                  </div>
                                </div>
                                <div className="text-white text-sm">
                                  {RECIPIENT_TYPE_LABELS[distribution.recipientType]} #{distribution.recipientId}
                                </div>
                                <div className="text-accent-400 text-xs mt-1">
                                  Ratio: {(distribution.distributionRatio * 100).toFixed(2)}%
                                </div>
                              </div>
                            ))}
                          </div>
                          
                          {/* Distribution Summary */}
                          <div className="mt-4 pt-3 border-t border-secondary-500">
                            <div className="flex justify-between items-center text-sm">
                              <div className="text-accent-400">
                                Total Distributed: {feePayment.feeDistributions.length} recipients
                              </div>
                              <div className="text-white font-medium">
                                {formatCurrency(getTotalDistributionAmount(feePayment.feeDistributions), feePayment.currency)}
                              </div>
                            </div>
                            {getTotalDistributionAmount(feePayment.feeDistributions) !== feePayment.feeAmount && (
                              <div className="text-warning text-xs mt-1">
                                Warning: Distribution total doesn't match fee amount
                              </div>
                            )}
                          </div>
                        </div>
                      </div>
                    </td>
                  </tr>
                )}
              </React.Fragment>
            ))}
          </tbody>
        </table>
      </div>
      
      {/* Fee Payment Summary */}
      <div className="mt-6 grid grid-cols-1 md:grid-cols-4 gap-4">
        <div className="bg-secondary-600 rounded-lg p-4">
          <div className="text-accent-400 text-sm">Total Payments</div>
          <div className="text-white font-bold text-lg">
            {feePayments.length}
          </div>
        </div>
        <div className="bg-secondary-600 rounded-lg p-4">
          <div className="text-accent-400 text-sm">Completed</div>
          <div className="text-success font-bold text-lg">
            {feePayments.filter(fp => fp.status === 'COMPLETED').length}
          </div>
        </div>
        <div className="bg-secondary-600 rounded-lg p-4">
          <div className="text-accent-400 text-sm">Pending</div>
          <div className="text-warning font-bold text-lg">
            {feePayments.filter(fp => fp.status === 'PENDING').length}
          </div>
        </div>
        <div className="bg-secondary-600 rounded-lg p-4">
          <div className="text-accent-400 text-sm">Total Amount</div>
          <div className="text-white font-bold text-lg">
            {feePayments.length > 0 ? formatCurrency(
              feePayments.reduce((total, fp) => total + fp.feeAmount, 0),
              feePayments[0]?.currency || 'USD'
            ) : '$0'}
          </div>
        </div>
      </div>
    </div>
  );
};

export default FeePaymentTable;