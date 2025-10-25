import { baseApi } from './baseApi.ts';
import {Product} from "../models/Product.ts";

export const productsApi = baseApi.injectEndpoints({
  endpoints: (builder) => ({
    getAllProducts: builder.query<Product[], void>({
      query: () => ({
        url: 'products/all',
        method: 'GET',
      }),
    }),
    getAvailableProducts: builder.query<Product[], void>({
        query: () => ({
            url: 'products/available',
            method: 'GET',
        }),
    }),
    getProductById: builder.query<Product, number>({
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
