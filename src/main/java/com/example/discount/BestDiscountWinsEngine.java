package com.example.discount;

import com.example.fpcore.Result;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Evaluates all eligible rules and applies only the best one,
 * ranked by {@link DiscountRanking#BEST_FIRST}.
 */
public final class BestDiscountWinsEngine {

    public record AppliedDiscount(String ruleName, DiscountGroup group, BigDecimal amount) {}

    public AppliedDiscount pickBest(OrderContext ctx, List<DiscountRule> rules) {
        return pickBestResult(ctx, rules)
                .orElse(() -> Result.success(noDiscount()))
                .getOrThrow();
    }

    public Result<AppliedDiscount> pickBestResult(OrderContext ctx, List<DiscountRule> rules) {
        SelectedDiscount best = null;
        for (DiscountRule rule : rules) {
            Result<BigDecimal> evaluated = rule.evaluate(ctx);
            if (evaluated.isFailure()) {
                return Result.failure(evaluated.failureCause());
            }
            if (evaluated.isEmpty()) {
                continue;
            }

            SelectedDiscount candidate = new SelectedDiscount(
                    rule.name(), rule.group(), rule.priority(), safeAmount(ctx, evaluated.getOrThrow()));
            if (best == null || DiscountRanking.BEST_FIRST.compare(candidate, best) < 0) {
                best = candidate;
            }
        }

        if (best == null) {
            return Result.empty();
        }
        return Result.success(new AppliedDiscount(best.ruleName(), best.group(), best.amount()));
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

    private static BigDecimal safeAmount(OrderContext ctx, BigDecimal amount) {
        if (amount == null) return BigDecimal.ZERO;
        BigDecimal safe = amount.max(BigDecimal.ZERO);
        BigDecimal capped = safe.min(ctx.pricing().total().max(BigDecimal.ZERO));
        return capped.setScale(2, RoundingMode.HALF_UP);
    }
}
