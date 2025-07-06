import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import BorrowerPage from './pages/BorrowerPage';
import InvestorPage from './pages/InvestorPage';
import SyndicatePage from './pages/SyndicatePage';
import FacilityPage from './pages/FacilityPage';
import DrawdownPage from './pages/DrawdownPage';
import DrawdownDetailPage from './pages/DrawdownDetailPage';
import FeePaymentPage from './pages/FeePaymentPage';
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
              <p className="text-accent-400 text-lg">
                Welcome to the Syndicated Loan Management System
              </p>
            </div>
          </Layout>
        } />
        
        {/* Borrower routes */}
        <Route path="/borrowers" element={<BorrowerPage />} />
        
        {/* Investor routes */}
        <Route path="/lenders" element={<InvestorPage />} />
        
        {/* Syndicate routes */}
        <Route path="/syndicates" element={<SyndicatePage />} />
        
        {/* Facility routes */}
        <Route path="/facilities" element={<FacilityPage />} />
        
        {/* Drawdown routes */}
        <Route path="/drawdowns" element={<DrawdownPage />} />
        <Route path="/drawdowns/:id" element={<DrawdownDetailPage />} />
        
        {/* Fee Payment routes */}
        <Route path="/fees" element={<FeePaymentPage />} />
        
        {/* Placeholder routes */}
        <Route path="/loans" element={
          <Layout>
            <div className="text-center">
              <h1 className="text-3xl font-bold text-white mb-4">ローン管理</h1>
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
