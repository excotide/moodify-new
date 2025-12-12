import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.tsx'
import { ActivePageProvider } from './context/ActivePageContext'
import { AuthProvider } from './context/AuthContext'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <AuthProvider>
      <ActivePageProvider>
        <App />
      </ActivePageProvider>
    </AuthProvider>
  </StrictMode>,
)
