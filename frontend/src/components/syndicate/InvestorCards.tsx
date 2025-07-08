import React, { useState, useEffect } from 'react';
import { investorApi } from '../../lib/api';
import { investorTypeLabels } from '../../schemas/investor';
import type { Investor } from '../../types/api';

interface InvestorCardsProps {
  mode: 'lead-bank' | 'members';
  selectedLeadBank?: number;
  selectedMembers?: number[];
  onLeadBankChange?: (leadBankId: number) => void;
  onMembersChange?: (memberIds: number[]) => void;
  error?: string;
}

const InvestorCards: React.FC<InvestorCardsProps> = ({
  mode,
  selectedLeadBank,
  selectedMembers = [],
  onLeadBankChange,
  onMembersChange,
  error
}) => {
  const [investors, setInvestors] = useState<Investor[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchInvestors();
  }, []);

  const fetchInvestors = async () => {
    try {
      setLoading(true);
      // 全件取得（選択用）
      const response = await investorApi.getAll(0, 100);
      setInvestors(response.data.content.filter(investor => investor.status === 'ACTIVE'));
    } catch (err) {
      console.error('Failed to fetch investors:', err);
    } finally {
      setLoading(false);
    }
  };

  // Lead Bank選択の場合はLEAD_BANKタイプのみフィルタ（より厳密に）
  const filteredInvestors = mode === 'lead-bank'
    ? investors.filter(investor => 
        investor.investorType === 'LEAD_BANK'
      )
    : investors;

  const getInvestorTypeColor = (type: string) => {
    switch (type) {
      case 'LEAD_BANK':
        return 'bg-accent-500/20 text-accent-500';
      case 'BANK':
        return 'bg-blue-500/20 text-blue-400';
      case 'INSURANCE':
        return 'bg-purple-500/20 text-purple-400';
      case 'FUND':
        return 'bg-success/20 text-success';
      case 'GOVERNMENT':
        return 'bg-yellow-500/20 text-yellow-400';
      default:
        return 'bg-secondary-600 text-accent-400';
    }
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('ja-JP', {
      style: 'currency',
      currency: 'JPY',
      minimumFractionDigits: 0,
      notation: 'compact'
    }).format(amount);
  };

  const getCapacityPercentage = (current: number, capacity: number) => {
    return Math.min((current / capacity) * 100, 100);
  };

  const handleCardClick = (investorId: number) => {
    if (mode === 'lead-bank') {
      // リードバンク選択時の即時バリデーション
      const selectedInvestor = investors.find(inv => inv.id === investorId);
      if (selectedInvestor && selectedInvestor.investorType !== 'LEAD_BANK') {
        // 不適切な選択の場合はエラーを表示（実際にはフィルタリングされているので発生しないはず）
        console.error('Invalid lead bank selection:', selectedInvestor);
        return;
      }
      onLeadBankChange?.(investorId);
    } else {
      // Members selection
      const newSelection = selectedMembers.includes(investorId)
        ? selectedMembers.filter(id => id !== investorId)
        : [...selectedMembers, investorId];
      onMembersChange?.(newSelection);
    }
  };

  const isSelected = (investorId: number) => {
    if (mode === 'lead-bank') {
      return selectedLeadBank === investorId;
    }
    return selectedMembers.includes(investorId);
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
    <div>
      <label className="block text-sm font-medium text-white mb-4">
        {mode === 'lead-bank' ? 'Select Lead Bank' : 'Select Member Investors'} <span className="text-error">*</span>
      </label>

      {filteredInvestors.length === 0 ? (
        <div className="text-center py-8 text-accent-400">
          {mode === 'lead-bank' ? 'No LEAD_BANK type investors found' : 'No investors found'}
          {mode === 'lead-bank' && (
            <div className="text-xs mt-2 text-accent-500">
              Only LEAD_BANK type investors can become lead banks
            </div>
          )}
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {filteredInvestors.map((investor) => {
            const selected = isSelected(investor.id);
            const capacityPercentage = getCapacityPercentage(
              investor.currentInvestmentAmount,
              investor.investmentCapacity
            );

            return (
              <div
                key={investor.id}
                onClick={() => handleCardClick(investor.id)}
                className={`relative p-4 border rounded-lg cursor-pointer transition-all duration-200 hover:scale-105 ${
                  selected
                    ? 'border-accent-500 bg-accent-500/10 ring-2 ring-accent-500/30'
                    : 'border-secondary-500 bg-secondary-600 hover:border-accent-400'
                }`}
              >
                {/* Selection Indicator */}
                <div className="absolute top-3 right-3">
                  {mode === 'lead-bank' ? (
                    <div className={`w-4 h-4 rounded-full border-2 ${
                      selected ? 'bg-accent-500 border-accent-500' : 'border-secondary-400'
                    }`}>
                      {selected && (
                        <div className="w-full h-full flex items-center justify-center">
                          <div className="w-2 h-2 bg-white rounded-full"></div>
                        </div>
                      )}
                    </div>
                  ) : (
                    <div className={`w-4 h-4 border-2 rounded ${
                      selected ? 'bg-accent-500 border-accent-500' : 'border-secondary-400'
                    }`}>
                      {selected && (
                        <svg className="w-full h-full text-white" fill="currentColor" viewBox="0 0 20 20">
                          <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                        </svg>
                      )}
                    </div>
                  )}
                </div>

                {/* Investor Info */}
                <div className="pr-8">
                  {/* Avatar and Name */}
                  <div className="flex items-center gap-3 mb-3">
                    <div className="w-10 h-10 bg-accent-500 rounded-full flex items-center justify-center text-white font-bold">
                      {investor.name.charAt(0)}
                    </div>
                    <div>
                      <div className="text-white font-medium text-sm">{investor.name}</div>
                      <div className="text-accent-400 text-xs">ID: {investor.id}</div>
                    </div>
                  </div>

                  {/* Investor Type */}
                  <div className="mb-3">
                    <span className={`inline-flex px-2 py-1 text-xs font-medium rounded-full ${getInvestorTypeColor(investor.investorType)}`}>
                      {investorTypeLabels[investor.investorType]}
                    </span>
                  </div>

                  {/* Investment Capacity */}
                  <div className="mb-2">
                    <div className="flex justify-between text-xs text-accent-400 mb-1">
                      <span>Investment Capacity</span>
                      <span>{Math.round(capacityPercentage)}%</span>
                    </div>
                    <div className="w-full bg-primary-900 rounded-full h-2">
                      <div 
                        className="bg-accent-500 h-2 rounded-full transition-all duration-300"
                        style={{ width: `${capacityPercentage}%` }}
                      ></div>
                    </div>
                    <div className="flex justify-between text-xs text-accent-400 mt-1">
                      <span>Current: {formatCurrency(investor.currentInvestmentAmount)}</span>
                      <span>Limit: {formatCurrency(investor.investmentCapacity)}</span>
                    </div>
                  </div>

                  {/* Contact */}
                  <div className="text-xs text-accent-400 truncate">
                    {investor.email}
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Selection Summary */}
      {mode === 'members' && selectedMembers.length > 0 && (
        <div className="mt-4 p-3 bg-accent-500/10 border border-accent-500/30 rounded-lg">
          <div className="text-sm text-accent-500 font-medium">
            Selected: {selectedMembers.length} member investor(s)
          </div>
          <div className="text-xs text-accent-400 mt-1">
            {selectedMembers.map(id => {
              const investor = investors.find(inv => inv.id === id);
              return investor?.name;
            }).join(', ')}
          </div>
        </div>
      )}

      {error && (
        <p className="mt-2 text-sm text-error">{error}</p>
      )}
    </div>
  );
};

export default InvestorCards;