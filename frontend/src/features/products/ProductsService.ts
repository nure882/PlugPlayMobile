import { Product } from "../../models/Product.ts";
import {
  useGetAvailableProductsQuery,
  useFilterProductsQuery,
  useGetAttributeGroupsMutation,
  useGetProductByIdQuery,
} from "../../api/productsApi.ts";
import { skipToken } from "@reduxjs/toolkit/query";
import { useEffect } from "react";

export interface AttributeGroup {
  id: number;
  name: string;
  dataType: string;
  unit?: string;
  productAttributeDtos: Array<{
    id: number;
    attributeId: number;
    productId: number;
    strValue?: string;
    numValue?: number;
  }>;
}

export interface FilterProductsResponse {
  products: Product[];
  total: number;
  totalPages: number;
  page: number;
  pageSize: number;
}

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
  private products: Product[] = [];
  private attributeGroups: AttributeGroup[] = [];

  /**
   * Build filter string from attribute filters
   */
  buildFilterString(attributeFilters?: Record<string, string[]>): string | undefined {
    if (!attributeFilters || Object.keys(attributeFilters).length === 0) {
      return undefined;
    }

    const filterParts: string[] = [];
    Object.entries(attributeFilters).forEach(([attrId, values]) => {
      if (values.length > 0) {
        values.forEach((value) => {
          filterParts.push(`${attrId}:${value}`);
        });
      }
    });

    return filterParts.length > 0 ? filterParts.join(',') : undefined;
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
    
    console.log('[ProductsService] Options:', { categoryId, minPrice, maxPrice, sort, page, pageSize });

    // Use int.MaxValue (2147483647) for "all categories"
    const actualCategoryId = categoryId ?? 2147483647;

    // Build filter query parameters
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

    console.log('[ProductsService] Filter params:', filterParams);

    // Always fetch filtered products (no skipToken)
    const {
      data: filteredResponse = { products: [], total: 0, totalPages: 0, page: 1, pageSize: 100 },
      isLoading: isLoadingFiltered,
      isError: isErrorFiltered,
      refetch: refetchFiltered,
    } = useFilterProductsQuery(filterParams);

    const productIds = (filteredResponse?.products ?? []).map((p: Product) => p.id) as number[];

    const [fetchAttributeGroups, {
      data: attributeGroups = [],
      isLoading: isLoadingAttributes,
      isError: isErrorAttributes,
    }] = useGetAttributeGroupsMutation();

    useEffect(() => {
      fetchAttributeGroups({ 
        categoryId: actualCategoryId, 
        productIds: productIds.length ? productIds : undefined 
      }).then((result) => {
        console.log("fetchAttributeGroups resolved:", result);
      }).catch((error) => {
        console.error("fetchAttributeGroups rejected:", error);
      });
    }, [categoryId, fetchAttributeGroups, productIds.length]); // Remove productIds.join(',')

    const products = filteredResponse?.products || [];
    const isLoading = isLoadingFiltered || isLoadingAttributes;
    const isError = isErrorFiltered || isErrorAttributes;

    console.log('[ProductsService] Results:', {
      productsCount: products.length,
      isLoading,
      isError,
      attributeGroupsCount: attributeGroups.length,
      products
    });

    // Update internal state
    this.products = products;
    this.attributeGroups = attributeGroups;

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

  /**
   * Hook to fetch a single product by ID
   */
  useProductById(productId: number) {
    const { data: product, isLoading, isError, refetch } = useGetProductByIdQuery(productId);

    return {
      product,
      isLoading,
      isError,
      refetch,
    };
  }

  /**
   * Hook to fetch attribute groups for a category
   */
  useAttributeGroups(categoryId?: number | null, productIds?: number[]) {
    // ...existing code...
    
        const [fetchAttributeGroups, {
          data: attributeGroups = [],
          isLoading: isLoadingAttributes,
          isError: isErrorAttributes,
        }] = useGetAttributeGroupsMutation();
    
        useEffect(() => {
          if (categoryId !== null && categoryId !== undefined) {
            // Ensure we're calling the mutation trigger function
            fetchAttributeGroups({ 
              categoryId, 
              productIds: productIds.length ? productIds : undefined 
            });
          }
        }, [categoryId, fetchAttributeGroups, productIds.length, productIds.join(',')]);
    
    // ...existing code...

    this.attributeGroups = attributeGroups;

    return {
      attributeGroups,
      isLoading,
      isError,
    };
  }

  /**
   * Get current products (cached)
   */
  getProducts(): Product[] {
    return this.products;
  }

  /**
   * Get current attribute groups (cached)
   */
  getAttributeGroups(): AttributeGroup[] {
    return this.attributeGroups;
  }

  /**
   * Extract unique values from attribute group
   */
  getUniqueAttributeValues(group: AttributeGroup): string[] {
    const values = group.productAttributeDtos
      .map((pa) => {
        if (group.dataType === 'string' || group.dataType === 'str') {
          return pa.strValue;
        } else if (group.dataType === 'bool') {
          return pa.numValue === 1 ? 'true' : 'false';
        } else {
          return pa.numValue?.toString();
        }
      })
      .filter((v) => v != null) as string[];

    return Array.from(new Set(values)).sort((a, b) => {
      if (group.dataType === 'decimal' || group.dataType === 'num') {
        return parseFloat(a) - parseFloat(b);
      }
      return a.localeCompare(b);
    });
  }

  /**
   * Apply local filtering to products (client-side)
   */
  applyLocalFilters(
    products: Product[],
    filters: {
      minPrice?: number;
      maxPrice?: number;
      searchQuery?: string;
    }
  ): Product[] {
    let filtered = [...products];

    if (filters.minPrice !== undefined && filters.minPrice > 0) {
      filtered = filtered.filter((p) => p.price >= filters.minPrice!);
    }
    if (filters.maxPrice !== undefined && filters.maxPrice < Infinity) {
      filtered = filtered.filter((p) => p.price <= filters.maxPrice!);
    }

    if (filters.searchQuery) {
      const query = filters.searchQuery.toLowerCase();
      filtered = filtered.filter(
        (p) =>
          p.name.toLowerCase().includes(query) ||
          p.description.toLowerCase().includes(query)
      );
    }

    return filtered;
  }

  /**
   * Sort products
   */
  sortProducts(products: Product[], sortOption: string): Product[] {
    const sorted = [...products];

    switch (sortOption) {
      case 'price-asc':
        return sorted.sort((a, b) => a.price - b.price);
      case 'price-desc':
        return sorted.sort((a, b) => b.price - a.price);
      case 'newest':
        return sorted.sort(
          (a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
        );
      case 'name-asc':
        return sorted.sort((a, b) => a.name.localeCompare(b.name));
      case 'name-desc':
        return sorted.sort((a, b) => b.name.localeCompare(a.name));
      default:
        return sorted;
    }
  }

  /**
   * Get products by category
   */
  getProductsByCategory(products: Product[], categoryId: number): Product[] {
    return products.filter((p) => p.category?.id === categoryId);
  }

  /**
   * Get available stock products
   */
  getAvailableProducts(products: Product[]): Product[] {
    return products.filter((p) => p.stockQuantity > 0);
  }
}

// Export singleton instance
export const productsService = new ProductsService();

// Export hook for convenience
export const useProductsService = (options: FilterOptions = {}) => {
  return productsService.useProducts(options);
};