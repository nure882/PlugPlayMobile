import {Review} from './Review';

export interface Product {
  id: number;
  name: string;
  price: number;
  description: string;
  stockQuantity: number;
  pictureUrls: string[];
  createdAt: string;
  category: {
    id: number;
    name: string;
    parentCategory?: {
      id: number;
      name: string;
    };
  } | null;
  reviews?: Review[];
}
