package com.example.fpcore;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.example.fpcore.CollectionUtils.*;

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
    // curried version of foldLeft
    public static <A,B> B fold (List<A> list, B identity, Function<B, Function<A, B>> f){
        B acc = identity;
        for (A a: list) {
            acc = f.apply(acc).apply(a);
        }
        return acc;
    }

    public static <A,B> B foldRight(List<A> list, B identity, Function<A, Function<B, B>> f) {
        return list.isEmpty() ? identity : f.apply(head(list)).apply(foldRight(tail(list), identity, f));
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

    // curried version of map
    public static <T,U> List<U> mapViaFoldLeft(List<T> list, Function<T,U> f){
        return fold(list, list(), x-> y-> append(x,f.apply(y)));
    }








}
