package com.example.exercises;

import com.example.exercises.SortKey.Item;
import com.example.exercises.SortKey.SortKeyEnum;
import com.example.fpcore.List;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Comparator;

import static com.example.exercises.SortKey.comparatorFor;
import static com.example.exercises.SortKey.comparatorOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SortKeyTest {

    private static final Item A = new Item("A", true, new BigDecimal("50"), 4.0);
    private static final Item B = new Item("B", true, new BigDecimal("30"), 3.0);
    private static final Item C = new Item("C", false, new BigDecimal("10"), 5.0);
    private static final Item D = new Item("D", true, new BigDecimal("30"), 4.5);
    private static final Item E = new Item("E", false, new BigDecimal("20"), 2.0);

    private static java.util.List<Item> items() {
        return java.util.List.of(A, B, C, D, E);
    }

    private static String sortedNames(Comparator<Item> comparator) {
        return items().stream()
                .sorted(comparator)
                .map(Item::name)
                .reduce("", String::concat);
    }

    @Test
    void in_stock_comes_first() {
        assertEquals("ABDCE", sortedNames(comparatorOf(SortKeyEnum.IN_STOCK)));
    }

    @Test
    void price_ascending_orders_cheapest_first() {
        assertEquals("CEBDA", sortedNames(comparatorOf(SortKeyEnum.PRICE_ASC)));
    }

    @Test
    void rating_descending_orders_highest_rated_first() {
        assertEquals("CDABE", sortedNames(comparatorOf(SortKeyEnum.RATING_DESC)));
    }

    @Test
    void in_stock_then_price_ascending() {
        Comparator<Item> comparator = comparatorFor(List.list(SortKeyEnum.IN_STOCK, SortKeyEnum.PRICE_ASC));

        assertEquals("BDACE", sortedNames(comparator));
    }

    @Test
    void ties_on_earlier_keys_are_broken_by_later_keys() {
        Comparator<Item> comparator = comparatorFor(
                List.list(SortKeyEnum.IN_STOCK, SortKeyEnum.PRICE_ASC, SortKeyEnum.RATING_DESC));

        assertEquals("DBACE", sortedNames(comparator));
    }

    @Test
    void key_order_matters() {
        Comparator<Item> stockFirst = comparatorFor(List.list(SortKeyEnum.IN_STOCK, SortKeyEnum.PRICE_ASC));
        Comparator<Item> priceFirst = comparatorFor(List.list(SortKeyEnum.PRICE_ASC, SortKeyEnum.IN_STOCK));

        assertEquals("BDACE", sortedNames(stockFirst));
        assertEquals("CEBDA", sortedNames(priceFirst));
    }

    @Test
    void empty_key_list_leaves_order_unchanged() {
        Comparator<Item> comparator = comparatorFor(List.list());

        assertEquals("ABCDE", sortedNames(comparator));
    }

    @Test
    void empty_key_list_treats_every_pair_as_equal() {
        Comparator<Item> comparator = comparatorFor(List.list());

        assertEquals(0, comparator.compare(A, C));
        assertEquals(0, comparator.compare(C, A));
        assertEquals(0, comparator.compare(B, B));
    }

    @Test
    void single_key_behaves_like_its_own_comparator() {
        Comparator<Item> folded = comparatorFor(List.list(SortKeyEnum.RATING_DESC));
        Comparator<Item> direct = comparatorOf(SortKeyEnum.RATING_DESC);

        for (Item left : items()) {
            for (Item right : items()) {
                assertEquals(Integer.signum(direct.compare(left, right)),
                        Integer.signum(folded.compare(left, right)));
            }
        }
    }

    @Test
    void rating_descending_puts_higher_rating_before_lower() {
        assertTrue(comparatorOf(SortKeyEnum.RATING_DESC).compare(C, E) < 0);
        assertTrue(comparatorOf(SortKeyEnum.RATING_DESC).compare(E, C) > 0);
    }

    @Test
    void in_stock_comparator_puts_available_item_before_unavailable() {
        assertTrue(comparatorOf(SortKeyEnum.IN_STOCK).compare(A, C) < 0);
        assertTrue(comparatorOf(SortKeyEnum.IN_STOCK).compare(C, A) > 0);
        assertEquals(0, comparatorOf(SortKeyEnum.IN_STOCK).compare(A, B));
    }
}
