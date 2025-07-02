import React, { useState, useEffect } from 'react';
import { syndicateApi } from '../../lib/api';
import type { Syndicate } from '../../types/api';

interface SyndicateTableProps {
  searchTerm?: string;
  onEdit?: (syndicate: Syndicate) => void;
  onDelete?: (syndicate: Syndicate) => void;
  refreshTrigger?: number;
}

const SyndicateTable: React.FC<SyndicateTableProps> = ({
  searchTerm = '',
  onEdit,
  onDelete,
  refreshTrigger = 0
}) => {
  const [syndicates, setSyndicates] = useState<Syndicate[]>([]);
  const [loading, setLoading] = useState(true);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  useEffect(() => {
    fetchSyndicates();
  }, [currentPage, searchTerm, refreshTrigger]);

  const fetchSyndicates = async () => {
    try {
      setLoading(true);
      const response = await syndicateApi.getAll(currentPage, undefined, searchTerm || undefined);
      setSyndicates(response.data.content);
      setTotalPages(response.data.totalPages);
      setTotalElements(response.data.totalElements);
    } catch (error) {
      console.error('Failed to fetch syndicates:', error);
      setSyndicates([]);
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('ja-JP', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  };

  const getStatusBadge = (syndicate: Syndicate) => {
    // シンジケートのステータスを決定するロジック
    // 現在はシンプルにcreatedAtベースで判断
    const daysSinceCreation = Math.floor(
      (Date.now() - new Date(syndicate.createdAt).getTime()) / (1000 * 60 * 60 * 24)
    );
    
    if (daysSinceCreation <= 7) {
      return <span className="inline-flex px-2 py-1 text-xs font-medium bg-success/20 text-success rounded-full">新規</span>;
    } else if (daysSinceCreation <= 30) {
      return <span className="inline-flex px-2 py-1 text-xs font-medium bg-accent-500/20 text-accent-500 rounded-full">進行中</span>;
    } else {
      return <span className="inline-flex px-2 py-1 text-xs font-medium bg-secondary-600 text-accent-400 rounded-full">確定済み</span>;
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center py-12">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-accent-500"></div>
        <span className="ml-3 text-accent-400">読み込み中...</span>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {/* Table Header */}
      <div className="flex items-center justify-between">
        <div className="text-sm text-accent-400">
          {totalElements}件のシンジケート
        </div>
        <div className="text-sm text-accent-400">
          ページ {currentPage + 1} / {Math.max(totalPages, 1)}
        </div>
      </div>

      {/* Table */}
      <div className="bg-primary-900 border border-secondary-500 rounded-xl overflow-hidden">
        {syndicates.length === 0 ? (
          <div className="p-12 text-center">
            <div className="text-accent-400 text-lg mb-2">
              {searchTerm ? '検索結果が見つかりません' : 'シンジケートが登録されていません'}
            </div>
            <div className="text-accent-400 text-sm">
              {searchTerm ? '別のキーワードで検索してみてください' : '新しいシンジケートを組成してください'}
            </div>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead>
                <tr className="border-b border-secondary-500">
                  <th className="px-6 py-4 text-left text-xs font-medium text-accent-400 uppercase tracking-wider">
                    シンジケート名
                  </th>
                  <th className="px-6 py-4 text-left text-xs font-medium text-accent-400 uppercase tracking-wider">
                    借り手ID
                  </th>
                  <th className="px-6 py-4 text-left text-xs font-medium text-accent-400 uppercase tracking-wider">
                    リードバンクID
                  </th>
                  <th className="px-6 py-4 text-left text-xs font-medium text-accent-400 uppercase tracking-wider">
                    メンバー数
                  </th>
                  <th className="px-6 py-4 text-left text-xs font-medium text-accent-400 uppercase tracking-wider">
                    ステータス
                  </th>
                  <th className="px-6 py-4 text-left text-xs font-medium text-accent-400 uppercase tracking-wider">
                    組成日
                  </th>
                  <th className="px-6 py-4 text-left text-xs font-medium text-accent-400 uppercase tracking-wider">
                    操作
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-secondary-500">
                {syndicates.map((syndicate) => (
                  <tr key={syndicate.id} className="hover:bg-secondary-600/50 transition-colors">
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex items-center">
                        <div className="w-10 h-10 bg-accent-500 rounded-full flex items-center justify-center text-white font-bold text-sm">
                          {syndicate.name.charAt(0)}
                        </div>
                        <div className="ml-3">
                          <div className="text-white font-medium">{syndicate.name}</div>
                          <div className="text-accent-400 text-sm">ID: {syndicate.id}</div>
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-white font-mono">{syndicate.borrowerId}</div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-white font-mono">{syndicate.leadBankId}</div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex items-center">
                        <span className="text-white font-medium">{syndicate.memberInvestorIds.length}</span>
                        <span className="text-accent-400 text-sm ml-1">名</span>
                      </div>
                      <div className="text-accent-400 text-xs">
                        IDs: {syndicate.memberInvestorIds.slice(0, 3).join(', ')}
                        {syndicate.memberInvestorIds.length > 3 && ` +${syndicate.memberInvestorIds.length - 3}`}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      {getStatusBadge(syndicate)}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-white">{formatDate(syndicate.createdAt)}</div>
                      <div className="text-accent-400 text-xs">
                        v{syndicate.version}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex items-center gap-2">
                        {onEdit && (
                          <button
                            onClick={() => onEdit(syndicate)}
                            className="p-2 text-accent-400 hover:text-accent-300 hover:bg-secondary-600 rounded-lg transition-colors"
                            title="編集"
                          >
                            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                            </svg>
                          </button>
                        )}
                        {onDelete && (
                          <button
                            onClick={() => onDelete(syndicate)}
                            className="p-2 text-error hover:text-red-400 hover:bg-error/10 rounded-lg transition-colors"
                            title="削除"
                          >
                            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                            </svg>
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="flex items-center justify-between">
          <div className="text-sm text-accent-400">
            {syndicates.length}件表示中
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

export default SyndicateTable;