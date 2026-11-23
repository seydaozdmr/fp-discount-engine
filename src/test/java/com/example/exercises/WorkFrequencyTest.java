package com.example.exercises;

import com.example.fpcore.List;
import com.example.fpcore.Tuple;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static com.example.exercises.WorkFrequency.allOf;
import static com.example.exercises.WorkFrequency.and;
import static com.example.exercises.WorkFrequency.frequencies;
import static com.example.exercises.WorkFrequency.keepAllOf;
import static com.example.exercises.WorkFrequency.keepWhere;
import static com.example.exercises.WorkFrequency.minLength;
import static com.example.exercises.WorkFrequency.minLengthF;
import static com.example.exercises.WorkFrequency.notBlank;
import static com.example.exercises.WorkFrequency.notStopWords;
import static com.example.exercises.WorkFrequency.removeStopWords;
import static com.example.exercises.WorkFrequency.removeStopWordsF;
import static com.example.exercises.WorkFrequency.top;
import static com.example.exercises.WorkFrequency.topWords;
import static com.example.exercises.WorkFrequency.words;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkFrequencyTest {

    private static final Set<String> STOP = Set.of("ve", "bir");

    private static <A> java.util.List<A> toJava(List<A> list) {
        return list.foldLeft(new ArrayList<A>(), acc -> a -> {
            ArrayList<A> copy = new ArrayList<>(acc);
            copy.add(a);
            return copy;
        });
    }

    private static <A> java.util.List<A> run(Function<List<A>, List<A>> step, List<A> input) {
        return toJava(step.apply(input));
    }

    private static Tuple<String, Integer> t(String word, int count) {
        return new Tuple<>(word, count);
    }

    @Nested
    class Steps {

        @Test
        void wordsLowercasesWithTurkishLocaleAndSplitsOnWhitespace() {
            assertEquals(java.util.List.of("ılık", "istanbul", "kargo"),
                    toJava(words.apply("ILIK   İstanbul\tKargo")));
            assertEquals(java.util.List.of("ılık"), toJava(words.apply("ILIK")));
        }

        @Test
        void wordsProducesLeadingEmptyTokenThatFiltersMustRemove() {
            assertEquals(java.util.List.of("", "kargo"), toJava(words.apply("  kargo")));
        }

        @Test
        void removeStopWordsDropsStopWordsAndBlanks() {
            assertEquals(java.util.List.of("kargo", "hızlı", "kargo"),
                    toJava(removeStopWords(STOP).apply(words.apply("  kargo ve hızlı bir kargo"))));
        }

        @Test
        void curriedRemoveStopWordsBehavesLikeMethodVersion() {
            List<String> input = words.apply("kargo ve hızlı bir kargo");
            assertEquals(toJava(removeStopWords(STOP).apply(input)),
                    toJava(removeStopWordsF.apply(STOP).apply(input)));
        }

        @Test
        void frequenciesCountsEachWord() {
            assertEquals(Map.of("a", 2, "b", 2, "c", 2), frequencies.apply(words.apply("b a c a b c")));
        }

        @Test
        void frequenciesOfEmptyListIsEmptyMap() {
            assertEquals(Map.of(), frequencies.apply(List.list()));
        }

        @Test
        void topOrdersByCountDescThenWordAsc() {
            Map<String, Integer> freq = Map.of("b", 2, "a", 2, "c", 3, "d", 1);
            assertEquals(java.util.List.of(t("c", 3), t("a", 2), t("b", 2)), toJava(top(3).apply(freq)));
        }

        @Test
        void topWithNLargerThanMapReturnsAll() {
            assertEquals(java.util.List.of(t("a", 1)), toJava(top(10).apply(Map.of("a", 1))));
        }

        @Test
        void topOfEmptyMapIsEmpty() {
            assertTrue(top(5).apply(Map.of()).isEmpty());
        }

        @Test
        void minLengthKeepsWordsExactlyAtBoundary() {
            List<String> input = List.list("ve", "iyi", "kargo");
            assertEquals(java.util.List.of("iyi", "kargo"), run(minLength.apply(3), input));
        }
    }

    @Nested
    class TopWordsExamples {

        @Test
        void mostFrequentSingleWord() {
            assertEquals(java.util.List.of(t("kargo", 2)), toJava(topWords("kargo hızlı kargo iyi", 1, Set.of())));
        }

        @Test
        void onlyStopWordsGivesEmptyResult() {
            assertTrue(topWords("ve ve ve", 5, Set.of("ve")).isEmpty());
        }

        @Test
        void equalFrequenciesAreAlphabetical() {
            assertEquals(java.util.List.of(t("a", 1), t("b", 1), t("c", 1)),
                    toJava(topWords("c b a", 3, Set.of())));
        }

        @Test
        void emptyTextGivesEmptyResult() {
            assertTrue(topWords("", 5, STOP).isEmpty());
        }
    }

    @Nested
    class Conditions {

        @Test
        void andIsTrueOnlyWhenBothAreTrue() {
            Function<String, Boolean> p = and(notBlank, minLengthF.apply(3));
            assertTrue(p.apply("iyi"));
            assertFalse(p.apply("ve"));
            assertFalse(p.apply(""));
        }

        @Test
        void andShortCircuitsWhenLeftIsFalse() {
            AtomicInteger calls = new AtomicInteger();
            Function<String, Boolean> counting = w -> {
                calls.incrementAndGet();
                return true;
            };

            and(notStopWords.apply(STOP), counting).apply("ve");

            assertEquals(0, calls.get());
        }

        @Test
        void allOfEmptyListAcceptsEverything() {
            Function<String, Boolean> none = allOf(List.list());
            assertTrue(none.apply(""));
            assertTrue(none.apply("ve"));
        }

        @Test
        void allOfSingleConditionBehavesLikeThatCondition() {
            Function<String, Boolean> min3 = minLengthF.apply(3);
            Function<String, Boolean> single = allOf(List.list(min3));
            for (String w : java.util.List.of("", "ve", "iyi", "kargo")) {
                assertEquals(min3.apply(w), single.apply(w), w);
            }
        }

        @Test
        @SuppressWarnings("unchecked")
        void allOfMatchesNestedAnd() {
            Function<String, Boolean> nested = and(notBlank, and(notStopWords.apply(STOP), minLengthF.apply(3)));
            Function<String, Boolean> folded = allOf(List.list(notBlank, notStopWords.apply(STOP), minLengthF.apply(3)));
            for (String w : java.util.List.of("", "ve", "bir", "iyi", "kargo", "ab")) {
                assertEquals(nested.apply(w), folded.apply(w), w);
            }
        }

        @Test
        void keepAllOfPipelineDropsStopWordsAndShortWords() {
            java.util.List<Tuple<String, Integer>> result = words
                    .andThen(keepWhere(keepAllOf))
                    .andThen(frequencies)
                    .andThen(top(3))
                    .andThen(WorkFrequencyTest::toJava)
                    .apply("kargo ve hızlı bir kargo antalya antalya yolcu ab");

            assertEquals(java.util.List.of(t("antalya", 2), t("kargo", 2), t("hızlı", 1)), result);
        }
    }

    @Nested
    class KeepWhereLaws {

        private final List<String> input = List.list("kargo", "ve", "", "iyi", "ab", "hızlı", "bir");
        private final Function<String, Boolean> p = notStopWords.apply(STOP);
        private final Function<String, Boolean> q = minLengthF.apply(3);

        @Test
        void preservesOrder() {
            assertEquals(java.util.List.of("kargo", "iyi", "hızlı"), run(keepWhere(and(p, q)), input));
        }

        @Test
        void doesNotModifyInput() {
            java.util.List<String> before = toJava(input);
            keepWhere(q).apply(input);
            assertEquals(before, toJava(input));
        }

        @Test
        void fusionLaw() {
            assertEquals(run(keepWhere(p).andThen(keepWhere(q)), input), run(keepWhere(and(p, q)), input));
        }

        @Test
        void alwaysTrueIsIdentity() {
            assertEquals(toJava(input), run(keepWhere(w -> true), input));
        }

        @Test
        void alwaysFalseGivesEmptyList() {
            assertTrue(keepWhere(w -> false).apply(input).isEmpty());
        }

        @Test
        void emptyInputStaysEmpty() {
            assertTrue(keepWhere(p).apply(List.list()).isEmpty());
        }

        @Test
        void partitionSplitsIntoMatchingAndRestKeepingOrder() {
            Tuple<List<String>, List<String>> split = input.partition(p);
            assertEquals(java.util.List.of("kargo", "", "iyi", "ab", "hızlı"), toJava(split._1));
            assertEquals(java.util.List.of("ve", "bir"), toJava(split._2));
        }

        @Test
        void partitionFirstIsFilterAndSecondIsFilterOfNegation() {
            Tuple<List<String>, List<String>> split = input.partition(q);
            assertEquals(toJava(input.filter(q)), toJava(split._1));
            assertEquals(toJava(input.filter(w -> !q.apply(w))), toJava(split._2));
        }

        @Test
        void partitionLosesNothing() {
            Tuple<List<String>, List<String>> split = input.partition(p);
            assertEquals(input.length(), split._1.length() + split._2.length());
        }

        @Test
        void partitionEdgeCases() {
            Tuple<List<String>, List<String>> empty = List.<String>list().partition(p);
            assertTrue(empty._1.isEmpty());
            assertTrue(empty._2.isEmpty());

            Tuple<List<String>, List<String>> all = input.partition(w -> true);
            assertEquals(toJava(input), toJava(all._1));
            assertTrue(all._2.isEmpty());

            Tuple<List<String>, List<String>> none = input.partition(w -> false);
            assertTrue(none._1.isEmpty());
            assertEquals(toJava(input), toJava(none._2));
        }

        @Test
        void partitionEvaluatesConditionOncePerElement() {
            AtomicInteger calls = new AtomicInteger();
            input.partition(w -> {
                calls.incrementAndGet();
                return w.length() >= 3;
            });
            assertEquals(input.length(), calls.get());
        }

        @Test
        void equivalentToDedicatedSteps() {
            assertEquals(run(removeStopWordsF.apply(STOP), input), run(keepWhere(and(notBlank, p)), input));
            assertEquals(run(minLength.apply(3), input), run(keepWhere(q), input));
        }
    }
}
