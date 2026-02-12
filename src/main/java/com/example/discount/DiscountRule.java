package com.example.discount;

import com.example.fpcore.Result;

import java.math.BigDecimal;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A rule = eligibility predicate + amount calculation function.
 * Group and priority are used by grouped/best-wins strategies.
 */
public record DiscountRule(
        String name,
        DiscountGroup group,
        int priority,
        Predicate<OrderContext> eligible,
        Function<OrderContext, BigDecimal> calculate
) {
    public Result<BigDecimal> evaluate(OrderContext ctx) {
        try {
            if (!eligible.test(ctx)) {
                return Result.empty();
            }
            BigDecimal amount = calculate.apply(ctx);
            if (amount == null) {
                return Result.failure("Rule '" + name + "' returned null amount");
            }
            return Result.success(amount);
        } catch (Exception e) {
            return Result.failure(new IllegalStateException("Rule '" + name + "' failed", e));
        }
    }
}
