package com.example.discount;

import java.util.List;

public record PricingResult(
        OrderPricing pricing,
        List<AppliedStep> steps
) {}
