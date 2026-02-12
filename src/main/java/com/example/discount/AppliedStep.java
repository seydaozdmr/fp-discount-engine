package com.example.discount;

import java.math.BigDecimal;

public record AppliedStep(
        String ruleName,
        DiscountGroup group,
        BigDecimal requestedDiscount,
        BigDecimal appliedDiscount,
        BigDecimal totalBefore,
        BigDecimal totalAfter,
        String note
) {}
