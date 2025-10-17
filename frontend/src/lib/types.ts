export interface Address {
  city: string;
  street: string;
  house: string;
  apartments: string;
}

// Product variant options
export interface ProductColor {
  name: string;
  hex: string;
  label: string;
}

export interface ProductMemory {
  size: string;
  label: string;
}

export interface DeliveryOption {
  icon: string;
  title: string;
  description: string;
}

// Basic product interface (for catalog)
export interface Product {
  id: string;
  name: string;
  price: number;
  originalPrice?: number;
  imageUrl: string;
  isFavorite: boolean;
  badge?: 'new' | 'sale';
  rating: number;
  condition: 'new' | 'used';
}

// Extended product interface (for detail page)
export interface ProductDetail extends Product {
  productCode: string;
  reviewCount: number;
  inStock: boolean;
  images: string[];
  colors?: ProductColor[];
  memory?: ProductMemory[];
  description: string;
  specifications: Record<string, string>;
  deliveryOptions: DeliveryOption[];
}
