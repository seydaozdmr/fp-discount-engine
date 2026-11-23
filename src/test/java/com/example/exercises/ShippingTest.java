package com.example.exercises;

import com.example.exercises.Shipping.Region;
import com.example.exercises.Shipping.Speed;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ShippingTest {

    @Test
    void marmara_standard_three_desi() {
        assertEquals(new BigDecimal("45.00"),
                Shipping.shippingFee.apply(Region.MARMARA).apply(Speed.STANDARD).apply(3));
    }

    @Test
    void dogu_express_three_desi_applies_express_multiplier() {
        assertEquals(new BigDecimal("112.50"),
                Shipping.shippingFee.apply(Region.DOGU).apply(Speed.EXPRESS).apply(3));
    }

    @Test
    void partially_applied_region_and_speed_waits_for_desi_only() {
        Function<Integer, BigDecimal> istanbul =
                Shipping.shippingFee.apply(Region.MARMARA).apply(Speed.STANDARD);

        assertEquals(new BigDecimal("35.00"), istanbul.apply(1));
        assertEquals(new BigDecimal("45.00"), istanbul.apply(3));
    }

    @Test
    void partially_applied_function_can_be_reused_with_different_desi() {
        Function<Integer, BigDecimal> doguExpress =
                Shipping.shippingFee.apply(Region.DOGU).apply(Speed.EXPRESS);

        assertEquals(new BigDecimal("97.50"), doguExpress.apply(1));
        assertEquals(new BigDecimal("112.50"), doguExpress.apply(3));
    }

    @Test
    void express_costs_one_and_a_half_times_standard_for_same_region_and_desi() {
        BigDecimal standard = Shipping.shippingFee.apply(Region.MARMARA).apply(Speed.STANDARD).apply(2);
        BigDecimal express = Shipping.shippingFee.apply(Region.MARMARA).apply(Speed.EXPRESS).apply(2);

        assertEquals(0, standard.multiply(new BigDecimal("1.5")).compareTo(express));
    }

    @Test
    void result_is_rounded_to_two_digits() {
        BigDecimal fee = Shipping.shippingFee.apply(Region.MARMARA).apply(Speed.STANDARD).apply(3);

        assertEquals(2, fee.scale());
    }

    @Test
    void marmara_function_field_matches_curried_call() {
        assertEquals(Shipping.shippingFee.apply(Region.MARMARA).apply(Speed.EXPRESS).apply(4),
                Shipping.marmaraRegionFunction.apply(Speed.EXPRESS).apply(4));
    }
}
