import {Product} from "../../models/Product.ts";
import {
  useFilterProductsQuery,
  useGetAttributeGroupsMutation,
} from "../../api/productsApi.ts";
import {useEffect, useMemo} from "react";
import AttributeGroup from "../../models/AttributeGroup.ts";

export interface FilterOptions {
  categoryId?: number | null;
  minPrice?: number;
  maxPrice?: number;
  attributeFilters?: Record<string, string[]>;
  sort?: string;
  page?: number;
  pageSize?: number;
}

export interface ProductsServiceResult {
  products: Product[];
  attributeGroups: AttributeGroup[];
  isLoading: boolean;
  isError: boolean;
  total?: number;
  totalPages?: number;
  refetch: () => void;
}

class ProductsService {
  /**
   * Build filter string from attribute filters
   */
  // ...existing code...
  buildFilterString(attributeFilters?: Record<string, string[]>): string | undefined {
    if (!attributeFilters || Object.keys(attributeFilters).length === 0) {
      return undefined;
    }
    const parts: string[] = [];

    Object.entries(attributeFilters).forEach(([attrId, values]) => {
      if (!values || values.length === 0) {
        return;
      }

      const safe = values
        .map(v => v.trim())
        .filter(v => v.length > 0)
        .map(v => v.replace(/[,;:]/g, ''));

      if (safe.length === 0) {
        return;
      }

      parts.push(`${attrId}:${safe.join(',')}`);
    });

    return parts.length > 0 ? parts.join(';') : undefined;
  }

  /**
   * Hook to fetch and manage products with optional filtering
   */
  useProducts(options: FilterOptions = {}): ProductsServiceResult {
    const {
      categoryId,
      minPrice,
      maxPrice,
      attributeFilters,
      sort = 'price-asc',
      page = 1,
      pageSize = 100,
    } = options;

    // Use int.MaxValue (2147483647) for "all categories"
    const actualCategoryId = categoryId ?? 2147483647;
    const filterString = this.buildFilterString(attributeFilters);

    const filterParams: any = {
      categoryId: actualCategoryId,
      minPrice: minPrice && minPrice > 0 ? minPrice : 0,
      maxPrice: maxPrice && maxPrice < 5000 ? maxPrice : 1000000,
      sort,
      page,
      pageSize,
    };

    if (filterString !== undefined) {
      filterParams.filter = filterString;
    }

    const {
      data: filteredResponse = {products: [], total: 0, totalPages: 0, page: 1, pageSize: 100},
      isLoading: isLoadingFiltered,
      isError: isErrorFiltered,
      refetch: refetchFiltered,
    } = useFilterProductsQuery(filterParams);

    const products = filteredResponse?.products || [];
    const productIds = products.map((p: Product) => p.id);

    const [fetchAttributeGroups, {
      data: attributeGroups = [],
      isLoading: isLoadingAttributes,
      isError: isErrorAttributes,
    }] = useGetAttributeGroupsMutation();

    const productIdsKey = useMemo(() => {
      return productIds.length > 0 ? productIds.sort((a, b) => a - b).join(',') : '';
    }, [productIds.length, products.length]);

    useEffect(() => {
      fetchAttributeGroups({
        categoryId: actualCategoryId,
        productIds: productIds.length > 0 ? productIds : undefined
      }).then((result) => {
        console.log("fetchAttributeGroups resolved:", result);
      }).catch((error) => {
        console.error("fetchAttributeGroups rejected:", error);
      });
    }, [actualCategoryId, productIdsKey, fetchAttributeGroups]);

    const isLoading = isLoadingFiltered || isLoadingAttributes;
    const isError = isErrorFiltered || isErrorAttributes;

    console.log('[ProductsService] Results:', {
      productsCount: products.length,
      isLoading,
      isError,
      attributeGroupsCount: attributeGroups.length,
      productIdsKey,
    });

    return {
      products,
      attributeGroups,
      isLoading,
      isError,
      total: filteredResponse?.total,
      totalPages: filteredResponse?.totalPages,
      refetch: refetchFiltered,
    };
  }
}

export const productsService = new ProductsService();

export const useProductsService = (options: FilterOptions = {}) => {
  return productsService.useProducts(options);
};
