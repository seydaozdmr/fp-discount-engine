package com.example.fpcore;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LazyStreamTest {

    @Test
    void lazy_memoizes_supplier_once() {
        AtomicInteger calls = new AtomicInteger(0);
        Lazy<Integer> lazy = Lazy.of(() -> {
            calls.incrementAndGet();
            return 42;
        });

        assertEquals(42, lazy.get());
        assertEquals(42, lazy.get());
        assertEquals(1, calls.get());
        assertTrue(lazy.isEvaluated());
    }

    @Test
    void take_from_infinite_stream_is_safe() {
        List<Integer> firstFive = LazyStream.from(1).take(5).toList();
        assertEquals(List.of(1, 2, 3, 4, 5), firstFive);
    }

    @Test
    void map_filter_chain_evaluates_only_until_needed() {
        AtomicInteger mappedCount = new AtomicInteger(0);

        LazyStream<Integer> stream = LazyStream.from(1)
                .map(n -> {
                    mappedCount.incrementAndGet();
                    return n * 3;
                })
                .filter(n -> n % 2 == 0);

        Option<Integer> firstEven = stream.headOption();

        assertEquals(6, firstEven.getOrThrow());
        assertEquals(2, mappedCount.get());
    }
}
