import React, { useState } from 'react';
import Layout from '../components/layout/Layout';
import InvestorForm from '../components/forms/InvestorForm';
import InvestorTable from '../components/investor/InvestorTable';
import type { Investor } from '../types/api';

const InvestorPage: React.FC = () => {
  const [showForm, setShowForm] = useState(false);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [refreshTrigger, setRefreshTrigger] = useState(0);

  const handleFormSuccess = (investor: Investor) => {
    setSuccessMessage(`投資家「${investor.name}」を正常に登録しました。`);
    setShowForm(false);
    setRefreshTrigger(prev => prev + 1); // リストを更新
    // 成功メッセージを3秒後に消去
    setTimeout(() => setSuccessMessage(null), 3000);
  };

  const handleFormCancel = () => {
    setShowForm(false);
  };

  return (
    <Layout>
      <div className="max-w-7xl mx-auto">
        {/* Page Header */}
        <div className="flex items-center justify-between mb-8">
          <div>
            <h1 className="text-3xl font-bold text-white">Investors</h1>
          </div>
          
          {!showForm && (
            <button
              onClick={() => setShowForm(true)}
              className="bg-accent-500 hover:bg-accent-400 text-white font-semibold py-3 px-6 rounded-lg transition-colors duration-200 flex items-center gap-2"
            >
              New Investor
            </button>
          )}
        </div>

        {/* Success Message */}
        {successMessage && (
          <div className="mb-6 p-4 bg-success/10 border border-success/20 rounded-lg">
            <div className="flex items-center gap-3">
              <svg className="w-5 h-5 text-success" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
              </svg>
              <p className="text-success font-medium">{successMessage}</p>
            </div>
          </div>
        )}

        {/* Investor Form */}
        {showForm && (
          <div className="mb-8">
            <InvestorForm
              onSuccess={handleFormSuccess}
              onCancel={handleFormCancel}
            />
          </div>
        )}

        {/* Investor Table */}
        {!showForm && (
          <InvestorTable refreshTrigger={refreshTrigger} />
        )}
      </div>
    </Layout>
  );
};

export default InvestorPage;