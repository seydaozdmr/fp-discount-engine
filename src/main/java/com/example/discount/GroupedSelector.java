package com.example.discount;

import com.example.fpcore.Option;
import com.example.fpcore.Result;

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
    // For better error handling, we can return Result here instead of throwing exceptions.
    public Result<List<SelectedDiscount>> selectBestPerGroupResult(OrderContext ctx, List<DiscountRule> rules) {
        // Group rules by their discount group
        Map<DiscountGroup, List<DiscountRule>> byGroup =
                rules.stream().collect(Collectors.groupingBy(DiscountRule::group));

        List<SelectedDiscount> selected = new ArrayList<>();
        // For each group, find the best discount and add it to the selected list
        for (var entry : byGroup.entrySet()) {
            // Find the best discount in the group
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
    // Finds the best discount in a group by evaluating all rules and
    // picking the one with the highest amount (and lowest priority as tiebreaker).
    private Result<SelectedDiscount> bestInGroupResult(
            OrderContext ctx,
            DiscountGroup group,
            List<DiscountRule> groupRules
    ) {
        return Result.traverseReduce(groupRules, rule -> toCandidateOption(ctx, group, rule))
                .map(GroupedSelector::bestCandidate)
                .flatMap(best -> best.isDefined() ? Result.success(best.getOrThrow()): Result.empty());
    }
    // Evaluates a rule and converts it to an Option<SelectedDiscount>. If evaluation fails, returns failure.
    private Result<Option<SelectedDiscount>> toCandidateOption(OrderContext ctx, DiscountGroup group, DiscountRule rule) {
        Result<BigDecimal> amountResult = rule.evaluate(ctx);

        if (amountResult.isFailure()) {
            return Result.failure(amountResult.failureCause());
        }
        if (amountResult.isEmpty()) {
            return Result.success(Option.none());
        }

        BigDecimal amount = safeAmount(ctx, amountResult.getOrThrow());
        SelectedDiscount candidate = new SelectedDiscount(rule.name(), group, rule.priority(), amount);
        return Result.success(Option.some(candidate));
    }

    private static Option<SelectedDiscount> bestCandidate(List<Option<SelectedDiscount>> candidates) {
        return candidates.stream()
                .filter(Option::isDefined)
                .map(Option::getOrThrow)
                .reduce((left, right) -> isBetter(left, right) ? left : right)
                .map(Option::some)
                .orElseGet(Option::none);
    }
    // Returns true if candidate is better than best by (amount desc, priority asc)
    private static boolean isBetter(SelectedDiscount candidate, SelectedDiscount best) {
        int amountCmp = candidate.amount().compareTo(best.amount());
        if (amountCmp != 0) {
            return amountCmp > 0;
        }
        return candidate.priority() < best.priority();
    }
    // Ensures amount is non-negative and does not exceed order total.
    private static BigDecimal safeAmount(OrderContext ctx, BigDecimal amount) {
        if (amount == null) return BigDecimal.ZERO;
        BigDecimal safe = amount.max(BigDecimal.ZERO);
        return safe.min(ctx.pricing().total().max(BigDecimal.ZERO));
    }
}
