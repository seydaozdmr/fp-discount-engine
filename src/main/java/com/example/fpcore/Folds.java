package com.example.fpcore;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public final class Folds {

    private Folds() {}

    // A left fold (also known as reduce) that processes the list from left to right.
    public static <A, B> B foldLeft(List<A> list, B identity, BiFunction<B,A,B> f) {
        B acc = identity;
        for (A a : list) {
            acc = f.apply(acc, a);
        }
        return acc;
    }

    public static <A> List<A> reverse (List<A> list) {
        return foldLeft(list, List.<A>of(), (acc, a) -> {
            ArrayList<A> next = new ArrayList<>();
            next.add(a);
            next.addAll(acc);
            return List.copyOf(next);
        });
    }

    public static <A, B> List<B> map (List<A> list, Function<A,B> f) {
        return foldLeft(list, List.<B>of(), (acc, a) -> {
            ArrayList<B> next = new ArrayList<>(acc.size() + 1);
            next.addAll(acc);
            next.add(f.apply(a));
            return List.copyOf(next);
        });
    }

    public static <A> List<A> filter (List<A> list, Predicate<A> p) {
        return foldLeft(list, List.<A>of(), (acc, a) -> {
            if (!p.test(a)) return acc;
            ArrayList<A> next = new ArrayList<>(acc.size() + 1);
            next.addAll(acc);
            next.add(a);
            return List.copyOf(next);
        });
    }




}
