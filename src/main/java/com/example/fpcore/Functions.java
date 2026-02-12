package com.example.fpcore;

import java.util.function.BiFunction;
import java.util.function.Function;

public final class Functions {

    private Functions() {
    }

    public static <A, B, C> Function<A, C> compose(Function<B, C> g, Function<A, B> f) {
        return a -> g.apply(f.apply(a));
    }

    public static <A, B, C> Function<A, C> andThen(Function<A, B> f, Function<B, C> g) {
        return a -> g.apply(f.apply(a));
    }

    public static <A, B, C> Function<A, Function<B, C>> curry(BiFunction<A, B, C> f) {
        return a -> b -> f.apply(a, b);
    }

    public static <A, B, C, D> Function<A, Function<B, Function<C, D>>> curry3(TriFunction<A, B, C, D> f) {
        return a -> b -> c -> f.apply(a, b, c);
    }

    public static <A, B> Function<Option<A>, Option<B>> liftOption(Function<A, B> f) {
        return oa -> oa.map(f);
    }

    public static <A, B> Function<Result<A>, Result<B>> liftResult(Function<A, B> f) {
        return ra -> ra.map(f);
    }

    @FunctionalInterface
    public interface TriFunction<A, B, C, D> {
        D apply(A a, B b, C c);
    }
}
