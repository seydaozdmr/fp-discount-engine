package com.example.exercises;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.function.Function;

import static com.example.exercises.PricePipeline.addVat;
import static com.example.exercises.PricePipeline.discount;
import static com.example.exercises.PricePipeline.fixedAmountDiscount;
import static com.example.exercises.PricePipeline.fixedPipeline;
import static com.example.exercises.PricePipeline.round;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FixedPricePipelineTest {

    private static final BigDecimal TEN = new BigDecimal("10");
    private static final BigDecimal VAT = new BigDecimal("0.20");

    @Test
    void fixedAmountDiscount_subtracts_amount() {
        assertEquals(0, new BigDecimal("90").compareTo(fixedAmountDiscount.apply(TEN).apply(new BigDecimal("100"))));
    }

    @Test
    void fixedAmountDiscount_applied_to_amount_returns_reusable_function() {
        Function<BigDecimal, BigDecimal> tenOff = fixedAmountDiscount.apply(TEN);

        assertEquals(0, new BigDecimal("40").compareTo(tenOff.apply(new BigDecimal("50"))));
        assertEquals(0, new BigDecimal("90").compareTo(tenOff.apply(new BigDecimal("100"))));
    }

    @Test
    void fixedAmountDiscount_does_not_round_by_itself() {
        assertEquals(new BigDecimal("9.995"),
                fixedAmountDiscount.apply(new BigDecimal("0.005")).apply(new BigDecimal("10")));
    }

    @Test
    void fixedPipeline_applies_fixed_discount_then_vat() {
        assertEquals(new BigDecimal("108.00"),
                fixedPipeline.apply(TEN).apply(VAT).apply(new BigDecimal("100")));
    }

    @Test
    void fixedPipeline_partially_applied_waits_only_for_price() {
        Function<BigDecimal, BigDecimal> tenOffWithVat = fixedPipeline.apply(TEN).apply(VAT);

        assertEquals(new BigDecimal("108.00"), tenOffWithVat.apply(new BigDecimal("100")));
        assertEquals(new BigDecimal("48.00"), tenOffWithVat.apply(new BigDecimal("50")));
    }

    @Test
    void fixedPipeline_applied_to_amount_only_still_waits_for_rate() {
        Function<BigDecimal, Function<BigDecimal, BigDecimal>> tenOff = fixedPipeline.apply(TEN);

        assertEquals(new BigDecimal("108.00"), tenOff.apply(VAT).apply(new BigDecimal("100")));
        assertEquals(new BigDecimal("99.00"), tenOff.apply(new BigDecimal("0.10")).apply(new BigDecimal("100")));
    }

    @Test
    void fixedPipeline_with_zero_vat_only_subtracts_the_amount() {
        assertEquals(new BigDecimal("90.00"),
                fixedPipeline.apply(TEN).apply(BigDecimal.ZERO).apply(new BigDecimal("100")));
    }

    @Test
    void fixedPipeline_equals_manual_nested_calls() {
        BigDecimal price = new BigDecimal("73.45");

        BigDecimal manual = round.apply(addVat(VAT).apply(fixedAmountDiscount.apply(TEN).apply(price)));

        assertEquals(manual, fixedPipeline.apply(TEN).apply(VAT).apply(price));
    }

    @Test
    void fixed_discount_before_vat_differs_from_vat_before_fixed_discount() {
        Function<BigDecimal, BigDecimal> vatFirst =
                addVat(VAT).andThen(fixedAmountDiscount.apply(TEN)).andThen(round);

        assertEquals(new BigDecimal("108.00"), fixedPipeline.apply(TEN).apply(VAT).apply(new BigDecimal("100")));
        assertEquals(new BigDecimal("110.00"), vatFirst.apply(new BigDecimal("100")));
    }

    @Test
    void percentage_and_fixed_discounts_do_not_commute() {
        Function<BigDecimal, BigDecimal> percentThenFixed =
                discount(new BigDecimal("0.10")).andThen(fixedAmountDiscount.apply(TEN)).andThen(round);
        Function<BigDecimal, BigDecimal> fixedThenPercent =
                fixedAmountDiscount.apply(TEN).andThen(discount(new BigDecimal("0.10"))).andThen(round);

        assertEquals(new BigDecimal("80.00"), percentThenFixed.apply(new BigDecimal("100")));
        assertEquals(new BigDecimal("81.00"), fixedThenPercent.apply(new BigDecimal("100")));
    }

    @Test
    void round_rounds_half_up_to_two_digits() {
        assertEquals(new BigDecimal("2.01"), round.apply(new BigDecimal("2.005")));
        assertEquals(new BigDecimal("2.00"), round.apply(new BigDecimal("2.004")));
        assertEquals(new BigDecimal("5.00"), round.apply(new BigDecimal("5")));
    }

    @Test
    void discount_multiplies_price_by_one_minus_rate_without_rounding() {
        assertEquals(0, new BigDecimal("0.045").compareTo(
                discount(new BigDecimal("0.10")).apply(new BigDecimal("0.05"))));
    }
}
