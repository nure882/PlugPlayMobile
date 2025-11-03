// ...existing code...
import { configureStore } from '@reduxjs/toolkit'
import { TypedUseSelectorHook, useDispatch, useSelector } from 'react-redux'
import { persistStore, persistReducer } from 'redux-persist'
import storage from 'redux-persist/lib/storage'
import { baseApi } from '../api/baseApi.ts'
// import { productsApi } from '../api/productsApi.ts' // removed - endpoints injected into baseApi

const persistConfig = {
  key: 'root',
  storage,
  whitelist: [],
};

import { combineReducers } from '@reduxjs/toolkit'

const rootReducer = combineReducers({
  // use the reducerPath key (usually 'api') to avoid duplicate keys
  [baseApi.reducerPath]: baseApi.reducer,
});

export const store = configureStore({
  reducer: persistReducer(persistConfig, rootReducer),
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware({
      serializableCheck: {
        ignoredActions: ['persist/PERSIST', 'persist/REHYDRATE'],
      },
    }).concat(baseApi.middleware), // only include baseApi.middleware once
  devTools: true,
});

export const persistor = persistStore(store);


export type RootState = ReturnType<typeof store.getState>
export type AppDispatch = typeof store.dispatch
export const useAppDispatch: () => AppDispatch = useDispatch
export const useAppSelector: TypedUseSelectorHook<RootState> = useSelector
// ...existing code...