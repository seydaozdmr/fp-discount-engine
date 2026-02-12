package com.example.discount;

import com.example.discount.application.api.PricingRequest;
import com.example.discount.application.service.PricingService;
import com.example.fpcore.Result;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PricingServiceTest {

    @Test
    void quote_uses_dynamic_campaign_rules_and_returns_success() {
        PricingService service = new PricingService();

        Result<PricingResult> result = service.quote(new PricingRequest(
                new BigDecimal("1200.00"),
                true,
                true
        ));

        assertTrue(result.isSuccess());
        assertTrue(result.getOrThrow().steps().stream()
                .anyMatch(step -> step.ruleName().startsWith("OVER_")));
    }

    @Test
    void quote_request_validation_accumulates_errors() {
        PricingService service = new PricingService();
        Result<PricingResult> result = service.quote(new PricingRequest(null, null, null));

        assertTrue(result.isFailure());
        String message = result.failureCause().getMessage();
        assertTrue(message.contains("subtotal is required"));
        assertTrue(message.contains("vip is required"));
        assertTrue(message.contains("hasCoupon is required"));
    }
}
