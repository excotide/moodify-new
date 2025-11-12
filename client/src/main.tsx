import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.tsx'
import { ActivePageProvider } from './context/ActivePageContext'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <ActivePageProvider>
      <App />
    </ActivePageProvider>
  </StrictMode>,
)
