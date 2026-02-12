package com.example.fpcore;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public final class Lazy<T> implements Supplier<T> {

    private Supplier<T> supplier;
    private volatile boolean evaluated;
    private T value;

    private Lazy(Supplier<T> supplier) {
        this.supplier = Objects.requireNonNull(supplier, "supplier");
    }

    public static <T> Lazy<T> of(Supplier<T> supplier) {
        return new Lazy<>(supplier);
    }

    public static <T> Lazy<T> ofValue(T value) {
        return new Lazy<>(() -> value);
    }

    @Override
    public T get() {
        if (!evaluated) {
            synchronized (this) {
                if (!evaluated) {
                    value = supplier.get();
                    supplier = null;
                    evaluated = true;
                }
            }
        }
        return value;
    }

    public <U> Lazy<U> map(Function<T, U> f) {
        return Lazy.of(() -> f.apply(get()));
    }

    public <U> Lazy<U> flatMap(Function<T, Lazy<U>> f) {
        return Lazy.of(() -> f.apply(get()).get());
    }

    public boolean isEvaluated() {
        return evaluated;
    }
}
