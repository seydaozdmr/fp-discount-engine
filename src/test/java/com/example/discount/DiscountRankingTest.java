package com.example.discount;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DiscountRankingTest {

    private static final OrderContext CTX =
            new OrderContext(true, true, OrderPricing.of(new BigDecimal("100.00")));

    private static DiscountRule fixed(String name, DiscountGroup group, int priority, String amount) {
        return new DiscountRule(name, group, priority, c -> true, c -> new BigDecimal(amount));
    }

    @Test
    void best_first_orders_by_amount_desc_then_priority_asc_then_name_asc() {
        List<SelectedDiscount> candidates = new ArrayList<>(List.of(
                new SelectedDiscount("B", DiscountGroup.VIP, 5, new BigDecimal("10")),
                new SelectedDiscount("C", DiscountGroup.VIP, 1, new BigDecimal("5")),
                new SelectedDiscount("A", DiscountGroup.VIP, 5, new BigDecimal("10")),
                new SelectedDiscount("D", DiscountGroup.VIP, 1, new BigDecimal("10"))
        ));

        candidates.sort(DiscountRanking.BEST_FIRST);

        assertEquals(List.of("D", "A", "B", "C"),
                candidates.stream().map(SelectedDiscount::ruleName).toList());
    }

    @Test
    void full_tie_is_resolved_the_same_way_by_both_engines_regardless_of_input_order() {
        DiscountRule alpha = fixed("ALPHA", DiscountGroup.CAMPAIGN, 5, "10.00");
        DiscountRule beta = fixed("BETA", DiscountGroup.CAMPAIGN, 5, "10.00");

        for (List<DiscountRule> rules : List.of(List.of(alpha, beta), List.of(beta, alpha))) {
            String bestWins = new BestDiscountWinsEngine().pickBest(CTX, rules).ruleName();
            String grouped = new GroupedSelector().selectBestPerGroup(CTX, rules).get(0).ruleName();

            assertEquals("ALPHA", bestWins, "best-wins with input " + rules.stream().map(DiscountRule::name).toList());
            assertEquals("ALPHA", grouped, "grouped with input " + rules.stream().map(DiscountRule::name).toList());
        }
    }

    @Test
    void grouped_selector_returns_groups_in_enum_declaration_order() {
        List<DiscountRule> rules = List.of(
                fixed("VIP5", DiscountGroup.VIP, 1, "5.00"),
                fixed("CAMPAIGN7", DiscountGroup.CAMPAIGN, 1, "7.00"),
                fixed("COUPON3", DiscountGroup.COUPON, 1, "3.00")
        );

        List<DiscountGroup> groups = new GroupedSelector().selectBestPerGroup(CTX, rules).stream()
                .map(SelectedDiscount::group)
                .toList();

        assertEquals(List.of(DiscountGroup.COUPON, DiscountGroup.CAMPAIGN, DiscountGroup.VIP), groups);
    }
}
