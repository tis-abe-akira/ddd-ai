import React, { useState, useEffect } from 'react';
import { investorApi } from '../../lib/api';
import { investorTypeLabels } from '../../schemas/investor';
import type { Investor } from '../../types/api';

interface InvestorTableProps {
  onRefresh?: () => void;
  refreshTrigger?: number;
  onEdit?: (investor: Investor) => void;
  onDelete?: (investor: Investor) => void;
}

interface PaginationInfo {
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

const InvestorTable: React.FC<InvestorTableProps> = ({ onRefresh, refreshTrigger, onEdit, onDelete }) => {
  const [investors, setInvestors] = useState<Investor[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [pagination, setPagination] = useState<PaginationInfo>({
    page: 0,
    size: 0, // バックエンドのデフォルトに従う
    totalElements: 0,
    totalPages: 0,
  });

  const fetchInvestors = async (page: number = 0, size?: number) => {
    try {
      setLoading(true);
      setError(null);
      
      // sizeが指定されていない場合はバックエンドのデフォルトに従う
      const response = await investorApi.getAll(page, size);
      const pageData = response.data;
      
      setInvestors(pageData.content);
      setPagination({
        page: pageData.number,
        size: pageData.size,
        totalElements: pageData.totalElements,
        totalPages: pageData.totalPages,
      });
    } catch (err: any) {
      setError(err.message || 'Investor一覧の取得に失敗しました');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    // 初回はバックエンドのデフォルトサイズに従う
    fetchInvestors(0);
  }, [refreshTrigger]);

  const handlePageChange = (newPage: number) => {
    if (newPage >= 0 && newPage < pagination.totalPages) {
      fetchInvestors(newPage, pagination.size);
    }
  };

  const getInvestorTypeColor = (type: string) => {
    switch (type) {
      case 'LEAD_BANK':
        return 'text-accent-500';
      case 'BANK':
        return 'text-blue-400';
      case 'INSURANCE':
        return 'text-purple-400';
      case 'FUND':
        return 'text-green-400';
      case 'GOVERNMENT':
        return 'text-yellow-400';
      default:
        return 'text-accent-400';
    }
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('ja-JP', {
      style: 'currency',
      currency: 'JPY',
      minimumFractionDigits: 0,
    }).format(amount);
  };

  const filteredInvestors = investors.filter(investor =>
    investor.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
    investor.email.toLowerCase().includes(searchTerm.toLowerCase()) ||
    (investor.companyId && investor.companyId.toLowerCase().includes(searchTerm.toLowerCase()))
  );

  if (loading) {
    return (
      <div className="flex items-center justify-center py-12">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-accent-500"></div>
        <span className="ml-3 text-accent-400">Loading...</span>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-error/10 border border-error/20 rounded-lg p-4">
        <p className="text-error">{error}</p>
        <button
          onClick={() => fetchInvestors(pagination.page, pagination.size)}
          className="mt-2 text-sm text-accent-500 hover:text-accent-400 underline"
        >
          再試行
        </button>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Search Bar */}
      <div className="relative">
        <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
          <svg className="h-5 w-5 text-accent-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
          </svg>
        </div>
        <input
          type="text"
          placeholder="Search"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          className="block w-full pl-10 pr-3 py-2 bg-secondary-600 border border-secondary-500 rounded-lg text-white placeholder:text-accent-400 focus:outline-none focus:ring-2 focus:ring-accent-500 focus:border-transparent"
        />
      </div>

      {/* Table */}
      <div className="bg-primary-900 border border-secondary-500 rounded-lg overflow-hidden">
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-secondary-500">
            <thead className="bg-secondary-600">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-accent-400 uppercase tracking-wider">
                  Name
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-accent-400 uppercase tracking-wider">
                  Type
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-accent-400 uppercase tracking-wider">
                  Email
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-accent-400 uppercase tracking-wider">
                  Phone
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-accent-400 uppercase tracking-wider">
                  Company ID
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-accent-400 uppercase tracking-wider">
                  Investment Capacity
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-accent-400 uppercase tracking-wider">
                  Current Investment
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-accent-400 uppercase tracking-wider">
                  Status
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-accent-400 uppercase tracking-wider">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody className="bg-primary-900 divide-y divide-secondary-500">
              {filteredInvestors.length === 0 ? (
                <tr>
                  <td colSpan={9} className="px-6 py-8 text-center text-accent-400">
                    {searchTerm ? '検索結果が見つかりません' : 'Investorが登録されていません'}
                  </td>
                </tr>
              ) : (
                filteredInvestors.map((investor) => (
                  <tr key={investor.id} className="hover:bg-secondary-600/30 transition-colors">
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm font-medium text-white">{investor.name}</div>
                      <div className="text-sm text-accent-400">ID: {investor.id}</div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full bg-secondary-600 ${getInvestorTypeColor(investor.investorType)}`}>
                        {investorTypeLabels[investor.investorType]}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-white">
                      {investor.email}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-white">
                      {investor.phoneNumber}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-accent-400">
                      {investor.companyId || '-'}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-accent-500">
                      {formatCurrency(investor.investmentCapacity)}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-white">
                      {formatCurrency(investor.currentInvestmentAmount)}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${
                        investor.status === 'ACTIVE' 
                          ? 'bg-success/20 text-success' 
                          : 'bg-error/20 text-error'
                      }`}>
                        {investor.status}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex items-center gap-2">
                        {onEdit && (
                          <button
                            onClick={() => {
                              console.log('Edit investor clicked!', investor);
                              onEdit(investor);
                            }}
                            className="p-2 text-accent-400 hover:text-accent-300 hover:bg-secondary-600 rounded-lg transition-colors"
                            title="Edit"
                          >
                            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                            </svg>
                          </button>
                        )}
                        {onDelete && (
                          <button
                            onClick={() => {
                              console.log('Delete investor clicked!', investor);
                              onDelete(investor);
                            }}
                            disabled={investor.status === 'RESTRICTED'}
                            className={`p-2 rounded-lg transition-colors ${
                              investor.status === 'RESTRICTED'
                                ? 'text-gray-400 cursor-not-allowed'
                                : 'text-error hover:text-red-400 hover:bg-error/10'
                            }`}
                            title={investor.status === 'RESTRICTED' ? 'Cannot delete restricted investor' : 'Delete'}
                          >
                            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                            </svg>
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination */}
        {pagination.totalPages > 1 && (
          <div className="bg-secondary-600 px-4 py-3 border-t border-secondary-500 sm:px-6">
            <div className="flex items-center justify-between">
              <div className="flex-1 flex justify-between sm:hidden">
                <button
                  onClick={() => handlePageChange(pagination.page - 1)}
                  disabled={pagination.page === 0}
                  className="relative inline-flex items-center px-4 py-2 text-sm font-medium text-white bg-secondary-500 border border-secondary-400 rounded-md hover:bg-secondary-400 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  Previous
                </button>
                <button
                  onClick={() => handlePageChange(pagination.page + 1)}
                  disabled={pagination.page >= pagination.totalPages - 1}
                  className="ml-3 relative inline-flex items-center px-4 py-2 text-sm font-medium text-white bg-secondary-500 border border-secondary-400 rounded-md hover:bg-secondary-400 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  Next
                </button>
              </div>
              <div className="hidden sm:flex-1 sm:flex sm:items-center sm:justify-between">
                <div>
                  <p className="text-sm text-accent-400">
                    Showing{' '}
                    <span className="font-medium">{pagination.page * pagination.size + 1}</span>
                    {' '}to{' '}
                    <span className="font-medium">
                      {Math.min((pagination.page + 1) * pagination.size, pagination.totalElements)}
                    </span>
                    {' '}of{' '}
                    <span className="font-medium">{pagination.totalElements}</span>
                    {' '}results
                  </p>
                </div>
                <div>
                  <nav className="relative z-0 inline-flex rounded-md shadow-sm -space-x-px" aria-label="Pagination">
                    <button
                      onClick={() => handlePageChange(pagination.page - 1)}
                      disabled={pagination.page === 0}
                      className="relative inline-flex items-center px-2 py-2 rounded-l-md border border-secondary-400 bg-secondary-500 text-sm font-medium text-white hover:bg-secondary-400 disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                      <span className="sr-only">Previous</span>
                      <svg className="h-5 w-5" fill="currentColor" viewBox="0 0 20 20">
                        <path fillRule="evenodd" d="M12.707 5.293a1 1 0 010 1.414L9.414 10l3.293 3.293a1 1 0 01-1.414 1.414l-4-4a1 1 0 010-1.414l4-4a1 1 0 011.414 0z" clipRule="evenodd" />
                      </svg>
                    </button>
                    
                    {Array.from({ length: Math.min(5, pagination.totalPages) }, (_, i) => {
                      let pageNumber;
                      if (pagination.totalPages <= 5) {
                        pageNumber = i;
                      } else if (pagination.page < 3) {
                        pageNumber = i;
                      } else if (pagination.page >= pagination.totalPages - 2) {
                        pageNumber = pagination.totalPages - 5 + i;
                      } else {
                        pageNumber = pagination.page - 2 + i;
                      }
                      
                      return (
                        <button
                          key={pageNumber}
                          onClick={() => handlePageChange(pageNumber)}
                          className={`relative inline-flex items-center px-4 py-2 border text-sm font-medium ${
                            pageNumber === pagination.page
                              ? 'z-10 bg-accent-500 border-accent-500 text-white'
                              : 'bg-secondary-500 border-secondary-400 text-white hover:bg-secondary-400'
                          }`}
                        >
                          {pageNumber + 1}
                        </button>
                      );
                    })}
                    
                    <button
                      onClick={() => handlePageChange(pagination.page + 1)}
                      disabled={pagination.page >= pagination.totalPages - 1}
                      className="relative inline-flex items-center px-2 py-2 rounded-r-md border border-secondary-400 bg-secondary-500 text-sm font-medium text-white hover:bg-secondary-400 disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                      <span className="sr-only">Next</span>
                      <svg className="h-5 w-5" fill="currentColor" viewBox="0 0 20 20">
                        <path fillRule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clipRule="evenodd" />
                      </svg>
                    </button>
                  </nav>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default InvestorTable;