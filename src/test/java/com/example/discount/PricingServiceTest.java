package com.example.discount;

import com.example.discount.application.api.PricingRequest;
import com.example.discount.application.api.PricingResponse;
import com.example.discount.application.config.CampaignProperties;
import com.example.discount.application.service.PricingService;
import com.example.fpcore.Result;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @Test
    void quote_http_maps_success_to_ok_response() {
        PricingService service = new PricingService();
        ResponseEntity<?> response = service.quoteHttp(new PricingRequest(
                new BigDecimal("1200.00"),
                true,
                true
        ));
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof PricingResponse);
    }

    @Test
    void quote_http_maps_validation_failure_to_bad_request() {
        PricingService service = new PricingService();
        ResponseEntity<?> response = service.quoteHttp(new PricingRequest(null, null, null));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof com.example.discount.application.api.ErrorResponse);
    }

    @Test
    void quote_rejects_non_positive_subtotal_and_still_reports_other_fields() {
        PricingService service = new PricingService();
        Result<PricingResult> result = service.quote(new PricingRequest(new BigDecimal("-1"), true, null));

        assertTrue(result.isFailure());
        assertEquals("subtotal must be greater than zero; hasCoupon is required",
                result.failureCause().getMessage());
    }

    @Test
    void campaign_tiers_come_from_configuration() {
        CampaignProperties highThreshold = new CampaignProperties(
                new BigDecimal("2000.00"), new BigDecimal("0.02"), new BigDecimal("0.01"), new BigDecimal("0.10"));
        PricingService service = new PricingService(highThreshold);

        Result<PricingResult> result = service.quote(new PricingRequest(new BigDecimal("1200.00"), false, false));

        assertFalse(result.isSuccess() && result.getOrThrow().steps().stream()
                .anyMatch(step -> step.ruleName().startsWith("OVER_")));
    }

    @Test
    void invalid_configuration_fails_fast_with_all_errors() {
        CampaignProperties bad = new CampaignProperties(
                BigDecimal.ZERO, new BigDecimal("0.02"), new BigDecimal("0.01"), new BigDecimal("0.01"));

        IllegalStateException e = assertThrows(IllegalStateException.class, () -> new PricingService(bad));
        assertTrue(e.getMessage().contains("pricing.campaign.tier-step: 0"));
        assertTrue(e.getMessage().contains("pricing.campaign.max-rate: 0.01"));
    }
}
