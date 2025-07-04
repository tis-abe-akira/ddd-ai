import React, { useState, useEffect } from 'react';
import { borrowerApi } from '../../lib/api';
import type { Borrower } from '../../types/api';

interface BorrowerListProps {
  onRefresh?: () => void;
  refreshTrigger?: number;
}

interface PaginationInfo {
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

const BorrowerList: React.FC<BorrowerListProps> = ({ onRefresh, refreshTrigger }) => {
  const [borrowers, setBorrowers] = useState<Borrower[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [pagination, setPagination] = useState<PaginationInfo>({
    page: 0,
    size: 10,
    totalElements: 0,
    totalPages: 0,
  });

  const fetchBorrowers = async (page: number = 0, size: number = 10) => {
    try {
      setLoading(true);
      setError(null);
      
      const response = await borrowerApi.getAll(page, size);
      const pageData = response.data;
      
      setBorrowers(pageData.content);
      setPagination({
        page: pageData.number,
        size: pageData.size,
        totalElements: pageData.totalElements,
        totalPages: pageData.totalPages,
      });
    } catch (err: any) {
      setError(err.message || 'Borrower一覧の取得に失敗しました');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchBorrowers(pagination.page, pagination.size);
  }, [refreshTrigger]);

  const handlePageChange = (newPage: number) => {
    if (newPage >= 0 && newPage < pagination.totalPages) {
      fetchBorrowers(newPage, pagination.size);
    }
  };

  const getCreditRatingColor = (rating: string) => {
    switch (rating) {
      case 'AAA':
      case 'AA':
      case 'A':
        return 'bg-success/20 text-success border-success/30';
      case 'BBB':
      case 'BB':
        return 'bg-accent-500/20 text-accent-400 border-accent-500/30';
      case 'B':
      case 'CCC':
        return 'bg-yellow-500/20 text-yellow-400 border-yellow-500/30';
      default:
        return 'bg-error/20 text-error border-error/30';
    }
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('ja-JP', {
      style: 'currency',
      currency: 'JPY',
      minimumFractionDigits: 0,
    }).format(amount);
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center py-12">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-accent-500"></div>
        <span className="ml-3 text-accent-400">読み込み中...</span>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-error/10 border border-error/20 rounded-lg p-4">
        <p className="text-error">{error}</p>
        <button
          onClick={() => fetchBorrowers(pagination.page, pagination.size)}
          className="mt-2 text-sm text-accent-500 hover:text-accent-400 underline"
        >
          再試行
        </button>
      </div>
    );
  }

  if (borrowers.length === 0) {
    return (
      <div className="bg-primary-900 border border-secondary-500 rounded-xl p-8 text-center">
        <svg className="w-16 h-16 text-accent-400 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
        </svg>
        <h3 className="text-xl font-semibold text-white mb-2">借り手が見つかりません</h3>
        <p className="text-accent-400">まだ借り手が登録されていません。</p>
      </div>
    );
  }

  return (
    <div>
      {/* Borrower Cards Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-6">
        {borrowers.map((borrower, index) => {
          const cardColors = [
            'from-pink-300 via-purple-300 to-indigo-400',
            'from-green-300 via-blue-300 to-purple-400', 
            'from-yellow-300 via-red-300 to-pink-400',
            'from-blue-300 via-green-300 to-yellow-400',
            'from-purple-300 via-pink-300 to-red-400',
            'from-indigo-300 via-purple-300 to-pink-400'
          ];
          const gradientClass = cardColors[index % cardColors.length];
          
          return (
          <div
            key={borrower.id}
            className="relative overflow-hidden rounded-xl p-6 hover:scale-105 transition-transform duration-300 group cursor-pointer shadow-lg"
            style={{
              background: `linear-gradient(135deg, rgba(255,182,193,0.8), rgba(221,160,221,0.8), rgba(173,216,230,0.8))`,
              backdropFilter: 'blur(10px)',
            }}
          >
            {/* Header with Name and ID */}
            <div className="flex items-start justify-between mb-4">
              <div className="flex-1">
                <h3 className="text-lg font-semibold text-white mb-1 group-hover:text-accent-400 transition-colors">
                  {borrower.name}
                </h3>
                <p className="text-sm text-accent-400">ID: {borrower.id}</p>
              </div>
              <div className={`px-3 py-1 rounded-full text-xs font-medium border ${getCreditRatingColor(borrower.creditRating)}`}>
                {borrower.creditRating}
              </div>
            </div>

            {/* Contact Info */}
            <div className="space-y-2 mb-4">
              <div className="flex items-center gap-2">
                <svg className="w-4 h-4 text-accent-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 8l7.89 4.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                </svg>
                <span className="text-sm text-white truncate">{borrower.email}</span>
              </div>
              
              <div className="flex items-center gap-2">
                <svg className="w-4 h-4 text-accent-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" />
                </svg>
                <span className="text-sm text-white">{borrower.phoneNumber}</span>
              </div>

              {borrower.companyId && (
                <div className="flex items-center gap-2">
                  <svg className="w-4 h-4 text-accent-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
                  </svg>
                  <span className="text-sm text-accent-400">会社ID: {borrower.companyId}</span>
                </div>
              )}
            </div>

            {/* Credit Limit */}
            <div className="pt-4 border-t border-secondary-500">
              <div className="flex items-center justify-between">
                <span className="text-sm text-accent-400">信用限度額</span>
                <span className="text-lg font-semibold text-accent-500">
                  {formatCurrency(borrower.creditLimit)}
                </span>
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* Pagination */}
      {pagination.totalPages > 1 && (
        <div className="flex items-center justify-between bg-primary-900 border border-secondary-500 rounded-lg p-4">
          <div className="text-sm text-accent-400">
            {pagination.totalElements}件中 {pagination.page * pagination.size + 1}-
            {Math.min((pagination.page + 1) * pagination.size, pagination.totalElements)}件を表示
          </div>
          
          <div className="flex items-center gap-2">
            <button
              onClick={() => handlePageChange(pagination.page - 1)}
              disabled={pagination.page === 0}
              className="px-3 py-2 text-sm font-medium text-white bg-secondary-600 rounded-lg hover:bg-secondary-500 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
            >
              前へ
            </button>
            
            <div className="flex items-center gap-1">
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
                    className={`px-3 py-2 text-sm font-medium rounded-lg transition-colors ${
                      pageNumber === pagination.page
                        ? 'bg-accent-500 text-primary-900'
                        : 'text-white bg-secondary-600 hover:bg-secondary-500'
                    }`}
                  >
                    {pageNumber + 1}
                  </button>
                );
              })}
            </div>
            
            <button
              onClick={() => handlePageChange(pagination.page + 1)}
              disabled={pagination.page >= pagination.totalPages - 1}
              className="px-3 py-2 text-sm font-medium text-white bg-secondary-600 rounded-lg hover:bg-secondary-500 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
            >
              次へ
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default BorrowerList;