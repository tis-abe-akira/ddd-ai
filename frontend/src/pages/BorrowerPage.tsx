import React, { useState } from 'react';
import Layout from '../components/layout/Layout';
import BorrowerForm from '../components/forms/BorrowerForm';
import type { Borrower } from '../types/api';

const BorrowerPage: React.FC = () => {
  const [showForm, setShowForm] = useState(false);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const handleFormSuccess = (borrower: Borrower) => {
    setSuccessMessage(`借り手「${borrower.name}」を正常に登録しました。`);
    setShowForm(false);
    // 成功メッセージを3秒後に消去
    setTimeout(() => setSuccessMessage(null), 3000);
  };

  const handleFormCancel = () => {
    setShowForm(false);
  };

  return (
    <Layout>
      <div className="max-w-4xl mx-auto">
        {/* Page Header */}
        <div className="flex items-center justify-between mb-8">
          <div>
            <h1 className="text-3xl font-bold text-white mb-2">借り手管理</h1>
            <p className="text-accent-400">借り手の登録・管理を行います</p>
          </div>
          
          {!showForm && (
            <button
              onClick={() => setShowForm(true)}
              className="bg-accent-500 hover:bg-accent-400 text-primary-900 font-semibold py-3 px-6 rounded-lg transition-colors duration-200 flex items-center gap-2"
            >
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
              </svg>
              新規借り手登録
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

        {/* Placeholder for Borrower List */}
        {!showForm && (
          <div className="bg-primary-900 border border-secondary-500 rounded-xl p-8 text-center">
            <svg className="w-16 h-16 text-accent-400 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
            </svg>
            <h3 className="text-xl font-semibold text-white mb-2">借り手リスト</h3>
            <p className="text-accent-400 mb-4">登録済みの借り手がここに表示されます</p>
            <p className="text-sm text-accent-400">まだ借り手が登録されていません。「新規借り手登録」ボタンから登録を開始してください。</p>
          </div>
        )}
      </div>
    </Layout>
  );
};

export default BorrowerPage;