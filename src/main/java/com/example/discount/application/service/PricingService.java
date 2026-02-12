package com.example.discount.application.service;

import com.example.discount.DiscountGroup;
import com.example.discount.DiscountOrchestratorV2;
import com.example.discount.DiscountRule;
import com.example.discount.OrderContext;
import com.example.discount.OrderPricing;
import com.example.discount.PricingResult;
import com.example.discount.application.api.PricingRequest;
import com.example.fpcore.Result;
import com.example.fpcore.Validation;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class PricingService {

    private static final BigDecimal THOUSAND = new BigDecimal("1000.00");
    private final DiscountOrchestratorV2 orchestrator = new DiscountOrchestratorV2();

    public Result<PricingResult> quote(PricingRequest request) {
        return validateRequest(request)
                .toResult()
                .flatMap(ctx -> orchestrator.priceValidated(ctx, defaultRules()));
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

    private List<DiscountRule> defaultRules() {
        return List.of(
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
                ),
                new DiscountRule(
                        "OVER_1000_5_PERCENT",
                        DiscountGroup.CAMPAIGN,
                        10,
                        c -> c.pricing().total().compareTo(THOUSAND) > 0,
                        c -> c.pricing().total().multiply(new BigDecimal("0.05"))
                )
        );
    }
}
