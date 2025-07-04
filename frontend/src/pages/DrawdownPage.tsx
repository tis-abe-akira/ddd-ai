import React, { useState } from 'react';
import Layout from '../components/layout/Layout';
import DrawdownForm from '../components/forms/DrawdownForm';
import DrawdownTable from '../components/drawdown/DrawdownTable';
import type { Drawdown } from '../types/api';

const DrawdownPage: React.FC = () => {
  const [showForm, setShowForm] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [facilityFilter, setFacilityFilter] = useState<number | undefined>();
  const [refreshTrigger, setRefreshTrigger] = useState(0);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [selectedDrawdown, setSelectedDrawdown] = useState<Drawdown | null>(null);

  const handleSuccess = (drawdown: Drawdown) => {
    setSuccessMessage(`Drawdown "#${drawdown.id}" has been executed successfully.`);
    setShowForm(false);
    setRefreshTrigger(prev => prev + 1);
    // 成功メッセージを5秒後に消去
    setTimeout(() => setSuccessMessage(null), 5000);
  };

  const handleCancel = () => {
    setShowForm(false);
  };

  const handleView = (drawdown: Drawdown) => {
    setSelectedDrawdown(drawdown);
  };

  const closeDrawdownDetail = () => {
    setSelectedDrawdown(null);
  };

  const formatCurrency = (amount: number, currency: string) => {
    return new Intl.NumberFormat('ja-JP', {
      style: 'currency',
      currency: currency,
      minimumFractionDigits: 0,
    }).format(amount);
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return `${date.getFullYear()}-${date.getMonth() + 1}-${date.getDate()}`;
  };

  return (
    <Layout>
      <div className="max-w-7xl mx-auto">
        {/* Page Header */}
        <div className="flex items-center justify-between mb-8">
          <div>
            <h1 className="text-3xl font-bold text-white">Drawdowns</h1>
            <p className="text-accent-400">Execute and manage loan drawdowns</p>
          </div>
          <button
            onClick={() => setShowForm(!showForm)}
            className="bg-accent-500 hover:bg-accent-400 text-white font-semibold py-3 px-6 rounded-lg transition-colors duration-200 flex items-center gap-2"
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
            </svg>
            {showForm ? 'Close Form' : 'New Drawdown'}
          </button>
        </div>

        {/* Success Message */}
        {successMessage && (
          <div className="mb-6 p-4 bg-success/20 border border-success/30 rounded-lg flex items-center gap-3">
            <svg className="w-5 h-5 text-success flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
            </svg>
            <span className="text-success font-medium">{successMessage}</span>
            <button
              onClick={() => setSuccessMessage(null)}
              className="ml-auto text-success hover:text-success/80"
            >
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>
        )}

        {/* Form */}
        {showForm && (
          <div className="mb-8">
            <DrawdownForm 
              onSuccess={handleSuccess}
              onCancel={handleCancel}
            />
          </div>
        )}

        {/* Search and Filters */}
        {!showForm && (
          <div className="mb-6">
            <div className="bg-primary-900 border border-secondary-500 rounded-xl p-6">
              <div className="flex flex-col md:flex-row gap-4">
                <div className="flex-1">
                  <label htmlFor="search" className="block text-sm font-medium text-white mb-2">
                    Search
                  </label>
                  <div className="relative">
                    <svg className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-accent-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                    </svg>
                    <input
                      id="search"
                      type="text"
                      placeholder="Search by drawdown ID or purpose..."
                      value={searchTerm}
                      onChange={(e) => setSearchTerm(e.target.value)}
                      className="w-full pl-10 pr-4 py-3 bg-secondary-600 border border-secondary-500 rounded-lg text-white placeholder:text-accent-400 focus:outline-none focus:ring-2 focus:ring-accent-500 focus:border-transparent"
                    />
                  </div>
                </div>
                
                <div className="md:w-64">
                  <label htmlFor="facilityFilter" className="block text-sm font-medium text-white mb-2">
                    Facility Filter
                  </label>
                  <input
                    id="facilityFilter"
                    type="number"
                    placeholder="Facility ID"
                    value={facilityFilter || ''}
                    onChange={(e) => setFacilityFilter(e.target.value ? Number(e.target.value) : undefined)}
                    className="w-full px-4 py-3 bg-secondary-600 border border-secondary-500 rounded-lg text-white placeholder:text-accent-400 focus:outline-none focus:ring-2 focus:ring-accent-500 focus:border-transparent"
                  />
                </div>
                
                <div className="flex items-end gap-2">
                  <button
                    onClick={() => setRefreshTrigger(prev => prev + 1)}
                    className="bg-secondary-600 hover:bg-secondary-500 text-white font-semibold py-3 px-4 rounded-lg transition-colors duration-200 flex items-center gap-2"
                    title="Refresh"
                  >
                    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                    </svg>
                    Refresh
                  </button>
                  
                  {facilityFilter && (
                    <button
                      onClick={() => setFacilityFilter(undefined)}
                      className="bg-error hover:bg-error/80 text-white font-semibold py-3 px-4 rounded-lg transition-colors duration-200"
                      title="Clear Filter"
                    >
                      <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                      </svg>
                    </button>
                  )}
                </div>
              </div>

              {/* Quick Stats */}
              <div className="mt-6 grid grid-cols-1 md:grid-cols-4 gap-4">
                <div className="bg-secondary-600 rounded-lg p-4">
                  <div className="text-accent-400 text-sm">Scheduled Today</div>
                  <div className="text-white text-2xl font-bold">-</div>
                </div>
                <div className="bg-secondary-600 rounded-lg p-4">
                  <div className="text-accent-400 text-sm">Executed This Month</div>
                  <div className="text-white text-2xl font-bold">-</div>
                </div>
                <div className="bg-secondary-600 rounded-lg p-4">
                  <div className="text-accent-400 text-sm">Total Executed</div>
                  <div className="text-white text-2xl font-bold">-</div>
                </div>
                <div className="bg-secondary-600 rounded-lg p-4">
                  <div className="text-accent-400 text-sm">Average Amount</div>
                  <div className="text-white text-2xl font-bold">-</div>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Table */}
        {!showForm && (
          <DrawdownTable
            searchTerm={searchTerm}
            facilityFilter={facilityFilter}
            onView={handleView}
            refreshTrigger={refreshTrigger}
          />
        )}

        {/* Drawdown Detail Modal */}
        {selectedDrawdown && (
          <div className="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50">
            <div className="bg-primary-900 border border-secondary-500 rounded-xl p-6 max-w-4xl w-full max-h-[90vh] overflow-y-auto">
              <div className="flex items-center justify-between mb-6">
                <h2 className="text-2xl font-bold text-white">
                  Drawdown Details #{selectedDrawdown.id}
                </h2>
                <button
                  onClick={closeDrawdownDetail}
                  className="p-2 text-accent-400 hover:text-white hover:bg-secondary-600 rounded-lg transition-colors"
                >
                  <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                  </svg>
                </button>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                {/* Basic Information */}
                <div className="bg-secondary-600 rounded-lg p-4">
                  <h3 className="text-white font-semibold mb-4">Basic Information</h3>
                  <div className="space-y-3">
                    <div>
                      <div className="text-accent-400 text-sm">Drawdown Amount</div>
                      <div className="text-white text-xl font-bold">
                        {formatCurrency(selectedDrawdown.amount, selectedDrawdown.currency)}
                      </div>
                    </div>
                    <div>
                      <div className="text-accent-400 text-sm">Loan ID</div>
                      <div className="text-white font-medium">#{selectedDrawdown.loanId}</div>
                    </div>
                    <div>
                      <div className="text-accent-400 text-sm">Execution Date</div>
                      <div className="text-white font-medium">{formatDate(selectedDrawdown.transactionDate)}</div>
                    </div>
                    <div>
                      <div className="text-accent-400 text-sm">Purpose</div>
                      <div className="text-white font-medium">{selectedDrawdown.purpose}</div>
                    </div>
                  </div>
                </div>

                {/* Amount Pies */}
                <div className="bg-secondary-600 rounded-lg p-4">
                  <h3 className="text-white font-semibold mb-4">
                    Investor Allocation ({selectedDrawdown.amountPies?.length || 0} investors)
                  </h3>
                  <div className="space-y-2 max-h-64 overflow-y-auto">
                    {selectedDrawdown.amountPies?.map((pie, index) => (
                      <div key={index} className="flex justify-between items-center bg-primary-900 rounded p-3">
                        <div>
                          <div className="text-white font-medium">Investor #{pie.investorId}</div>
                          <div className="text-accent-400 text-sm">ID: {pie.id}</div>
                        </div>
                        <div className="text-right">
                          <div className="text-white font-bold">
                            {formatCurrency(pie.amount, selectedDrawdown.currency)}
                          </div>
                          <div className="text-accent-400 text-sm">
                            {((pie.amount / selectedDrawdown.amount) * 100).toFixed(1)}%
                          </div>
                        </div>
                      </div>
                    )) || (
                      <div className="text-accent-400 text-center py-4">
                        No investor allocation information available
                      </div>
                    )}
                  </div>
                  
                  {/* Total Verification */}
                  {selectedDrawdown.amountPies && selectedDrawdown.amountPies.length > 0 && (
                    <div className="mt-4 pt-3 border-t border-secondary-500">
                      <div className="flex justify-between items-center">
                        <span className="text-accent-400">Total Allocation</span>
                        <span className="text-white font-bold">
                          {formatCurrency(
                            selectedDrawdown.amountPies.reduce((sum, pie) => sum + pie.amount, 0),
                            selectedDrawdown.currency
                          )}
                        </span>
                      </div>
                      <div className="flex justify-between items-center mt-1">
                        <span className="text-accent-400">Difference</span>
                        <span className={`font-medium ${
                          Math.abs(selectedDrawdown.amount - selectedDrawdown.amountPies.reduce((sum, pie) => sum + pie.amount, 0)) < 0.01
                            ? 'text-success'
                            : 'text-warning'
                        }`}>
                          {formatCurrency(
                            selectedDrawdown.amount - selectedDrawdown.amountPies.reduce((sum, pie) => sum + pie.amount, 0),
                            selectedDrawdown.currency
                          )}
                        </span>
                      </div>
                    </div>
                  )}
                </div>
              </div>

              {/* Additional Information */}
              <div className="mt-6 bg-secondary-600 rounded-lg p-4">
                <h3 className="text-white font-semibold mb-4">System Information</h3>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4 text-sm">
                  <div>
                    <div className="text-accent-400">Created At</div>
                    <div className="text-white">{formatDate(selectedDrawdown.createdAt)}</div>
                  </div>
                  <div>
                    <div className="text-accent-400">Updated At</div>
                    <div className="text-white">{formatDate(selectedDrawdown.updatedAt)}</div>
                  </div>
                  <div>
                    <div className="text-accent-400">Version</div>
                    <div className="text-white">v{selectedDrawdown.version}</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>
    </Layout>
  );
};

export default DrawdownPage;