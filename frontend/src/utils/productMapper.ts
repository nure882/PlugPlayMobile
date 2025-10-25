import { BackendProduct, ProductDetail } from '../models/Product.ts';


export const mapBackendProductToDetail = (backendProduct: BackendProduct): ProductDetail => {
  return {
    id: backendProduct.id.toString(),
    name: backendProduct.name,
    price: backendProduct.price,
    description: backendProduct.description,
    stockQuantity: backendProduct.stockQuantity,
    pictureUrl: backendProduct.pictureUrl,
    createdAt: backendProduct.createdAt,
    category: backendProduct.category,
  };
};


export const mapBackendProductToCatalog = (backendProduct: BackendProduct): ProductDetail => {
  return mapBackendProductToDetail(backendProduct);
};
