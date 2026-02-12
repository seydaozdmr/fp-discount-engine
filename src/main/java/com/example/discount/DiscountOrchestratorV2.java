package com.example.discount;

import java.math.BigDecimal;
import java.util.List;

/**
 * End-to-end grouped stacking orchestration (best-per-group + group order + exclusivity + cap + audit).
 */
public final class DiscountOrchestratorV2 {

    private final GroupedSelector selector = new GroupedSelector();
    private final GroupedStackingApplier applier = new GroupedStackingApplier();

    public PricingResult price(OrderContext ctx, List<DiscountRule> rules) {
        return priceResult(ctx, rules).getOrThrow();
    }

    public Result<PricingResult> priceResult(OrderContext ctx, List<DiscountRule> rules) {
        return selector.selectBestPerGroupResult(ctx, rules)
                .map(selected -> applySelected(ctx, selected));
    }

    private PricingResult applySelected(OrderContext ctx, List<SelectedDiscount> selected) {
        ExclusivityPolicy exclusivity = new ExclusivityPolicy()
                .block(DiscountGroup.COUPON, DiscountGroup.CAMPAIGN)
                .block(DiscountGroup.CAMPAIGN, DiscountGroup.COUPON);

        DiscountCapPolicy cap = new DiscountCapPolicy(new BigDecimal("0.30")); // 30% cap

        var groupOrder = List.of(
                DiscountGroup.CAMPAIGN,
                DiscountGroup.VIP,
                DiscountGroup.COUPON
        );

        return applier.apply(ctx, selected, groupOrder, exclusivity, cap);
    }
}
