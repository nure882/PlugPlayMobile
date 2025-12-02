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