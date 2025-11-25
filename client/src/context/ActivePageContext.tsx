import { createContext, useContext, useState } from "react";

// Create ActivePageContext
// ======================================LOGIKA NAVISAGI==========================================
const ActivePageContext = createContext<any>(null);

export const ActivePageProvider = ({ children }: { children: React.ReactNode }) => {
  const [activePage, setActivePage] = useState("Home");

  return (
    <ActivePageContext.Provider value={{ activePage, setActivePage }}>
      {children}
    </ActivePageContext.Provider>
  );
};

export const useActivePageContext = () => useContext(ActivePageContext);