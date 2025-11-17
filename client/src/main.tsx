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

// Remove the initial splash once the app has mounted.
// Add a short fade-out so the transition feels smooth.
const _splash = document.getElementById('app-splash');
if (_splash) {
  // Allow the renderer to flush and then fade out splash
  requestAnimationFrame(() => {
    _splash.classList.add('splash-hidden');
    // Remove from DOM after transition
    setTimeout(() => _splash.remove(), 350);
  });
}
