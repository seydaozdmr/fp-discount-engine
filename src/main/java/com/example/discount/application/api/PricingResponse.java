package com.example.discount.application.api;

import com.example.discount.PricingResult;

import java.math.BigDecimal;
import java.util.List;

public record PricingResponse(
        BigDecimal subtotal,
        BigDecimal discountTotal,
        BigDecimal total,
        List<PricingStepResponse> steps
) {
    public static PricingResponse from(PricingResult pricingResult) {
        return new PricingResponse(
                pricingResult.pricing().subtotal(),
                pricingResult.pricing().discountTotal(),
                pricingResult.pricing().total(),
                pricingResult.steps().stream().map(PricingStepResponse::from).toList()
        );
    }
}
