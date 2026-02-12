package com.example.discount;

import java.math.BigDecimal;
import java.util.List;

public final class BestWinsResultDemo {

    private BestWinsResultDemo() {
    }

    public static void run() {
        System.out.println("=== BestWins Result Demo ===");

        BestDiscountWinsEngine engine = new BestDiscountWinsEngine();
        OrderContext ctx = new OrderContext(true, true, OrderPricing.of(new BigDecimal("1200.00")));

        List<DiscountRule> okRules = List.of(
                new DiscountRule("VIP10", DiscountGroup.VIP, 10, OrderContext::vip,
                        c -> c.pricing().total().multiply(new BigDecimal("0.10"))),
                new DiscountRule("COUPON50", DiscountGroup.COUPON, 10, OrderContext::hasCoupon,
                        c -> new BigDecimal("50.00")),
                new DiscountRule("OVER1000_5", DiscountGroup.CAMPAIGN, 10,
                        c -> c.pricing().total().compareTo(new BigDecimal("1000.00")) > 0,
                        c -> c.pricing().total().multiply(new BigDecimal("0.05")))
        );
        Result<BestDiscountWinsEngine.AppliedDiscount> successPick = engine.pickBestResult(ctx, okRules);
        Result<OrderPricing> successPricing = engine.applyBestResult(ctx, okRules);
        System.out.println("success pick: " + successPick);
        System.out.println("success pricing: " + successPricing);

        DiscountRule neverEligible = new DiscountRule("NOPE", DiscountGroup.CAMPAIGN, 1, c -> false, c -> new BigDecimal("10.00"));
        Result<BestDiscountWinsEngine.AppliedDiscount> emptyPick = engine.pickBestResult(ctx, List.of(neverEligible));
        Result<OrderPricing> emptyPricing = engine.applyBestResult(ctx, List.of(neverEligible));
        System.out.println("empty pick: " + emptyPick);
        System.out.println("empty pricing fallback: " + emptyPricing);

        DiscountRule badRule = new DiscountRule(
                "BAD_BEST",
                DiscountGroup.CAMPAIGN,
                1,
                c -> true,
                c -> {
                    throw new IllegalStateException("best-wins rule exploded");
                }
        );
        Result<BestDiscountWinsEngine.AppliedDiscount> failurePick = engine.pickBestResult(ctx, List.of(badRule));
        System.out.println("failure pick: " + failurePick);
        System.out.println();
    }
}
