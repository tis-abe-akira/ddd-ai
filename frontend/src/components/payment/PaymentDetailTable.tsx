import React, { useState } from 'react';
import { paymentApi } from '../../lib/api';
import type { PaymentDetail, PaymentStatus } from '../../types/api';

interface PaymentDetailTableProps {
  paymentDetails: PaymentDetail[];
  currency: string;
  onPaymentSuccess?: () => void;
}

const PaymentDetailTable: React.FC<PaymentDetailTableProps> = ({
  paymentDetails,
  currency,
  onPaymentSuccess
}) => {
  const [processingPayments, setProcessingPayments] = useState<Set<number>>(new Set());

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('ja-JP', {
      style: 'currency',
      currency: currency,
      minimumFractionDigits: 0,
    }).format(amount);
  };

  const getStatusColor = (status: PaymentStatus) => {
    switch (status) {
      case 'PENDING':
        return 'bg-warning/20 text-warning';
      case 'PAID':
        return 'bg-success/20 text-success';
      case 'OVERDUE':
        return 'bg-error/20 text-error';
      default:
        return 'bg-secondary-600 text-accent-400';
    }
  };

  const getStatusLabel = (status: PaymentStatus) => {
    switch (status) {
      case 'PENDING':
        return 'Pending';
      case 'PAID':
        return 'Paid';
      case 'OVERDUE':
        return 'Overdue';
      default:
        return status;
    }
  };

  const isPayable = (paymentDetail: PaymentDetail) => {
    return paymentDetail.paymentStatus === 'PENDING' || paymentDetail.paymentStatus === 'OVERDUE';
  };

  const handlePayment = async (paymentDetail: PaymentDetail) => {
    const confirmPayment = window.confirm(
      `Process scheduled payment for Payment #${paymentDetail.paymentNumber}?\n\n` +
      `Principal: ${formatCurrency(paymentDetail.principalPayment)}\n` +
      `Interest: ${formatCurrency(paymentDetail.interestPayment)}\n` +
      `Total: ${formatCurrency(paymentDetail.principalPayment + paymentDetail.interestPayment)}\n` +
      `Due Date: ${formatDate(paymentDetail.dueDate)}\n\n` +
      `This action cannot be undone.`
    );

    if (!confirmPayment) return;

    try {
      setProcessingPayments(prev => new Set(prev).add(paymentDetail.id));
      
      await paymentApi.processScheduledPayment(paymentDetail.id);
      
      // Success notification
      alert(`Payment #${paymentDetail.paymentNumber} has been processed successfully!`);
      
      // Refresh the data
      onPaymentSuccess?.();
    } catch (error: any) {
      console.error('Payment processing failed:', error);
      alert(`Failed to process payment: ${error.message || 'Unknown error'}`);
    } finally {
      setProcessingPayments(prev => {
        const newSet = new Set(prev);
        newSet.delete(paymentDetail.id);
        return newSet;
      });
    }
  };

  const getTotalPayment = (paymentDetail: PaymentDetail) => {
    return paymentDetail.principalPayment + paymentDetail.interestPayment;
  };

  if (paymentDetails.length === 0) {
    return (
      <div className="bg-primary-900 border border-secondary-500 rounded-xl p-6">
        <h3 className="text-white font-semibold mb-4">Payment Schedule</h3>
        <div className="text-center py-8">
          <div className="text-accent-400 text-lg mb-2">No payment schedule available</div>
          <div className="text-accent-400 text-sm">Payment details will appear here once the loan is finalized</div>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-primary-900 border border-secondary-500 rounded-xl p-6">
      <h3 className="text-white font-semibold mb-4">
        Payment Schedule ({paymentDetails.length} payments)
      </h3>
      
      <div className="overflow-x-auto">
        <table className="w-full">
          <thead>
            <tr className="border-b border-secondary-500">
              <th className="px-4 py-3 text-left text-xs font-medium text-accent-400 uppercase tracking-wider">
                Payment #
              </th>
              <th className="px-4 py-3 text-left text-xs font-medium text-accent-400 uppercase tracking-wider">
                Due Date
              </th>
              <th className="px-4 py-3 text-right text-xs font-medium text-accent-400 uppercase tracking-wider">
                Principal
              </th>
              <th className="px-4 py-3 text-right text-xs font-medium text-accent-400 uppercase tracking-wider">
                Interest
              </th>
              <th className="px-4 py-3 text-right text-xs font-medium text-accent-400 uppercase tracking-wider">
                Total Payment
              </th>
              <th className="px-4 py-3 text-right text-xs font-medium text-accent-400 uppercase tracking-wider">
                Remaining Balance
              </th>
              <th className="px-4 py-3 text-center text-xs font-medium text-accent-400 uppercase tracking-wider">
                Status
              </th>
              <th className="px-4 py-3 text-center text-xs font-medium text-accent-400 uppercase tracking-wider">
                Action
              </th>
            </tr>
          </thead>
          <tbody className="divide-y divide-secondary-500">
            {paymentDetails.map((paymentDetail) => (
              <tr key={paymentDetail.id} className="hover:bg-secondary-600/50 transition-colors">
                <td className="px-4 py-4 text-white font-medium">
                  {paymentDetail.paymentNumber}
                </td>
                <td className="px-4 py-4 text-white">
                  {formatDate(paymentDetail.dueDate)}
                  {paymentDetail.actualPaymentDate && (
                    <div className="text-accent-400 text-xs">
                      Paid: {formatDate(paymentDetail.actualPaymentDate)}
                    </div>
                  )}
                </td>
                <td className="px-4 py-4 text-right text-white font-medium">
                  {formatCurrency(paymentDetail.principalPayment)}
                </td>
                <td className="px-4 py-4 text-right text-white font-medium">
                  {formatCurrency(paymentDetail.interestPayment)}
                </td>
                <td className="px-4 py-4 text-right text-white font-bold">
                  {formatCurrency(getTotalPayment(paymentDetail))}
                </td>
                <td className="px-4 py-4 text-right text-white">
                  {formatCurrency(paymentDetail.remainingBalance)}
                </td>
                <td className="px-4 py-4 text-center">
                  <span 
                    className={`inline-flex px-2 py-1 text-xs font-medium rounded-full ${getStatusColor(paymentDetail.paymentStatus)}`}
                  >
                    {getStatusLabel(paymentDetail.paymentStatus)}
                  </span>
                </td>
                <td className="px-4 py-4 text-center">
                  {isPayable(paymentDetail) ? (
                    <button
                      onClick={() => handlePayment(paymentDetail)}
                      disabled={processingPayments.has(paymentDetail.id)}
                      className="bg-success hover:bg-success/80 disabled:opacity-50 disabled:cursor-not-allowed text-white font-medium py-2 px-4 rounded-lg transition-colors duration-200 text-sm"
                    >
                      {processingPayments.has(paymentDetail.id) ? (
                        <div className="flex items-center gap-2">
                          <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                          Processing...
                        </div>
                      ) : (
                        'Pay'
                      )}
                    </button>
                  ) : (
                    <span className="text-accent-400 text-xs">
                      {paymentDetail.paymentStatus === 'PAID' ? 'Completed' : 'No Action'}
                    </span>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      
      {/* Payment Summary */}
      <div className="mt-6 grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="bg-secondary-600 rounded-lg p-4">
          <div className="text-accent-400 text-sm">Total Payments</div>
          <div className="text-white font-bold text-lg">
            {paymentDetails.length}
          </div>
        </div>
        <div className="bg-secondary-600 rounded-lg p-4">
          <div className="text-accent-400 text-sm">Pending Payments</div>
          <div className="text-warning font-bold text-lg">
            {paymentDetails.filter(p => p.paymentStatus === 'PENDING').length}
          </div>
        </div>
        <div className="bg-secondary-600 rounded-lg p-4">
          <div className="text-accent-400 text-sm">Completed Payments</div>
          <div className="text-success font-bold text-lg">
            {paymentDetails.filter(p => p.paymentStatus === 'PAID').length}
          </div>
        </div>
      </div>
    </div>
  );
};

export default PaymentDetailTable;