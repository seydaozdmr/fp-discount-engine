package com.example.discount;

import com.example.fpcore.Result;

import java.math.BigDecimal;
import java.util.List;

public final class SequentialResultDemo {

    private SequentialResultDemo() {
    }

    public static void run() {
        System.out.println("=== Sequential Result Demo ===");

        OrderContext ctx = new OrderContext(true, true, OrderPricing.of(new BigDecimal("1200.00")));
        List<DiscountRule> okRules = List.of(
                new DiscountRule("VIP10", DiscountGroup.VIP, 10, OrderContext::vip,
                        c -> c.pricing().total().multiply(new BigDecimal("0.10"))),
                new DiscountRule("COUPON50", DiscountGroup.COUPON, 10, OrderContext::hasCoupon,
                        c -> new BigDecimal("50.00"))
        );

        Result<OrderPricing> success = new SequentialDiscountEngine().applySequentiallyResult(ctx, okRules);
        System.out.println("success: " + success);

        DiscountRule badRule = new DiscountRule(
                "BAD_SEQ",
                DiscountGroup.CAMPAIGN,
                1,
                c -> true,
                c -> {
                    throw new IllegalStateException("sequential rule exploded");
                }
        );
        Result<OrderPricing> failure = new SequentialDiscountEngine().applySequentiallyResult(ctx, List.of(badRule));
        System.out.println("failure: " + failure);
        System.out.println();
    }
}
