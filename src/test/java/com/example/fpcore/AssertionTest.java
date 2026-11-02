package com.example.fpcore;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AssertionTest {

    @Test
    void assertCondition_returns_success_or_failure_with_message() {
        assertEquals(5, Assertion.assertPositive(5, "neg").getOrThrow());
        Result<Integer> failed = Assertion.assertPositive(-1, "must be positive");
        assertTrue(failed.isFailure());
        assertEquals("must be positive", failed.failureCause().getMessage());
    }

    @Test
    void predicate_exception_becomes_failure_instead_of_throwing() {
        Result<String> r = Assertion.assertCondition(null, s -> s.isEmpty(), "bad string");
        assertTrue(r.isFailure());
        assertEquals("bad string", r.failureCause().getMessage());
    }

    @Test
    void assertPositive_bigdecimal_handles_null_zero_and_positive() {
        assertTrue(Assertion.assertPositive((BigDecimal) null, "x").isFailure());
        assertTrue(Assertion.assertPositive(BigDecimal.ZERO, "x").isFailure());
        assertTrue(Assertion.assertPositive(BigDecimal.TEN, "x").isSuccess());
    }

    @Test
    void validation_fromResult_and_and_accumulate_errors() {
        Validation<String> v = Validation.valid("ok")
                .and(Validation.fromResult(Assertion.assertTrue(false, "first")))
                .and(Validation.fromResult(Assertion.assertTrue(true, "never")))
                .and(Validation.fromResult(Result.empty(), "second"));
        assertEquals(List.of("first", "second"), v.errors());
    }
}
