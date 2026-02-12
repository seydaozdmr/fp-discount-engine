package com.example.discount;

/**
 * Immutable context used when evaluating discount rules.
 * Extend this with whatever you need (restaurantId, items, customer tier, etc.).
 */
public record OrderContext(
        boolean vip,
        boolean hasCoupon,
        OrderPricing pricing
) {
    public OrderContext withPricing(OrderPricing newPricing) {
        return new OrderContext(vip, hasCoupon, newPricing);
    }
}
