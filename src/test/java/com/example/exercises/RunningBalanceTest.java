package com.example.exercises;

import com.example.fpcore.List;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.function.Function;

import static com.example.exercises.RunningBalance.runningBalance;
import static com.example.exercises.RunningBalance.scanLeft;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RunningBalanceTest {

    private static <A> java.util.List<A> toJava(List<A> list) {
        return list.foldLeft(new ArrayList<A>(), acc -> a -> {
            ArrayList<A> copy = new ArrayList<>(acc);
            copy.add(a);
            return copy;
        });
    }

    private static List<Integer> ints(int... values) {
        Integer[] boxed = new Integer[values.length];
        for (int i = 0; i < values.length; i++) {
            boxed[i] = values[i];
        }
        return List.list(boxed);
    }

    @Test
    void runningBalanceMatchesStatementExample() {
        assertEquals(java.util.List.of(100, 150, 120, -80), toJava(runningBalance(100, ints(50, -30, -200))));
    }

    @Test
    void emptyTransactionsGiveOnlyOpeningBalance() {
        assertEquals(java.util.List.of(100), toJava(runningBalance(100, List.list())));
    }

    @Test
    void singleTransaction() {
        assertEquals(java.util.List.of(0, 25), toJava(runningBalance(0, ints(25))));
    }

    @Test
    void doesNotModifyInput() {
        List<Integer> transactions = ints(50, -30, -200);
        java.util.List<Integer> before = toJava(transactions);
        runningBalance(100, transactions);
        assertEquals(before, toJava(transactions));
    }

    @Test
    void resultIsOneLongerThanInput() {
        List<Integer> transactions = ints(1, 2, 3, 4, 5);
        assertEquals(transactions.length() + 1, runningBalance(0, transactions).length());
    }

    @Test
    void firstElementIsZeroValue() {
        assertEquals(42, runningBalance(42, ints(1, 2, 3)).head());
    }

    @Test
    void lastElementEqualsFoldLeftResult() {
        List<Integer> transactions = ints(50, -30, -200, 15, 7);
        Function<Integer, Function<Integer, Integer>> add = b -> tx -> b + tx;

        Integer folded = transactions.foldLeft(100, add);
        Integer lastOfScan = runningBalance(100, transactions).reverse().head();

        assertEquals(folded, lastOfScan);
    }

    @Test
    void worksWithDifferentTypes() {
        Function<String, Function<Integer, String>> append = acc -> n -> acc + n;
        assertEquals(java.util.List.of("", "1", "12", "123"), toJava(scanLeft(ints(1, 2, 3), "", append)));
    }

    @Test
    void stepFunctionSeesPreviousValueNotInitial() {
        Function<Integer, Function<Integer, Integer>> product = acc -> n -> acc * n;
        assertEquals(java.util.List.of(1, 2, 6, 24), toJava(scanLeft(ints(2, 3, 4), 1, product)));
    }

    @Test
    void bigDecimalBalancesAreComparedWithCompareTo() {
        List<BigDecimal> transactions = List.list(new BigDecimal("10.50"), new BigDecimal("-0.25"));
        Function<BigDecimal, Function<BigDecimal, BigDecimal>> add = b -> tx -> b.add(tx);

        java.util.List<BigDecimal> result = toJava(scanLeft(transactions, new BigDecimal("100"), add));

        assertEquals(3, result.size());
        assertEquals(0, new BigDecimal("100").compareTo(result.get(0)));
        assertEquals(0, new BigDecimal("110.5").compareTo(result.get(1)));
        assertEquals(0, new BigDecimal("110.25").compareTo(result.get(2)));
    }

    @Test
    void tenThousandTransactionsDoNotOverflowTheStack() {
        int n = 10_000;
        Integer[] ones = new Integer[n];
        java.util.Arrays.fill(ones, 1);
        List<Integer> transactions = List.list(ones);

        List<Integer> result = assertTimeoutPreemptively(java.time.Duration.ofSeconds(10),
                () -> runningBalance(0, transactions));

        assertEquals(n + 1, result.length());
        assertEquals(0, result.head());
        assertEquals(n, result.reverse().head());
    }

    @Test
    void tenThousandTransactionsKeepRunningTotalsInOrder() {
        int n = 10_000;
        Integer[] ones = new Integer[n];
        java.util.Arrays.fill(ones, 1);

        java.util.List<Integer> result = toJava(runningBalance(0, List.list(ones)));

        for (int i = 0; i <= n; i++) {
            if (result.get(i) != i) {
                throw new AssertionError("index " + i + " expected " + i + " but was " + result.get(i));
            }
        }
        assertTrue(result.size() == n + 1);
    }
}
