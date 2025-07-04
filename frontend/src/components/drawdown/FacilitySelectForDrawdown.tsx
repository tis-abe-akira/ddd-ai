import React, { useState, useEffect } from 'react';
import { facilityApi } from '../../lib/api';
import type { Facility } from '../../types/api';

interface FacilitySelectForDrawdownProps {
  value?: number;
  onChange: (facilityId: number | undefined, facility?: Facility) => void;
  error?: string;
}

const FacilitySelectForDrawdown: React.FC<FacilitySelectForDrawdownProps> = ({ 
  value, 
  onChange, 
  error 
}) => {
  const [facilities, setFacilities] = useState<Facility[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [isOpen, setIsOpen] = useState(false);

  useEffect(() => {
    fetchFacilities();
  }, []);

  const fetchFacilities = async () => {
    try {
      setLoading(true);
      // 全件取得（検索用）
      const response = await facilityApi.getAll(0, 100);
      // DRAFT状態のファシリティのみフィルタ（ドローダウン実行可能）
      setFacilities(response.data.content.filter(facility => facility.status === 'DRAFT'));
    } catch (err) {
      console.error('Failed to fetch facilities:', err);
    } finally {
      setLoading(false);
    }
  };

  const filteredFacilities = facilities.filter(facility =>
    facility.id.toString().includes(searchTerm) ||
    facility.interestTerms.toLowerCase().includes(searchTerm.toLowerCase()) ||
    facility.syndicateId.toString().includes(searchTerm)
  );

  const selectedFacility = facilities.find(f => f.id === value);

  const formatCurrency = (amount: number, currency: string) => {
    return new Intl.NumberFormat('ja-JP', {
      style: 'currency',
      currency: currency,
      minimumFractionDigits: 0,
      notation: 'compact'
    }).format(amount);
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('ja-JP', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  };

  // 利用可能額を計算（将来的にはAPIから取得）
  const getAvailableAmount = (facility: Facility) => {
    // 現在は簡易実装：Commitment全額が利用可能として表示
    return facility.commitment;
  };

  const getUtilizationPercentage = (facility: Facility) => {
    // 現在は簡易実装：0%として表示（将来的には実際の利用額から計算）
    return 0;
  };

  return (
    <div className="relative">
      <label className="block text-sm font-medium text-white mb-2">
        ファシリティを選択 <span className="text-error">*</span>
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
          {selectedFacility ? (
            <div className="flex items-center gap-3">
              <div className="w-8 h-8 bg-accent-500 rounded-full flex items-center justify-center text-white text-sm font-bold">
                F{selectedFacility.id}
              </div>
              <div>
                <div className="text-white font-medium">
                  Facility #{selectedFacility.id}
                </div>
                <div className="text-accent-400 text-sm">
                  {formatCurrency(selectedFacility.commitment, selectedFacility.currency)} | {selectedFacility.interestTerms}
                </div>
              </div>
              <div className="ml-auto flex flex-col items-end">
                <div className="text-success text-sm font-medium">
                  利用可能: {formatCurrency(getAvailableAmount(selectedFacility), selectedFacility.currency)}
                </div>
                <div className="text-accent-400 text-xs">
                  利用率: {getUtilizationPercentage(selectedFacility)}%
                </div>
              </div>
            </div>
          ) : (
            <span className="text-accent-400">ファシリティを選択してください</span>
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
                placeholder="ファシリティを検索..."
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
            ) : filteredFacilities.length === 0 ? (
              <div className="p-4 text-center text-accent-400">
                {searchTerm ? '検索結果が見つかりません' : 'ドローダウン可能なファシリティがありません'}
                {!searchTerm && (
                  <div className="text-xs mt-2 text-accent-500">
                    ドローダウンはDRAFT状態のファシリティでのみ実行できます
                  </div>
                )}
              </div>
            ) : (
              filteredFacilities.map((facility) => (
                <button
                  key={facility.id}
                  type="button"
                  onClick={() => {
                    onChange(facility.id, facility);
                    setIsOpen(false);
                    setSearchTerm('');
                  }}
                  className={`w-full p-3 text-left hover:bg-secondary-500 transition-colors border-b border-secondary-500 last:border-b-0 ${
                    value === facility.id ? 'bg-accent-500/20' : ''
                  }`}
                >
                  <div className="flex items-center gap-3">
                    <div className="w-8 h-8 bg-accent-500 rounded-full flex items-center justify-center text-white text-sm font-bold">
                      F{facility.id}
                    </div>
                    <div className="flex-1">
                      <div className="text-white font-medium">Facility #{facility.id}</div>
                      <div className="text-accent-400 text-sm">
                        Syndicate #{facility.syndicateId} | {facility.interestTerms}
                      </div>
                      <div className="text-accent-400 text-xs">
                        期間: {formatDate(facility.startDate)} 〜 {formatDate(facility.endDate)}
                      </div>
                    </div>
                    <div className="text-right">
                      <div className="text-white font-bold">
                        {formatCurrency(facility.commitment, facility.currency)}
                      </div>
                      <div className="text-success text-sm">
                        利用可能: {formatCurrency(getAvailableAmount(facility), facility.currency)}
                      </div>
                      <div className="text-accent-400 text-xs">
                        投資家: {facility.sharePies?.length || 0}名
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

      {/* Selected Facility Details */}
      {selectedFacility && (
        <div className="mt-4 p-4 bg-accent-500/10 border border-accent-500/30 rounded-lg">
          <h4 className="text-accent-500 font-medium mb-3">選択されたファシリティ詳細</h4>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
            <div>
              <div className="text-accent-400">融資枠額</div>
              <div className="text-white font-medium">
                {formatCurrency(selectedFacility.commitment, selectedFacility.currency)}
              </div>
            </div>
            <div>
              <div className="text-accent-400">利用可能額</div>
              <div className="text-success font-medium">
                {formatCurrency(getAvailableAmount(selectedFacility), selectedFacility.currency)}
              </div>
            </div>
            <div>
              <div className="text-accent-400">投資家数</div>
              <div className="text-white font-medium">{selectedFacility.sharePies?.length || 0}名</div>
            </div>
            <div>
              <div className="text-accent-400">ステータス</div>
              <div className="text-yellow-400 font-medium">DRAFT</div>
            </div>
          </div>
        </div>
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

export default FacilitySelectForDrawdown;