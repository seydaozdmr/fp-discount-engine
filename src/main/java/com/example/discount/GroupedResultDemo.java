package com.example.discount;

import com.example.fpcore.Result;

import java.math.BigDecimal;
import java.util.List;

public final class GroupedResultDemo {

    private GroupedResultDemo() {
    }

    public static void run() {
        System.out.println("=== Grouped Result Demo ===");

        DiscountOrchestratorV2 orchestrator = new DiscountOrchestratorV2();
        OrderContext ctx = new OrderContext(true, true, OrderPricing.of(new BigDecimal("1000.00")));

        List<DiscountRule> okRules = List.of(
                new DiscountRule("VIP10", DiscountGroup.VIP, 10, OrderContext::vip,
                        c -> c.pricing().total().multiply(new BigDecimal("0.10"))),
                new DiscountRule("COUPON150", DiscountGroup.COUPON, 10, OrderContext::hasCoupon,
                        c -> new BigDecimal("150.00")),
                new DiscountRule("CAMPAIGN200", DiscountGroup.CAMPAIGN, 10, c -> true,
                        c -> new BigDecimal("200.00"))
        );
        Result<PricingResult> success = orchestrator.priceValidated(ctx, okRules);
        System.out.println("success: " + success);

        DiscountRule badRule = new DiscountRule(
                "BAD_GROUPED",
                DiscountGroup.CAMPAIGN,
                1,
                c -> true,
                c -> {
                    throw new IllegalStateException("grouped rule exploded");
                }
        );
        Result<PricingResult> failure = orchestrator.priceValidated(ctx, List.of(badRule));
        System.out.println("failure: " + failure);
        System.out.println();
    }
}
