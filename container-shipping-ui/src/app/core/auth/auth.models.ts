export type Role =
  | 'USER'
  | 'DRIVER'
  | 'EMPLOYEE'
  | 'ADMIN'
  | 'SUPER_ADMIN';


export interface UserResponse {
  userId: number;
  name: string;
  email: string;
  phone: string;
  role: Role;
  status: 'ACTIVE' | 'SUSPENDED' | 'BLOCKED';
  emailVerified: boolean;
  phoneVerified: boolean;
  assignedPortId?: number | null;
  createdAt: string;
  updatedAt: string;
}


export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  user: UserResponse;
}


export interface LoginRequest {
  email: string;
  password: string;
}


export interface RegistrationResponse {
  userId: number;
  name: string;
  email: string;
  phone: string;
  role: Role;
  status: string;
  message: string;
}


export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
  phone: string;
}


export interface PagedResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}