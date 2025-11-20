import type { UserInfo } from "./UserInfo";

export type AuthContextType = {
  isAuthenticated: boolean;
  user: UserInfo | null;
  login: (token?: string, userUuid?: string, userInfo?: UserInfo) => void;
  logout: () => void;
};