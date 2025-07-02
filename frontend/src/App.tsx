import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import BorrowerPage from './pages/BorrowerPage';
import Layout from './components/layout/Layout';

function App() {
  return (
    <Router>
      <Routes>
        {/* Default route - Dashboard placeholder */}
        <Route path="/" element={
          <Layout>
            <div className="max-w-4xl mx-auto text-center">
              <h1 className="text-4xl font-bold text-white mb-4">
                Syndicated Loan Management System
              </h1>
              <p className="text-accent-400 text-lg mb-8">
                シンジケートローン管理システムへようこそ
              </p>
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                <div className="bg-primary-900 border border-secondary-500 rounded-xl p-6 hover:border-accent-500 transition-colors">
                  <h3 className="text-xl font-semibold text-white mb-2">借り手管理</h3>
                  <p className="text-accent-400 mb-4">借り手の登録・管理</p>
                  <a 
                    href="/borrowers"
                    className="inline-block bg-accent-500 hover:bg-accent-400 text-primary-900 font-semibold py-2 px-4 rounded transition-colors"
                  >
                    管理画面へ
                  </a>
                </div>
                <div className="bg-primary-900 border border-secondary-500 rounded-xl p-6 opacity-50">
                  <h3 className="text-xl font-semibold text-white mb-2">ローン管理</h3>
                  <p className="text-accent-400 mb-4">ローンの作成・管理</p>
                  <span className="inline-block bg-secondary-600 text-accent-400 font-semibold py-2 px-4 rounded">
                    準備中
                  </span>
                </div>
                <div className="bg-primary-900 border border-secondary-500 rounded-xl p-6 opacity-50">
                  <h3 className="text-xl font-semibold text-white mb-2">投資家管理</h3>
                  <p className="text-accent-400 mb-4">投資家の登録・管理</p>
                  <span className="inline-block bg-secondary-600 text-accent-400 font-semibold py-2 px-4 rounded">
                    準備中
                  </span>
                </div>
              </div>
            </div>
          </Layout>
        } />
        
        {/* Borrower routes */}
        <Route path="/borrowers" element={<BorrowerPage />} />
        
        {/* Placeholder routes */}
        <Route path="/loans" element={
          <Layout>
            <div className="text-center">
              <h1 className="text-3xl font-bold text-white mb-4">ローン管理</h1>
              <p className="text-accent-400">準備中です</p>
            </div>
          </Layout>
        } />
        
        <Route path="/lenders" element={
          <Layout>
            <div className="text-center">
              <h1 className="text-3xl font-bold text-white mb-4">投資家管理</h1>
              <p className="text-accent-400">準備中です</p>
            </div>
          </Layout>
        } />
        
        <Route path="/reports" element={
          <Layout>
            <div className="text-center">
              <h1 className="text-3xl font-bold text-white mb-4">レポート</h1>
              <p className="text-accent-400">準備中です</p>
            </div>
          </Layout>
        } />
      </Routes>
    </Router>
  );
}

export default App
