package com.plugplay.plugplaymobile.domain.model

enum class DeliveryMethod(val id: Int) {
    Courier(0),
    Post(1),
    Premium(2),
    Pickup(3)
}

enum class PaymentMethod(val id: Int) {
    Card(0),
    CashAfterDelivery(1),
    GooglePay(2)
}

enum class OrderStatus(val id: Int) {
    Created(0),
    Approved(1),
    Collected(2),
    Delivered(3),
    Cancelled(4)
}

enum class PaymentStatus(val id: Int) {
    Paid(0),
    Failed(1),
    TestPaid(2),
    NotPaid(3)
}