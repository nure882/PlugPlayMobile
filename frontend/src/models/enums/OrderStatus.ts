enum OrderStatus
{
  Created = 0,
  Approved = 1,
  Collected = 2,
  Delivered = 3,
  Cancelled = 4,
}

export const OrderStatusInfo: Record<OrderStatus, {
  label: string;
  displayColor: string;
}> = {
  [OrderStatus.Created]: {
    label: "Created",
    displayColor: 'bg-yellow-100 text-yellow-700'
  },
  [OrderStatus.Approved]: {
    label: "Approved",
    displayColor: 'bg-blue-100 text-blue-700'
  },
  [OrderStatus.Collected]: {
    label: "Collected",
    displayColor: 'bg-green-100 text-green-700'
  },
  [OrderStatus.Delivered]: {
    label: "Delivered",
    displayColor: 'bg green-200 text-green-900'
  },
  [OrderStatus.Cancelled]: {
    label: "Cancelled",
    displayColor: 'bg-gray-100 text-gray-700'
  },
};

export default OrderStatus;

