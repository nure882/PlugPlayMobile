import { configureStore } from '@reduxjs/toolkit'
import { TypedUseSelectorHook, useDispatch, useSelector } from 'react-redux'
import { persistStore, persistReducer } from 'redux-persist'
import storage from 'redux-persist/lib/storage'
import { baseApi } from '../api/baseApi.ts'
import { combineReducers } from '@reduxjs/toolkit'
import filterReducer from '../app/slices/filterSlice.ts'

const persistConfig = {
  key: 'root',
  storage,
  whitelist: ['filter'], // Persist filter state
};

const rootReducer = combineReducers({
  [baseApi.reducerPath]: baseApi.reducer,
  filter: filterReducer,
});

export const store = configureStore({
  reducer: persistReducer(persistConfig, rootReducer),
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware({
      serializableCheck: {
        ignoredActions: ['persist/PERSIST', 'persist/REHYDRATE'],
      },
    }).concat(baseApi.middleware),
  devTools: true,
});

export const persistor = persistStore(store);

export type RootState = ReturnType<typeof store.getState>
export type AppDispatch = typeof store.dispatch
export const useAppDispatch: () => AppDispatch = useDispatch
export const useAppSelector: TypedUseSelectorHook<RootState> = useSelector
