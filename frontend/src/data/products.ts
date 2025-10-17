import type { Product, ProductDetail } from '../lib/types';

export const mockProducts: Product[] = [
  {
    id: '1',
    name: 'OnePlus 13 256GB Green',
    price: 44999,
    originalPrice: 54999,
    imageUrl: 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=400&h=400&fit=crop&crop=center',
    isFavorite: true,
    badge: 'sale',
    rating: 4.6,
    condition: 'new',
  },
  {
    id: '2',
    name: 'Sony WH-1000XM5 Wireless Noise Cancelling',
    price: 12999,
    imageUrl: 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=400&h=400&fit=crop&crop=center',
    isFavorite: false,
    badge: 'new',
    rating: 4.8,
    condition: 'new',
  },
  {
    id: '3',
    name: 'MacBook Pro 14 M3 16GB 512GB',
    price: 89999,
    originalPrice: 99999,
    imageUrl: 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=400&h=400&fit=crop&crop=center',
    isFavorite: true,
    badge: 'sale',
    rating: 4.7,
    condition: 'new',
  },
  {
    id: '4',
    name: 'AirPods Pro 2 USB-C',
    price: 8999,
    imageUrl: 'https://images.unsplash.com/photo-1600294037681-c80b4cb5b434?w=400&h=400&fit=crop&crop=center',
    isFavorite: false,
    badge: 'new',
    rating: 4.7,
    condition: 'new',
  },
  {
    id: '5',
    name: 'Apple Watch Series 9 45mm',
    price: 15999,
    imageUrl: 'https://images.unsplash.com/photo-1551698618-1dfe5d97d256?w=400&h=400&fit=crop&crop=center',
    isFavorite: true,
    rating: 4.6,
    condition: 'new',
  },
  {
    id: '6',
    name: 'PlayStation 5 DualSense Controller',
    price: 2999,
    imageUrl: 'https://images.unsplash.com/photo-1606144042614-b2417e99c4e3?w=400&h=400&fit=crop&crop=center',
    isFavorite: false,
    rating: 4.5,
    condition: 'used',
  },
  {
    id: '7',
    name: 'Samsung Galaxy S24 Ultra 256GB',
    price: 49999,
    originalPrice: 59999,
    imageUrl: 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=400&h=400&fit=crop&crop=center',
    isFavorite: true,
    rating: 4.9,
    condition: 'new',
  },
  {
    id: '8',
    name: 'Bose QuietComfort Ultra Headphones',
    price: 14999,
    imageUrl: 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=400&h=400&fit=crop&crop=center',
    isFavorite: false,
    rating: 4.8,
    condition: 'new',
  },
];

// Mock detailed products data
export const mockProductDetails: Record<string, ProductDetail> = {
  '1': {
    id: '1',
    name: 'OnePlus 13 256GB/16TB White (CN)',
    price: 33999,
    originalPrice: 39999,
    imageUrl: 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=800&h=800&fit=crop&crop=center',
    isFavorite: false,
    badge: 'sale',
    rating: 4.7,
    condition: 'new',
    productCode: '67854549',
    reviewCount: 644,
    inStock: true,
    images: [
      'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=800&h=800&fit=crop&crop=center',
      'https://images.unsplash.com/photo-1598327105666-5b89351aff97?w=800&h=800&fit=crop&crop=center',
      'https://images.unsplash.com/photo-1605236453806-6ff36851218e?w=800&h=800&fit=crop&crop=center',
      'https://images.unsplash.com/photo-1592286927505-c6d69a85e5c1?w=800&h=800&fit=crop&crop=center',
    ],
    colors: [
      { name: 'white', hex: '#FFFFFF', label: 'White' },
      { name: 'black', hex: '#000000', label: 'Black' },
      { name: 'blue', hex: '#3B82F6', label: 'Blue' },
    ],
    memory: [
      { size: '256GB', label: '256GB' },
      { size: '512GB', label: '512GB' },
      { size: '1TB', label: '1TB' },
    ],
    description: 'OnePlus 13 - flagship smartphone with cutting-edge technology and elegant design. Equipped with a powerful Snapdragon 8 Gen 3 processor, which provides incredible performance for games, multimedia and everyday tasks. The 50MP camera with an advanced stabilization system allows you to create professional photos and videos in any conditions. Fast 100W charging provides a full battery charge in just 23 minutes.',
    specifications: {
      'Display': '6.82" LTPO OLED, 3168x1440, 120Hz',
      'Processor': 'Snapdragon 8 Gen 3',
      'RAM': '16GB LPDDR5X',
      'Storage': '256GB UFS 4.0',
      'Main Camera': '50MP + 50MP + 64MP',
      'Front Camera': '32MP',
      'Battery': '5400mAh',
      'Charging': '100W SuperVOOC',
      'OS': 'OxygenOS 14 (Android 14)',
      'Weight': '210g',
    },
    deliveryOptions: [
      {
        icon: 'Truck',
        title: 'Fast delivery',
        description: 'Delivery to Kyiv on the next day',
      },
      {
        icon: 'Shield',
        title: '2 year warranty',
        description: 'Official manufacturer warranty',
      },
      {
        icon: 'RotateCcw',
        title: 'Return within 14 days',
        description: 'Ability to return the product',
      },
      {
        icon: 'Package',
        title: 'Safe packaging',
        description: 'Reliable protection during delivery',
      },
    ],
  },
  '2': {
    id: '2',
    name: 'Sony WH-1000XM5 Wireless Noise Cancelling Headphones',
    price: 12999,
    imageUrl: 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800&h=800&fit=crop&crop=center',
    isFavorite: false,
    badge: 'new',
    rating: 4.8,
    condition: 'new',
    productCode: '87654321',
    reviewCount: 892,
    inStock: true,
    images: [
      'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800&h=800&fit=crop&crop=center',
      'https://images.unsplash.com/photo-1546435770-a3e426bf472b?w=800&h=800&fit=crop&crop=center',
      'https://images.unsplash.com/photo-1484704849700-f032a568e944?w=800&h=800&fit=crop&crop=center',
      'https://images.unsplash.com/photo-1487215078519-e21cc028cb29?w=800&h=800&fit=crop&crop=center',
    ],
    colors: [
      { name: 'black', hex: '#000000', label: 'Black' },
      { name: 'silver', hex: '#C0C0C0', label: 'Silver' },
    ],
    description: 'Sony WH-1000XM5 - premium wireless headphones with industry-leading noise cancellation. The new processor and 8 microphones provide exceptional sound isolation. 30-hour battery life and fast charging support. Comfortable design for all-day wear.',
    specifications: {
      'Type': 'Over-ear wireless headphones',
      'Noise Cancellation': 'Active (ANC)',
      'Driver': '30mm',
      'Frequency Response': '4Hz - 40kHz',
      'Battery Life': 'Up to 30 hours',
      'Charging': 'USB-C, Fast Charging',
      'Bluetooth': '5.2',
      'Weight': '250g',
    },
    deliveryOptions: [
      {
        icon: 'Truck',
        title: 'Fast delivery',
        description: 'Delivery to Kyiv on the next day',
      },
      {
        icon: 'Shield',
        title: '1 year warranty',
        description: 'Official manufacturer warranty',
      },
      {
        icon: 'RotateCcw',
        title: 'Return within 14 days',
        description: 'Ability to return the product',
      },
      {
        icon: 'Package',
        title: 'Safe packaging',
        description: 'Reliable protection during delivery',
      },
    ],
  },
  '3': {
    id: '3',
    name: 'MacBook Pro 14 M3 16GB 512GB',
    price: 89999,
    originalPrice: 99999,
    imageUrl: 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=800&h=800&fit=crop&crop=center',
    isFavorite: false,
    badge: 'sale',
    rating: 4.7,
    condition: 'new',
    productCode: '12345678',
    reviewCount: 1247,
    inStock: true,
    images: [
      'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=800&h=800&fit=crop&crop=center',
      'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=800&h=800&fit=crop&crop=center',
      'https://images.unsplash.com/photo-1611186871348-b1ce696e52c9?w=800&h=800&fit=crop&crop=center',
      'https://images.unsplash.com/photo-1454165804606-c3d57bc86b40?w=800&h=800&fit=crop&crop=center',
    ],
    colors: [
      { name: 'space-gray', hex: '#71797E', label: 'Space Gray' },
      { name: 'silver', hex: '#E3E4E5', label: 'Silver' },
    ],
    memory: [
      { size: '512GB', label: '512GB' },
      { size: '1TB', label: '1TB' },
      { size: '2TB', label: '2TB' },
    ],
    description: 'MacBook Pro 14 with M3 chip - the perfect laptop for professionals. Incredible performance, stunning Liquid Retina XDR display, and up to 22 hours of battery life. Ideal for video editing, 3D modeling, and software development.',
    specifications: {
      'Display': '14.2" Liquid Retina XDR',
      'Resolution': '3024 x 1964',
      'Processor': 'Apple M3',
      'RAM': '16GB Unified Memory',
      'Storage': '512GB SSD',
      'Graphics': 'M3 10-core GPU',
      'Battery': 'Up to 22 hours',
      'Ports': '3x Thunderbolt 4, HDMI, SD card',
      'Weight': '1.55kg',
    },
    deliveryOptions: [
      {
        icon: 'Truck',
        title: 'Fast delivery',
        description: 'Delivery to Kyiv on the next day',
      },
      {
        icon: 'Shield',
        title: '1 year warranty',
        description: 'Official Apple warranty',
      },
      {
        icon: 'RotateCcw',
        title: 'Return within 14 days',
        description: 'Ability to return the product',
      },
      {
        icon: 'Package',
        title: 'Safe packaging',
        description: 'Reliable protection during delivery',
      },
    ],
  },
};

// Helper function to get product details
export const getProductById = (id: string): ProductDetail | undefined => {
  // Если есть детальные данные, возвращаем их
  if (mockProductDetails[id]) {
    return mockProductDetails[id];
  }
  
  // Если нет детальных данных, ищем базовые данные продукта
  const basicProduct = mockProducts.find(p => p.id === id);
  if (!basicProduct) {
    return undefined;
  }

  // Создаем детальную версию из базовых данных
  return {
    ...basicProduct,
    productCode: `PC${basicProduct.id}`,
    reviewCount: 0,
    inStock: true,
    images: [basicProduct.imageUrl],
    description: 'Product description will be added soon.',
    specifications: {},
    deliveryOptions: [
      {
        icon: 'Truck',
        title: 'Fast delivery',
        description: 'Delivery to Kyiv on the next day',
      },
    ],
  };
};
