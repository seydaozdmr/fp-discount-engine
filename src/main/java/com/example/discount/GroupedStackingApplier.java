package com.example.discount;

import org.springframework.core.annotation.Order;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Stream;


/**
 * Applies selected discounts by group order, enforcing:
 * - exclusivity matrix
 * - global discount cap
 * - total floor (never below zero)
 * and produces an audit trail.
 */
public final class GroupedStackingApplier {

    private record Acc(OrderPricing pricing, BigDecimal capRemaining, Set<DiscountGroup> appliedGroups, List<AppliedStep> appliedSteps){}


    public PricingResult applyFold(OrderContext initialCtx, List<SelectedDiscount> selected, List<DiscountGroup> groupOrder,
                                   ExclusivityPolicy exclusivityPolicy, DiscountCapPolicy capPolicy) {
        var byGroup = new EnumMap<DiscountGroup, SelectedDiscount>(DiscountGroup.class);
        for (var s : selected) byGroup.put(s.group(), s);

        BigDecimal cap = capPolicy.maxAllowedDiscount(initialCtx.pricing());
        BigDecimal capRemaining = cap.subtract(initialCtx.pricing().discountTotal()).max(BigDecimal.ZERO);

        List<SelectedDiscount> orderSelected = groupOrder.stream().map(byGroup::get).filter(Objects::nonNull).toList();

        //acc holds the current pricing, remaining cap, applied groups and steps
        Acc start = new Acc (initialCtx.pricing(), capRemaining, Set.of(), List.of());

        Acc out = orderSelected.stream().reduce(start, (acc, cand)-> {
            BigDecimal before = acc.pricing().total();

            boolean allowed = exclusivityPolicy.isAllowed(acc.appliedGroups(), cand.group());

            if (!allowed) {
                AppliedStep blockedStep = new AppliedStep(cand.ruleName(), cand.group(), safe(cand.amount()), BigDecimal.ZERO,
                        before, before, "SKIPPED: blocked by exclusivity policy");

                List<AppliedStep> newSteps = Stream.concat(acc.appliedSteps().stream(),Stream.of(blockedStep)).toList();
                return new Acc(acc.pricing(), acc.capRemaining(), acc.appliedGroups(), newSteps);
            }

            //TODO 1 : requested = max(cand.amount, 0) to avoid negative discounts
            BigDecimal requested = safe(cand.amount());

            //TODO 2: applyable = min(requested, start.capRemaining, before) to enforce cap and total floor
            BigDecimal applyable = requested.min(acc.capRemaining()).min(before.max(BigDecimal.ZERO)); //total floor is zero, not negative
            //TODO 3: update pricing by adding applyable discount
            OrderPricing newPricing = acc.pricing().addDiscount(applyable);
            BigDecimal after = newPricing.total();

            //TODO 4: newCap: start.capRemaining - applyable
            BigDecimal newCapRemaining = acc.capRemaining().subtract(applyable).max(BigDecimal.ZERO); //cap remaining can't be negative

            //TODO 5: create AppliedStep with appropriate note (APPLIED, CAPPED or SKIPPED)
            String note = "APPLIED";
            if (applyable.compareTo(requested) < 0) {
                note = "CAPPED: requested=" + requested + ", applied=" + applyable + ", capRemaining=" + acc.capRemaining();
            }

            if (applyable.signum() == 0 && requested.signum() > 0) {
                note = "SKIPPED: cap exhausted or total is zero";
            }

            AppliedStep step = new AppliedStep(cand.ruleName(), cand.group(), requested, applyable, before, after, note);

            // TODO 6 : immutable steps append
            List<AppliedStep> newSteps = Stream.concat(acc.appliedSteps().stream(), Stream.of(step)).toList();

            //TODO 7: applied groups immutable update
            Set<DiscountGroup> newAppliedGroups = addGroup(acc.appliedGroups(), cand.group());

            return new Acc(newPricing, newCapRemaining, newAppliedGroups, newSteps);
        }, (a,b) -> {throw new UnsupportedOperationException("no parallel stream");});


        return new PricingResult(out.pricing(), out.appliedSteps());
    }

    private static Set<DiscountGroup> addGroup(Set<DiscountGroup> groups, DiscountGroup group){
        EnumSet<DiscountGroup> copy = groups.isEmpty() ? EnumSet.noneOf(DiscountGroup.class) : EnumSet.copyOf(groups);
        copy.add(group);
        return Set.copyOf(copy);
    }

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
            //max allowed by cap and total floor
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
        //negative discounts don't make sense in this context, treat as zero
        return v.max(BigDecimal.ZERO);
    }
}
