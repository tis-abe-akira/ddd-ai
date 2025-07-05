import React, { useState, useEffect } from 'react';
import { syndicateApi, investorApi } from '../../lib/api';
import type { Facility, SyndicateDetail, Investor } from '../../types/api';

interface FacilityDetailProps {
  facility: Facility | null;
  isOpen: boolean;
  onClose: () => void;
}

const FacilityDetail: React.FC<FacilityDetailProps> = ({
  facility,
  isOpen,
  onClose
}) => {
  const [syndicateDetail, setSyndicateDetail] = useState<SyndicateDetail | null>(null);
  const [investors, setInvestors] = useState<Record<number, Investor>>({});
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (facility && isOpen) {
      fetchRelatedData();
    }
  }, [facility, isOpen]);

  const fetchRelatedData = async () => {
    if (!facility) return;
    
    try {
      setLoading(true);
      
      // Syndicate詳細を取得
      const syndicateResponse = await syndicateApi.getByIdWithDetails(facility.syndicateId);
      setSyndicateDetail(syndicateResponse.data);
      
      // SharePieに関連する投資家の詳細を取得
      if (facility.sharePies && facility.sharePies.length > 0) {
        const investorIds = facility.sharePies.map(pie => pie.investorId);
        const investorResponses = await Promise.all(
          investorIds.map(id => investorApi.getById(id))
        );
        
        const investorMap: Record<number, Investor> = {};
        investorResponses.forEach(response => {
          investorMap[response.data.id] = response.data;
        });
        setInvestors(investorMap);
      }
    } catch (error) {
      console.error('Failed to fetch related data:', error);
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('ja-JP', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  };

  const formatCurrency = (amount: number, currency: string) => {
    return new Intl.NumberFormat('ja-JP', {
      style: 'currency',
      currency: currency,
      minimumFractionDigits: 0,
    }).format(amount);
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'DRAFT':
        return 'bg-yellow-500/20 text-yellow-400 border-yellow-500/30';
      case 'FIXED':
        return 'bg-success/20 text-success border-success/30';
      default:
        return 'bg-secondary-600 text-accent-400 border-secondary-500';
    }
  };

  const getStatusLabel = (status: string) => {
    switch (status) {
      case 'DRAFT':
        return 'Draft (編集可能)';
      case 'FIXED':
        return 'Fixed (確定済み)';
      default:
        return status;
    }
  };

  const getInvestorTypeColor = (investorType: string) => {
    switch (investorType) {
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

  const calculateTotalShare = () => {
    return facility?.sharePies?.reduce((total, pie) => total + pie.share, 0) || 0;
  };

  if (!isOpen || !facility) return null;

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div className="bg-primary-900 border border-secondary-500 rounded-xl max-w-4xl w-full max-h-[90vh] overflow-y-auto">
        {/* Header */}
        <div className="flex items-center justify-between p-6 border-b border-secondary-500">
          <div className="flex items-center gap-4">
            <div className="w-12 h-12 bg-accent-500 rounded-full flex items-center justify-center text-white font-bold text-lg">
              F{facility.id}
            </div>
            <div>
              <h2 className="text-xl font-bold text-white">Facility #{facility.id} 詳細</h2>
              <p className="text-accent-400 text-sm">Credit Facility Information</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-2 text-accent-400 hover:text-white hover:bg-secondary-600 rounded-lg transition-colors"
          >
            <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        {loading ? (
          <div className="p-12 text-center">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-accent-500 mx-auto"></div>
            <span className="text-accent-400 mt-3 block">Loading details...</span>
          </div>
        ) : (
          <div className="p-6 space-y-6">
            {/* Basic Information */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="space-y-4">
                <h3 className="text-lg font-semibold text-white border-b border-secondary-500 pb-2">
                  基本情報
                </h3>
                
                <div className="space-y-3">
                  <div>
                    <div className="text-sm text-accent-400">Facility Amount</div>
                    <div className="text-2xl font-bold text-white">
                      {formatCurrency(facility.commitment, facility.currency)}
                    </div>
                    <div className="text-accent-400 text-sm">{facility.currency}</div>
                  </div>
                  
                  <div>
                    <div className="text-sm text-accent-400">Interest Terms</div>
                    <div className="text-white font-medium">{facility.interestTerms}</div>
                  </div>
                  
                  <div>
                    <div className="text-sm text-accent-400">Status</div>
                    <span className={`inline-flex px-3 py-1 text-sm font-medium rounded-full border ${getStatusColor(facility.status)}`}>
                      {getStatusLabel(facility.status)}
                    </span>
                  </div>
                  
                  <div>
                    <div className="text-sm text-accent-400">Version</div>
                    <div className="text-white font-mono">v{facility.version}</div>
                  </div>
                </div>
              </div>
              
              <div className="space-y-4">
                <h3 className="text-lg font-semibold text-white border-b border-secondary-500 pb-2">
                  期間情報
                </h3>
                
                <div className="space-y-3">
                  <div>
                    <div className="text-sm text-accent-400">Start Date</div>
                    <div className="text-white font-medium">{formatDate(facility.startDate)}</div>
                  </div>
                  
                  <div>
                    <div className="text-sm text-accent-400">End Date</div>
                    <div className="text-white font-medium">{formatDate(facility.endDate)}</div>
                  </div>
                  
                  <div>
                    <div className="text-sm text-accent-400">Created At</div>
                    <div className="text-white font-medium">{formatDate(facility.createdAt)}</div>
                  </div>
                  
                  <div>
                    <div className="text-sm text-accent-400">Updated At</div>
                    <div className="text-white font-medium">{formatDate(facility.updatedAt)}</div>
                  </div>
                </div>
              </div>
            </div>

            {/* Syndicate Information */}
            {syndicateDetail && (
              <div className="space-y-4">
                <h3 className="text-lg font-semibold text-white border-b border-secondary-500 pb-2">
                  関連シンジケート情報
                </h3>
                
                <div className="bg-secondary-600 rounded-lg p-4">
                  <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                    <div>
                      <div className="text-sm text-accent-400">Syndicate Name</div>
                      <div className="text-white font-medium">{syndicateDetail.name}</div>
                    </div>
                    <div>
                      <div className="text-sm text-accent-400">Borrower</div>
                      <div className="text-white font-medium">{syndicateDetail.borrowerName}</div>
                    </div>
                    <div>
                      <div className="text-sm text-accent-400">Lead Bank</div>
                      <div className="text-white font-medium">{syndicateDetail.leadBankName}</div>
                    </div>
                  </div>
                </div>
              </div>
            )}

            {/* Share Pie Information */}
            {facility.sharePies && facility.sharePies.length > 0 && (
              <div className="space-y-4">
                <div className="flex items-center justify-between">
                  <h3 className="text-lg font-semibold text-white border-b border-secondary-500 pb-2">
                    投資家別持分配分
                  </h3>
                  <div className="text-sm">
                    <span className="text-accent-400">合計: </span>
                    <span className={`font-bold ${Math.abs(calculateTotalShare() - 1.0) < 0.0001 ? 'text-success' : 'text-warning'}`}>
                      {Math.round(calculateTotalShare() * 100)}%
                    </span>
                  </div>
                </div>
                
                <div className="space-y-3">
                  {facility.sharePies.map((pie) => {
                    const investor = investors[pie.investorId];
                    const amount = facility.commitment * pie.share;
                    
                    return (
                      <div key={pie.id} className="bg-secondary-600 rounded-lg p-4">
                        <div className="flex items-center justify-between">
                          <div className="flex items-center gap-3">
                            <div className="w-10 h-10 bg-accent-500 rounded-full flex items-center justify-center text-white font-bold text-sm">
                              {investor?.name?.charAt(0) || 'I'}
                            </div>
                            <div className="flex-1">
                              <div className="text-white font-medium">
                                {investor?.name || `Investor ID: ${pie.investorId}`}
                              </div>
                              <div className="text-accent-400 text-sm">ID: {pie.investorId}</div>
                              {investor && (
                                <div className="flex items-center gap-2 mt-1">
                                  <span className={`inline-flex px-2 py-1 text-xs font-medium rounded-full ${getInvestorTypeColor(investor.investorType)}`}>
                                    {investor.investorType}
                                  </span>
                                  <span className="text-accent-400 text-xs">
                                    Capacity: {formatCurrency(investor.investmentCapacity, facility.currency)}
                                  </span>
                                </div>
                              )}
                            </div>
                          </div>
                          
                          <div className="text-right">
                            <div className="text-white font-bold text-lg">
                              {Math.round(pie.share * 100)}%
                            </div>
                            <div className="text-accent-400 text-sm">
                              {formatCurrency(amount, facility.currency)}
                            </div>
                          </div>
                        </div>
                      </div>
                    );
                  })}
                </div>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
};

export default FacilityDetail;