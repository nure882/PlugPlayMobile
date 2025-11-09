export interface Address {
  id? : number;
  city: string;
  street: string;
  house: string;
  apartments: string;
}

export interface DeliveryOption {
  icon: string;
  title: string;
  description: string;
}
