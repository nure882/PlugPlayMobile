import {StrictMode} from 'react';
import {createRoot} from 'react-dom/client';
import App from './App.tsx';
import './index.css';
import {Provider} from "react-redux";
import {persistor, store} from "./app/configureStore.ts";
import {PersistGate} from "redux-persist/integration/react";
import { AuthProvider } from './context/AuthContext.tsx';
import { CartProvider } from './context/CartContext.tsx';

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <Provider store={store}>
      <PersistGate persistor={persistor} loading={null}>
        <AuthProvider>
          <CartProvider>
            <App/>
          </CartProvider>
        </AuthProvider>
      </PersistGate>
    </Provider>
  </StrictMode>
);
