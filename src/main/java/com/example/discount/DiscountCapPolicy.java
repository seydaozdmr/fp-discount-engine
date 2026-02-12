package com.example.discount;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Global cap for total discounts (e.g. subtotal * 0.30).
 */
public final class DiscountCapPolicy {

    private final BigDecimal maxRatio;

    public DiscountCapPolicy(BigDecimal maxRatio) {
        this.maxRatio = maxRatio;
    }

    public BigDecimal maxAllowedDiscount(OrderPricing pricing) {
        return pricing.subtotal()
                .multiply(maxRatio)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
