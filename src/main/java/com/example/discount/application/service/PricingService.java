package com.example.discount.application.service;

import com.example.discount.DiscountGroup;
import com.example.discount.DiscountOrchestratorV2;
import com.example.discount.DiscountRule;
import com.example.discount.OrderContext;
import com.example.discount.OrderPricing;
import com.example.discount.PricingResult;
import com.example.discount.application.api.ErrorResponse;
import com.example.discount.application.api.PricingRequest;
import com.example.discount.application.api.PricingResponse;
import com.example.discount.application.config.CampaignProperties;
import com.example.fpcore.Assertion;
import com.example.fpcore.LazyStream;
import com.example.fpcore.Result;
import com.example.fpcore.Validation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PricingService {

    private final DiscountOrchestratorV2 orchestrator = new DiscountOrchestratorV2();
    private final CampaignProperties campaign;

    public PricingService() {
        this(CampaignProperties.defaults());
    }

    /**
     * Invalid configuration is an edge effect: fail at startup, listing every bad property.
     */
    @Autowired
    public PricingService(CampaignProperties campaign) {
        this.campaign = campaign.validate().getOrThrow();
    }

    public Result<PricingResult> quote(PricingRequest request) {
        return validateRequest(request)
                .toResult()
                .flatMap(ctx -> orchestrator.priceValidated(ctx, defaultRules(ctx)));
    }

    public ResponseEntity<?> quoteHttp(PricingRequest request) {
        return toHttpResponse(quote(request));
    }

    private ResponseEntity<?> toHttpResponse(Result<PricingResult> result) {
        if (result.isSuccess()) {
            return ResponseEntity.ok(PricingResponse.from(result.getOrThrow()));
        }
        if (result.isFailure()) {
            return ResponseEntity.badRequest().body(new ErrorResponse(result.failureCause().getMessage()));
        }
        if (result.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(new ErrorResponse("no price could be calculated"));
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("Unexpected pricing result state"));
    }

    private Validation<OrderContext> validateRequest(PricingRequest request) {
        if (request == null) {
            return Validation.invalid("Request body is required");
        }
        // Result inside a field (stop at first problem), Validation across fields (collect all)
        return Validation.map3(
                Validation.fromResult(Assertion.assertNotNull(request.subtotal(), "subtotal is required")
                        .flatMap(s -> Assertion.assertPositive(s, "subtotal must be greater than zero"))),
                Validation.fromResult(Assertion.assertNotNull(request.vip(), "vip is required")),
                Validation.fromResult(Assertion.assertNotNull(request.hasCoupon(), "hasCoupon is required")),
                (subtotal, vip, hasCoupon) -> new OrderContext(vip, hasCoupon, OrderPricing.of(subtotal))
        );
    }

    private List<DiscountRule> defaultRules(OrderContext ctx) {
        LazyStream<DiscountRule> baseRules = LazyStream.of(
                new DiscountRule(
                        "VIP_10_PERCENT",
                        DiscountGroup.VIP,
                        10,
                        OrderContext::vip,
                        c -> c.pricing().total().multiply(new BigDecimal("0.10"))
                ),
                new DiscountRule(
                        "COUPON_50_TRY",
                        DiscountGroup.COUPON,
                        5,
                        OrderContext::hasCoupon,
                        c -> new BigDecimal("50.00")
                )
        );

        LazyStream<DiscountRule> dynamicCampaignRules = LazyStream
                .iterate(new CampaignTier(campaign.tierStep(), campaign.startRate()), this::nextTier)
                .takeWhile(tier -> tier.threshold().compareTo(ctx.pricing().subtotal()) <= 0
                        && tier.rate().compareTo(campaign.maxRate()) <= 0)
                .map(this::toCampaignRule);

        return baseRules.append(() -> dynamicCampaignRules).toList();
    }

    private CampaignTier nextTier(CampaignTier current) {
        return new CampaignTier(
                current.threshold().add(campaign.tierStep()),
                current.rate().add(campaign.rateStep())
        );
    }

    private DiscountRule toCampaignRule(CampaignTier tier) {
        String name = "OVER_" + tier.threshold().intValue() + "_DYNAMIC";
        int priority = 100 - tier.rate().movePointRight(2).intValue();
        return new DiscountRule(
                name,
                DiscountGroup.CAMPAIGN,
                priority,
                c -> c.pricing().total().compareTo(tier.threshold()) >= 0,
                c -> c.pricing().total().multiply(tier.rate())
        );
    }

    private record CampaignTier(BigDecimal threshold, BigDecimal rate) {
    }
}
