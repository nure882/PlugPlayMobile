export interface Product {
  id: string;
  name: string;
  price: number;
  originalPrice?: number;
  rating: number;
  reviewCount: number;
  image: string;
  badge?: 'NEW' | 'SALE';
  isFavorite: boolean;
}

export const mockProducts: Product[] = [
  {
    id: '1',
    name: 'OnePlus 13 256GB Green',
    price: 44999,
    originalPrice: 54999,
    rating: 4.8,
    reviewCount: 156,
    image: 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=400&h=400&fit=crop&crop=center',
    badge: 'SALE',
    isFavorite: true
  },
  {
    id: '2',
    name: 'Sony WH-1000XM5 Wireless Noise Cancelling',
    price: 12999,
    rating: 4.9,
    reviewCount: 432,
    image: 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=400&h=400&fit=crop&crop=center',
    isFavorite: false
  },
  {
    id: '3',
    name: 'MacBook Pro 14 M3 16GB 512GB',
    price: 89999,
    originalPrice: 99999,
    rating: 4.7,
    reviewCount: 89,
    image: 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=400&h=400&fit=crop&crop=center',
    badge: 'SALE',
    isFavorite: true
  },
  {
    id: '4',
    name: 'AirPods Pro 2 USB-C',
    price: 8999,
    rating: 4.8,
    reviewCount: 672,
    image: 'https://images.unsplash.com/photo-1600294037681-c80b4cb5b434?w=400&h=400&fit=crop&crop=center',
    badge: 'NEW',
    isFavorite: false
  },
  {
    id: '5',
    name: 'Apple Watch Series 9 45mm',
    price: 15999,
    rating: 4.6,
    reviewCount: 234,
    image: 'https://images.unsplash.com/photo-1551698618-1dfe5d97d256?w=400&h=400&fit=crop&crop=center',
    isFavorite: true
  },
  {
    id: '6',
    name: 'PlayStation 5 DualSense Controller',
    price: 2999,
    rating: 4.5,
    reviewCount: 567,
    image: 'https://images.unsplash.com/photo-1606144042614-b2417e99c4e3?w=400&h=400&fit=crop&crop=center',
    isFavorite: false
  },
  {
    id: '7',
    name: 'Samsung Galaxy S24 Ultra 256GB',
    price: 49999,
    originalPrice: 59999,
    rating: 4.7,
    reviewCount: 345,
    image: 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=400&h=400&fit=crop&crop=center',
    badge: 'SALE',
    isFavorite: true
  },
  {
    id: '8',
    name: 'Bose QuietComfort Ultra Headphones',
    price: 14999,
    rating: 4.8,
    reviewCount: 123,
    image: 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=400&h=400&fit=crop&crop=center',
    badge: 'NEW',
    isFavorite: false
  }
];
