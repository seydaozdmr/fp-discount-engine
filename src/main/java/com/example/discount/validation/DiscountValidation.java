package com.example.discount.validation;

import com.example.discount.DiscountRule;
import com.example.discount.OrderContext;
import com.example.fpcore.Validation;

import java.util.ArrayList;
import java.util.List;

public final class DiscountValidation {

    private DiscountValidation() {
    }

    public record PricingCommand(OrderContext context, List<DiscountRule> rules) {
    }

    public static Validation<PricingCommand> validate(OrderContext context, List<DiscountRule> rules) {
        return Validation.map2(
                validateContext(context),
                validateRules(rules),
                (ctx, rs) -> new PricingCommand(ctx, List.copyOf(rs))
        );
    }

    private static Validation<OrderContext> validateContext(OrderContext context) {
        List<String> errors = new ArrayList<>();
        if (context == null) {
            errors.add("OrderContext is required");
            return Validation.invalid(errors);
        }
        if (context.pricing() == null) {
            errors.add("Pricing is required");
        } else {
            if (context.pricing().subtotal().signum() < 0) {
                errors.add("Subtotal cannot be negative");
            }
            if (context.pricing().discountTotal().signum() < 0) {
                errors.add("Discount total cannot be negative");
            }
        }
        return errors.isEmpty() ? Validation.valid(context) : Validation.invalid(errors);
    }

    private static Validation<List<DiscountRule>> validateRules(List<DiscountRule> rules) {
        List<String> errors = new ArrayList<>();
        if (rules == null || rules.isEmpty()) {
            errors.add("At least one discount rule is required");
            return Validation.invalid(errors);
        }

        for (int i = 0; i < rules.size(); i++) {
            DiscountRule rule = rules.get(i);
            if (rule == null) {
                errors.add("Rule at index " + i + " is null");
                continue;
            }
            if (rule.name() == null || rule.name().isBlank()) {
                errors.add("Rule at index " + i + " must have a name");
            }
            if (rule.group() == null) {
                errors.add("Rule '" + safeName(rule) + "' must have a group");
            }
            if (rule.eligible() == null) {
                errors.add("Rule '" + safeName(rule) + "' must have an eligibility predicate");
            }
            if (rule.calculate() == null) {
                errors.add("Rule '" + safeName(rule) + "' must have a calculation function");
            }
        }

        return errors.isEmpty() ? Validation.valid(rules) : Validation.invalid(errors);
    }

    private static String safeName(DiscountRule rule) {
        return rule.name() == null ? "<unnamed>" : rule.name();
    }
}
