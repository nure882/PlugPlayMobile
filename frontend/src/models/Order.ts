import { Address } from "./Address.ts";
import OrderItem from "./OrderItem.ts";

// Extended OrderItem with UI-specific properties
export interface OrderItemWithDetails extends OrderItem {
    productName?: string;
    price?: number;
}


export interface Order {
    id: number;
    userId: number;
    orderDate: string;
    status: number | string; // Support both number (from API) and string (for display)
    totalAmount: number;
    discountAmount: number;
    deliveryMethod: number | string; // Support both number (from API) and string (for display)
    paymentMethod: number;
    deliveryAddressId: number;
    paymentStatus: number;
    transactionId: number;
    paymentCreated: string;
    paymentProcessed: string;
    paymentFailureReason: string | null;
    updatedAt: string;
    deliveryAddress: Address;
    orderItems: OrderItemWithDetails[];
    // UI-specific properties
    fullAddress?: string;
    paymentType?: string;
    shipmentCost?: number;
}

export type OrderStatus = 'Shipped' | 'Processing' | 'Delivered' | 'Cancelled';

export const getStatusColor = (status: number | string): string => {
    // Handle string status values
    if (typeof status === 'string') {
        switch (status) {
            case 'Processing':
                return 'bg-yellow-100 text-yellow-700';
            case 'Shipped':
                return 'bg-blue-100 text-blue-700';
            case 'Delivered':
                return 'bg-green-100 text-green-700';
            case 'Cancelled':
                return 'bg-gray-100 text-gray-700';
            default:
                return 'bg-gray-100 text-gray-700';
        }
    }

    // Handle number status values
    switch (status) {
        case 0: // Processing
            return 'bg-yellow-100 text-yellow-700';
        case 1: // Shipped
            return 'bg-blue-100 text-blue-700';
        case 2: // Delivered
            return 'bg-green-100 text-green-700';
        case 3: // Cancelled
            return 'bg-gray-100 text-gray-700';
        default:
            return 'bg-gray-100 text-gray-700';
    }
};

// Mock data for testing
export const mockOrders: Order[] = [
    {
        id: 10233,
        userId: 1,
        orderDate: 'Nov 12, 2025',
        status: 'Shipped',
        totalAmount: 219.98,
        discountAmount: 0,
        deliveryMethod: 'Standard Delivery',
        paymentMethod: 0,
        deliveryAddressId: 1,
        paymentStatus: 1,
        transactionId: 1001,
        paymentCreated: '2025-11-12T09:00:00Z',
        paymentProcessed: '2025-11-12T09:05:00Z',
        paymentFailureReason: null,
        updatedAt: '2025-11-12T10:00:00Z',
        fullAddress: 'Moscow, Kutuzovsky Prospekt, House 8, Apartment 105',
        paymentType: 'Cash on Delivery',
        shipmentCost: 5.99,
        deliveryAddress: {
            id: 1,
            city: 'Moscow',
            street: 'Kutuzovsky Prospekt',
            house: '8',
            apartments: '105',
        },
        orderItems: [
            {
                id: 1,
                productId: 7,
                productName: 'Pro NVMe 1TB SSD',
                quantity: 1,
                price: 129.99,
            },
            {
                id: 2,
                productId: 6,
                productName: 'SmartFit Pro Band',
                quantity: 1,
                price: 89.99,
            },
        ],
    },
    {
        id: 10232,
        userId: 1,
        orderDate: 'Nov 10, 2025',
        status: 'Processing',
        totalAmount: 219.97,
        discountAmount: 0,
        deliveryMethod: 'Express Delivery',
        paymentMethod: 1,
        deliveryAddressId: 2,
        paymentStatus: 1,
        transactionId: 1002,
        paymentCreated: '2025-11-10T13:00:00Z',
        paymentProcessed: '2025-11-10T13:05:00Z',
        paymentFailureReason: null,
        updatedAt: '2025-11-10T14:00:00Z',
        fullAddress: 'Kyiv, Khreshchatyk Street, House 22, Apartment 15',
        paymentType: 'Credit Card',
        shipmentCost: 12.99,
        deliveryAddress: {
            id: 2,
            city: 'Kyiv',
            street: 'Khreshchatyk Street',
            house: '22',
            apartments: '15',
        },
        orderItems: [
            {
                id: 3,
                productId: 12,
                productName: 'Wireless Headphones X1',
                quantity: 2,
                price: 79.99,
            },
            {
                id: 4,
                productId: 8,
                productName: 'USB-C Hub Pro',
                quantity: 1,
                price: 59.99,
            },
        ],
    },
    {
        id: 10231,
        userId: 1,
        orderDate: 'Nov 08, 2025',
        status: 'Delivered',
        totalAmount: 59.99,
        discountAmount: 0,
        deliveryMethod: 'Standard Delivery',
        paymentMethod: 2,
        deliveryAddressId: 3,
        paymentStatus: 1,
        transactionId: 1003,
        paymentCreated: '2025-11-08T11:00:00Z',
        paymentProcessed: '2025-11-08T11:05:00Z',
        paymentFailureReason: null,
        updatedAt: '2025-11-08T12:00:00Z',
        fullAddress: 'Berlin, Friedrichstrasse, House 100, Apartment 42',
        paymentType: 'PayPal',
        shipmentCost: 5.99,
        deliveryAddress: {
            id: 3,
            city: 'Berlin',
            street: 'Friedrichstrasse',
            house: '100',
            apartments: '42',
        },
        orderItems: [
            {
                id: 5,
                productId: 15,
                productName: 'Gaming Mouse RGB',
                quantity: 1,
                price: 59.99,
            },
        ],
    },
];


