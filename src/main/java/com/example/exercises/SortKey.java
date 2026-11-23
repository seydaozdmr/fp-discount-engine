package com.example.exercises;

import com.example.fpcore.List;

import java.math.BigDecimal;
import java.util.Comparator;

public class SortKey {

    public enum SortKeyEnum {
        IN_STOCK, PRICE_ASC, RATING_DESC
    }

    record Item(String name, boolean inStock, BigDecimal price, double rating) {}

    static Comparator<Item> comparatorOf(SortKeyEnum key) {
        return switch (key){
            case IN_STOCK -> Comparator.comparing(Item::inStock, Comparator.reverseOrder());
            case PRICE_ASC -> Comparator.comparing(Item::price);
            case RATING_DESC -> Comparator.comparingDouble(Item::rating).reversed();
        };
    }

    static Comparator<Item> comparatorFor(List<SortKeyEnum> keys){
        Comparator<Item> identity = (a,b) -> 0;
        return keys.foldLeft(identity, acc -> key -> acc.thenComparing(comparatorOf(key)));
    }







}
