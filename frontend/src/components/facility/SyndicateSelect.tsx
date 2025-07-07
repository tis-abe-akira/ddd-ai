import React, { useState, useEffect } from 'react';
import { syndicateApi } from '../../lib/api';
import type { Syndicate } from '../../types/api';

interface SyndicateSelectProps {
  value?: number;
  onChange: (syndicateId: number | undefined) => void;
  error?: string;
  disabled?: boolean;
}

const SyndicateSelect: React.FC<SyndicateSelectProps> = ({ value, onChange, error, disabled = false }) => {
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
    // DRAFTステータスのSyndicateのみ表示（1 Syndicate = 1 Facilityのビジネスルール）
    syndicate.status === 'DRAFT' &&
    (syndicate.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
     syndicate.id.toString().includes(searchTerm))
  );

  const selectedSyndicate = syndicates.find(s => s.id === value);

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  };

  const getStatusDisplay = (syndicate: Syndicate) => {
    switch (syndicate.status) {
      case 'DRAFT':
        return { text: 'Draft', color: 'text-success', tooltip: 'Available for facility creation' };
      case 'ACTIVE':
        return { text: 'Active', color: 'text-accent-500', tooltip: 'Already used for facility creation' };
      case 'CLOSED':
        return { text: 'Closed', color: 'text-accent-400', tooltip: 'No longer active' };
      default:
        return { text: 'Unknown', color: 'text-accent-400', tooltip: 'Unknown status' };
    }
  };

  return (
    <div className="relative">
      <label className="block text-sm font-medium text-white mb-2">
        Select Syndicate <span className="text-error">*</span>
      </label>
      
      {/* Select Button */}
      <button
        type="button"
        onClick={() => !disabled && setIsOpen(!isOpen)}
        disabled={disabled}
        className={`w-full px-4 py-3 text-left border rounded-lg focus:outline-none transition-colors ${
          disabled 
            ? 'bg-secondary-700 border-secondary-600 cursor-not-allowed opacity-60' 
            : `bg-secondary-600 border-secondary-500 focus:ring-2 focus:ring-accent-500 ${error ? 'border-error' : ''}`
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
                  ID: {selectedSyndicate.id} | Members: {selectedSyndicate.memberInvestorIds.length}
                </div>
              </div>
              <div 
                className={`ml-auto px-2 py-1 rounded text-xs font-medium cursor-help ${getStatusDisplay(selectedSyndicate).color}`}
                title={getStatusDisplay(selectedSyndicate).tooltip}
              >
                {getStatusDisplay(selectedSyndicate).text}
              </div>
            </div>
          ) : (
            <span className="text-accent-400">Please select a syndicate</span>
          )}
          <svg className={`w-5 h-5 text-accent-400 transition-transform ${isOpen ? 'rotate-180' : ''}`} fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
          </svg>
        </div>
      </button>

      {/* Dropdown */}
      {isOpen && !disabled && (
        <div className="absolute z-10 w-full mt-1 bg-secondary-600 border border-secondary-500 rounded-lg shadow-lg max-h-96 overflow-hidden">
          {/* Search Input */}
          <div className="p-3 border-b border-secondary-500">
            <div className="relative">
              <svg className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-accent-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
              </svg>
              <input
                type="text"
                placeholder="Search syndicates..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="w-full pl-10 pr-3 py-2 bg-primary-900 border border-secondary-500 rounded text-white placeholder:text-accent-400 focus:outline-none focus:ring-2 focus:ring-accent-500"
              />
            </div>
          </div>

          {/* Options List */}
          <div className="max-h-60 overflow-y-auto">
            {loading ? (
              <div className="p-4 text-center text-accent-400">Loading...</div>
            ) : filteredSyndicates.length === 0 ? (
              <div className="p-4 text-center text-accent-400">
                {searchTerm ? 'No search results found' : 'No syndicates available for facility creation'}
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
                        Borrower ID: {syndicate.borrowerId} | Lead Bank: {syndicate.leadBankId}
                      </div>
                      <div className="text-accent-400 text-xs">
                        Members: {syndicate.memberInvestorIds.length} | Created: {formatDate(syndicate.createdAt)}
                      </div>
                    </div>
                    <div className="text-right">
                      <div 
                        className={`px-2 py-1 rounded text-xs font-medium cursor-help ${getStatusDisplay(syndicate).color}`}
                        title={getStatusDisplay(syndicate).tooltip}
                      >
                        {getStatusDisplay(syndicate).text}
                      </div>
                      <div className="text-accent-400 text-xs mt-1">
                        ID: {syndicate.id} | v{syndicate.version}
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