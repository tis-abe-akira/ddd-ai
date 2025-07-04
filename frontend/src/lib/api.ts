import axios from 'axios';
import type { AxiosResponse } from 'axios';
import type { 
  Borrower, 
  Company, 
  Investor, 
  Syndicate,
  Facility,
  Loan,
  Drawdown,
  Payment,
  CreateBorrowerRequest,
  CreateCompanyRequest,
  CreateInvestorRequest,
  CreateSyndicateRequest,
  CreateFacilityRequest,
  UpdateFacilityRequest,
  CreateDrawdownRequest,
  CreatePaymentRequest,
  ApiResponse,
  ApiError,
  PageResponse
} from '../types/api';

// API Base Configuration
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request/Response Interceptors
apiClient.interceptors.response.use(
  (response: AxiosResponse) => response,
  (error) => {
    // API エラーハンドリング
    const apiError: ApiError = {
      message: error.response?.data?.message || 'An error occurred',
      status: error.response?.status || 500,
      errors: error.response?.data?.errors,
    };
    return Promise.reject(apiError);
  }
);

// API Functions

// Companies
export const companyApi = {
  getAll: () => apiClient.get<Company[]>('/parties/companies'),
  getById: (id: number) => apiClient.get<Company>(`/parties/companies/${id}`),
  create: (data: CreateCompanyRequest) => apiClient.post<Company>('/parties/companies', data),
  update: (id: number, data: Partial<CreateCompanyRequest>) => 
    apiClient.put<Company>(`/parties/companies/${id}`, data),
  delete: (id: number) => apiClient.delete(`/parties/companies/${id}`),
};

// Borrowers
export const borrowerApi = {
  getAll: (page?: number, size?: number) => {
    const params = new URLSearchParams();
    if (page !== undefined) params.append('page', page.toString());
    // sizeは指定された場合のみ送信（undefinedならバックエンドのデフォルトに従う）
    if (size !== undefined) params.append('size', size.toString());
    params.append('sort', 'id,desc'); // 新しい順にソート
    
    return apiClient.get<PageResponse<Borrower>>(`/parties/borrowers?${params.toString()}`);
  },
  getById: (id: number) => apiClient.get<Borrower>(`/parties/borrowers/${id}`),
  create: (data: CreateBorrowerRequest) => apiClient.post<Borrower>('/parties/borrowers', data),
  update: (id: number, data: Partial<CreateBorrowerRequest>) => 
    apiClient.put<Borrower>(`/parties/borrowers/${id}`, data),
  delete: (id: number) => apiClient.delete(`/parties/borrowers/${id}`),
};

// Investors
export const investorApi = {
  getAll: (page?: number, size?: number) => {
    const params = new URLSearchParams();
    if (page !== undefined) params.append('page', page.toString());
    // sizeは指定された場合のみ送信（undefinedならバックエンドのデフォルトに従う）
    if (size !== undefined) params.append('size', size.toString());
    params.append('sort', 'id,desc'); // 新しい順にソート
    
    return apiClient.get<PageResponse<Investor>>(`/parties/investors?${params.toString()}`);
  },
  getById: (id: number) => apiClient.get<Investor>(`/parties/investors/${id}`),
  create: (data: CreateInvestorRequest) => apiClient.post<Investor>('/parties/investors', data),
  update: (id: number, data: Partial<CreateInvestorRequest>) => 
    apiClient.put<Investor>(`/parties/investors/${id}`, data),
  delete: (id: number) => apiClient.delete(`/parties/investors/${id}`),
};

// Syndicates
export const syndicateApi = {
  getAll: (page?: number, size?: number, search?: string) => {
    const params = new URLSearchParams();
    if (page !== undefined) params.append('page', page.toString());
    // sizeは指定された場合のみ送信（undefinedならバックエンドのデフォルトに従う）
    if (size !== undefined) params.append('size', size.toString());
    if (search) params.append('search', search);
    params.append('sort', 'id,desc'); // 新しい順にソート
    
    return apiClient.get<PageResponse<Syndicate>>(`/syndicates?${params.toString()}`);
  },
  getById: (id: number) => apiClient.get<Syndicate>(`/syndicates/${id}`),
  create: (data: CreateSyndicateRequest) => apiClient.post<Syndicate>('/syndicates', data),
  update: (id: number, data: Partial<CreateSyndicateRequest>) => 
    apiClient.put<Syndicate>(`/syndicates/${id}`, data),
  delete: (id: number) => apiClient.delete(`/syndicates/${id}`),
};

// Facilities
export const facilityApi = {
  getAll: (page?: number, size?: number, search?: string) => {
    const params = new URLSearchParams();
    if (page !== undefined) params.append('page', page.toString());
    // sizeは指定された場合のみ送信（undefinedならバックエンドのデフォルトに従う）
    if (size !== undefined) params.append('size', size.toString());
    if (search) params.append('search', search);
    params.append('sort', 'id,desc'); // 新しい順にソート
    
    return apiClient.get<PageResponse<Facility>>(`/facilities?${params.toString()}`);
  },
  getById: (id: number) => apiClient.get<Facility>(`/facilities/${id}`),
  create: (data: CreateFacilityRequest) => apiClient.post<Facility>('/facilities', data),
  update: (id: number, data: UpdateFacilityRequest) => 
    apiClient.put<Facility>(`/facilities/${id}`, data),
  delete: (id: number) => apiClient.delete(`/facilities/${id}`),
};

// Drawdowns
export const drawdownApi = {
  getAll: () => apiClient.get<Drawdown[]>('/loans/drawdowns'),
  getAllPaged: (page?: number, size?: number) => {
    const params = new URLSearchParams();
    if (page !== undefined) params.append('page', page.toString());
    if (size !== undefined) params.append('size', size.toString());
    params.append('sort', 'id,desc'); // 新しい順にソート
    
    return apiClient.get<PageResponse<Drawdown>>(`/loans/drawdowns/paged?${params.toString()}`);
  },
  getById: (id: number) => apiClient.get<Drawdown>(`/loans/drawdowns/${id}`),
  getByFacilityId: (facilityId: number) => apiClient.get<Drawdown[]>(`/loans/drawdowns/facility/${facilityId}`),
  create: (data: CreateDrawdownRequest) => apiClient.post<Drawdown>('/loans/drawdowns', data),
};

// Loans
export const loanApi = {
  getById: (id: number) => apiClient.get<Loan>(`/loans/${id}`),
  getByDrawdownId: (drawdownId: number) => apiClient.get<Loan>(`/loans/drawdown/${drawdownId}`),
  getAll: (page?: number, size?: number) => {
    const params = new URLSearchParams();
    if (page !== undefined) params.append('page', page.toString());
    if (size !== undefined) params.append('size', size.toString());
    params.append('sort', 'id,desc');
    
    return apiClient.get<PageResponse<Loan>>(`/loans?${params.toString()}`);
  },
};

// Payments
export const paymentApi = {
  getByLoanId: (loanId: number) => apiClient.get<Payment[]>(`/loans/payments/loan/${loanId}`),
  create: (data: CreatePaymentRequest) => apiClient.post<Payment>('/loans/payments', data),
};

// Utility function for handling API responses
export const handleApiResponse = <T>(response: AxiosResponse<T>): T => {
  return response.data;
};

export default apiClient;