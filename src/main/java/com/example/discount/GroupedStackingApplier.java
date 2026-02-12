package com.example.discount;

import java.math.BigDecimal;
import java.util.*;

/**
 * Applies selected discounts by group order, enforcing:
 * - exclusivity matrix
 * - global discount cap
 * - total floor (never below zero)
 * and produces an audit trail.
 */
public final class GroupedStackingApplier {

    public PricingResult apply(
            OrderContext initialCtx,
            List<SelectedDiscount> selected,
            List<DiscountGroup> groupOrder,
            ExclusivityPolicy exclusivityPolicy,
            DiscountCapPolicy capPolicy
    ) {
        var byGroup = new EnumMap<DiscountGroup, SelectedDiscount>(DiscountGroup.class);
        for (var s : selected) byGroup.put(s.group(), s);

        var appliedGroups = EnumSet.noneOf(DiscountGroup.class);
        var steps = new ArrayList<AppliedStep>();

        OrderContext current = initialCtx;

        BigDecimal cap = capPolicy.maxAllowedDiscount(current.pricing());
        BigDecimal capRemaining = cap.subtract(current.pricing().discountTotal()).max(BigDecimal.ZERO);

        for (DiscountGroup g : groupOrder) {
            SelectedDiscount cand = byGroup.get(g);
            if (cand == null) continue;

            BigDecimal before = current.pricing().total();

            // Exclusivity
            if (!exclusivityPolicy.isAllowed(appliedGroups, g)) {
                steps.add(new AppliedStep(
                        cand.ruleName(), g,
                        safe(cand.amount()), BigDecimal.ZERO,
                        before, before,
                        "SKIPPED: blocked by exclusivity policy"
                ));
                continue;
            }

            BigDecimal requested = safe(cand.amount());
            BigDecimal allowedByCap = requested.min(capRemaining);
            BigDecimal allowedByTotal = allowedByCap.min(before.max(BigDecimal.ZERO));

            OrderPricing updated = current.pricing().addDiscount(allowedByTotal);
            BigDecimal after = updated.total();

            String note = "APPLIED";
            if (allowedByTotal.compareTo(requested) < 0) {
                note = "CAPPED: requested=" + requested + ", applied=" + allowedByTotal + ", capRemaining=" + capRemaining;
            }
            if (allowedByTotal.signum() == 0 && requested.signum() > 0) {
                note = "SKIPPED: cap exhausted or total is zero";
            }

            steps.add(new AppliedStep(
                    cand.ruleName(), g,
                    requested, allowedByTotal,
                    before, after,
                    note
            ));

            current = current.withPricing(updated);
            appliedGroups.add(g);
            capRemaining = capRemaining.subtract(allowedByTotal).max(BigDecimal.ZERO);
        }

        return new PricingResult(current.pricing(), List.copyOf(steps));
    }

    private static BigDecimal safe(BigDecimal v) {
        if (v == null) return BigDecimal.ZERO;
        return v.max(BigDecimal.ZERO);
    }
}
