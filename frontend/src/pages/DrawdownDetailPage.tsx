import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import Layout from '../components/layout/Layout';
import { drawdownApi, loanApi, paymentApi } from '../lib/api';
import type { Drawdown, Loan, Payment } from '../types/api';

const DrawdownDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [drawdown, setDrawdown] = useState<Drawdown | null>(null);
  const [loan, setLoan] = useState<Loan | null>(null);
  const [payments, setPayments] = useState<Payment[]>([]);
  const [loading, setLoading] = useState(true);
  const [loanLoading, setLoanLoading] = useState(false);
  const [paymentsLoading, setPaymentsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (id) {
      fetchDrawdownDetail(parseInt(id));
    }
  }, [id]);

  const fetchDrawdownDetail = async (drawdownId: number) => {
    try {
      setLoading(true);
      const response = await drawdownApi.getById(drawdownId);
      const drawdownData = response.data;
      setDrawdown(drawdownData);
      
      // 関連するLoan情報を取得
      await fetchLoanDetail(drawdownData.loanId);
    } catch (error) {
      console.error('Failed to fetch drawdown detail:', error);
      setError('Failed to load drawdown details');
    } finally {
      setLoading(false);
    }
  };

  const fetchLoanDetail = async (loanId: number) => {
    try {
      setLoanLoading(true);
      const loanResponse = await loanApi.getById(loanId);
      setLoan(loanResponse.data);
      
      // 支払い履歴を取得
      await fetchPayments(loanId);
    } catch (error) {
      console.error('Failed to fetch loan detail:', error);
      // Loanが見つからない場合は警告として表示
    } finally {
      setLoanLoading(false);
    }
  };

  const fetchPayments = async (loanId: number) => {
    try {
      setPaymentsLoading(true);
      const paymentsResponse = await paymentApi.getByLoanId(loanId);
      setPayments(paymentsResponse.data);
    } catch (error) {
      console.error('Failed to fetch payments:', error);
      // 支払い履歴がない場合は空配列として扱う
      setPayments([]);
    } finally {
      setPaymentsLoading(false);
    }
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return `${date.getFullYear()}-${date.getMonth() + 1}-${date.getDate()}`;
  };

  const formatCurrency = (amount: number, currency?: string) => {
    const currencyCode = currency || 'USD';
    if (!amount && amount !== 0) return 'N/A';
    
    return new Intl.NumberFormat('ja-JP', {
      style: 'currency',
      currency: currencyCode,
      minimumFractionDigits: 0,
    }).format(amount);
  };

  if (loading) {
    return (
      <Layout>
        <div className="flex items-center justify-center py-12">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-accent-500"></div>
          <span className="ml-3 text-accent-400">Loading drawdown details...</span>
        </div>
      </Layout>
    );
  }

  if (error || !drawdown) {
    return (
      <Layout>
        <div className="max-w-4xl mx-auto">
          <div className="text-center py-12">
            <h1 className="text-2xl font-bold text-white mb-4">Error</h1>
            <p className="text-accent-400 mb-4">{error || 'Drawdown not found'}</p>
            <button
              onClick={() => navigate('/drawdowns')}
              className="bg-accent-500 hover:bg-accent-400 text-white px-4 py-2 rounded-lg"
            >
              Back to Drawdowns
            </button>
          </div>
        </div>
      </Layout>
    );
  }

  return (
    <Layout>
      <div className="max-w-6xl mx-auto">
        {/* Header */}
        <div className="flex items-center justify-between mb-8">
          <div className="flex items-center gap-4">
            <button
              onClick={() => navigate('/drawdowns')}
              className="p-2 text-accent-400 hover:text-white hover:bg-secondary-600 rounded-lg transition-colors"
              title="Back to Drawdowns"
            >
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
              </svg>
            </button>
            <div>
              <h1 className="text-3xl font-bold text-white">Drawdown #{drawdown.id}</h1>
              <p className="text-accent-400">Detailed view and related loan information</p>
            </div>
          </div>
          <div className="text-sm text-accent-400">
            Version {drawdown.version}
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* Drawdown Basic Information */}
          <div className="bg-primary-900 border border-secondary-500 rounded-xl p-6">
            <h2 className="text-xl font-bold text-white mb-4">Drawdown Information</h2>
            <div className="space-y-4">
              <div>
                <label className="block text-accent-400 text-sm">Amount</label>
                <div className="text-white text-2xl font-bold">
                  {formatCurrency(drawdown.amount, drawdown.currency)}
                </div>
              </div>
              <div>
                <label className="block text-accent-400 text-sm">Loan ID</label>
                <div className="text-white font-mono">#{drawdown.loanId}</div>
              </div>
              <div>
                <label className="block text-accent-400 text-sm">Purpose</label>
                <div className="text-white">{drawdown.purpose}</div>
              </div>
              <div>
                <label className="block text-accent-400 text-sm">Execution Date</label>
                <div className="text-white">{formatDate(drawdown.transactionDate)}</div>
              </div>
              <div>
                <label className="block text-accent-400 text-sm">Created Date</label>
                <div className="text-white">{formatDate(drawdown.createdAt)}</div>
              </div>
            </div>
          </div>

          {/* Investor Allocation */}
          <div className="bg-primary-900 border border-secondary-500 rounded-xl p-6">
            <h2 className="text-xl font-bold text-white mb-4">
              Investor Allocation ({drawdown.amountPies?.length || 0} investors)
            </h2>
            <div className="space-y-3 max-h-80 overflow-y-auto">
              {drawdown.amountPies?.map((pie, index) => (
                <div key={index} className="flex justify-between items-center bg-secondary-600 rounded-lg p-3">
                  <div>
                    <div className="text-white font-medium">Investor #{pie.investorId}</div>
                    <div className="text-accent-400 text-sm">ID: {pie.id}</div>
                  </div>
                  <div className="text-right">
                    <div className="text-white font-bold">
                      {formatCurrency(pie.amount, drawdown.currency)}
                    </div>
                    <div className="text-accent-400 text-sm">
                      {((pie.amount / drawdown.amount) * 100).toFixed(1)}%
                    </div>
                  </div>
                </div>
              )) || (
                <div className="text-accent-400 text-center py-8">
                  No investor allocation information available
                </div>
              )}
            </div>

            {/* Total Verification */}
            {drawdown.amountPies && drawdown.amountPies.length > 0 && (
              <div className="mt-4 pt-4 border-t border-secondary-500">
                <div className="flex justify-between items-center">
                  <span className="text-accent-400">Total Allocation</span>
                  <span className="text-white font-bold">
                    {formatCurrency(
                      drawdown.amountPies.reduce((sum, pie) => sum + pie.amount, 0),
                      drawdown.currency
                    )}
                  </span>
                </div>
                <div className="flex justify-between items-center mt-1">
                  <span className="text-accent-400">Difference</span>
                  <span className={`font-medium ${
                    Math.abs(drawdown.amount - drawdown.amountPies.reduce((sum, pie) => sum + pie.amount, 0)) < 0.01
                      ? 'text-success'
                      : 'text-warning'
                  }`}>
                    {formatCurrency(
                      drawdown.amount - drawdown.amountPies.reduce((sum, pie) => sum + pie.amount, 0),
                      drawdown.currency
                    )}
                  </span>
                </div>
              </div>
            )}
          </div>
        </div>

        {/* Related Loan Information */}
        <div className="mt-6 bg-primary-900 border border-secondary-500 rounded-xl p-6">
          <h2 className="text-xl font-bold text-white mb-4">Related Loan Information</h2>
          
          {loanLoading ? (
            <div className="bg-secondary-600 rounded-lg p-4">
              <div className="flex items-center justify-center py-8">
                <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-accent-500"></div>
                <span className="ml-3 text-accent-400">Loading loan details...</span>
              </div>
            </div>
          ) : loan ? (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="bg-secondary-600 rounded-lg p-4">
                <h3 className="text-white font-semibold mb-3">Loan Terms</h3>
                <div className="space-y-2">
                  <div>
                    <label className="block text-accent-400 text-sm">Loan ID</label>
                    <div className="text-white font-mono">#{loan.id}</div>
                  </div>
                  <div>
                    <label className="block text-accent-400 text-sm">Status</label>
                    <span className={`inline-flex px-2 py-1 text-xs font-medium rounded-full ${
                      loan.status === 'ACTIVE' 
                        ? 'bg-success/20 text-success' 
                        : loan.status === 'DRAFT'
                        ? 'bg-warning/20 text-warning'
                        : 'bg-secondary-500 text-accent-400'
                    }`}>
                      {loan.status}
                    </span>
                  </div>
                  <div>
                    <label className="block text-accent-400 text-sm">Principal Amount</label>
                    <div className="text-white font-bold">
                      {formatCurrency(loan.principalAmount?.amount || loan.principalAmount, loan.currency)}
                    </div>
                  </div>
                  <div>
                    <label className="block text-accent-400 text-sm">Outstanding Balance</label>
                    <div className="text-white font-bold">
                      {formatCurrency(loan.outstandingBalance?.amount || loan.outstandingBalance, loan.currency)}
                    </div>
                  </div>
                </div>
              </div>
              
              <div className="bg-secondary-600 rounded-lg p-4">
                <h3 className="text-white font-semibold mb-3">Repayment Terms</h3>
                <div className="space-y-2">
                  <div>
                    <label className="block text-accent-400 text-sm">Annual Interest Rate</label>
                    <div className="text-white">{((loan.annualInterestRate?.value || loan.annualInterestRate) * 100).toFixed(2)}%</div>
                  </div>
                  <div>
                    <label className="block text-accent-400 text-sm">Repayment Period</label>
                    <div className="text-white">{loan.repaymentPeriodMonths} months</div>
                  </div>
                  <div>
                    <label className="block text-accent-400 text-sm">Repayment Cycle</label>
                    <div className="text-white">{loan.repaymentCycle}</div>
                  </div>
                  <div>
                    <label className="block text-accent-400 text-sm">Repayment Method</label>
                    <div className="text-white">{loan.repaymentMethod}</div>
                  </div>
                  <div>
                    <label className="block text-accent-400 text-sm">Drawdown Date</label>
                    <div className="text-white">{formatDate(loan.drawdownDate)}</div>
                  </div>
                </div>
              </div>
            </div>
          ) : (
            <div className="bg-secondary-600 rounded-lg p-4">
              <div className="text-accent-400 text-center py-8">
                <svg className="w-12 h-12 mx-auto mb-2 opacity-50" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.664-.833-2.464 0L3.34 16.5c-.77.833.192 2.5 1.732 2.5z" />
                </svg>
                <p>Loan information not available</p>
                <p className="text-sm mt-1">Loan ID: #{drawdown.loanId}</p>
              </div>
            </div>
          )}
        </div>

        {/* Payment Schedule & History */}
        <div className="mt-6 bg-primary-900 border border-secondary-500 rounded-xl p-6">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-xl font-bold text-white">Payment Schedule & History</h2>
            <div className="text-sm text-accent-400">
              {loan?.paymentDetails?.length || 0} scheduled, {payments.length} completed
            </div>
          </div>
          
          {(loanLoading || paymentsLoading) ? (
            <div className="bg-secondary-600 rounded-lg p-4">
              <div className="flex items-center justify-center py-8">
                <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-accent-500"></div>
                <span className="ml-3 text-accent-400">Loading payment information...</span>
              </div>
            </div>
          ) : loan?.paymentDetails && loan.paymentDetails.length > 0 ? (
            <div className="space-y-4">
              {loan.paymentDetails.map((scheduledPayment, index) => {
                // Find matching actual payment by payment number or date
                const actualPayment = payments.find(p => {
                  const paymentDate = new Date(p.paymentDate);
                  const dueDate = new Date(scheduledPayment.dueDate);
                  return Math.abs(paymentDate.getTime() - dueDate.getTime()) <= 7 * 24 * 60 * 60 * 1000; // Within 7 days
                });
                
                const isCompleted = !!actualPayment;
                const isOverdue = !isCompleted && new Date(scheduledPayment.dueDate) < new Date();
                
                return (
                  <div key={scheduledPayment.id} className={`rounded-lg p-4 border-l-4 ${
                    isCompleted 
                      ? 'bg-success/10 border-success' 
                      : isOverdue 
                        ? 'bg-warning/10 border-warning'
                        : 'bg-secondary-600 border-accent-500'
                  }`}>
                    <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                      {/* Payment Number & Status */}
                      <div>
                        <div className="flex items-center gap-2 mb-2">
                          <div className={`w-8 h-8 rounded-full flex items-center justify-center text-white font-bold text-xs ${
                            isCompleted 
                              ? 'bg-success' 
                              : isOverdue 
                                ? 'bg-warning'
                                : 'bg-accent-500'
                          }`}>
                            #{scheduledPayment.paymentNumber}
                          </div>
                          <div>
                            <div className="text-white font-medium">
                              Payment #{scheduledPayment.paymentNumber}
                            </div>
                            <div className="text-accent-400 text-xs">
                              Due: {formatDate(scheduledPayment.dueDate)}
                            </div>
                          </div>
                        </div>
                        <div className={`px-2 py-1 rounded text-xs font-medium ${
                          isCompleted 
                            ? 'bg-success/20 text-success'
                            : isOverdue 
                              ? 'bg-warning/20 text-warning'
                              : 'bg-accent-500/20 text-accent-300'
                        }`}>
                          {isCompleted ? 'Completed' : isOverdue ? 'Overdue' : 'Pending'}
                        </div>
                      </div>

                      {/* Scheduled Amounts */}
                      <div>
                        <div className="text-accent-400 text-sm font-medium mb-2">Scheduled</div>
                        <div className="space-y-1 text-sm">
                          <div className="flex justify-between">
                            <span className="text-accent-400">Principal:</span>
                            <span className="text-white">
                              {formatCurrency(scheduledPayment.principalPayment?.amount || scheduledPayment.principalPayment, loan.currency)}
                            </span>
                          </div>
                          <div className="flex justify-between">
                            <span className="text-accent-400">Interest:</span>
                            <span className="text-white">
                              {formatCurrency(scheduledPayment.interestPayment?.amount || scheduledPayment.interestPayment, loan.currency)}
                            </span>
                          </div>
                          <div className="flex justify-between border-t border-secondary-500 pt-1">
                            <span className="text-accent-400 font-medium">Total:</span>
                            <span className="text-white font-bold">
                              {formatCurrency(
                                (scheduledPayment.principalPayment?.amount || scheduledPayment.principalPayment) +
                                (scheduledPayment.interestPayment?.amount || scheduledPayment.interestPayment),
                                loan.currency
                              )}
                            </span>
                          </div>
                        </div>
                      </div>

                      {/* Actual Amounts (if paid) */}
                      <div>
                        <div className="text-accent-400 text-sm font-medium mb-2">
                          {isCompleted ? 'Actual' : 'Expected'}
                        </div>
                        {actualPayment ? (
                          <div className="space-y-1 text-sm">
                            <div className="flex justify-between">
                              <span className="text-accent-400">Principal:</span>
                              <span className="text-white">
                                {formatCurrency(actualPayment.principalAmount, actualPayment.currency)}
                              </span>
                            </div>
                            <div className="flex justify-between">
                              <span className="text-accent-400">Interest:</span>
                              <span className="text-white">
                                {formatCurrency(actualPayment.interestAmount, actualPayment.currency)}
                              </span>
                            </div>
                            <div className="flex justify-between border-t border-secondary-500 pt-1">
                              <span className="text-accent-400 font-medium">Total:</span>
                              <span className="text-white font-bold">
                                {formatCurrency(actualPayment.totalAmount, actualPayment.currency)}
                              </span>
                            </div>
                            <div className="text-xs text-accent-400 mt-1">
                              Paid: {formatDate(actualPayment.paymentDate)}
                            </div>
                          </div>
                        ) : (
                          <div className="text-accent-400 text-sm">
                            {isOverdue ? 'Payment overdue' : 'Not yet paid'}
                          </div>
                        )}
                      </div>

                      {/* Remaining Balance */}
                      <div>
                        <div className="text-accent-400 text-sm font-medium mb-2">After Payment</div>
                        <div className="text-white font-bold">
                          {formatCurrency(scheduledPayment.remainingBalance?.amount || scheduledPayment.remainingBalance, loan.currency)}
                        </div>
                        <div className="text-accent-400 text-xs mt-1">Remaining Balance</div>
                      </div>
                    </div>

                    {/* Investor Distributions (if actual payment exists) */}
                    {actualPayment?.paymentDistributions && actualPayment.paymentDistributions.length > 0 && (
                      <div className="mt-4 pt-4 border-t border-secondary-500">
                        <div className="text-white font-medium mb-2">
                          Investor Distributions ({actualPayment.paymentDistributions.length} investors)
                        </div>
                        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-2">
                          {actualPayment.paymentDistributions.map((distribution) => (
                            <div key={distribution.id} className="bg-primary-900 rounded p-2 text-xs">
                              <div className="flex justify-between items-center">
                                <div className="text-accent-400">
                                  Investor #{distribution.investorId}
                                </div>
                                <div className="text-white font-medium">
                                  {formatCurrency(distribution.totalAmount, actualPayment.currency)}
                                </div>
                              </div>
                              <div className="flex justify-between text-xs mt-1 text-accent-400">
                                <span>P: {formatCurrency(distribution.principalAmount, actualPayment.currency)}</span>
                                <span>I: {formatCurrency(distribution.interestAmount, actualPayment.currency)}</span>
                              </div>
                            </div>
                          ))}
                        </div>
                      </div>
                    )}
                  </div>
                );
              })}
              
              {/* Payment Summary */}
              {payments.length > 0 && (
                <div className="bg-secondary-600 rounded-lg p-4 border-t-2 border-accent-500">
                  <h3 className="text-white font-semibold mb-3">Payment Summary</h3>
                  <div className="grid grid-cols-1 md:grid-cols-4 gap-4 text-sm">
                    <div>
                      <div className="text-accent-400">Payments Made</div>
                      <div className="text-white font-bold">
                        {payments.length} / {loan?.paymentDetails?.length || 0}
                      </div>
                    </div>
                    <div>
                      <div className="text-accent-400">Total Principal Paid</div>
                      <div className="text-white font-bold">
                        {formatCurrency(
                          payments.reduce((sum, p) => sum + p.principalAmount, 0),
                          payments[0]?.currency || loan.currency
                        )}
                      </div>
                    </div>
                    <div>
                      <div className="text-accent-400">Total Interest Paid</div>
                      <div className="text-white font-bold">
                        {formatCurrency(
                          payments.reduce((sum, p) => sum + p.interestAmount, 0),
                          payments[0]?.currency || loan.currency
                        )}
                      </div>
                    </div>
                    <div>
                      <div className="text-accent-400">Total Payments</div>
                      <div className="text-white font-bold">
                        {formatCurrency(
                          payments.reduce((sum, p) => sum + p.totalAmount, 0),
                          payments[0]?.currency || loan.currency
                        )}
                      </div>
                    </div>
                  </div>
                </div>
              )}
            </div>
          ) : (
            <div className="bg-secondary-600 rounded-lg p-4">
              <div className="text-accent-400 text-center py-8">
                <svg className="w-12 h-12 mx-auto mb-2 opacity-50" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 9V7a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2m2 4h10a2 2 0 002-2v-6a2 2 0 00-2-2H9a2 2 0 00-2 2v6a2 2 0 002 2zm7-5a2 2 0 11-4 0 2 2 0 014 0z" />
                </svg>
                <p>No payment schedule available</p>
                <p className="text-sm mt-1">Payment schedule will be generated when loan is created</p>
              </div>
            </div>
          )}
        </div>
      </div>
    </Layout>
  );
};

export default DrawdownDetailPage;