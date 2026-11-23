package com.example.exercises;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.function.Function;

import static com.example.exercises.PricePipeline.addVat;
import static com.example.exercises.PricePipeline.discount;
import static com.example.exercises.PricePipeline.pipeline;
import static com.example.exercises.PricePipeline.round;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PricePipelineTest {

    private static final BigDecimal VAT = new BigDecimal("0.20");
    private static final BigDecimal TEN_PERCENT = new BigDecimal("0.10");

    private static Function<BigDecimal, BigDecimal> minus(BigDecimal amount) {
        return price -> price.subtract(amount);
    }

    @Test
    void pipeline_applies_discount_then_vat_then_rounding() {
        assertEquals(new BigDecimal("108.00"), pipeline.apply(new BigDecimal("100")));
    }

    @Test
    void pipeline_rounds_to_two_digits_half_up() {
        assertEquals(new BigDecimal("21.59"), pipeline.apply(new BigDecimal("19.99")));
    }

    @Test
    void percentage_discount_and_vat_commute() {
        Function<BigDecimal, BigDecimal> reversed =
                addVat(VAT).andThen(discount(TEN_PERCENT)).andThen(round);

        assertEquals(pipeline.apply(new BigDecimal("100")), reversed.apply(new BigDecimal("100")));
        assertEquals(new BigDecimal("108.00"), reversed.apply(new BigDecimal("100")));
    }

    @Test
    void fixed_amount_discount_makes_order_matter() {
        BigDecimal ten = new BigDecimal("10");
        Function<BigDecimal, BigDecimal> discountFirst = minus(ten).andThen(addVat(VAT)).andThen(round);
        Function<BigDecimal, BigDecimal> vatFirst = addVat(VAT).andThen(minus(ten)).andThen(round);

        assertEquals(new BigDecimal("108.00"), discountFirst.apply(new BigDecimal("100")));
        assertEquals(new BigDecimal("110.00"), vatFirst.apply(new BigDecimal("100")));
    }

    @Test
    void pipeline_is_reusable_and_does_not_change_between_calls() {
        assertEquals(pipeline.apply(new BigDecimal("50")), pipeline.apply(new BigDecimal("50")));
    }
}
