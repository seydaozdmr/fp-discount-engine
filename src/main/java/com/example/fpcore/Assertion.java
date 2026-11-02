package com.example.fpcore;

import java.math.BigDecimal;
import java.util.function.Predicate;

/**
 * Functional assertions (Saumont, Listing 15.1): instead of throwing,
 * each assertion returns Success(value) or Failure(message).
 */
public final class Assertion {

    private Assertion() {
    }

    public static <T> Result<T> assertCondition(T value, Predicate<T> f, String message) {
        try {
            return f.test(value)
                    ? Result.success(value)
                    : Result.failure(message);
        } catch (Exception e) {
            return Result.failure(new IllegalStateException(message, e));
        }
    }

    public static <T> Result<T> assertNotNull(T value, String message) {
        return value != null ? Result.success(value) : Result.failure(message);
    }

    public static Result<Boolean> assertTrue(boolean condition, String message) {
        return assertCondition(condition, x -> x, message);
    }

    public static Result<Boolean> assertFalse(boolean condition, String message) {
        return assertCondition(condition, x -> !x, message);
    }

    public static Result<Integer> assertPositive(int value, String message) {
        return assertCondition(value, x -> x > 0, message);
    }

    public static Result<Integer> assertPositiveOrZero(int value, String message) {
        return assertCondition(value, x -> x >= 0, message);
    }

    public static Result<Integer> assertInRange(int value, int min, int max) {
        return assertCondition(value, x -> x >= min && x < max,
                String.format("value %s should be between %s and %s (exclusive)", value, min, max));
    }

    public static Result<BigDecimal> assertPositive(BigDecimal value, String message) {
        return assertNotNull(value, message)
                .flatMap(v -> assertCondition(v, x -> x.signum() > 0, message));
    }
}
