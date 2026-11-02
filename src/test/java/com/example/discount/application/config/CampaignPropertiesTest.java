package com.example.discount.application.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CampaignPropertiesTest {

    private static CampaignProperties bind(Map<String, String> props) {
        return new Binder(new MapConfigurationPropertySource(props))
                .bindOrCreate("pricing.campaign", CampaignProperties.class);
    }

    @Test
    void missing_properties_fall_back_to_defaults() {
        assertEquals(CampaignProperties.defaults(), bind(Map.of()));
    }

    @Test
    void binds_kebab_case_properties() {
        CampaignProperties p = bind(Map.of(
                "pricing.campaign.tier-step", "250",
                "pricing.campaign.max-rate", "0.05"));
        assertEquals(new BigDecimal("250"), p.tierStep());
        assertEquals(new BigDecimal("0.05"), p.maxRate());
        assertTrue(p.validate().isValid());
    }

    @Test
    void validate_reports_every_bad_property_with_name_and_value() {
        CampaignProperties p = new CampaignProperties(
                new BigDecimal("-500"), new BigDecimal("0.02"), null, new BigDecimal("1.5"));
        assertEquals(List.of(
                "Invalid value for property pricing.campaign.tier-step: -500 (must be greater than zero)",
                "Property \"pricing.campaign.rate-step\" not found",
                "Invalid value for property pricing.campaign.max-rate: 1.5 (must be >= start-rate and < 1)"
        ), p.validate().errors());
    }
}
