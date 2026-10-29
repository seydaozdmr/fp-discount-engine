package com.example.fpcore;

import java.util.function.Supplier;

public interface TailCall<T> {

    TailCall<T> next();

    default boolean isComplete() {
        return false;
    }

    default T result() {
        throw new IllegalStateException("Computation is not complete");
    }

    default T eval() {
        TailCall<T> current = this;
        while (!current.isComplete()) {
            current = current.next();
        }
        return current.result();
    }

    static <T> TailCall<T> call(Supplier<TailCall<T>> nextStep) {
        return nextStep::get;
    }

    static <T> TailCall<T> done(T value) {
        return new Completed<>(value);
    }

    record Completed<T>(T value) implements TailCall<T> {

        @Override
        public TailCall<T> next() {
            return this;
        }

        @Override
        public boolean isComplete() {
            return true;
        }

        @Override
        public T result() {
            return value;
        }
    }
}
