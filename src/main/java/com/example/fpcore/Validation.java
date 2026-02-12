package com.example.fpcore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class Validation<T> {

    private final T value;
    private final List<String> errors;

    private Validation(T value, List<String> errors) {
        this.value = value;
        this.errors = errors;
    }

    public static <T> Validation<T> valid(T value) {
        return new Validation<>(Objects.requireNonNull(value, "value"), List.of());
    }

    public static <T> Validation<T> invalid(String error) {
        return new Validation<>(null, List.of(error));
    }

    public static <T> Validation<T> invalid(List<String> errors) {
        return new Validation<>(null, List.copyOf(errors));
    }

    public boolean isValid() {
        return errors.isEmpty();
    }

    public List<String> errors() {
        return errors;
    }

    public T getOrThrow() {
        if (!isValid()) {
            throw new IllegalStateException(String.join("; ", errors));
        }
        return value;
    }

    public <U> Validation<U> map(Function<T, U> f) {
        if (!isValid()) {
            return Validation.invalid(errors);
        }
        return Validation.valid(f.apply(value));
    }

    public <U> Validation<U> flatMap(Function<T, Validation<U>> f) {
        if (!isValid()) {
            return Validation.invalid(errors);
        }
        return f.apply(value);
    }

    public Result<T> toResult() {
        if (isValid()) {
            return Result.success(value);
        }
        return Result.failure(String.join("; ", errors));
    }

    public static <A, B, R> Validation<R> map2(
            Validation<A> va,
            Validation<B> vb,
            BiFunction<A, B, R> f
    ) {
        List<String> all = new ArrayList<>();
        if (!va.isValid()) {
            all.addAll(va.errors());
        }
        if (!vb.isValid()) {
            all.addAll(vb.errors());
        }
        if (!all.isEmpty()) {
            return Validation.invalid(all);
        }
        return Validation.valid(f.apply(va.value, vb.value));
    }

    public static <A, B, C, R> Validation<R> map3(
            Validation<A> va,
            Validation<B> vb,
            Validation<C> vc,
            Functions.TriFunction<A, B, C, R> f
    ) {
        List<String> all = new ArrayList<>();
        if (!va.isValid()) {
            all.addAll(va.errors());
        }
        if (!vb.isValid()) {
            all.addAll(vb.errors());
        }
        if (!vc.isValid()) {
            all.addAll(vc.errors());
        }
        if (!all.isEmpty()) {
            return Validation.invalid(all);
        }
        return Validation.valid(f.apply(va.value, vb.value, vc.value));
    }
}
