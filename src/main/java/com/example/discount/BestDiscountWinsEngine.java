package com.example.discount;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

/**
 * Evaluates all eligible rules and applies only the one with the biggest discount.
 * If there is a tie, lower priority wins.
 */
public final class BestDiscountWinsEngine {

    public record AppliedDiscount(String ruleName, DiscountGroup group, BigDecimal amount) {}

    public AppliedDiscount pickBest(OrderContext ctx, List<DiscountRule> rules) {
        return rules.stream()
                .filter(r -> r.eligible().test(ctx))
                .map(r -> new AppliedDiscount(r.name(), r.group(), safeAmount(ctx, r.calculate().apply(ctx))))
                .max(Comparator
                        .comparing((AppliedDiscount ad) -> ad.amount)
                        .thenComparingInt(ad -> -findPriority(rules, ad.ruleName)))
                .orElse(new AppliedDiscount("NO_DISCOUNT", DiscountGroup.CAMPAIGN, BigDecimal.ZERO));
    }

    public OrderPricing applyBest(OrderContext ctx, List<DiscountRule> rules) {
        AppliedDiscount best = pickBest(ctx, rules);
        return ctx.pricing().addDiscount(best.amount());
    }

    private static int findPriority(List<DiscountRule> rules, String ruleName) {
        return rules.stream()
                .filter(r -> r.name().equals(ruleName))
                .findFirst()
                .map(DiscountRule::priority)
                .orElse(Integer.MAX_VALUE);
    }

    private static BigDecimal safeAmount(OrderContext ctx, BigDecimal amount) {
        if (amount == null) return BigDecimal.ZERO;
        BigDecimal safe = amount.max(BigDecimal.ZERO);
        BigDecimal capped = safe.min(ctx.pricing().total().max(BigDecimal.ZERO));
        return capped.setScale(2, RoundingMode.HALF_UP);
    }
}
