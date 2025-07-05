import React, { useState } from 'react';
import Layout from '../components/layout/Layout';
import FacilityForm from '../components/forms/FacilityForm';
import FacilityTable from '../components/facility/FacilityTable';
import FacilityDetail from '../components/facility/FacilityDetail';
import { facilityApi } from '../lib/api';
import type { Facility } from '../types/api';

const FacilityPage: React.FC = () => {
  const [showForm, setShowForm] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [refreshTrigger, setRefreshTrigger] = useState(0);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [selectedFacility, setSelectedFacility] = useState<Facility | null>(null);
  const [showDetail, setShowDetail] = useState(false);
  const [editMode, setEditMode] = useState<'create' | 'edit'>('create');
  const [editData, setEditData] = useState<Facility | undefined>(undefined);

  const handleSuccess = (facility: Facility) => {
    const action = editMode === 'edit' ? 'updated' : 'created';
    setSuccessMessage(`Facility "#${facility.id}" has been ${action} successfully.`);
    setShowForm(false);
    setEditMode('create');
    setEditData(undefined);
    setRefreshTrigger(prev => prev + 1);
    // 成功メッセージを3秒後に消去
    setTimeout(() => setSuccessMessage(null), 3000);
  };

  const handleCancel = () => {
    setShowForm(false);
    setEditMode('create');
    setEditData(undefined);
  };

  const handleDelete = async (facility: Facility) => {
    if (window.confirm(`Are you sure you want to delete facility "#${facility.id}"?`)) {
      try {
        await facilityApi.delete(facility.id);
        setSuccessMessage(`Facility "#${facility.id}" has been deleted successfully.`);
        setRefreshTrigger(prev => prev + 1);
        // 成功メッセージを3秒後に消去
        setTimeout(() => setSuccessMessage(null), 3000);
      } catch (error: any) {
        console.error('Failed to delete facility:', error);
        const errorMessage = error?.message || 'Failed to delete facility. Please try again.';
        alert(errorMessage);
      }
    }
  };

  const handleEdit = (facility: Facility) => {
    console.log('Edit facility:', facility);
    setEditMode('edit');
    setEditData(facility);
    setShowForm(true);
  };

  const handleDetail = (facility: Facility) => {
    setSelectedFacility(facility);
    setShowDetail(true);
  };

  const handleCloseDetail = () => {
    setShowDetail(false);
    setSelectedFacility(null);
  };

  return (
    <Layout>
      <div className="max-w-7xl mx-auto">
        {/* Page Header */}
        <div className="flex items-center justify-between mb-8">
          <div>
            <h1 className="text-3xl font-bold text-white">Facilities</h1>
            <p className="text-accent-400">Create and manage financing facilities</p>
          </div>
          <button
            onClick={() => {
              if (showForm) {
                setShowForm(false);
                setEditMode('create');
                setEditData(undefined);
              } else {
                setEditMode('create');
                setEditData(undefined);
                setShowForm(true);
              }
            }}
            className="bg-accent-500 hover:bg-accent-400 text-white font-semibold py-3 px-6 rounded-lg transition-colors duration-200 flex items-center gap-2"
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
            </svg>
            {showForm 
              ? 'Close Form' 
              : editMode === 'edit' 
                ? 'New Facility' 
                : 'New Facility'
            }
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
              mode={editMode}
              editData={editData}
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
                    Search
                  </label>
                  <div className="relative">
                    <svg className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-accent-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                    </svg>
                    <input
                      id="search"
                      type="text"
                      placeholder="Search by facility ID or interest terms..."
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
                    title="Refresh"
                  >
                    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                    </svg>
                    Refresh
                  </button>
                </div>
              </div>

              {/* Quick Stats */}
              <div className="mt-6 grid grid-cols-1 md:grid-cols-3 gap-4">
                <div className="bg-secondary-600 rounded-lg p-4">
                  <div className="text-accent-400 text-sm">Draft</div>
                  <div className="text-white text-2xl font-bold">-</div>
                </div>
                <div className="bg-secondary-600 rounded-lg p-4">
                  <div className="text-accent-400 text-sm">Fixed</div>
                  <div className="text-white text-2xl font-bold">-</div>
                </div>
                <div className="bg-secondary-600 rounded-lg p-4">
                  <div className="text-accent-400 text-sm">Total Facility Amount</div>
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
            onDetail={handleDetail}
            refreshTrigger={refreshTrigger}
          />
        )}

        {/* Detail Modal */}
        <FacilityDetail
          facility={selectedFacility}
          isOpen={showDetail}
          onClose={handleCloseDetail}
        />
      </div>
    </Layout>
  );
};

export default FacilityPage;