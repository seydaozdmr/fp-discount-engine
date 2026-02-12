package com.example.discount.application.api;

import java.math.BigDecimal;

public record PricingRequest(
        BigDecimal subtotal,
        Boolean vip,
        Boolean hasCoupon
) {
}
