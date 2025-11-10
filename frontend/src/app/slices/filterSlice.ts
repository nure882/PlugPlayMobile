import { createSlice, PayloadAction } from '@reduxjs/toolkit';

export interface FilterState {
  selectedCategory: number | null;
  searchQuery: string;
  priceRange: {
    min: number;
    max: number;
  };
  attributeFilters: Record<string, string[]>;
  sortOption: {
    value: string;
    label: string;
  };
}

const initialState: FilterState = {
  selectedCategory: null,
  searchQuery: '',
  priceRange: {
    min: 0,
    max: 5000,
  },
  attributeFilters: {},
  sortOption: {
    value: 'price-asc',
    label: 'Price (Low to High)',
  },
};

const filterSlice = createSlice({
  name: 'filter',
  initialState,
  reducers: {
    setSelectedCategory: (state, action: PayloadAction<number | null>) => {
      state.selectedCategory = action.payload;
      // Reset filters when category changes
      state.attributeFilters = {};
      state.priceRange = { min: 0, max: 5000 };
      state.sortOption = { value: 'price-asc', label: 'Price (Low to High)' };
    },
    setSearchQuery: (state, action: PayloadAction<string>) => {
      state.searchQuery = action.payload;
    },
    setPriceRange: (state, action: PayloadAction<{ min: number; max: number }>) => {
      state.priceRange = action.payload;
    },
    setAttributeFilters: (state, action: PayloadAction<Record<string, string[]>>) => {
      state.attributeFilters = action.payload;
    },
    updateAttributeFilter: (state, action: PayloadAction<{ attributeId: string; values: string[] }>) => {
      const { attributeId, values } = action.payload;
      if (values.length === 0) {
        delete state.attributeFilters[attributeId];
      } else {
        state.attributeFilters[attributeId] = values;
      }
    },
    setSortOption: (state, action: PayloadAction<{ value: string; label: string }>) => {
      state.sortOption = action.payload;
    },
    resetFilters: (state) => {
      state.attributeFilters = {};
      state.priceRange = { min: 0, max: 5000 };
      state.sortOption = { value: 'price-asc', label: 'Price (Low to High)' };
      state.searchQuery = '';
    },
    resetAllFilters: () => initialState,
  },
});

export const {
  setSelectedCategory,
  setSearchQuery,
  setPriceRange,
  setAttributeFilters,
  updateAttributeFilter,
  setSortOption,
  resetFilters,
  resetAllFilters,
} = filterSlice.actions;

export default filterSlice.reducer;
