import React, { createContext, useContext, useState, useEffect } from "react";

type UserInfo = {
  uuid?: string;
  username?: string;
  [key: string]: any;
};

type AuthContextType = {
  isAuthenticated: boolean;
  user: UserInfo | null;
  login: (token?: string, userUuid?: string, userInfo?: UserInfo) => void;
  logout: () => void;
};

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(false);
  const [user, setUser] = useState<UserInfo | null>(null);

  useEffect(() => {
    const token = localStorage.getItem("authToken");
    const storedUuid = localStorage.getItem("userUuid");
    const storedUser = localStorage.getItem("userInfo");
    setIsAuthenticated(Boolean(token));
    if (storedUser) {
      try {
        setUser(JSON.parse(storedUser));
      } catch (e) {
        setUser(storedUuid ? { uuid: storedUuid } : null);
      }
    } else if (storedUuid) {
      setUser({ uuid: storedUuid });
    }
  }, []);

  const login = (token?: string, userUuid?: string, userInfo?: UserInfo) => {
    if (token) localStorage.setItem("authToken", token);
    else localStorage.setItem("authToken", "__demo_token__");

    if (userUuid) localStorage.setItem("userUuid", userUuid);
    if (userInfo) localStorage.setItem("userInfo", JSON.stringify(userInfo));

    // mark that a login just happened so other pages can react (one-time)
    try { localStorage.setItem("justLoggedIn", String(Date.now())); } catch {}

    // set in state
    setUser(userInfo ?? (userUuid ? { uuid: userUuid } : null));
    setIsAuthenticated(true);
  };

  const logout = () => {
    localStorage.removeItem("authToken");
    localStorage.removeItem("userUuid");
    localStorage.removeItem("userInfo");
    setUser(null);
    setIsAuthenticated(false);
  };

  return (
    <AuthContext.Provider value={{ isAuthenticated, user, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuthContext = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuthContext must be used within AuthProvider");
  return ctx;
};

export default AuthContext;
