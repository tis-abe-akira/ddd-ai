import React, { useState, useEffect } from 'react';
import { facilityApi } from '../../lib/api';
import type { Facility } from '../../types/api';

interface FacilityTableProps {
  searchTerm?: string;
  onEdit?: (facility: Facility) => void;
  onDelete?: (facility: Facility) => void;
  onDetail?: (facility: Facility) => void;
  refreshTrigger?: number;
}

const FacilityTable: React.FC<FacilityTableProps> = ({
  searchTerm = '',
  onEdit,
  onDelete,
  onDetail,
  refreshTrigger = 0
}) => {
  const [facilities, setFacilities] = useState<Facility[]>([]);
  const [loading, setLoading] = useState(true);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  useEffect(() => {
    fetchFacilities();
  }, [currentPage, searchTerm, refreshTrigger]);

  const fetchFacilities = async () => {
    try {
      setLoading(true);
      const response = await facilityApi.getAll(currentPage, undefined, searchTerm || undefined);
      setFacilities(response.data.content);
      setTotalPages(response.data.totalPages);
      setTotalElements(response.data.totalElements);
    } catch (error) {
      console.error('Failed to fetch facilities:', error);
      setFacilities([]);
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return `${date.getFullYear()}-${date.getMonth() + 1}-${date.getDate()}`;
  };

  const formatCurrency = (amount: number, currency: string) => {
    return new Intl.NumberFormat('ja-JP', {
      style: 'currency',
      currency: currency,
      minimumFractionDigits: 0,
      notation: 'compact'
    }).format(amount);
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'DRAFT':
        return 'bg-yellow-500/20 text-yellow-400';
      case 'FIXED':
        return 'bg-success/20 text-success';
      default:
        return 'bg-secondary-600 text-accent-400';
    }
  };

  const getStatusLabel = (status: string) => {
    switch (status) {
      case 'DRAFT':
        return 'Draft';
      case 'FIXED':
        return 'Fixed';
      default:
        return status;
    }
  };

  const calculateTotalSharePies = (facility: Facility) => {
    return facility.sharePies?.reduce((total, pie) => total + pie.share, 0) || 0;
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center py-12">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-accent-500"></div>
        <span className="ml-3 text-accent-400">Loading...</span>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {/* Table Header */}
      <div className="flex items-center justify-between">
        <div className="text-sm text-accent-400">
          {totalElements} facility(ies)
        </div>
        <div className="text-sm text-accent-400">
          Page {currentPage + 1} / {Math.max(totalPages, 1)}
        </div>
      </div>

      {/* Table */}
      <div className="bg-primary-900 border border-secondary-500 rounded-xl overflow-hidden">
        {facilities.length === 0 ? (
          <div className="p-12 text-center">
            <div className="text-accent-400 text-lg mb-2">
              {searchTerm ? 'No search results found' : 'No facilities created'}
            </div>
            <div className="text-accent-400 text-sm">
              {searchTerm ? 'Try searching with different keywords' : 'Please create a new facility'}
            </div>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead>
                <tr className="border-b border-secondary-500">
                  <th className="px-6 py-4 text-left text-xs font-medium text-accent-400 uppercase tracking-wider">
                    Facility
                  </th>
                  <th className="px-6 py-4 text-left text-xs font-medium text-accent-400 uppercase tracking-wider">
                    Amount
                  </th>
                  <th className="px-6 py-4 text-left text-xs font-medium text-accent-400 uppercase tracking-wider">
                    Syndicate
                  </th>
                  <th className="px-6 py-4 text-left text-xs font-medium text-accent-400 uppercase tracking-wider">
                    Period
                  </th>
                  <th className="px-6 py-4 text-left text-xs font-medium text-accent-400 uppercase tracking-wider">
                    Investors
                  </th>
                  <th className="px-6 py-4 text-left text-xs font-medium text-accent-400 uppercase tracking-wider">
                    Status
                  </th>
                  <th className="px-6 py-4 text-left text-xs font-medium text-accent-400 uppercase tracking-wider">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-secondary-500">
                {facilities.map((facility) => {
                  const totalShare = calculateTotalSharePies(facility);
                  const isValidSharePie = Math.abs(totalShare - 1.0) < 0.0001;
                  
                  return (
                    <tr 
                      key={facility.id} 
                      className="hover:bg-secondary-600/50 transition-colors cursor-pointer"
                      onClick={() => onDetail?.(facility)}
                    >
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex items-center">
                          <div className="w-10 h-10 bg-accent-500 rounded-full flex items-center justify-center text-white font-bold text-sm">
                            F{facility.id}
                          </div>
                          <div className="ml-3">
                            <div className="text-white font-medium">Facility #{facility.id}</div>
                            <div className="text-accent-400 text-sm">{facility.interestTerms}</div>
                          </div>
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="text-white font-bold text-lg">
                          {formatCurrency(facility.commitment, facility.currency)}
                        </div>
                        <div className="text-accent-400 text-xs">{facility.currency}</div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="text-white font-mono">#{facility.syndicateId}</div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="text-white">{formatDate(facility.startDate)}</div>
                        <div className="text-accent-400 text-sm">〜 {formatDate(facility.endDate)}</div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex items-center">
                          <span className="text-white font-medium">{facility.sharePies?.length || 0}</span>
                          <span className="text-accent-400 text-sm ml-1">investors</span>
                        </div>
                        <div className={`text-xs ${isValidSharePie ? 'text-success' : 'text-warning'}`}>
                          {Math.round(totalShare * 100)}%
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className={`inline-flex px-2 py-1 text-xs font-medium rounded-full ${getStatusColor(facility.status)}`}>
                          {getStatusLabel(facility.status)}
                        </span>
                        <div className="text-accent-400 text-xs mt-1">
                          v{facility.version}
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex items-center gap-2">
                          {onEdit && facility.status === 'DRAFT' && (
                            <button
                              onClick={(e) => {
                                e.stopPropagation();
                                onEdit(facility);
                              }}
                              className="p-2 text-accent-400 hover:text-accent-300 hover:bg-secondary-600 rounded-lg transition-colors"
                              title="Edit"
                            >
                              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                              </svg>
                            </button>
                          )}
                          {onDelete && facility.status === 'DRAFT' && (
                            <button
                              onClick={(e) => {
                                e.stopPropagation();
                                onDelete(facility);
                              }}
                              className="p-2 text-error hover:text-red-400 hover:bg-error/10 rounded-lg transition-colors"
                              title="Delete"
                            >
                              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                              </svg>
                            </button>
                          )}
                          {facility.status === 'FIXED' && (
                            <div className="text-accent-400 text-xs">
                              Fixed
                            </div>
                          )}
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="flex items-center justify-between">
          <div className="text-sm text-accent-400">
            Showing {facilities.length} items
          </div>
          <div className="flex items-center gap-2">
            <button
              onClick={() => setCurrentPage(Math.max(0, currentPage - 1))}
              disabled={currentPage === 0}
              className="p-2 text-accent-400 hover:text-white hover:bg-secondary-600 disabled:opacity-50 disabled:cursor-not-allowed rounded-lg transition-colors"
            >
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
              </svg>
            </button>
            
            <div className="flex items-center gap-1">
              {Array.from({ length: Math.min(5, totalPages) }, (_, i) => {
                const pageNum = currentPage <= 2 ? i : currentPage - 2 + i;
                if (pageNum >= totalPages) return null;
                
                return (
                  <button
                    key={pageNum}
                    onClick={() => setCurrentPage(pageNum)}
                    className={`w-8 h-8 text-sm font-medium rounded-lg transition-colors ${
                      currentPage === pageNum
                        ? 'bg-accent-500 text-white'
                        : 'text-accent-400 hover:text-white hover:bg-secondary-600'
                    }`}
                  >
                    {pageNum + 1}
                  </button>
                );
              })}
            </div>
            
            <button
              onClick={() => setCurrentPage(Math.min(totalPages - 1, currentPage + 1))}
              disabled={currentPage >= totalPages - 1}
              className="p-2 text-accent-400 hover:text-white hover:bg-secondary-600 disabled:opacity-50 disabled:cursor-not-allowed rounded-lg transition-colors"
            >
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
              </svg>
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default FacilityTable;