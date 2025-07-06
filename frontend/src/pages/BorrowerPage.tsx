import React, { useState } from 'react';
import Layout from '../components/layout/Layout';
import BorrowerForm from '../components/forms/BorrowerForm';
import BorrowerTable from '../components/borrower/BorrowerTable';
import { borrowerApi } from '../lib/api';
import type { Borrower } from '../types/api';

const BorrowerPage: React.FC = () => {
  const [showForm, setShowForm] = useState(false);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [refreshTrigger, setRefreshTrigger] = useState(0);
  const [editMode, setEditMode] = useState<'create' | 'edit'>('create');
  const [editData, setEditData] = useState<Borrower | undefined>(undefined);

  const handleFormSuccess = (borrower: Borrower) => {
    const action = editMode === 'edit' ? 'updated' : 'registered';
    setSuccessMessage(`Borrower "${borrower.name}" has been ${action} successfully.`);
    setShowForm(false);
    setEditMode('create');
    setEditData(undefined);
    setRefreshTrigger(prev => prev + 1); // リストを更新
    // 成功メッセージを3秒後に消去
    setTimeout(() => setSuccessMessage(null), 3000);
  };

  const handleFormCancel = () => {
    setShowForm(false);
    setEditMode('create');
    setEditData(undefined);
  };

  const handleEdit = (borrower: Borrower) => {
    console.log('Edit borrower:', borrower);
    setEditMode('edit');
    setEditData(borrower);
    setShowForm(true);
  };

  const handleDelete = async (borrower: Borrower) => {
    if (window.confirm(`Are you sure you want to delete borrower "${borrower.name}"?`)) {
      try {
        await borrowerApi.delete(borrower.id);
        setSuccessMessage(`Borrower "${borrower.name}" has been deleted successfully.`);
        setRefreshTrigger(prev => prev + 1);
        // 成功メッセージを3秒後に消去
        setTimeout(() => setSuccessMessage(null), 3000);
      } catch (error: any) {
        console.error('Failed to delete borrower:', error);
        const errorMessage = error?.message || 'Failed to delete borrower. Please try again.';
        alert(errorMessage);
      }
    }
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
              mode={editMode}
              editData={editData}
            />
          </div>
        )}

        {/* Borrower Table */}
        {!showForm && (
          <BorrowerTable 
            refreshTrigger={refreshTrigger}
            onEdit={handleEdit}
            onDelete={handleDelete}
          />
        )}
      </div>
    </Layout>
  );
};

export default BorrowerPage;