package com.example.exercises;

import com.example.fpcore.List;
import com.example.fpcore.Tuple;

import java.util.*;
import java.util.function.Function;

public class WorkFrequency {

    static final Set<String> stop = Set.of("ve", "bir");


    public static final Function<String, List<String>> words = word -> List.list(word.toLowerCase(new Locale("tr")).split("\\s+"));

    public static Function<List<String>, List<String>> removeStopWords(Set<String> stopWords) {
        return ws -> ws.filter(w -> !w.isBlank() && !stopWords.contains(w));
    }

    public static final Function<Set<String>, Function<List<String>,List<String>>> removeStopWordsF = stop -> ws -> ws.filter(w -> !w.isBlank() && !stop.contains(w));

    public static final Function<List<String>, Map<String,Integer>> frequencies = ws -> ws.foldLeft(Map.<String,Integer>of(),
            acc -> w -> {
                Map<String, Integer> copy = new HashMap<>(acc);
                copy.merge(w,1,Integer::sum);
                //Her adımda copy döner,
                return copy;
            });

    public static Function<Map<String, Integer>, List<Tuple<String, Integer>>> top(int n) {
        return freq -> {
            @SuppressWarnings("unchecked")
            Tuple<String, Integer>[] arr = freq.entrySet().stream()
                    .map(e -> new Tuple<>(e.getKey(), e.getValue()))
                    .sorted(byCountThenWord)
                    .limit(n)
                    .toArray(Tuple[]::new);
            return List.list(arr);
        };
    }

    public static final Function<Integer, Function<List<String>, List<String>>> minLength = k -> ws -> ws.filter(w-> w.length() >= k);

    public static final Comparator<Tuple<String, Integer>> byCountThenWord =   Comparator.comparing((Tuple<String, Integer> t) -> t._2, Comparator.reverseOrder())
            .thenComparing(t -> t._1);

    public static List<Tuple<String,Integer>> topWords(String text, int n, Set<String> stopWords) {
        return words.andThen(removeStopWordsF.apply(stopWords)).andThen(frequencies).andThen(top(n)).apply(text);
    }

    public static final Function<String,Boolean> notBlank = w-> !w.isBlank();

    public static final Function<Set<String>,Function<String, Boolean>> notStopWords = stop -> w -> !stop.contains(w);

    public static final Function<Integer, Function<String, Boolean>> minLengthF = k -> w -> w.length() >= k;

    static <A> Function<A, Boolean> and(Function<A, Boolean> p, Function<A, Boolean> q) {
        return a -> p.apply(a) && q.apply(a);
    }

    static Function<List<String>, List<String>> keepWhere(Function<String, Boolean> p) {
        return ws -> ws.filter(p);
    }

    public static <A> Function<A, Boolean> allOf (List<Function<A, Boolean>> conditions){
        Function<A, Boolean> alwaysTrue = a -> true;
        return conditions.foldLeft(alwaysTrue, acc -> c -> and (acc,c));
    }

    @SuppressWarnings("unchecked")
    static Function<String, Boolean> keepAllOf =
            allOf(List.list(notBlank, notStopWords.apply(stop), minLengthF.apply(3)));

    Function<String, Boolean> keep = and(notBlank, and(notStopWords.apply(stop), minLengthF.apply(3)));



    public static void main(String[] args) {
        Set<String> stop = Set.of("ve", "bir");
        System.out.println(removeStopWords(stop).apply(words.apply("kargo ve hızlı bir kargo")));

        Map<String, Integer> freq = frequencies.apply(words.apply("b a c a b c"));



        System.out.println(top(3).apply(freq));

        List<Tuple<String,Integer>> test = words.andThen(keepWhere(keepAllOf)).andThen(frequencies).andThen(top(3)).apply("kargo ve hızlı bir kargo antalya antalya yolcu");
        System.out.println("test result : " + test);


    }





}
