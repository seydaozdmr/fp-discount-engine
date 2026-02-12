package com.example.discount;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Result represents either:
 * - Success(value)
 * - Failure(exception)
 * - Empty (optional/absent data, not an error)
 */
public abstract class Result<T> {

    @SuppressWarnings("rawtypes")
    private static final Result EMPTY = new Empty<>();

    public abstract <U> Result<U> map(Function<T, U> f);

    public abstract <U> Result<U> flatMap(Function<T, Result<U>> f);

    public abstract T getOrElse(T defaultValue);

    public abstract T getOrElse(Supplier<T> defaultValue);

    public abstract Result<T> orElse(Supplier<Result<T>> defaultValue);

    public abstract T getOrThrow();

    public boolean isSuccess() {
        return this instanceof Success<?>;
    }

    public boolean isFailure() {
        return this instanceof Failure<?>;
    }

    public boolean isEmpty() {
        return this instanceof Empty<?>;
    }

    public Result<T> mapFailure(String message) {
        if (this instanceof Failure<T> f) {
            return failure(new IllegalStateException(message, f.exception));
        }
        return this;
    }

    public RuntimeException failureCause() {
        if (this instanceof Failure<T> f) {
            return f.exception;
        }
        return null;
    }

    public static <T> Result<T> success(T value) {
        return new Success<>(value);
    }

    public static <T> Result<T> failure(String message) {
        return new Failure<>(new IllegalStateException(message));
    }

    public static <T> Result<T> failure(RuntimeException exception) {
        return new Failure<>(exception);
    }

    public static <T> Result<T> failure(Exception exception) {
        return new Failure<>(new IllegalStateException(exception.getMessage(), exception));
    }

    @SuppressWarnings("unchecked")
    public static <T> Result<T> empty() {
        return EMPTY;
    }

    private static class Empty<T> extends Result<T> {
        @Override
        public <U> Result<U> map(Function<T, U> f) {
            return empty();
        }

        @Override
        public <U> Result<U> flatMap(Function<T, Result<U>> f) {
            return empty();
        }

        @Override
        public T getOrElse(T defaultValue) {
            return defaultValue;
        }

        @Override
        public T getOrElse(Supplier<T> defaultValue) {
            return defaultValue.get();
        }

        @Override
        public Result<T> orElse(Supplier<Result<T>> defaultValue) {
            return defaultValue.get();
        }

        @Override
        public T getOrThrow() {
            throw new IllegalStateException("No value present");
        }

        @Override
        public String toString() {
            return "Empty()";
        }
    }

    private static final class Failure<T> extends Empty<T> {
        private final RuntimeException exception;

        private Failure(RuntimeException exception) {
            this.exception = Objects.requireNonNull(exception, "exception");
        }

        @Override
        public <U> Result<U> map(Function<T, U> f) {
            return failure(exception);
        }

        @Override
        public <U> Result<U> flatMap(Function<T, Result<U>> f) {
            return failure(exception);
        }

        @Override
        public T getOrElse(T defaultValue) {
            throw exception;
        }

        @Override
        public T getOrElse(Supplier<T> defaultValue) {
            throw exception;
        }

        @Override
        public Result<T> orElse(Supplier<Result<T>> defaultValue) {
            return this;
        }

        @Override
        public T getOrThrow() {
            throw exception;
        }

        @Override
        public String toString() {
            return "Failure(" + exception.getMessage() + ")";
        }
    }

    private static final class Success<T> extends Result<T> {
        private final T value;

        private Success(T value) {
            this.value = Objects.requireNonNull(value, "value");
        }

        @Override
        public <U> Result<U> map(Function<T, U> f) {
            try {
                return success(f.apply(value));
            } catch (Exception e) {
                return failure(e);
            }
        }

        @Override
        public <U> Result<U> flatMap(Function<T, Result<U>> f) {
            try {
                return f.apply(value);
            } catch (Exception e) {
                return failure(e);
            }
        }

        @Override
        public T getOrElse(T defaultValue) {
            return value;
        }

        @Override
        public T getOrElse(Supplier<T> defaultValue) {
            return value;
        }

        @Override
        public Result<T> orElse(Supplier<Result<T>> defaultValue) {
            return this;
        }

        @Override
        public T getOrThrow() {
            return value;
        }

        @Override
        public String toString() {
            return "Success(" + value + ")";
        }
    }
}
