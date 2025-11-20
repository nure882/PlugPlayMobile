export enum DeliveryMethod {
  Courier = 0,
  Post = 1,
  Premium = 2,
  Pickup = 3,
}

export const DeliveryMethodInfo: Record<DeliveryMethod, {
  label: string;
  price: number;
}> = {
  [DeliveryMethod.Courier]: {
    label: "Courier Delivery",
    price: 100,
  },
  [DeliveryMethod.Post]: {
    label: "Postal Service",
    price: 80,
  },
  [DeliveryMethod.Premium]: {
    label: "Premium Delivery",
    price: 150,
  },
  [DeliveryMethod.Pickup]: {
    label: "Store Pickup",
    price: 0,
  },
};

export default DeliveryMethod;

