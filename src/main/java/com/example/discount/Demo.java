package com.example.discount;

import java.math.BigDecimal;
import java.util.List;

/**
 * Run a few examples from the conversation.
 */
public final class Demo {

    private static final BigDecimal THOUSAND = new BigDecimal("1000.00");

    public static void main(String[] args) {

        OrderPricing start = OrderPricing.of(new BigDecimal("1200.00"));
        OrderContext ctx = new OrderContext(true, true, start);

        List<DiscountRule> rules = List.of(
                // VIP 10%
                new DiscountRule(
                        "VIP_10_PERCENT",
                        DiscountGroup.VIP,
                        10,
                        OrderContext::vip,
                        c -> c.pricing().total().multiply(new BigDecimal("0.10"))
                ),
                // Coupon 50
                new DiscountRule(
                        "COUPON_50_TRY",
                        DiscountGroup.COUPON,
                        5,
                        OrderContext::hasCoupon,
                        c -> new BigDecimal("50.00")
                ),
                // Campaign 5% if total > 1000
                new DiscountRule(
                        "OVER_1000_5_PERCENT",
                        DiscountGroup.CAMPAIGN,
                        10,
                        c -> c.pricing().total().compareTo(THOUSAND) > 0,
                        c -> c.pricing().total().multiply(new BigDecimal("0.05"))
                )
        );

        System.out.println("=== 1) Sequential (compound) ===");
        OrderPricing seq = new SequentialDiscountEngine().applySequentially(ctx, rules);
        System.out.println(seq);

        System.out.println("\n=== 2) Best discount wins ===");
        BestDiscountWinsEngine bestEngine = new BestDiscountWinsEngine();
        BestDiscountWinsEngine.AppliedDiscount best = bestEngine.pickBest(ctx, rules);
        OrderPricing bestPricing = bestEngine.applyBest(ctx, rules);
        System.out.println("Best: " + best);
        System.out.println(bestPricing);

        System.out.println("\n=== 3) Grouped stacking (best-per-group + exclusivity + cap + audit) ===");
        PricingResult grouped = new DiscountOrchestratorV2().price(ctx, rules);
        System.out.println(grouped.pricing());
        grouped.steps().forEach(step -> System.out.println(" - " + step));
    }
}
