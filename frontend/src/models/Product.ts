export interface BackendProduct {
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

export interface Product {
  id: string;
  name: string;
  price: number;
  description: string;
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

export interface ProductDetail extends Product {}
