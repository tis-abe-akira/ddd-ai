import React, { useState, useEffect } from 'react';
import { syndicateApi } from '../../lib/api';
import type { Syndicate } from '../../types/api';

interface SyndicateSelectProps {
  value?: number;
  onChange: (syndicateId: number | undefined) => void;
  error?: string;
}

const SyndicateSelect: React.FC<SyndicateSelectProps> = ({ value, onChange, error }) => {
  const [syndicates, setSyndicates] = useState<Syndicate[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [isOpen, setIsOpen] = useState(false);

  useEffect(() => {
    fetchSyndicates();
  }, []);

  const fetchSyndicates = async () => {
    try {
      setLoading(true);
      // 全件取得（検索用）
      const response = await syndicateApi.getAll(0, 100);
      setSyndicates(response.data.content);
    } catch (err) {
      console.error('Failed to fetch syndicates:', err);
    } finally {
      setLoading(false);
    }
  };

  const filteredSyndicates = syndicates.filter(syndicate =>
    syndicate.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
    syndicate.id.toString().includes(searchTerm)
  );

  const selectedSyndicate = syndicates.find(s => s.id === value);

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('ja-JP', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  };

  const getStatusColor = (syndicate: Syndicate) => {
    const daysSinceCreation = Math.floor(
      (Date.now() - new Date(syndicate.createdAt).getTime()) / (1000 * 60 * 60 * 24)
    );
    
    if (daysSinceCreation <= 7) {
      return 'text-success';
    } else if (daysSinceCreation <= 30) {
      return 'text-accent-500';
    } else {
      return 'text-accent-400';
    }
  };

  return (
    <div className="relative">
      <label className="block text-sm font-medium text-white mb-2">
        シンジケートを選択 <span className="text-error">*</span>
      </label>
      
      {/* Select Button */}
      <button
        type="button"
        onClick={() => setIsOpen(!isOpen)}
        className={`w-full px-4 py-3 text-left bg-secondary-600 border rounded-lg focus:outline-none focus:ring-2 focus:ring-accent-500 transition-colors ${
          error ? 'border-error' : 'border-secondary-500'
        }`}
      >
        <div className="flex items-center justify-between">
          {selectedSyndicate ? (
            <div className="flex items-center gap-3">
              <div className="w-8 h-8 bg-accent-500 rounded-full flex items-center justify-center text-white text-sm font-bold">
                {selectedSyndicate.name.charAt(0)}
              </div>
              <div>
                <div className="text-white font-medium">{selectedSyndicate.name}</div>
                <div className="text-accent-400 text-sm">
                  ID: {selectedSyndicate.id} | メンバー: {selectedSyndicate.memberInvestorIds.length}名
                </div>
              </div>
              <div className={`ml-auto px-2 py-1 rounded text-xs font-medium ${getStatusColor(selectedSyndicate)}`}>
                組成済み
              </div>
            </div>
          ) : (
            <span className="text-accent-400">シンジケートを選択してください</span>
          )}
          <svg className={`w-5 h-5 text-accent-400 transition-transform ${isOpen ? 'rotate-180' : ''}`} fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
          </svg>
        </div>
      </button>

      {/* Dropdown */}
      {isOpen && (
        <div className="absolute z-10 w-full mt-1 bg-secondary-600 border border-secondary-500 rounded-lg shadow-lg max-h-96 overflow-hidden">
          {/* Search Input */}
          <div className="p-3 border-b border-secondary-500">
            <div className="relative">
              <svg className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-accent-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
              </svg>
              <input
                type="text"
                placeholder="シンジケートを検索..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="w-full pl-10 pr-3 py-2 bg-primary-900 border border-secondary-500 rounded text-white placeholder:text-accent-400 focus:outline-none focus:ring-2 focus:ring-accent-500"
              />
            </div>
          </div>

          {/* Options List */}
          <div className="max-h-60 overflow-y-auto">
            {loading ? (
              <div className="p-4 text-center text-accent-400">読み込み中...</div>
            ) : filteredSyndicates.length === 0 ? (
              <div className="p-4 text-center text-accent-400">
                {searchTerm ? '検索結果が見つかりません' : 'シンジケートが組成されていません'}
              </div>
            ) : (
              filteredSyndicates.map((syndicate) => (
                <button
                  key={syndicate.id}
                  type="button"
                  onClick={() => {
                    onChange(syndicate.id);
                    setIsOpen(false);
                    setSearchTerm('');
                  }}
                  className={`w-full p-3 text-left hover:bg-secondary-500 transition-colors border-b border-secondary-500 last:border-b-0 ${
                    value === syndicate.id ? 'bg-accent-500/20' : ''
                  }`}
                >
                  <div className="flex items-center gap-3">
                    <div className="w-8 h-8 bg-accent-500 rounded-full flex items-center justify-center text-white text-sm font-bold">
                      {syndicate.name.charAt(0)}
                    </div>
                    <div className="flex-1">
                      <div className="text-white font-medium">{syndicate.name}</div>
                      <div className="text-accent-400 text-sm">
                        借り手ID: {syndicate.borrowerId} | リードバンク: {syndicate.leadBankId}
                      </div>
                      <div className="text-accent-400 text-xs">
                        メンバー数: {syndicate.memberInvestorIds.length}名 | 組成日: {formatDate(syndicate.createdAt)}
                      </div>
                    </div>
                    <div className="text-right">
                      <div className={`px-2 py-1 rounded text-xs font-medium ${getStatusColor(syndicate)}`}>
                        ID: {syndicate.id}
                      </div>
                      <div className="text-accent-400 text-xs mt-1">
                        v{syndicate.version}
                      </div>
                    </div>
                  </div>
                </button>
              ))
            )}
          </div>
        </div>
      )}

      {error && (
        <p className="mt-1 text-sm text-error">{error}</p>
      )}

      {/* Overlay to close dropdown */}
      {isOpen && (
        <div 
          className="fixed inset-0 z-5" 
          onClick={() => setIsOpen(false)}
        />
      )}
    </div>
  );
};

export default SyndicateSelect;