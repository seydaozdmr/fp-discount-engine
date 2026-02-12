package com.example.discount;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Immutable snapshot of pricing values used by discount engines.
 */
public final class OrderPricing {

    private final BigDecimal subtotal;      // base price before discounts
    private final BigDecimal discountTotal; // accumulated discounts
    private final BigDecimal total;         // subtotal - discountTotal

    private OrderPricing(BigDecimal subtotal, BigDecimal discountTotal) {
        this.subtotal = subtotal;
        this.discountTotal = discountTotal;
        this.total = subtotal.subtract(discountTotal);
    }

    public static OrderPricing of(BigDecimal subtotal) {
        Objects.requireNonNull(subtotal, "subtotal");
        return new OrderPricing(money(subtotal), BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
    }

    public BigDecimal subtotal() {
        return subtotal;
    }

    public BigDecimal discountTotal() {
        return discountTotal;
    }

    public BigDecimal total() {
        return total;
    }

    /**
     * Adds a discount amount safely:
     * - never applies negative discount
     * - never reduces total below 0
     */
    public OrderPricing addDiscount(BigDecimal discount) {
        Objects.requireNonNull(discount, "discount");
        BigDecimal d = money(discount).max(BigDecimal.ZERO);
        BigDecimal maxAllowed = total.max(BigDecimal.ZERO);
        BigDecimal applied = d.min(maxAllowed);
        return new OrderPricing(subtotal, money(discountTotal.add(applied)));
    }

    private static BigDecimal money(BigDecimal v) {
        return v.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String toString() {
        return "OrderPricing{subtotal=" + subtotal + ", discountTotal=" + discountTotal + ", total=" + total + "}";
    }
}
