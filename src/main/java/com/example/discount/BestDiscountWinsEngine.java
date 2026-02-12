package com.example.discount;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Evaluates all eligible rules and applies only the one with the biggest discount.
 * If there is a tie, lower priority wins.
 */
public final class BestDiscountWinsEngine {

    public record AppliedDiscount(String ruleName, DiscountGroup group, BigDecimal amount) {}
    private record Candidate(DiscountRule rule, BigDecimal amount) {}

    public AppliedDiscount pickBest(OrderContext ctx, List<DiscountRule> rules) {
        return pickBestResult(ctx, rules)
                .orElse(() -> Result.success(noDiscount()))
                .getOrThrow();
    }

    public Result<AppliedDiscount> pickBestResult(OrderContext ctx, List<DiscountRule> rules) {
        Candidate best = null;
        for (DiscountRule rule : rules) {
            Result<BigDecimal> evaluated = rule.evaluate(ctx);
            if (evaluated.isFailure()) {
                return Result.failure(evaluated.failureCause());
            }
            if (evaluated.isEmpty()) {
                continue;
            }

            Candidate candidate = new Candidate(rule, safeAmount(ctx, evaluated.getOrThrow()));
            if (best == null || isBetter(candidate, best)) {
                best = candidate;
            }
        }

        if (best == null) {
            return Result.empty();
        }
        return Result.success(new AppliedDiscount(best.rule.name(), best.rule.group(), best.amount));
    }

    public OrderPricing applyBest(OrderContext ctx, List<DiscountRule> rules) {
        return applyBestResult(ctx, rules).getOrThrow();
    }

    public Result<OrderPricing> applyBestResult(OrderContext ctx, List<DiscountRule> rules) {
        return pickBestResult(ctx, rules)
                .map(best -> ctx.pricing().addDiscount(best.amount()))
                .orElse(() -> Result.success(ctx.pricing()));
    }

    private static AppliedDiscount noDiscount() {
        return new AppliedDiscount("NO_DISCOUNT", DiscountGroup.CAMPAIGN, BigDecimal.ZERO);
    }

    private static boolean isBetter(Candidate a, Candidate b) {
        int amountCmp = a.amount.compareTo(b.amount);
        if (amountCmp != 0) {
            return amountCmp > 0;
        }
        return a.rule.priority() < b.rule.priority();
    }

    private static BigDecimal safeAmount(OrderContext ctx, BigDecimal amount) {
        if (amount == null) return BigDecimal.ZERO;
        BigDecimal safe = amount.max(BigDecimal.ZERO);
        BigDecimal capped = safe.min(ctx.pricing().total().max(BigDecimal.ZERO));
        return capped.setScale(2, RoundingMode.HALF_UP);
    }
}
