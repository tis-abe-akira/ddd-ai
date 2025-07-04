import React, { useState, useEffect } from 'react';
import { investorApi } from '../../lib/api';
import type { Investor } from '../../types/api';
import type { SharePieFormData } from '../../schemas/facility';

interface SharePieAllocationProps {
  value: SharePieFormData[];
  onChange: (sharePies: SharePieFormData[]) => void;
  syndicateId?: number;
  error?: string;
}

const SharePieAllocation: React.FC<SharePieAllocationProps> = ({
  value,
  onChange,
  syndicateId,
  error
}) => {
  const [investors, setInvestors] = useState<Investor[]>([]);
  const [loading, setLoading] = useState(true);
  const [showAddForm, setShowAddForm] = useState(false);
  const [selectedInvestorId, setSelectedInvestorId] = useState<number | undefined>();
  const [sharePercentage, setSharePercentage] = useState<string>('');

  useEffect(() => {
    fetchInvestors();
  }, []);

  const fetchInvestors = async () => {
    try {
      setLoading(true);
      // 全件取得（選択用）
      const response = await investorApi.getAll(0, 100);
      setInvestors(response.data.content.filter(investor => investor.isActive));
    } catch (err) {
      console.error('Failed to fetch investors:', err);
    } finally {
      setLoading(false);
    }
  };

  // 既に選択されている投資家IDのリスト
  const selectedInvestorIds = value.map(pie => pie.investorId);
  
  // まだ選択されていない投資家のリスト
  const availableInvestors = investors.filter(
    investor => !selectedInvestorIds.includes(investor.id)
  );

  // 合計持分比率の計算
  const totalShare = value.reduce((sum, pie) => sum + pie.share, 0);
  const totalPercentage = Math.round(totalShare * 100);

  const getInvestorName = (investorId: number) => {
    return investors.find(inv => inv.id === investorId)?.name || `投資家ID: ${investorId}`;
  };

  const getInvestorTypeColor = (investorId: number) => {
    const investor = investors.find(inv => inv.id === investorId);
    if (!investor) return 'bg-secondary-600 text-accent-400';
    
    switch (investor.investorType) {
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

  const handleAddSharePie = () => {
    if (!selectedInvestorId || !sharePercentage) return;
    
    const shareDecimal = parseFloat(sharePercentage) / 100;
    if (isNaN(shareDecimal) || shareDecimal <= 0 || shareDecimal > 1) return;

    const newSharePie: SharePieFormData = {
      investorId: selectedInvestorId,
      share: shareDecimal
    };

    onChange([...value, newSharePie]);
    setSelectedInvestorId(undefined);
    setSharePercentage('');
    setShowAddForm(false);
  };

  const handleRemoveSharePie = (index: number) => {
    const newSharePies = value.filter((_, i) => i !== index);
    onChange(newSharePies);
  };

  const handleUpdateShare = (index: number, newPercentage: string) => {
    const shareDecimal = parseFloat(newPercentage) / 100;
    if (isNaN(shareDecimal)) return;

    const newSharePies = [...value];
    newSharePies[index] = { ...newSharePies[index], share: shareDecimal };
    onChange(newSharePies);
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('ja-JP', {
      style: 'currency',
      currency: 'JPY',
      minimumFractionDigits: 0,
      notation: 'compact'
    }).format(amount);
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
    <div className="space-y-6">
      <div>
        <label className="block text-sm font-medium text-white mb-4">
          Investor Share Allocation <span className="text-error">*</span>
        </label>

        {/* Current Allocations */}
        <div className="space-y-3">
          {value.map((pie, index) => {
            const investor = investors.find(inv => inv.id === pie.investorId);
            return (
              <div key={index} className="bg-secondary-600 rounded-lg p-4 border border-secondary-500">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-3 flex-1">
                    <div className="w-10 h-10 bg-accent-500 rounded-full flex items-center justify-center text-white font-bold text-sm">
                      {getInvestorName(pie.investorId).charAt(0)}
                    </div>
                    <div className="flex-1">
                      <div className="text-white font-medium">{getInvestorName(pie.investorId)}</div>
                      <div className="text-accent-400 text-sm">ID: {pie.investorId}</div>
                      {investor && (
                        <div className="flex items-center gap-2 mt-1">
                          <span className={`inline-flex px-2 py-1 text-xs font-medium rounded-full ${getInvestorTypeColor(pie.investorId)}`}>
                            {investor.investorType}
                          </span>
                          <span className="text-accent-400 text-xs">
                            Investment Capacity: {formatCurrency(investor.investmentCapacity)}
                          </span>
                        </div>
                      )}
                    </div>
                  </div>
                  
                  <div className="flex items-center gap-3">
                    <div className="flex items-center gap-2">
                      <input
                        type="number"
                        value={Math.round(pie.share * 100)}
                        onChange={(e) => handleUpdateShare(index, e.target.value)}
                        min="1"
                        max="100"
                        className="w-20 px-2 py-1 bg-primary-900 border border-secondary-500 rounded text-white text-center focus:outline-none focus:ring-2 focus:ring-accent-500"
                      />
                      <span className="text-accent-400 text-sm">%</span>
                    </div>
                    
                    <button
                      type="button"
                      onClick={() => handleRemoveSharePie(index)}
                      className="p-2 text-error hover:text-red-400 hover:bg-error/10 rounded-lg transition-colors"
                      title="Remove"
                    >
                      <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                      </svg>
                    </button>
                  </div>
                </div>
              </div>
            );
          })}
        </div>

        {/* Add New Allocation */}
        {!showAddForm && availableInvestors.length > 0 && (
          <button
            type="button"
            onClick={() => setShowAddForm(true)}
            className="w-full mt-4 p-4 border-2 border-dashed border-secondary-500 rounded-lg text-accent-400 hover:border-accent-500 hover:text-accent-300 transition-colors flex items-center justify-center gap-2"
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
            </svg>
            Add Investor
          </button>
        )}

        {/* Add Form */}
        {showAddForm && (
          <div className="mt-4 p-4 bg-accent-500/10 border border-accent-500/30 rounded-lg">
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div className="md:col-span-2">
                <label className="block text-sm font-medium text-white mb-2">投資家</label>
                <select
                  value={selectedInvestorId || ''}
                  onChange={(e) => setSelectedInvestorId(e.target.value ? Number(e.target.value) : undefined)}
                  className="w-full px-3 py-2 bg-secondary-600 border border-secondary-500 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-accent-500"
                >
                  <option value="">投資家を選択...</option>
                  {availableInvestors.map((investor) => (
                    <option key={investor.id} value={investor.id}>
                      {investor.name} (ID: {investor.id})
                    </option>
                  ))}
                </select>
              </div>
              
              <div>
                <label className="block text-sm font-medium text-white mb-2">持分比率 (%)</label>
                <input
                  type="number"
                  value={sharePercentage}
                  onChange={(e) => setSharePercentage(e.target.value)}
                  placeholder="例: 25"
                  min="1"
                  max="100"
                  className="w-full px-3 py-2 bg-secondary-600 border border-secondary-500 rounded-lg text-white placeholder:text-accent-400 focus:outline-none focus:ring-2 focus:ring-accent-500"
                />
              </div>
            </div>
            
            <div className="flex gap-3 mt-4">
              <button
                type="button"
                onClick={handleAddSharePie}
                disabled={!selectedInvestorId || !sharePercentage}
                className="bg-accent-500 hover:bg-accent-400 disabled:opacity-50 disabled:cursor-not-allowed text-white font-semibold py-2 px-4 rounded-lg transition-colors"
              >
                追加
              </button>
              <button
                type="button"
                onClick={() => {
                  setShowAddForm(false);
                  setSelectedInvestorId(undefined);
                  setSharePercentage('');
                }}
                className="bg-secondary-600 hover:bg-secondary-500 text-white font-semibold py-2 px-4 rounded-lg transition-colors"
              >
                キャンセル
              </button>
            </div>
          </div>
        )}
      </div>

      {/* Total Summary */}
      <div className={`p-4 rounded-lg border ${
        Math.abs(totalPercentage - 100) < 1 
          ? 'bg-success/10 border-success/30' 
          : 'bg-warning/10 border-warning/30'
      }`}>
        <div className="flex items-center justify-between">
          <span className="text-white font-medium">持分比率合計</span>
          <div className="flex items-center gap-2">
            <span className={`text-lg font-bold ${
              Math.abs(totalPercentage - 100) < 1 ? 'text-success' : 'text-warning'
            }`}>
              {totalPercentage}%
            </span>
            {Math.abs(totalPercentage - 100) < 1 ? (
              <svg className="w-5 h-5 text-success" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
              </svg>
            ) : (
              <svg className="w-5 h-5 text-warning" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.664-.833-2.464 0L3.34 16.5c-.77.833.192 2.5 1.732 2.5z" />
              </svg>
            )}
          </div>
        </div>
        <div className="text-accent-400 text-sm mt-1">
          {Math.abs(totalPercentage - 100) < 1 
            ? '持分比率の合計が100%に達しました' 
            : `合計が100%になるように調整してください（現在: ${totalPercentage - 100 > 0 ? '+' : ''}${totalPercentage - 100}%）`
          }
        </div>
      </div>

      {error && (
        <p className="text-sm text-error">{error}</p>
      )}
    </div>
  );
};

export default SharePieAllocation;