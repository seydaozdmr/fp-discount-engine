package com.example.exercises;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BasicTest {

    @Test
    void addVat_adds_twenty_percent() {
        assertEquals(new BigDecimal("120.00"), Basic.addVat(new BigDecimal("0.20")).apply(new BigDecimal("100.00")));
    }

    @Test
    void addVat_rounds_half_up_to_two_digits() {
        assertEquals(new BigDecimal("21.99"), Basic.addVat(new BigDecimal("0.10")).apply(new BigDecimal("19.99")));
    }

    @Test
    void addVat_with_zero_rate_keeps_price_with_two_digits() {
        assertEquals(new BigDecimal("5.00"), Basic.addVat(BigDecimal.ZERO).apply(new BigDecimal("5")));
    }
}
