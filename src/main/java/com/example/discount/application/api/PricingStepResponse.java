package com.example.discount.application.api;

import com.example.discount.AppliedStep;

import java.math.BigDecimal;

public record PricingStepResponse(
        String ruleName,
        String group,
        BigDecimal requestedDiscount,
        BigDecimal appliedDiscount,
        BigDecimal totalBefore,
        BigDecimal totalAfter,
        String note
) {
    public static PricingStepResponse from(AppliedStep step) {
        return new PricingStepResponse(
                step.ruleName(),
                step.group().name(),
                step.requestedDiscount(),
                step.appliedDiscount(),
                step.totalBefore(),
                step.totalAfter(),
                step.note()
        );
    }
}
