import { useState } from 'react';
import { ChevronDown } from 'lucide-react';
import { Order, getStatusColor } from '../../models/Order';

interface OrderHistoryCardProps {
    order: Order;
}

export default function OrderHistoryCard({ order }: OrderHistoryCardProps) {
    const [isExpanded, setIsExpanded] = useState(false);

    return (
        <div className="border border-gray-200 rounded-lg overflow-hidden bg-white">
            {/* Header - Always visible */}
            <button
                onClick={() => setIsExpanded(!isExpanded)}
                className="w-full p-4 flex items-start justify-between hover:bg-gray-50 transition-colors"
            >
                <div className="flex-1 text-left space-y-2">
                    <div className="flex items-center justify-between">
                        <div>
                            <p className="text-xs text-gray-500">Order ID</p>
                            <p className="text-sm font-semibold text-gray-900">#{order.id}</p>
                        </div>
                        <div className="flex items-center gap-2">
                            <span
                                className={`px-3 py-1 rounded text-xs font-medium ${getStatusColor(
                                    order.status
                                )}`}
                            >
                                {order.status}
                            </span>
                            <ChevronDown
                                className={`w-5 h-5 text-gray-400 transition-transform duration-300 ${isExpanded ? 'rotate-180' : ''
                                    }`}
                            />
                        </div>
                    </div>

                    <div className="flex items-center justify-between">
                        <div>
                            <p className="text-xs text-gray-500">Date</p>
                            <p className="text-sm text-gray-700">{order.orderDate}</p>
                        </div>
                        <div className="text-right">
                            <p className="text-xs text-gray-500">Total Amount</p>
                            <p className="text-sm font-semibold text-gray-900">
                                ${order.totalAmount.toFixed(2)}
                            </p>
                        </div>
                    </div>
                </div>
            </button>

            {/* Expanded Details */}
            <div
                className={`transition-all duration-300 ease-in-out overflow-hidden ${isExpanded ? 'max-h-[1000px] opacity-100' : 'max-h-0 opacity-0'
                    }`}
            >
                <div className="px-4 pb-4 pt-2 border-t border-gray-100 space-y-4">
                    {/* Delivery Method */}
                    <div>
                        <p className="text-xs text-gray-500 mb-1">Delivery Method</p>
                        <p className="text-sm text-gray-900">{order.deliveryMethod}</p>
                    </div>

                    {/* Full Address */}
                    <div>
                        <p className="text-xs text-gray-500 mb-1">Full Address</p>
                        <p className="text-sm text-gray-900">{order.fullAddress}</p>
                    </div>

                    {/* Payment Type */}
                    <div>
                        <p className="text-xs text-gray-500 mb-1">Payment Type</p>
                        <p className="text-sm text-gray-900">{order.paymentType}</p>
                    </div>

                    {/* Order Items */}
                    <div>
                        <p className="text-xs text-gray-500 mb-2">Order Items</p>
                        <div className="space-y-2">
                            {order.orderItems.map((item, index) => (
                                <div
                                    key={index}
                                    className="flex items-center justify-between p-3 bg-gray-50 rounded border border-gray-100"
                                >
                                    <div className="flex-1">
                                        <p className="text-sm font-medium text-gray-900">
                                            {item.productName}
                                        </p>
                                        <p className="text-xs text-gray-500">Product ID: {item.productId}</p>
                                    </div>
                                    <div className="text-right">
                                        <p className="text-sm text-gray-700">Qty: {item.quantity}</p>
                                        <p className="text-sm font-medium text-gray-900">
                                            ${item.price.toFixed(2)}
                                        </p>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}
