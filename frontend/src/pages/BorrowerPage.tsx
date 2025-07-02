import React, { useState } from 'react';
import Layout from '../components/layout/Layout';
import BorrowerForm from '../components/forms/BorrowerForm';
import BorrowerTable from '../components/borrower/BorrowerTable';
import type { Borrower } from '../types/api';

const BorrowerPage: React.FC = () => {
  const [showForm, setShowForm] = useState(false);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [refreshTrigger, setRefreshTrigger] = useState(0);

  const handleFormSuccess = (borrower: Borrower) => {
    setSuccessMessage(`借り手「${borrower.name}」を正常に登録しました。`);
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
            <h1 className="text-3xl font-bold text-white">Borrowers</h1>
          </div>
          
          {!showForm && (
            <button
              onClick={() => setShowForm(true)}
              className="bg-accent-500 hover:bg-accent-400 text-primary-900 font-semibold py-3 px-6 rounded-lg transition-colors duration-200 flex items-center gap-2"
            >
              New Borrower
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

        {/* Borrower Form */}
        {showForm && (
          <div className="mb-8">
            <BorrowerForm
              onSuccess={handleFormSuccess}
              onCancel={handleFormCancel}
            />
          </div>
        )}

        {/* Borrower Table */}
        {!showForm && (
          <BorrowerTable refreshTrigger={refreshTrigger} />
        )}
      </div>
    </Layout>
  );
};

export default BorrowerPage;