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
import com.example.fpcore.LazyStream;
import com.example.fpcore.Result;
import com.example.fpcore.Validation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class PricingService {

    private static final BigDecimal FIVE_HUNDRED = new BigDecimal("500.00");
    private static final BigDecimal ONE_PERCENT = new BigDecimal("0.01");
    private static final BigDecimal TWO_PERCENT = new BigDecimal("0.02");
    private static final BigDecimal MAX_DYNAMIC_RATE = new BigDecimal("0.10");
    private final DiscountOrchestratorV2 orchestrator = new DiscountOrchestratorV2();

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
        List<String> errors = new ArrayList<>();
        if (request == null) {
            errors.add("Request body is required");
            return Validation.invalid(errors);
        }

        if (request.subtotal() == null) {
            errors.add("subtotal is required");
        } else if (request.subtotal().signum() <= 0) {
            errors.add("subtotal must be greater than zero");
        }

        if (request.vip() == null) {
            errors.add("vip is required");
        }

        if (request.hasCoupon() == null) {
            errors.add("hasCoupon is required");
        }

        if (!errors.isEmpty()) {
            return Validation.invalid(errors);
        }

        return Validation.valid(new OrderContext(
                request.vip(),
                request.hasCoupon(),
                OrderPricing.of(request.subtotal())
        ));
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
                .iterate(new CampaignTier(FIVE_HUNDRED, TWO_PERCENT), this::nextTier)
                .takeWhile(tier -> tier.threshold().compareTo(ctx.pricing().subtotal()) <= 0
                        && tier.rate().compareTo(MAX_DYNAMIC_RATE) <= 0)
                .map(this::toCampaignRule);

        return baseRules.append(() -> dynamicCampaignRules).toList();
    }

    private CampaignTier nextTier(CampaignTier current) {
        return new CampaignTier(
                current.threshold().add(FIVE_HUNDRED),
                current.rate().add(ONE_PERCENT)
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
