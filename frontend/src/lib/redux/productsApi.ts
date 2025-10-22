import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react';

export const API_BASE_URL = 'http://localhost:5298'; // port may be different

// Backend Product DTO interface
export interface BackendProductDto {
  id: number;
  name: string;
  description: string;
  price: number;
  stockQuantity: number;
  createdAt: string;
  category: {
    id: number;
    name: string;
    parentCategory?: {
      id: number;
      name: string;
    };
  } | null;
}

export const productsApi = createApi({
  reducerPath: 'productsApi',
  baseQuery: fetchBaseQuery({ baseUrl: `${API_BASE_URL}/api` }),
  endpoints: (builder) => ({
    getAllProducts: builder.query<BackendProductDto[], void>({
      query: () => ({
        url: 'products/all',
        method: 'GET',
      }),
    }),
    getProductById: builder.query<BackendProductDto, number>({
      query: (id) => ({
        url: `products/${id}`,
        method: 'GET',
      }),
    }),
  }),
});

export const {
  useGetAllProductsQuery,
  useGetProductByIdQuery,
} = productsApi;
