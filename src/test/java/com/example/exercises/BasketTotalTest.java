package com.example.exercises;

import com.example.exercises.BasketTotal.BasketLineItem;
import com.example.fpcore.List;
import com.example.fpcore.Tuple;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static com.example.exercises.BasketTotal.totals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BasketTotalTest {

    private static BasketLineItem line(String unitPrice, int quantity) {
        return new BasketLineItem(new BigDecimal(unitPrice), quantity);
    }

    @Test
    void sums_amount_and_quantity_in_one_pass() {
        Tuple<BigDecimal, Integer> result = totals(List.list(line("10.00", 2), line("5.50", 1)));

        assertEquals(new Tuple<>(new BigDecimal("25.50"), 3), result);
    }

    @Test
    void empty_basket_gives_zero_amount_and_zero_quantity() {
        Tuple<BigDecimal, Integer> result = totals(List.list());

        assertEquals(new Tuple<>(BigDecimal.ZERO, 0), result);
    }

    @Test
    void single_line_is_unit_price_times_quantity() {
        Tuple<BigDecimal, Integer> result = totals(List.list(line("7.25", 4)));

        assertEquals(new Tuple<>(new BigDecimal("29.00"), 4), result);
    }

    @Test
    void decimal_amounts_are_exact_not_floating_point() {
        Tuple<BigDecimal, Integer> result = totals(List.list(line("0.10", 3)));

        assertEquals(new Tuple<>(new BigDecimal("0.30"), 3), result);
    }

    @Test
    void many_small_lines_do_not_accumulate_rounding_error() {
        Tuple<BigDecimal, Integer> result = totals(List.list(
                line("0.10", 1), line("0.10", 1), line("0.10", 1), line("0.10", 1), line("0.10", 1),
                line("0.10", 1), line("0.10", 1), line("0.10", 1), line("0.10", 1), line("0.10", 1)));

        assertEquals(new Tuple<>(new BigDecimal("1.00"), 10), result);
    }

    @Test
    void line_with_zero_quantity_adds_nothing() {
        Tuple<BigDecimal, Integer> result = totals(List.list(line("99.99", 0), line("1.00", 2)));

        assertEquals(new Tuple<>(new BigDecimal("2.00"), 2), result);
    }

    @Test
    void line_order_does_not_change_the_totals() {
        Tuple<BigDecimal, Integer> forward = totals(List.list(line("10.00", 2), line("5.50", 1), line("3.25", 3)));
        Tuple<BigDecimal, Integer> backward = totals(List.list(line("3.25", 3), line("5.50", 1), line("10.00", 2)));

        assertEquals(forward, backward);
        assertEquals(new Tuple<>(new BigDecimal("35.25"), 6), forward);
    }

    @Test
    void different_scales_are_summed_by_value() {
        Tuple<BigDecimal, Integer> result = totals(List.list(
                new BasketLineItem(BigDecimal.valueOf(100), 2),
                new BasketLineItem(BigDecimal.valueOf(22.5), 3)));

        assertEquals(0, new BigDecimal("267.5").compareTo(result._1));
        assertEquals(5, result._2);
    }

    @Test
    void calling_totals_twice_gives_same_result_and_leaves_input_untouched() {
        List<BasketLineItem> basket = List.list(line("10.00", 2), line("5.50", 1));

        assertEquals(totals(basket), totals(basket));
        assertEquals(2, basket.length());
    }
}
