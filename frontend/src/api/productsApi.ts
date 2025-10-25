import { baseApi } from './baseApi.ts';

export interface BackendProductDto {
  id: number;
  name: string;
  description: string;
  price: number;
  stockQuantity: number;
  pictureUrl: string;
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

export const productsApi = baseApi.injectEndpoints({
  endpoints: (builder) => ({
    getAllProducts: builder.query<BackendProductDto[], void>({
      query: () => ({
        url: 'products/all',
        method: 'GET',
      }),
    }),
    getAvailableProducts: builder.query<BackendProductDto[], void>({
        query: () => ({
            url: 'products/available',
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
  useGetAvailableProductsQuery
} = productsApi;
