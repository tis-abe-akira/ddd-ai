import React, { useState, useEffect } from 'react';
import { borrowerApi } from '../../lib/api';
import type { Borrower } from '../../types/api';

interface BorrowerSelectProps {
  value?: number;
  onChange: (borrowerId: number | undefined) => void;
  error?: string;
}

const BorrowerSelect: React.FC<BorrowerSelectProps> = ({ value, onChange, error }) => {
  const [borrowers, setBorrowers] = useState<Borrower[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [isOpen, setIsOpen] = useState(false);

  useEffect(() => {
    fetchBorrowers();
  }, []);

  const fetchBorrowers = async () => {
    try {
      setLoading(true);
      // 全件取得（検索用）
      const response = await borrowerApi.getAll(0, 100);
      setBorrowers(response.data.content);
    } catch (err) {
      console.error('Failed to fetch borrowers:', err);
    } finally {
      setLoading(false);
    }
  };

  const filteredBorrowers = borrowers.filter(borrower =>
    borrower.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
    borrower.email.toLowerCase().includes(searchTerm.toLowerCase()) ||
    (borrower.companyId && borrower.companyId.toLowerCase().includes(searchTerm.toLowerCase()))
  );

  const selectedBorrower = borrowers.find(b => b.id === value);

  const getCreditRatingColor = (rating: string) => {
    switch (rating) {
      case 'AAA':
      case 'AA':
      case 'A':
        return 'text-success';
      case 'BBB':
      case 'BB':
        return 'text-accent-500';
      case 'B':
      case 'CCC':
        return 'text-yellow-400';
      default:
        return 'text-error';
    }
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('ja-JP', {
      style: 'currency',
      currency: 'JPY',
      minimumFractionDigits: 0,
    }).format(amount);
  };

  return (
    <div className="relative">
      <label className="block text-sm font-medium text-white mb-2">
        Select Borrower <span className="text-error">*</span>
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
          {selectedBorrower ? (
            <div className="flex items-center gap-3">
              <div className="w-8 h-8 bg-accent-500 rounded-full flex items-center justify-center text-white text-sm font-bold">
                {selectedBorrower.name.charAt(0)}
              </div>
              <div>
                <div className="text-white font-medium">{selectedBorrower.name}</div>
                <div className="text-accent-400 text-sm">{selectedBorrower.email}</div>
              </div>
              <div className={`ml-auto px-2 py-1 rounded text-xs font-medium ${getCreditRatingColor(selectedBorrower.creditRating)}`}>
                {selectedBorrower.creditRating}
              </div>
            </div>
          ) : (
            <span className="text-accent-400">Please select a borrower</span>
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
                placeholder="Search borrowers..."
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
            ) : filteredBorrowers.length === 0 ? (
              <div className="p-4 text-center text-accent-400">
                {searchTerm ? 'No search results found' : 'No borrowers registered'}
              </div>
            ) : (
              filteredBorrowers.map((borrower) => (
                <button
                  key={borrower.id}
                  type="button"
                  onClick={() => {
                    onChange(borrower.id);
                    setIsOpen(false);
                    setSearchTerm('');
                  }}
                  className={`w-full p-3 text-left hover:bg-secondary-500 transition-colors border-b border-secondary-500 last:border-b-0 ${
                    value === borrower.id ? 'bg-accent-500/20' : ''
                  }`}
                >
                  <div className="flex items-center gap-3">
                    <div className="w-8 h-8 bg-accent-500 rounded-full flex items-center justify-center text-white text-sm font-bold">
                      {borrower.name.charAt(0)}
                    </div>
                    <div className="flex-1">
                      <div className="text-white font-medium">{borrower.name}</div>
                      <div className="text-accent-400 text-sm">{borrower.email}</div>
                      {borrower.companyId && (
                        <div className="text-accent-400 text-xs">Company ID: {borrower.companyId}</div>
                      )}
                    </div>
                    <div className="text-right">
                      <div className={`px-2 py-1 rounded text-xs font-medium ${getCreditRatingColor(borrower.creditRating)}`}>
                        {borrower.creditRating}
                      </div>
                      <div className="text-accent-400 text-xs mt-1">
                        {formatCurrency(borrower.creditLimit)}
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

export default BorrowerSelect;