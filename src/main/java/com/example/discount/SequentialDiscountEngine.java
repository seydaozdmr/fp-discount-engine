package com.example.discount;

import com.example.fpcore.Result;

import java.math.BigDecimal;
import java.util.List;

/**
 * Applies all eligible rules in the given order (compound stacking).
 */
public final class SequentialDiscountEngine {

    public OrderPricing applySequentially(OrderContext ctx, List<DiscountRule> rules) {
        return applySequentiallyResult(ctx, rules).getOrThrow();
    }

    public Result<OrderPricing> applySequentiallyResult(OrderContext ctx, List<DiscountRule> rules) {
        return rules.stream().reduce(
                Result.success(ctx),
                (acc, rule) -> acc.flatMap(current ->
                        rule.evaluate(current)
                                .map(amount -> current.withPricing(current.pricing().addDiscount(safeAmount(current, amount))))
                                .orElse(() -> Result.success(current))
                ),
                (left, right) -> left.flatMap(ignored -> right)
        ).map(OrderContext::pricing);
    }

    private static BigDecimal safeAmount(OrderContext ctx, BigDecimal amount) {
        if (amount == null) return BigDecimal.ZERO;
        BigDecimal safe = amount.max(BigDecimal.ZERO);
        return safe.min(ctx.pricing().total().max(BigDecimal.ZERO));
    }
}
