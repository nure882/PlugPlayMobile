export type OrderStatus = 'Shipped' | 'Processing' | 'Delivered' | 'Cancelled';

export interface OrderItem {
    productName: string;
    productId: number;
    quantity: number;
    price: number;
}

export interface Order {
    id: number;
    orderDate: string;
    status: OrderStatus;
    totalAmount: number;
    deliveryMethod: string;
    fullAddress: string;
    paymentType: string;
    orderItems: OrderItem[];
}

export const getStatusColor = (status: OrderStatus): string => {
    switch (status) {
        case 'Shipped':
            return 'bg-blue-100 text-blue-700';
        case 'Processing':
            return 'bg-yellow-100 text-yellow-700';
        case 'Delivered':
            return 'bg-green-100 text-green-700';
        case 'Cancelled':
            return 'bg-gray-100 text-gray-700';
        default:
            return 'bg-gray-100 text-gray-700';
    }
};

// Mock data for testing
export const mockOrders: Order[] = [
    {
        id: 10233,
        orderDate: 'Nov 12, 2025',
        status: 'Shipped',
        totalAmount: 89.99,
        deliveryMethod: 'Standard Delivery',
        fullAddress: 'Moscow, Kutuzovsky Prospekt, House 8, Apartment 105',
        paymentType: 'Cash on Delivery',
        orderItems: [
            {
                productName: 'Pro NVMe 1TB SSD',
                productId: 7,
                quantity: 1,
                price: 129.99,
            },
            {
                productName: 'SmartFit Pro Band',
                productId: 6,
                quantity: 1,
                price: 89.99,
            },
        ],
    },
    {
        id: 10232,
        orderDate: 'Nov 10, 2025',
        status: 'Processing',
        totalAmount: 219.97,
        deliveryMethod: 'Express Delivery',
        fullAddress: 'Kyiv, Khreshchatyk Street, House 22, Apartment 15',
        paymentType: 'Credit Card',
        orderItems: [
            {
                productName: 'Wireless Headphones X1',
                productId: 12,
                quantity: 2,
                price: 79.99,
            },
            {
                productName: 'USB-C Hub Pro',
                productId: 8,
                quantity: 1,
                price: 59.99,
            },
        ],
    },
    {
        id: 10231,
        orderDate: 'Nov 08, 2025',
        status: 'Delivered',
        totalAmount: 59.99,
        deliveryMethod: 'Standard Delivery',
        fullAddress: 'Berlin, Friedrichstrasse, House 100, Apartment 42',
        paymentType: 'PayPal',
        orderItems: [
            {
                productName: 'Gaming Mouse RGB',
                productId: 15,
                quantity: 1,
                price: 59.99,
            },
        ],
    },
];
