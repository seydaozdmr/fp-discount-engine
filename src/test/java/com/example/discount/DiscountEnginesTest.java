package com.example.discount;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DiscountEnginesTest {

    @Test
    void sequential_compound_applies_all_in_order() {
        OrderPricing start = OrderPricing.of(new BigDecimal("1200.00"));
        OrderContext ctx = new OrderContext(true, true, start);

        List<DiscountRule> rules = List.of(
                new DiscountRule("VIP10", DiscountGroup.VIP, 10, OrderContext::vip,
                        c -> c.pricing().total().multiply(new BigDecimal("0.10"))),
                new DiscountRule("COUPON50", DiscountGroup.COUPON, 10, OrderContext::hasCoupon,
                        c -> new BigDecimal("50.00")),
                new DiscountRule("OVER1000_5", DiscountGroup.CAMPAIGN, 10,
                        c -> c.pricing().total().compareTo(new BigDecimal("1000.00")) > 0,
                        c -> c.pricing().total().multiply(new BigDecimal("0.05")))
        );

        OrderPricing result = new SequentialDiscountEngine().applySequentially(ctx, rules);

        assertEquals(new BigDecimal("978.50"), result.total());
        assertEquals(new BigDecimal("221.50"), result.discountTotal());
    }

    @Test
    void best_wins_picks_highest_amount() {
        OrderPricing start = OrderPricing.of(new BigDecimal("1200.00"));
        OrderContext ctx = new OrderContext(true, true, start);

        List<DiscountRule> rules = List.of(
                new DiscountRule("VIP10", DiscountGroup.VIP, 10, OrderContext::vip,
                        c -> c.pricing().total().multiply(new BigDecimal("0.10"))), // 120
                new DiscountRule("COUPON50", DiscountGroup.COUPON, 10, OrderContext::hasCoupon,
                        c -> new BigDecimal("50.00")), // 50
                new DiscountRule("OVER1000_5", DiscountGroup.CAMPAIGN, 10,
                        c -> c.pricing().total().compareTo(new BigDecimal("1000.00")) > 0,
                        c -> c.pricing().total().multiply(new BigDecimal("0.05"))) // 60
        );

        BestDiscountWinsEngine engine = new BestDiscountWinsEngine();
        var best = engine.pickBest(ctx, rules);

        assertEquals("VIP10", best.ruleName());
        assertEquals(new BigDecimal("120.00"), best.amount());
    }

    @Test
    void grouped_exclusivity_blocks_coupon_vs_campaign_and_cap_applies() {
        OrderPricing start = OrderPricing.of(new BigDecimal("1000.00"));
        OrderContext ctx = new OrderContext(true, true, start);

        // campaign = 200, coupon = 150, vip = 100 => selected per group
        List<DiscountRule> rules = List.of(
                new DiscountRule("VIP10", DiscountGroup.VIP, 10, OrderContext::vip,
                        c -> c.pricing().total().multiply(new BigDecimal("0.10"))), // 100
                new DiscountRule("COUPON150", DiscountGroup.COUPON, 10, OrderContext::hasCoupon,
                        c -> new BigDecimal("150.00")),
                new DiscountRule("CAMPAIGN200", DiscountGroup.CAMPAIGN, 10, c -> true,
                        c -> new BigDecimal("200.00"))
        );

        // Orchestrator uses:
        // - exclusivity: coupon blocks campaign and vice versa
        // - cap: 30% => max 300
        // group order: CAMPAIGN, VIP, COUPON
        PricingResult result = new DiscountOrchestratorV2().price(ctx, rules);

        // CAMPAIGN applied (200), VIP applied (100) => cap reached (300), COUPON skipped due to exclusivity and/or cap
        assertEquals(new BigDecimal("700.00"), result.pricing().total());
        assertEquals(new BigDecimal("300.00"), result.pricing().discountTotal());
        assertTrue(result.steps().stream().anyMatch(s -> s.ruleName().equals("COUPON150") && s.appliedDiscount().compareTo(BigDecimal.ZERO) == 0));
    }

    @Test
    void grouped_selector_result_surfaces_rule_failures() {
        OrderPricing start = OrderPricing.of(new BigDecimal("100.00"));
        OrderContext ctx = new OrderContext(false, false, start);

        DiscountRule badRule = new DiscountRule(
                "BAD_RULE",
                DiscountGroup.CAMPAIGN,
                1,
                c -> true,
                c -> { throw new IllegalStateException("boom"); }
        );

        Result<List<SelectedDiscount>> result =
                new GroupedSelector().selectBestPerGroupResult(ctx, List.of(badRule));

        assertTrue(result.isFailure());
    }
}
