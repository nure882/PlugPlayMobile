enum PaymentMethod {
  Cash = 0,
  Card = 1,
}

export const PaymentMethodInfo: Record<PaymentMethod, {
  label: string;
}> = {
  [PaymentMethod.Cash]: {
    label: "Cash",
  },
  [PaymentMethod.Card]: {
    label: "Card",
  },
}

export default PaymentMethod;
