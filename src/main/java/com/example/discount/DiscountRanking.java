package com.example.discount;

import java.util.Comparator;

/**
 * Single source of truth for "which discount candidate is better".
 * Order: higher amount first, then lower priority, then rule name so that
 * full ties resolve deterministically instead of depending on input order.
 */
public final class DiscountRanking {

    private DiscountRanking() {}

    public static final Comparator<SelectedDiscount> BEST_FIRST =
            Comparator.comparing(SelectedDiscount::amount, Comparator.reverseOrder())
                    .thenComparingInt(SelectedDiscount::priority)
                    .thenComparing(SelectedDiscount::ruleName);
}
