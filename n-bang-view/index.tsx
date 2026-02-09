
import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App.tsx';
import SharedSettlementPage from './SharedSettlementPage.tsx';
import { AuthProvider } from './contexts/AuthContext.tsx';
import { ToastProvider } from './contexts/ToastContext.tsx';

const rootElement = document.getElementById('root');
if (!rootElement) {
  throw new Error("Could not find root element to mount to");
}

const path = window.location.pathname;
const sharedMatch = path.match(/^\/shared\/([a-f0-9-]+)$/);

const root = ReactDOM.createRoot(rootElement);

if (sharedMatch) {
  const uuid = sharedMatch[1];
  root.render(
    <React.StrictMode>
      <ToastProvider>
        <SharedSettlementPage uuid={uuid} />
      </ToastProvider>
    </React.StrictMode>
  );
} else {
  root.render(
    <React.StrictMode>
      <ToastProvider>
        <AuthProvider>
          <App />
        </AuthProvider>
      </ToastProvider>
    </React.StrictMode>
  );
}
