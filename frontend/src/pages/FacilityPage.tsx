import React, { useState } from 'react';
import Layout from '../components/layout/Layout';
import FacilityForm from '../components/forms/FacilityForm';
import FacilityTable from '../components/facility/FacilityTable';
import type { Facility } from '../types/api';

const FacilityPage: React.FC = () => {
  const [showForm, setShowForm] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [refreshTrigger, setRefreshTrigger] = useState(0);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const handleSuccess = (facility: Facility) => {
    setSuccessMessage(`ファシリティ「#${facility.id}」を正常に組成しました。`);
    setShowForm(false);
    setRefreshTrigger(prev => prev + 1);
    // 成功メッセージを3秒後に消去
    setTimeout(() => setSuccessMessage(null), 3000);
  };

  const handleCancel = () => {
    setShowForm(false);
  };

  const handleDelete = async (facility: Facility) => {
    if (window.confirm(`ファシリティ「#${facility.id}」を削除しますか？`)) {
      try {
        // TODO: API呼び出し実装
        console.log('Delete facility:', facility.id);
        setRefreshTrigger(prev => prev + 1);
      } catch (error) {
        console.error('Failed to delete facility:', error);
      }
    }
  };

  const handleEdit = (facility: Facility) => {
    // TODO: 編集機能実装
    console.log('Edit facility:', facility);
  };

  return (
    <Layout>
      <div className="max-w-7xl mx-auto">
        {/* Page Header */}
        <div className="flex items-center justify-between mb-8">
          <div>
            <h1 className="text-3xl font-bold text-white">Facilities</h1>
            <p className="text-accent-400">ファシリティの組成・管理を行います</p>
          </div>
          <button
            onClick={() => setShowForm(!showForm)}
            className="bg-accent-500 hover:bg-accent-400 text-white font-semibold py-3 px-6 rounded-lg transition-colors duration-200 flex items-center gap-2"
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
            </svg>
            {showForm ? 'フォームを閉じる' : '新規ファシリティ組成'}
          </button>
        </div>

        {/* Success Message */}
        {successMessage && (
          <div className="mb-6 p-4 bg-success/20 border border-success/30 rounded-lg flex items-center gap-3">
            <svg className="w-5 h-5 text-success flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
            </svg>
            <span className="text-success font-medium">{successMessage}</span>
          </div>
        )}

        {/* Form */}
        {showForm && (
          <div className="mb-8">
            <FacilityForm 
              onSuccess={handleSuccess}
              onCancel={handleCancel}
            />
          </div>
        )}

        {/* Search and Filters */}
        {!showForm && (
          <div className="mb-6">
            <div className="bg-primary-900 border border-secondary-500 rounded-xl p-6">
              <div className="flex flex-col md:flex-row gap-4">
                <div className="flex-1">
                  <label htmlFor="search" className="block text-sm font-medium text-white mb-2">
                    検索
                  </label>
                  <div className="relative">
                    <svg className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-accent-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                    </svg>
                    <input
                      id="search"
                      type="text"
                      placeholder="ファシリティIDまたは金利条件で検索..."
                      value={searchTerm}
                      onChange={(e) => setSearchTerm(e.target.value)}
                      className="w-full pl-10 pr-4 py-3 bg-secondary-600 border border-secondary-500 rounded-lg text-white placeholder:text-accent-400 focus:outline-none focus:ring-2 focus:ring-accent-500 focus:border-transparent"
                    />
                  </div>
                </div>
                
                <div className="flex items-end">
                  <button
                    onClick={() => setRefreshTrigger(prev => prev + 1)}
                    className="bg-secondary-600 hover:bg-secondary-500 text-white font-semibold py-3 px-4 rounded-lg transition-colors duration-200 flex items-center gap-2"
                    title="更新"
                  >
                    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                    </svg>
                    更新
                  </button>
                </div>
              </div>

              {/* Quick Stats */}
              <div className="mt-6 grid grid-cols-1 md:grid-cols-3 gap-4">
                <div className="bg-secondary-600 rounded-lg p-4">
                  <div className="text-accent-400 text-sm">ドラフト中</div>
                  <div className="text-white text-2xl font-bold">-</div>
                </div>
                <div className="bg-secondary-600 rounded-lg p-4">
                  <div className="text-accent-400 text-sm">確定済み</div>
                  <div className="text-white text-2xl font-bold">-</div>
                </div>
                <div className="bg-secondary-600 rounded-lg p-4">
                  <div className="text-accent-400 text-sm">総融資枠額</div>
                  <div className="text-white text-2xl font-bold">-</div>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Table */}
        {!showForm && (
          <FacilityTable
            searchTerm={searchTerm}
            onEdit={handleEdit}
            onDelete={handleDelete}
            refreshTrigger={refreshTrigger}
          />
        )}
      </div>
    </Layout>
  );
};

export default FacilityPage;