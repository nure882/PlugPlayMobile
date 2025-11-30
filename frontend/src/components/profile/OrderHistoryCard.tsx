import { useState } from 'react';
import { ChevronDown, X } from 'lucide-react';
import { Order} from '../../models/Order';
import DeliveryMethod, {DeliveryMethodInfo} from '../../models/enums/DeliveryMethod';
import PaymentMethod, {PaymentMethodInfo} from '../../models/enums/PaymentMethod';
import OrderStatus, { OrderStatusInfo } from '../../models/enums/OrderStatus';
import { Address } from '../../models/Address';

interface OrderHistoryCardProps {
    order: Order;
    onCancelOrder?: (orderId: number) => void;
    addresses : Address[];
}

function formatHryvnia(amount?: number) {
    const v = amount ?? 0;
    return `₴${v.toFixed(2)}`;
}

export default function OrderHistoryCard({ order, onCancelOrder, addresses }: OrderHistoryCardProps) {
    const [isExpanded, setIsExpanded] = useState(false);

    const canCancelOrder = order.status ===  OrderStatus.Created || order.status === OrderStatus.Approved;

    const handleCancelOrder = (e: React.MouseEvent) => {
        e.stopPropagation();
        if (onCancelOrder && window.confirm(`Are you sure you want to cancel order #${order.id}?`)) {
            onCancelOrder(order.id);
        }
    };

     const getformattedAddress = (addressId: number) => {
        const address = findAddress(addressId);
        
        return address
          ? `${address.city}, ${address.street} ${address.house} ${
              address.apartments && `, Apt ${address.apartments}`
            }`
          : "Address information not available";
    }

    const findAddress = (addressId: number) => {
        return addresses.find((a) => a.id == addressId);
    }

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
                                className={`px-3 py-1 rounded text-xs font-medium ${OrderStatusInfo[order.status].displayColor}`}
                            >
                                {OrderStatusInfo[order.status].label}
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
                                {formatHryvnia(order.totalAmount)}
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
                        <p className="text-sm text-gray-900">{DeliveryMethodInfo[order.deliveryMethod].label}</p>
                    </div>

                    {/* Full Address */}
                    <div>
                        <p className="text-xs text-gray-500 mb-1">Full Address</p>
                        <p className="text-sm text-gray-900">{getformattedAddress(order.deliveryAddressId)}</p>
                    </div>

                    {/* Payment Type */}
                    <div>
                        <p className="text-xs text-gray-500 mb-1">Payment Method</p>
                        <p className="text-sm text-gray-900">{PaymentMethodInfo[order.paymentMethod].label}</p>
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
                                            Price: {formatHryvnia(item.price)}
                                        </p>
                                        <p className="text-sm font-medium text-gray-900">
                                            Total: {formatHryvnia((item.price ?? 0) * item.quantity)}
                                        </p>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>

                    {/* Cost Breakdown */}
                    <div className="pt-2 border-t border-gray-200">
                        <div className="flex justify-between text-sm mb-1">
                            <span className="text-gray-600">Subtotal</span>
                            <span className="text-gray-900">
                                {formatHryvnia(order.totalAmount - (DeliveryMethodInfo[order.deliveryMethod].price))}
                            </span>
                        </div>
                        <div className="flex justify-between text-sm mb-2">
                            <span className="text-gray-600">Shipment Cost</span>
                            <span className="text-gray-900">{formatHryvnia(DeliveryMethodInfo[order.deliveryMethod].price)}</span>
                        </div>
                        <div className="flex justify-between text-base font-semibold pt-2 border-t border-gray-200">
                            <span className="text-gray-900">Total</span>
                            <span className="text-gray-900">{formatHryvnia(order.totalAmount)}</span>
                        </div>
                    </div>

                    {/* Cancel Order Button */}
                    {canCancelOrder && (
                        <div className="pt-2">
                            <button
                                onClick={handleCancelOrder}
                                className="w-full flex items-center justify-center gap-2 px-4 py-2.5 bg-red-50 hover:bg-red-100 text-red-600 hover:text-red-700 rounded-lg transition-colors font-medium text-sm border border-red-200"
                            >
                                <X className="w-4 h-4" />
                                Cancel Order
                            </button>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}
