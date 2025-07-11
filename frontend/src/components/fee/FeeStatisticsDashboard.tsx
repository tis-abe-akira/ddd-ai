import React, { useState, useEffect } from 'react';
import { feePaymentApi, facilityApi } from '../../lib/api';
import { FEE_TYPE_LABELS, getFeeTypeColor } from '../../lib/feeTypes';
import type { 
  FeePaymentStatistics,
  Facility,
  FeeType,
  ApiError 
} from '../../types/api';

interface FeeStatisticsDashboardProps {
  facilityId?: number;
  className?: string;
}

const FeeStatisticsDashboard: React.FC<FeeStatisticsDashboardProps> = ({
  facilityId,
  className = ''
}) => {
  const [statistics, setStatistics] = useState<FeePaymentStatistics | null>(null);
  const [facilities, setFacilities] = useState<Facility[]>([]);
  const [selectedFacility, setSelectedFacility] = useState<number>(facilityId || 0);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Load facilities on component mount
  useEffect(() => {
    const loadFacilities = async () => {
      try {
        const response = await facilityApi.getAll();
        setFacilities(response.data.content);
        
        // Auto-select first facility if none selected
        if (!selectedFacility && response.data.content.length > 0) {
          setSelectedFacility(response.data.content[0].id);
        }
      } catch (error) {
        console.error('Failed to load facilities:', error);
      }
    };
    loadFacilities();
  }, [selectedFacility]);

  // Load statistics when facility changes
  useEffect(() => {
    if (selectedFacility > 0) {
      loadStatistics(selectedFacility);
    }
  }, [selectedFacility]);

  const loadStatistics = async (facilityId: number) => {
    setIsLoading(true);
    setError(null);

    try {
      const response = await feePaymentApi.getStatistics(facilityId);
      setStatistics(response.data);
    } catch (error) {
      const apiError = error as ApiError;
      setError(apiError.message || 'Failed to load statistics');
      setStatistics(null);
    } finally {
      setIsLoading(false);
    }
  };

  const formatCurrency = (amount: number, currency: string = 'USD') => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: currency,
      minimumFractionDigits: 0,
    }).format(amount);
  };

  const getSelectedFacility = () => {
    return facilities.find(f => f.id === selectedFacility);
  };

  const getFeeTypePercentage = (feeType: FeeType, count: number) => {
    if (!statistics || statistics.totalFeePayments === 0) return 0;
    return (count / statistics.totalFeePayments) * 100;
  };

  const sortedFeeTypes = statistics 
    ? Object.entries(statistics.feePaymentCountsByType)
        .sort(([,a], [,b]) => b - a)
        .map(([feeType, count]) => ({ feeType: feeType as FeeType, count }))
    : [];

  const totalFeeTypes = Object.keys(statistics?.feePaymentCountsByType || {}).length;
  const averageAmountPerPayment = statistics && statistics.totalFeePayments > 0
    ? statistics.totalFeeAmount / statistics.totalFeePayments
    : 0;

  if (isLoading) {
    return (
      <div className={`bg-primary-900 border border-secondary-500 rounded-xl p-6 ${className}`}>
        <div className="flex items-center justify-center py-12">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-accent-500"></div>
          <span className="ml-3 text-accent-400">Loading statistics...</span>
        </div>
      </div>
    );
  }

  return (
    <div className={`bg-primary-900 border border-secondary-500 rounded-xl p-6 ${className}`}>
      <div className="flex items-center justify-between mb-6">
        <div>
          <h3 className="text-white font-semibold text-xl">Fee Statistics Dashboard</h3>
          <p className="text-accent-400 text-sm mt-1">
            Fee payment statistics and analysis by facility
          </p>
        </div>
        
        {/* Facility Selector */}
        <div className="min-w-0 max-w-xs">
          <select
            value={selectedFacility}
            onChange={(e) => setSelectedFacility(Number(e.target.value))}
            className="w-full px-4 py-2 bg-secondary-600 border border-secondary-500 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-accent-500"
          >
            <option value={0}>Select Facility</option>
            {facilities.map(facility => (
              <option key={facility.id} value={facility.id}>
                #{facility.id} - {facility.currency} {formatCurrency(facility.commitment, facility.currency)}
              </option>
            ))}
          </select>
        </div>
      </div>

      {error && (
        <div className="bg-error/10 border border-error/20 rounded-lg p-4 mb-6">
          <div className="flex items-center gap-3">
            <svg className="w-5 h-5 text-error flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <div>
              <div className="text-error font-medium text-sm">Error</div>
              <div className="text-error text-xs mt-1">{error}</div>
            </div>
          </div>
        </div>
      )}

      {!selectedFacility && (
        <div className="text-center py-12">
          <div className="text-accent-400 text-lg mb-2">Please select a facility</div>
          <div className="text-accent-400 text-sm">You need to select a facility to display statistics</div>
        </div>
      )}

      {selectedFacility && !statistics && !isLoading && !error && (
        <div className="text-center py-12">
          <div className="text-accent-400 text-lg mb-2">No statistics available</div>
          <div className="text-accent-400 text-sm">No fee payment history found for the selected facility</div>
        </div>
      )}

      {statistics && (
        <div className="space-y-6">
          {/* Overview Metrics */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            <div className="bg-gradient-to-br from-accent-500/20 to-accent-600/20 border border-accent-500/30 rounded-lg p-4">
              <div className="flex items-center justify-between">
                <div>
                  <div className="text-accent-400 text-sm">Total Payments</div>
                  <div className="text-white font-bold text-2xl">
                    {statistics.totalFeePayments}
                  </div>
                </div>
                <div className="bg-accent-500/20 rounded-full p-3">
                  <svg className="w-6 h-6 text-accent-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 7h6m0 10v-3m-3 3h.01M9 17h.01M9 14h.01M12 14h.01M15 11h.01M12 11h.01M9 11h.01M7 21h10a2 2 0 002-2V5a2 2 0 00-2-2H7a2 2 0 00-2 2v14a2 2 0 002 2z" />
                  </svg>
                </div>
              </div>
            </div>

            <div className="bg-gradient-to-br from-success/20 to-success/30 border border-success/30 rounded-lg p-4">
              <div className="flex items-center justify-between">
                <div>
                  <div className="text-accent-400 text-sm">Total Amount</div>
                  <div className="text-white font-bold text-xl">
                    {formatCurrency(statistics.totalFeeAmount, statistics.currency)}
                  </div>
                </div>
                <div className="bg-success/20 rounded-full p-3">
                  <svg className="w-6 h-6 text-success" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1" />
                  </svg>
                </div>
              </div>
            </div>

            <div className="bg-gradient-to-br from-warning/20 to-warning/30 border border-warning/30 rounded-lg p-4">
              <div className="flex items-center justify-between">
                <div>
                  <div className="text-accent-400 text-sm">Fee Types</div>
                  <div className="text-white font-bold text-2xl">
                    {totalFeeTypes}
                  </div>
                </div>
                <div className="bg-warning/20 rounded-full p-3">
                  <svg className="w-6 h-6 text-warning" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z" />
                  </svg>
                </div>
              </div>
            </div>

            <div className="bg-gradient-to-br from-purple-500/20 to-purple-600/20 border border-purple-500/30 rounded-lg p-4">
              <div className="flex items-center justify-between">
                <div>
                  <div className="text-accent-400 text-sm">Average Amount</div>
                  <div className="text-white font-bold text-lg">
                    {formatCurrency(averageAmountPerPayment, statistics.currency)}
                  </div>
                </div>
                <div className="bg-purple-500/20 rounded-full p-3">
                  <svg className="w-6 h-6 text-purple-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                  </svg>
                </div>
              </div>
            </div>
          </div>

          {/* Fee Type Breakdown */}
          <div className="bg-secondary-600 rounded-lg p-6">
            <h4 className="text-white font-semibold mb-4">Fee Type Breakdown</h4>
            <div className="space-y-4">
              {sortedFeeTypes.map(({ feeType, count }) => {
                const percentage = getFeeTypePercentage(feeType, count);
                return (
                  <div key={feeType} className="space-y-2">
                    <div className="flex items-center justify-between">
                      <div className="flex items-center gap-3">
                        <span className={`px-2 py-1 text-xs rounded-full ${getFeeTypeColor(feeType)}`}>
                          {FEE_TYPE_LABELS[feeType]}
                        </span>
                        <span className="text-white text-sm">
                          {count} payments
                        </span>
                      </div>
                      <div className="text-white font-medium">
                        {percentage.toFixed(1)}%
                      </div>
                    </div>
                    <div className="w-full bg-secondary-700 rounded-full h-2">
                      <div 
                        className="bg-accent-500 h-2 rounded-full transition-all duration-300"
                        style={{ width: `${percentage}%` }}
                      />
                    </div>
                  </div>
                );
              })}
            </div>
          </div>

          {/* Facility Information */}
          {getSelectedFacility() && (
            <div className="bg-secondary-600 rounded-lg p-6">
              <h4 className="text-white font-semibold mb-4">Facility Information</h4>
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div>
                  <div className="text-accent-400 text-sm">Facility ID</div>
                  <div className="text-white font-medium">#{getSelectedFacility()?.id}</div>
                </div>
                <div>
                  <div className="text-accent-400 text-sm">Commitment</div>
                  <div className="text-white font-medium">
                    {formatCurrency(getSelectedFacility()?.commitment || 0, getSelectedFacility()?.currency)}
                  </div>
                </div>
                <div>
                  <div className="text-accent-400 text-sm">Status</div>
                  <div className="text-white font-medium">
                    <span className={`px-2 py-1 text-xs rounded-full ${
                      getSelectedFacility()?.status === 'ACTIVE' 
                        ? 'bg-success/20 text-success' 
                        : 'bg-warning/20 text-warning'
                    }`}>
                      {getSelectedFacility()?.status}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default FeeStatisticsDashboard;