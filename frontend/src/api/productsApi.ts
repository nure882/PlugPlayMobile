import {baseApi} from './baseApi.ts';
import {Product} from "../models/Product.ts";
import AttributeGroup from "../models/AttributeGroup.ts";

interface FilterProductsResponse {
  products: Product[];
  total: number;
  totalPages: number;
  page: number;
  pageSize: number;
}

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
    filterProducts: builder.query<
      FilterProductsResponse,
      {
        categoryId: number;
        minPrice?: number;
        maxPrice?: number;
        filter?: string;
        sort?: string;
        page?: number;
        pageSize?: number;
      }
    >({
      query: ({
                categoryId,
                minPrice,
                maxPrice,
                filter: filterText,
                sort,
                page = 1,
                pageSize = 20,
              }) => ({
        url: `products/filter/${categoryId}`,
        method: 'GET',
        params: {
          minPrice,
          maxPrice,
          filter: filterText,
          sort,
          page,
          pageSize,
        },
      }),
      providesTags: [{type: 'Products'} as any],
    }),
    getAttributeGroups: builder.mutation<AttributeGroup[], { categoryId: number; productIds?: number[] }>({
      query: ({categoryId, productIds}) => {
        const body = productIds && productIds.length ? productIds : undefined;

        return {
          url: `products/attribute/${categoryId}`,
          method: 'POST',
          body,
        };
      },
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
  useGetAvailableProductsQuery,
  useFilterProductsQuery,
  useGetAttributeGroupsMutation,
} = productsApi;
