import { BackendProduct, ProductDetail } from '../types';

/**
 * Maps backend product data to frontend ProductDetail interface
 * Only maps fields that actually exist in the backend API
 */
export const mapBackendProductToDetail = (backendProduct: BackendProduct): ProductDetail => {
  return {
    id: backendProduct.id.toString(),
    name: backendProduct.name,
    price: backendProduct.price,
    description: backendProduct.description,
    stockQuantity: backendProduct.stockQuantity,
    createdAt: backendProduct.createdAt,
    category: backendProduct.category,
  };
};

/**
 * Maps backend product data to frontend Product interface (for catalog)
 * Only maps fields that actually exist in the backend API
 */
export const mapBackendProductToCatalog = (backendProduct: BackendProduct): ProductDetail => {
  return mapBackendProductToDetail(backendProduct);
};
