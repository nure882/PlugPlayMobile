export interface BackendProduct {
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

export interface Product {
  id: string;
  name: string;
  price: number;
  description: string;
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

export interface ProductDetail extends Product {}
