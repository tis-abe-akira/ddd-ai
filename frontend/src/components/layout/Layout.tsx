import React from 'react';
import Header from './Header';
import Sidebar from './Sidebar';

interface LayoutProps {
  children: React.ReactNode;
}

const Layout: React.FC<LayoutProps> = ({ children }) => {
  return (
    <div className="min-h-screen bg-primary-900 text-white">
      <Sidebar />
      <div className="pl-64">
        <Header />
        <main className="px-6 py-8">
          {children}
        </main>
      </div>
    </div>
  );
};

export default Layout;