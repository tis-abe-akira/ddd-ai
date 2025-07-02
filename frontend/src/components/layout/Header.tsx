import React from 'react';
import { Link, useLocation } from 'react-router-dom';

const Header: React.FC = () => {
  const location = useLocation();

  const navigation = [
    { name: 'Dashboard', href: '/' },
    { name: 'Loans', href: '/loans' },
    { name: 'Borrowers', href: '/borrowers' },
    { name: 'Lenders', href: '/lenders' },
    { name: 'Reports', href: '/reports' },
  ];

  const isActive = (href: string) => {
    if (href === '/') {
      return location.pathname === '/';
    }
    return location.pathname.startsWith(href);
  };

  return (
    <header className="flex items-center justify-between whitespace-nowrap border-b border-solid border-secondary-500 px-10 py-3 bg-primary-900">
      {/* Logo and Navigation */}
      <div className="flex items-center gap-8">
        {/* Logo */}
        <div className="flex items-center gap-4 text-white">
          <div className="size-4">
            <svg viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path
                d="M44 11.2727C44 14.0109 39.8386 16.3957 33.69 17.6364C39.8386 18.877 44 21.2618 44 24C44 26.7382 39.8386 29.123 33.69 30.3636C39.8386 31.6043 44 33.9891 44 36.7273C44 40.7439 35.0457 44 24 44C12.9543 44 4 40.7439 4 36.7273C4 33.9891 8.16144 31.6043 14.31 30.3636C8.16144 29.123 4 26.7382 4 24C4 21.2618 8.16144 18.877 14.31 17.6364C8.16144 16.3957 4 14.0109 4 11.2727C4 7.25611 12.9543 4 24 4C35.0457 4 44 7.25611 44 11.2727Z"
                fill="currentColor"
              />
            </svg>
          </div>
          <h2 className="text-white text-lg font-bold leading-tight tracking-[-0.015em]">
            LoanSys
          </h2>
        </div>

        {/* Navigation Links */}
        <nav className="flex items-center gap-9">
          {navigation.map((item) => (
            <Link
              key={item.name}
              to={item.href}
              className={`text-sm font-medium leading-normal transition-colors hover:text-accent-500 ${
                isActive(item.href) ? 'text-white' : 'text-accent-400'
              }`}
            >
              {item.name}
            </Link>
          ))}
        </nav>
      </div>

      {/* User Profile */}
      <div className="flex flex-1 justify-end gap-8">
        {/* Search - 将来実装予定 */}
        <div className="flex items-center gap-2">
          <label className="flex flex-col min-w-40 !h-10 max-w-64">
            <div className="flex w-full flex-1 items-stretch rounded-xl h-full">
              <div className="text-accent-400 flex border-none bg-secondary-600 items-center justify-center pl-4 rounded-l-xl border-r-0">
                <svg xmlns="http://www.w3.org/2000/svg" width="24px" height="24px" fill="currentColor" viewBox="0 0 256 256">
                  <path d="M229.66,218.34l-50.07-50.06a88.11,88.11,0,1,0-11.31,11.31l50.06,50.07a8,8,0,0,0,11.32-11.32ZM40,112a72,72,0,1,1,72,72A72.08,72.08,0,0,1,40,112Z" />
                </svg>
              </div>
              <input
                placeholder="Search"
                className="form-input flex w-full min-w-0 flex-1 resize-none overflow-hidden rounded-xl text-white focus:outline-0 focus:ring-0 border-none bg-secondary-600 focus:border-none h-full placeholder:text-accent-400 px-4 rounded-l-none border-l-0 pl-2 text-base font-normal leading-normal"
                disabled
              />
            </div>
          </label>
        </div>

        {/* Avatar */}
        <div className="bg-center bg-no-repeat aspect-square bg-cover rounded-full size-10 bg-accent-500 flex items-center justify-center text-primary-900 font-bold">
          U
        </div>
      </div>
    </header>
  );
};

export default Header;