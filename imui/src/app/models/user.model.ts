export interface User {
  userId: string;
}

export interface UserAddress {
  addressId?: number;
  line1?: string;
  line2?: string;
  city?: string;
  state?: string;
  country?: string;
  postalCode?: string;
}

export interface UserProfile {
  id?: number;
  firstName: string;
  middleName?: string;
  lastName: string;
  email: string;
  mobile?: string;
  profilePicture?: string;
  addresses?: UserAddress[];
  userId?: string;
}

export interface LoginRequest {
  userId: string;
  password?: string;
}

export interface AuthResponse {
  token: string;
  tokenType?: string;
  userId: string;
  userProfile?: UserProfile;
  expiresIn?: number;
}

export interface UserRegisterRequest {
  userId: string;
  password?: string;
  firstName: string;
  middleName?: string;
  lastName: string;
  email: string;
  mobile?: string;
}
