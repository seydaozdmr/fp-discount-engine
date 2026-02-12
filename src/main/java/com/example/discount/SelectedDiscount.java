package com.example.discount;

import java.math.BigDecimal;

/**
 * Candidate chosen per group (before application policies like exclusivity/cap).
 */
public record SelectedDiscount(
        String ruleName,
        DiscountGroup group,
        int priority,
        BigDecimal amount
) {}
