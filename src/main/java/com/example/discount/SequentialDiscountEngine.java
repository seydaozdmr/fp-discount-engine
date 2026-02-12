package com.example.discount;

import java.math.BigDecimal;
import java.util.List;

/**
 * Applies all eligible rules in the given order (compound stacking).
 */
public final class SequentialDiscountEngine {

    public OrderPricing applySequentially(OrderContext ctx, List<DiscountRule> rules) {
        OrderContext current = ctx;

        for (DiscountRule rule : rules) {
            if (!rule.eligible().test(current)) {
                continue;
            }
            BigDecimal discount = safeAmount(current, rule.calculate().apply(current));
            OrderPricing updated = current.pricing().addDiscount(discount);
            current = current.withPricing(updated);
        }

        return current.pricing();
    }

    private static BigDecimal safeAmount(OrderContext ctx, BigDecimal amount) {
        if (amount == null) return BigDecimal.ZERO;
        BigDecimal safe = amount.max(BigDecimal.ZERO);
        return safe.min(ctx.pricing().total().max(BigDecimal.ZERO));
    }
}
