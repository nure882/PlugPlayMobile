enum PaymentStatus
{
  Paid = 0,
  Failed = 1,
  TestPaid = 2,
  NotPaid = 3,
}

export const PaymenStatusInfo: Record<PaymentStatus, {
  label: string;
}> = {
  [PaymentStatus.Paid]: {
    label: "Paid",
  },
  [PaymentStatus.Failed]: {
    label: "Failed",
  },
  [PaymentStatus.TestPaid]: {
    label: "Test paid",
  },
  [PaymentStatus.NotPaid]: {
    label: "Not paid",
  },
}

export default PaymentStatus;
