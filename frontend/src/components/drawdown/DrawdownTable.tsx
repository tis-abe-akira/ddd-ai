import React, { useState, useEffect } from 'react';
import { drawdownApi } from '../../lib/api';
import type { Drawdown } from '../../types/api';

interface DrawdownTableProps {
  searchTerm?: string;
  facilityFilter?: number;
  onView?: (drawdown: Drawdown) => void;
  refreshTrigger?: number;
}

const DrawdownTable: React.FC<DrawdownTableProps> = ({
  searchTerm = '',
  facilityFilter,
  onView,
  refreshTrigger = 0
}) => {
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

  const getStatusColor = (transactionDate: string) => {
    const drawdownDate = new Date(transactionDate);
    const today = new Date();
    const daysDiff = Math.ceil((drawdownDate.getTime() - today.getTime()) / (1000 * 60 * 60 * 24));
    
    if (daysDiff > 0) {
      return 'bg-warning/20 text-warning'; // 未来（予定）
    } else if (daysDiff === 0) {
      return 'bg-accent-500/20 text-accent-500'; // 今日
    } else {
      return 'bg-success/20 text-success'; // 実行済み
    }
  };

  const getStatusLabel = (transactionDate: string) => {
    const drawdownDate = new Date(transactionDate);
    const today = new Date();
    const daysDiff = Math.ceil((drawdownDate.getTime() - today.getTime()) / (1000 * 60 * 60 * 24));
    
    if (daysDiff > 0) {
      return 'Scheduled';
    } else if (daysDiff === 0) {
      return 'Executing';
    } else {
      return 'Completed';
    }
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
                    <tr key={drawdown.id} className="hover:bg-secondary-600/50 transition-colors">
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
                        <span className={`inline-flex px-2 py-1 text-xs font-medium rounded-full ${getStatusColor(drawdown.transactionDate)}`}>
                          {getStatusLabel(drawdown.transactionDate)}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex items-center gap-2">
                          {onView && (
                            <button
                              onClick={() => onView(drawdown)}
                              className="p-2 text-accent-400 hover:text-accent-300 hover:bg-secondary-600 rounded-lg transition-colors"
                              title="View Details"
                            >
                              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                              </svg>
                            </button>
                          )}
                          
                          {/* AmountPie詳細モーダル（簡易版） */}
                          <div className="group relative">
                            <button className="p-2 text-accent-400 hover:text-accent-300 hover:bg-secondary-600 rounded-lg transition-colors">
                              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
                              </svg>
                            </button>
                            
                            {/* Tooltip */}
                            <div className="absolute bottom-full right-0 mb-2 hidden group-hover:block z-10">
                              <div className="bg-secondary-600 border border-secondary-500 rounded-lg p-3 min-w-64 shadow-lg">
                                <div className="text-white text-sm font-medium mb-2">Investor Allocation Details</div>
                                <div className="space-y-1">
                                  {drawdown.amountPies?.slice(0, 5).map((pie, index) => (
                                    <div key={index} className="flex justify-between text-xs">
                                      <span className="text-accent-400">Investor #{pie.investorId}</span>
                                      <span className="text-white">{formatCurrency(pie.amount, drawdown.currency)}</span>
                                    </div>
                                  )) || []}
                                  {(drawdown.amountPies?.length || 0) > 5 && (
                                    <div className="text-accent-400 text-xs text-center">
                                      +{(drawdown.amountPies?.length || 0) - 5} more...
                                    </div>
                                  )}
                                </div>
                              </div>
                            </div>
                          </div>
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