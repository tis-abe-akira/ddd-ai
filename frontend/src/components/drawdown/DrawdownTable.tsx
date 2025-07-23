import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { drawdownApi } from '../../lib/api';
import type { Drawdown } from '../../types/api';

interface DrawdownTableProps {
  searchTerm?: string;
  facilityFilter?: number;
  onView?: (drawdown: Drawdown) => void;
  onEdit?: (drawdown: Drawdown) => void;
  onDelete?: (drawdown: Drawdown) => void;
  refreshTrigger?: number;
}

const DrawdownTable: React.FC<DrawdownTableProps> = ({
  searchTerm = '',
  facilityFilter,
  onView,
  onEdit,
  onDelete,
  refreshTrigger = 0
}) => {
  const navigate = useNavigate();
  const [drawdowns, setDrawdowns] = useState<Drawdown[]>([]);
  const [loading, setLoading] = useState(true);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  useEffect(() => {
    fetchDrawdowns();
  }, [currentPage, searchTerm, facilityFilter, refreshTrigger]);

  const fetchDrawdowns = async () => {
    try {
      setLoading(true);
      
      if (facilityFilter) {
        // 特定ファシリティのドローダウンを取得
        const response = await drawdownApi.getByFacilityId(facilityFilter);
        setDrawdowns(response.data);
        setTotalPages(1);
        setTotalElements(response.data.length);
      } else {
        // ページネーション付きで全ドローダウンを取得
        const response = await drawdownApi.getAllPaged(currentPage);
        setDrawdowns(response.data.content);
        setTotalPages(response.data.totalPages);
        setTotalElements(response.data.totalElements);
      }
    } catch (error) {
      console.error('Failed to fetch drawdowns:', error);
      setDrawdowns([]);
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
        return 'bg-warning/20 text-warning';
      case 'ACTIVE':
        return 'bg-accent-500/20 text-accent-500';
      case 'COMPLETED':
        return 'bg-success/20 text-success';
      case 'FAILED':
        return 'bg-error/20 text-error';
      case 'CANCELLED':
        return 'bg-secondary-600 text-accent-400';
      case 'REFUNDED':
        return 'bg-purple-500/20 text-purple-400';
      default:
        return 'bg-secondary-600 text-accent-400';
    }
  };

  const getStatusInfo = (status: string) => {
    switch (status) {
      case 'DRAFT':
        return { 
          label: 'Pending', 
          tooltip: 'Pending - Transaction has been created but not yet started processing.' 
        };
      case 'ACTIVE':
        return { 
          label: 'Processing', 
          tooltip: 'Processing - Transaction is currently being executed.' 
        };
      case 'COMPLETED':
        return { 
          label: 'Completed', 
          tooltip: 'Completed - Transaction has been successfully executed and finalized.' 
        };
      case 'FAILED':
        return { 
          label: 'Failed', 
          tooltip: 'Failed - Transaction execution failed due to system error or business rule violation.' 
        };
      case 'CANCELLED':
        return { 
          label: 'Cancelled', 
          tooltip: 'Cancelled - Transaction was intentionally cancelled by user or system.' 
        };
      case 'REFUNDED':
        return { 
          label: 'Refunded', 
          tooltip: 'Refunded - Completed transaction has been refunded due to correction or contract change.' 
        };
      default:
        return { 
          label: status, 
          tooltip: 'Unknown transaction status' 
        };
    }
  };

  const canEdit = (status: string) => {
    // PENDING, FAILED状態では編集可能
    return status === 'DRAFT' || status === 'FAILED';
  };

  const canDelete = (status: string) => {
    // PENDING, FAILED状態では削除可能（COMPLETED, CANCELLED, REFUNDEDは削除不可）
    return status === 'DRAFT' || status === 'FAILED';
  };

  const filteredDrawdowns = drawdowns.filter(drawdown => {
    if (!searchTerm) return true;
    
    return (
      drawdown.id.toString().includes(searchTerm) ||
      drawdown.purpose.toLowerCase().includes(searchTerm.toLowerCase()) ||
      drawdown.loanId.toString().includes(searchTerm)
    );
  });

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
          {facilityFilter ? filteredDrawdowns.length : totalElements} drawdown(s)
          {facilityFilter && <span className="ml-2">(Facility #{facilityFilter})</span>}
        </div>
        {!facilityFilter && (
          <div className="text-sm text-accent-400">
            Page {currentPage + 1} / {Math.max(totalPages, 1)}
          </div>
        )}
      </div>

      {/* Table */}
      <div className="bg-primary-900 border border-secondary-500 rounded-xl overflow-hidden">
        {filteredDrawdowns.length === 0 ? (
          <div className="p-12 text-center">
            <div className="text-accent-400 text-lg mb-2">
              {searchTerm ? 'No search results found' : 'No drawdowns executed'}
            </div>
            <div className="text-accent-400 text-sm">
              {searchTerm ? 'Try searching with different keywords' : 'Please execute a new drawdown'}
            </div>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead>
                <tr className="border-b border-secondary-500">
                  <th className="px-6 py-4 text-left text-xs font-medium text-accent-400 uppercase tracking-wider">
                    Drawdown
                  </th>
                  <th className="px-6 py-4 text-left text-xs font-medium text-accent-400 uppercase tracking-wider">
                    Amount
                  </th>
                  <th className="px-6 py-4 text-left text-xs font-medium text-accent-400 uppercase tracking-wider">
                    Loan ID
                  </th>
                  <th className="px-6 py-4 text-left text-xs font-medium text-accent-400 uppercase tracking-wider">
                    Purpose
                  </th>
                  <th className="px-6 py-4 text-left text-xs font-medium text-accent-400 uppercase tracking-wider">
                    Execution Date
                  </th>
                  <th className="px-6 py-4 text-left text-xs font-medium text-accent-400 uppercase tracking-wider">
                    Investor Allocation
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
                {filteredDrawdowns.map((drawdown) => {
                  const totalAmountPies = drawdown.amountPies?.reduce((sum, pie) => sum + pie.amount, 0) || 0;
                  
                  return (
                    <tr 
                      key={drawdown.id} 
                      className="hover:bg-secondary-600/50 transition-colors cursor-pointer"
                      onClick={() => navigate(`/drawdowns/${drawdown.id}`)}
                    >
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex items-center">
                          <div className="w-10 h-10 bg-accent-500 rounded-full flex items-center justify-center text-white font-bold text-sm">
                            D{drawdown.id}
                          </div>
                          <div className="ml-3">
                            <div className="text-white font-medium">Drawdown #{drawdown.id}</div>
                            <div className="text-accent-400 text-sm">v{drawdown.version}</div>
                          </div>
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="text-white font-bold text-lg">
                          {formatCurrency(drawdown.amount, drawdown.currency)}
                        </div>
                        <div className="text-accent-400 text-xs">{drawdown.currency}</div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="text-white font-mono">#{drawdown.loanId}</div>
                      </td>
                      <td className="px-6 py-4">
                        <div className="text-white max-w-xs truncate" title={drawdown.purpose}>
                          {drawdown.purpose}
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="text-white">{formatDate(drawdown.transactionDate)}</div>
                        <div className="text-accent-400 text-xs">
                          Created {formatDate(drawdown.createdAt)}
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex items-center">
                          <span className="text-white font-medium">{drawdown.amountPies?.length || 0}</span>
                          <span className="text-accent-400 text-sm ml-1">investors</span>
                        </div>
                        <div className="text-accent-400 text-xs">
                          Allocated: {formatCurrency(totalAmountPies, drawdown.currency)}
                        </div>
                        {Math.abs(totalAmountPies - drawdown.amount) > 0.01 && (
                          <div className="text-warning text-xs">
                            Difference: {formatCurrency(drawdown.amount - totalAmountPies, drawdown.currency)}
                          </div>
                        )}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span 
                          className={`inline-flex px-2 py-1 text-xs font-medium rounded-full cursor-help ${getStatusColor(drawdown.status)}`}
                          title={getStatusInfo(drawdown.status).tooltip}
                        >
                          {getStatusInfo(drawdown.status).label}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex items-center gap-2">
                          {onEdit && canEdit(drawdown.status) && (
                            <button
                              onClick={(e) => {
                                e.stopPropagation();
                                onEdit(drawdown);
                              }}
                              className="p-2 text-accent-400 hover:text-accent-300 hover:bg-secondary-600 rounded-lg transition-colors"
                              title="Edit"
                            >
                              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                              </svg>
                            </button>
                          )}
                          {onDelete && canDelete(drawdown.status) && (
                            <button
                              onClick={(e) => {
                                e.stopPropagation();
                                onDelete(drawdown);
                              }}
                              className="p-2 text-error hover:text-red-400 hover:bg-error/10 rounded-lg transition-colors"
                              title="Delete"
                            >
                              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                              </svg>
                            </button>
                          )}
                          {(!canEdit(drawdown.status) && !canDelete(drawdown.status)) && (
                            <div className="text-accent-400 text-xs">
                              {drawdown.status === 'ACTIVE' ? 'Processing...' : 
                               drawdown.status === 'COMPLETED' ? 'Completed' : 
                               drawdown.status === 'CANCELLED' ? 'Cancelled' : 
                               'No Actions'}
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
      {!facilityFilter && totalPages > 1 && (
        <div className="flex items-center justify-between">
          <div className="text-sm text-accent-400">
            Showing {filteredDrawdowns.length} items
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

export default DrawdownTable;