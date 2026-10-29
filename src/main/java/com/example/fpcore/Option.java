package com.example.fpcore;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class Option<T> {

    private static final Option<?> NONE = new Option<>(null, false);

    private final T value;
    private final boolean defined;

    private Option(T value, boolean defined) {
        this.value = value;
        this.defined = defined;
    }

    public static <T> Option<T> some(T value) {
        return new Option<>(Objects.requireNonNull(value, "value"), true);
    }

    @SuppressWarnings("unchecked")
    public static <T> Option<T> none() {
        return (Option<T>) NONE;
    }

    public static <T> Option<T> ofNullable(T value) {
        return value == null ? none() : some(value);
    }

    public boolean isDefined() {
        return defined;
    }

    public boolean isEmpty() {
        return !defined;
    }

    public <U> Option<U> map(Function<T, U> mapper) {
        Objects.requireNonNull(mapper, "mapper");
        if (isEmpty()) {
            return none();
        }
        return Option.ofNullable(mapper.apply(value));
    }

    public <U> Option<U> flatMap(Function<T, Option<U>> mapper) {
        Objects.requireNonNull(mapper, "mapper");
        if (isEmpty()) {
            return none();
        }
        Option<U> next = mapper.apply(value);
        return next == null ? none() : next;
    }

    public Option<T> filter(Predicate<T> predicate) {
        Objects.requireNonNull(predicate, "predicate");
        if (isEmpty() || !predicate.test(value)) {
            return none();
        }
        return this;
    }

    public T getOrElse(T defaultValue) {
        return defined ? value : defaultValue;
    }

    public T getOrElseGet(Supplier<T> defaultSupplier) {
        return defined ? value : defaultSupplier.get();
    }

    public Option<T> orElse(Supplier<Option<T>> defaultSupplier) {
        return defined ? this : defaultSupplier.get();
    }

    public T getOrThrow() {
        if (isEmpty()) {
            throw new IllegalStateException("No value present");
        }
        return value;
    }

    public static <A,B,C> Option<C> map2 (Option<A> a, Option<B> b, Function<A, Function<B, C>> mapper) {
        return a.flatMap(ax -> b.map(bx -> mapper.apply(ax).apply(bx)));
    }

    private static <A, B> Option<List<B>> traverse(List<A> list, Function<A, Option<B>> f) {
        return list.foldRight(some(List.list()),
                x -> y -> map2(f.apply(x), y, a -> b -> b.cons(a)));
    }
    // sequence artık traverse cinsinden:
    public static <A> Option<List<A>> sequence(List<Option<A>> list) {
        return traverse(list, x -> x);
    }

    public Result<T> toResult(String message) {
        return defined ? Result.success(value) : Result.failure(message);
    }

    @Override
    public String toString() {
        return defined ? "Some(" + value + ")" : "None";
    }
}
