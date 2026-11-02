package com.example.discount.application.config;

import com.example.fpcore.Assertion;
import com.example.fpcore.Validation;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.math.BigDecimal;

/**
 * Dynamic campaign tiers, read from "pricing.campaign.*".
 * Tier n: threshold = n * tierStep, rate = startRate + (n - 1) * rateStep, while rate <= maxRate.
 */
@ConfigurationProperties(prefix = "pricing.campaign")
public record CampaignProperties(
        @DefaultValue(DEFAULT_TIER_STEP) BigDecimal tierStep,
        @DefaultValue(DEFAULT_START_RATE) BigDecimal startRate,
        @DefaultValue(DEFAULT_RATE_STEP) BigDecimal rateStep,
        @DefaultValue(DEFAULT_MAX_RATE) BigDecimal maxRate
) {

    static final String PREFIX = "pricing.campaign.";
    static final String DEFAULT_TIER_STEP = "500.00";
    static final String DEFAULT_START_RATE = "0.02";
    static final String DEFAULT_RATE_STEP = "0.01";
    static final String DEFAULT_MAX_RATE = "0.10";

    public static CampaignProperties defaults() {
        return new CampaignProperties(
                new BigDecimal(DEFAULT_TIER_STEP),
                new BigDecimal(DEFAULT_START_RATE),
                new BigDecimal(DEFAULT_RATE_STEP),
                new BigDecimal(DEFAULT_MAX_RATE)
        );
    }

    /**
     * Checks every property and reports all errors at once, each naming the property and its value.
     */
    public Validation<CampaignProperties> validate() {
        return Validation.valid(this)
                .and(positive("tier-step", tierStep))
                .and(positive("start-rate", startRate))
                .and(positive("rate-step", rateStep))
                .and(Validation.fromResult(Assertion.assertNotNull(maxRate, missing("max-rate"))
                        .flatMap(max -> Assertion.assertCondition(max,
                                m -> m.compareTo(BigDecimal.ONE) < 0
                                        && (startRate == null || m.compareTo(startRate) >= 0),
                                invalid("max-rate", max, "must be >= start-rate and < 1")))));
    }

    private static Validation<BigDecimal> positive(String name, BigDecimal value) {
        return Validation.fromResult(Assertion.assertNotNull(value, missing(name))
                .flatMap(v -> Assertion.assertCondition(v, x -> x.signum() > 0,
                        invalid(name, v, "must be greater than zero"))));
    }

    private static String missing(String name) {
        return String.format("Property \"%s%s\" not found", PREFIX, name);
    }

    private static String invalid(String name, BigDecimal value, String reason) {
        return String.format("Invalid value for property %s%s: %s (%s)", PREFIX, name, value, reason);
    }
}
