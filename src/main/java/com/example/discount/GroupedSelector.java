package com.example.discount;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Selects best rule per group by (amount desc, priority asc).
 */
public final class GroupedSelector {

    public List<SelectedDiscount> selectBestPerGroup(OrderContext ctx, List<DiscountRule> rules) {
        return selectBestPerGroupResult(ctx, rules).getOrThrow();
    }

    public Result<List<SelectedDiscount>> selectBestPerGroupResult(OrderContext ctx, List<DiscountRule> rules) {
        Map<DiscountGroup, List<DiscountRule>> byGroup =
                rules.stream().collect(Collectors.groupingBy(DiscountRule::group));

        List<SelectedDiscount> selected = new ArrayList<>();

        for (var entry : byGroup.entrySet()) {
            Result<SelectedDiscount> best = bestInGroupResult(ctx, entry.getKey(), entry.getValue());
            if (best.isFailure()) {
                return Result.failure(best.failureCause());
            }
            if (best.isSuccess()) {
                selected.add(best.getOrThrow());
            }
        }

        return Result.success(List.copyOf(selected));
    }

    private Result<SelectedDiscount> bestInGroupResult(
            OrderContext ctx,
            DiscountGroup group,
            List<DiscountRule> groupRules
    ) {
        SelectedDiscount best = null;

        for (DiscountRule rule : groupRules) {
            Result<BigDecimal> amountResult = rule.evaluate(ctx);
            if (amountResult.isFailure()) {
                return Result.failure(amountResult.failureCause());
            }
            if (amountResult.isEmpty()) {
                continue;
            }

            BigDecimal amount = safeAmount(ctx, amountResult.getOrThrow());
            SelectedDiscount candidate = new SelectedDiscount(rule.name(), group, rule.priority(), amount);
            if (best == null || isBetter(candidate, best)) {
                best = candidate;
            }
        }

        return best == null ? Result.empty() : Result.success(best);
    }

    private static boolean isBetter(SelectedDiscount candidate, SelectedDiscount best) {
        int amountCmp = candidate.amount().compareTo(best.amount());
        if (amountCmp != 0) {
            return amountCmp > 0;
        }
        return candidate.priority() < best.priority();
    }

    private static BigDecimal safeAmount(OrderContext ctx, BigDecimal amount) {
        if (amount == null) return BigDecimal.ZERO;
        BigDecimal safe = amount.max(BigDecimal.ZERO);
        return safe.min(ctx.pricing().total().max(BigDecimal.ZERO));
    }
}
