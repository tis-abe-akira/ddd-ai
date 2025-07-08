import React, { useState, useEffect } from 'react';
import Layout from '../components/layout/Layout';
import FeePaymentForm from '../components/forms/FeePaymentForm';
import FeePaymentTable from '../components/fee/FeePaymentTable';
import { feePaymentApi, facilityApi } from '../lib/api';
import { FEE_TYPE_LABELS, getFeeTypeOptions } from '../lib/feeTypes';
import type { 
  FeePayment, 
  FeePaymentStatistics,
  Facility,
  FeeType,
  ApiError,
  PageResponse 
} from '../types/api';

const FeePaymentPage: React.FC = () => {
  const [feePayments, setFeePayments] = useState<FeePayment[]>([]);
  const [facilities, setFacilities] = useState<Facility[]>([]);
  const [selectedFacility, setSelectedFacility] = useState<number | null>(null);
  const [selectedFeeType, setSelectedFeeType] = useState<FeeType | 'ALL'>('ALL');
  const [statistics, setStatistics] = useState<FeePaymentStatistics | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [isLoadingStats, setIsLoadingStats] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [showForm, setShowForm] = useState(false);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [pageSize] = useState(10);
  
  // Date range filter
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');

  // Load facilities on component mount
  useEffect(() => {
    const loadFacilities = async () => {
      try {
        const response = await facilityApi.getAll();
        setFacilities(response.data.content);
      } catch (error) {
        console.error('Failed to load facilities:', error);
      }
    };
    loadFacilities();
  }, []);

  // Load fee payments based on filters
  useEffect(() => {
    loadFeePayments();
  }, [selectedFacility, selectedFeeType, startDate, endDate, currentPage]);

  // Load statistics when facility is selected
  useEffect(() => {
    if (selectedFacility) {
      loadStatistics(selectedFacility);
    } else {
      setStatistics(null);
    }
  }, [selectedFacility]);

  const loadFeePayments = async () => {
    setIsLoading(true);
    setError(null);

    try {
      let response: { data: PageResponse<FeePayment> };

      if (startDate && endDate) {
        // Date range filter
        response = await feePaymentApi.getByDateRange(startDate, endDate, currentPage, pageSize);
      } else if (selectedFacility) {
        // Facility filter
        response = await feePaymentApi.getByFacilityId(selectedFacility, currentPage, pageSize);
      } else if (selectedFeeType !== 'ALL') {
        // Fee type filter
        response = await feePaymentApi.getByType(selectedFeeType, currentPage, pageSize);
      } else {
        // All fee payments
        response = await feePaymentApi.getAll(currentPage, pageSize);
      }

      setFeePayments(response.data.content);
      setTotalPages(response.data.totalPages);
      setTotalElements(response.data.totalElements);
    } catch (error) {
      const apiError = error as ApiError;
      setError(apiError.message || 'Failed to load fee payments');
      setFeePayments([]);
    } finally {
      setIsLoading(false);
    }
  };

  const loadStatistics = async (facilityId: number) => {
    setIsLoadingStats(true);
    try {
      const response = await feePaymentApi.getStatistics(facilityId);
      setStatistics(response.data);
    } catch (error) {
      console.error('Failed to load statistics:', error);
      setStatistics(null);
    } finally {
      setIsLoadingStats(false);
    }
  };

  const handleFeePaymentSuccess = (feePayment: FeePayment) => {
    setShowForm(false);
    setCurrentPage(0); // Reset to first page
    loadFeePayments();
    
    // Reload statistics if we're viewing a specific facility
    if (selectedFacility) {
      loadStatistics(selectedFacility);
    }
    
    // Success notification
    alert(`Fee payment #${feePayment.id} has been created successfully!`);
  };

  const handleViewDetails = (feePayment: FeePayment) => {
    // Create a detailed view modal or navigate to detail page
    const details = [
      `Fee Payment ID: ${feePayment.id}`,
      `Type: ${FEE_TYPE_LABELS[feePayment.feeType]}`,
      `Amount: ${formatCurrency(feePayment.feeAmount, feePayment.currency)}`,
      `Date: ${feePayment.feeDate}`,
      `Recipient: ${feePayment.recipientType} #${feePayment.recipientId}`,
      `Description: ${feePayment.description}`,
      `Status: ${feePayment.status}`,
    ].join('\n');
    
    alert(details);
  };

  const handleDelete = async (feePayment: FeePayment) => {
    try {
      await feePaymentApi.delete(feePayment.id);
      
      // Success notification
      alert(`Fee payment #${feePayment.id} has been deleted successfully!`);
      
      // Reload the current page to reflect the changes
      loadFeePayments();
      
      // Reload statistics if we're viewing a specific facility
      if (selectedFacility) {
        loadStatistics(selectedFacility);
      }
    } catch (error) {
      const apiError = error as ApiError;
      alert(`Failed to delete fee payment: ${apiError.message}`);
    }
  };

  const formatCurrency = (amount: number, currency: string = 'USD') => {
    return new Intl.NumberFormat('ja-JP', {
      style: 'currency',
      currency: currency,
      minimumFractionDigits: 0,
    }).format(amount);
  };

  const clearFilters = () => {
    setSelectedFacility(null);
    setSelectedFeeType('ALL');
    setStartDate('');
    setEndDate('');
    setCurrentPage(0);
  };

  const handlePageChange = (page: number) => {
    setCurrentPage(page);
  };

  const feeTypeOptions = getFeeTypeOptions();

  return (
    <Layout>
      <div className="space-y-6">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold text-white">Fee Payments</h1>
            <p className="text-accent-400 mt-2">
              Manage fee payments, view history, and analyze statistics
            </p>
          </div>
          <button
            onClick={() => setShowForm(!showForm)}
            className={`px-6 py-3 rounded-lg font-semibold transition-colors duration-200 ${
              showForm
                ? 'bg-secondary-600 hover:bg-secondary-500 text-white'
                : 'bg-accent-500 hover:bg-accent-400 text-white'
            }`}
          >
            {showForm ? 'Back to List' : 'New Fee Payment'}
          </button>
        </div>

      {/* Statistics Panel */}
      {selectedFacility && statistics && (
        <div className="bg-primary-900 border border-secondary-500 rounded-xl p-6">
          <h3 className="text-white font-semibold mb-4">
            Facility #{selectedFacility} Fee Statistics
          </h3>
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            <div className="bg-secondary-600 rounded-lg p-4">
              <div className="text-accent-400 text-sm">Total Payments</div>
              <div className="text-white font-bold text-xl">
                {statistics.totalFeePayments}
              </div>
            </div>
            <div className="bg-secondary-600 rounded-lg p-4">
              <div className="text-accent-400 text-sm">Total Amount</div>
              <div className="text-white font-bold text-xl">
                {formatCurrency(statistics.totalFeeAmount, statistics.currency)}
              </div>
            </div>
            <div className="bg-secondary-600 rounded-lg p-4">
              <div className="text-accent-400 text-sm">Most Common Type</div>
              <div className="text-white font-bold text-lg">
                {Object.entries(statistics.feePaymentCountsByType)
                  .sort(([,a], [,b]) => b - a)[0]?.[0] 
                  ? FEE_TYPE_LABELS[Object.entries(statistics.feePaymentCountsByType)
                      .sort(([,a], [,b]) => b - a)[0][0] as FeeType]
                  : 'N/A'}
              </div>
            </div>
            <div className="bg-secondary-600 rounded-lg p-4">
              <div className="text-accent-400 text-sm">Types Count</div>
              <div className="text-white font-bold text-xl">
                {Object.keys(statistics.feePaymentCountsByType).length}
              </div>
            </div>
          </div>
        </div>
      )}

      {showForm ? (
        <FeePaymentForm
          onSuccess={handleFeePaymentSuccess}
          onCancel={() => setShowForm(false)}
        />
      ) : (
        <>
          {/* Filters */}
          <div className="bg-primary-900 border border-secondary-500 rounded-xl p-6">
            <h3 className="text-white font-semibold mb-4">Filters</h3>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
              {/* Facility Filter */}
              <div>
                <label htmlFor="facilityFilter" className="block text-sm font-medium text-white mb-2">
                  Facility
                </label>
                <select
                  id="facilityFilter"
                  value={selectedFacility || ''}
                  onChange={(e) => {
                    const value = e.target.value;
                    setSelectedFacility(value ? Number(value) : null);
                    setCurrentPage(0);
                  }}
                  className="w-full px-4 py-2 bg-secondary-600 border border-secondary-500 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-accent-500"
                >
                  <option value="">All Facilities</option>
                  {facilities.map(facility => (
                    <option key={facility.id} value={facility.id}>
                      #{facility.id} - {facility.currency} {formatCurrency(facility.commitment, facility.currency)}
                    </option>
                  ))}
                </select>
              </div>

              {/* Fee Type Filter */}
              <div>
                <label htmlFor="feeTypeFilter" className="block text-sm font-medium text-white mb-2">
                  Fee Type
                </label>
                <select
                  id="feeTypeFilter"
                  value={selectedFeeType}
                  onChange={(e) => {
                    setSelectedFeeType(e.target.value as FeeType | 'ALL');
                    setCurrentPage(0);
                  }}
                  className="w-full px-4 py-2 bg-secondary-600 border border-secondary-500 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-accent-500"
                >
                  <option value="ALL">All Types</option>
                  {feeTypeOptions.map(option => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </select>
              </div>

              {/* Start Date Filter */}
              <div>
                <label htmlFor="startDate" className="block text-sm font-medium text-white mb-2">
                  Start Date
                </label>
                <input
                  type="date"
                  id="startDate"
                  value={startDate}
                  onChange={(e) => {
                    setStartDate(e.target.value);
                    setCurrentPage(0);
                  }}
                  className="w-full px-4 py-2 bg-secondary-600 border border-secondary-500 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-accent-500"
                />
              </div>

              {/* End Date Filter */}
              <div>
                <label htmlFor="endDate" className="block text-sm font-medium text-white mb-2">
                  End Date
                </label>
                <input
                  type="date"
                  id="endDate"
                  value={endDate}
                  onChange={(e) => {
                    setEndDate(e.target.value);
                    setCurrentPage(0);
                  }}
                  className="w-full px-4 py-2 bg-secondary-600 border border-secondary-500 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-accent-500"
                />
              </div>
            </div>

            {/* Clear Filters Button */}
            <div className="mt-4">
              <button
                onClick={clearFilters}
                className="bg-secondary-600 hover:bg-secondary-500 text-white font-medium py-2 px-4 rounded-lg transition-colors duration-200"
              >
                Clear Filters
              </button>
            </div>
          </div>

          {/* Error Display */}
          {error && (
            <div className="bg-error/10 border border-error/20 rounded-xl p-6">
              <div className="flex items-center gap-3">
                <svg className="w-6 h-6 text-error flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                <div>
                  <div className="text-error font-medium">Error Loading Fee Payments</div>
                  <div className="text-error text-sm mt-1">{error}</div>
                </div>
              </div>
            </div>
          )}

          {/* Fee Payment Table */}
          <FeePaymentTable
            feePayments={feePayments}
            onViewDetails={handleViewDetails}
            onDelete={handleDelete}
            isLoading={isLoading}
          />

          {/* Pagination */}
          {totalPages > 1 && (
            <div className="bg-primary-900 border border-secondary-500 rounded-xl p-6">
              <div className="flex items-center justify-between">
                <div className="text-accent-400 text-sm">
                  Showing {currentPage * pageSize + 1} to {Math.min((currentPage + 1) * pageSize, totalElements)} of {totalElements} results
                </div>
                <div className="flex items-center gap-2">
                  <button
                    onClick={() => handlePageChange(currentPage - 1)}
                    disabled={currentPage === 0}
                    className="px-3 py-2 bg-secondary-600 hover:bg-secondary-500 disabled:opacity-50 disabled:cursor-not-allowed text-white rounded-lg transition-colors duration-200"
                  >
                    Previous
                  </button>
                  <div className="flex items-center gap-1">
                    {Array.from({ length: Math.min(totalPages, 5) }, (_, i) => {
                      const page = Math.max(0, Math.min(totalPages - 5, currentPage - 2)) + i;
                      return (
                        <button
                          key={page}
                          onClick={() => handlePageChange(page)}
                          className={`px-3 py-2 rounded-lg transition-colors duration-200 ${
                            page === currentPage
                              ? 'bg-accent-500 text-white'
                              : 'bg-secondary-600 hover:bg-secondary-500 text-white'
                          }`}
                        >
                          {page + 1}
                        </button>
                      );
                    })}
                  </div>
                  <button
                    onClick={() => handlePageChange(currentPage + 1)}
                    disabled={currentPage >= totalPages - 1}
                    className="px-3 py-2 bg-secondary-600 hover:bg-secondary-500 disabled:opacity-50 disabled:cursor-not-allowed text-white rounded-lg transition-colors duration-200"
                  >
                    Next
                  </button>
                </div>
              </div>
            </div>
          )}
        </>
      )}
      </div>
    </Layout>
  );
};

export default FeePaymentPage;