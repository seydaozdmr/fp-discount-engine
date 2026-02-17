package com.example.fpcore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public abstract class Result<T> {

    @SuppressWarnings("rawtypes")
    private static final Result EMPTY = new Empty<>();

    public abstract <U> Result<U> map(Function<T, U> f);

    public abstract <U> Result<U> flatMap(Function<T, Result<U>> f);

    public abstract T getOrElse(T defaultValue);

    public abstract T getOrElseGet(Supplier<T> defaultSupplier);

    public abstract Result<T> orElse(Supplier<Result<T>> defaultSupplier);

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

    public Result<T> filter(Predicate<T> predicate, String message) {
        Objects.requireNonNull(predicate, "predicate");
        return flatMap(v -> predicate.test(v) ? this : Result.failure(message));
    }

    public Result<T> mapFailure(String message) {
        if (this instanceof Failure<T> f) {
            return Result.failure(new IllegalStateException(message, f.exception));
        }
        return this;
    }

    public RuntimeException failureCause() {
        if (this instanceof Failure<T> f) {
            return f.exception;
        }
        return null;
    }

    public Option<T> toOption() {
        if (isSuccess()) {
            return Option.some(getOrThrow());
        }
        return Option.none();
    }

    public static <T> Result<T> success(T value) {
        return new Success<>(Objects.requireNonNull(value, "value"));
    }

    public static <T> Result<T> failure(String message) {
        return new Failure<>(new IllegalStateException(message));
    }

    public static <T> Result<T> failure(RuntimeException exception) {
        return new Failure<>(Objects.requireNonNull(exception, "exception"));
    }

    public static <T> Result<T> failure(Exception exception) {
        return new Failure<>(new IllegalStateException(exception.getMessage(), exception));
    }

    @SuppressWarnings("unchecked")
    public static <T> Result<T> empty() {
        return (Result<T>) EMPTY;
    }

    public static <A, B> Function<Result<A>, Result<B>> lift(Function<A, B> f) {
        return ra -> ra.map(f);
    }

    public static <A, B, C> Result<C> map2(Result<A> ra, Result<B> rb, Function<A, Function<B, C>> f) {
        return ra.flatMap(a -> rb.map(b -> f.apply(a).apply(b)));
    }

    public static <A> Result<List<A>> sequence(List<Result<A>> list) {
        Result<List<A>> acc = Result.success(List.of());
        for (Result<A> item : list) {
            acc = map2(item, acc, a -> xs -> {
                ArrayList<A> next = new ArrayList<>(xs.size() + 1);
                next.addAll(xs);
                next.add(a);
                return List.copyOf(next);
            });
            if (acc.isFailure() || acc.isEmpty()) {
                return acc;
            }
        }
        return acc;
    }

    public static <A, B> Result<List<B>> traverse(List<A> list, Function<A, Result<B>> f) {
        List<Result<B>> mapped = new ArrayList<>(list.size());
        for (A a : list) {
            mapped.add(f.apply(a));
        }
        return sequence(mapped);
    }

    public static <A, B> Result<List<B>> traverseReduce(List<A> list, Function<A, Result<B>> f) {
        Result<List<B>> start = Result.success(List.of());

        return list.stream().reduce(
                start,
                (acc, a) -> map2(acc, f.apply(a), xs -> b -> {
                    ArrayList<B> next = new ArrayList<>(xs.size() + 1);
                    next.addAll(xs);
                    next.add(b);
                    return List.copyOf(next);
                } ), (x, y) -> { throw new UnsupportedOperationException("no parallel");
                }
        );
    }

    public static <A,B> Result<List<B>> traverseReduceShortCircuit(List<A> list, Function<A, Result<B>> f){
        Result<List<B>> result = Result.success(List.of());
        for (A a : list) {
            if (!result.isSuccess()) {
                return result;
            }
            Result<B> current = f.apply(a);
            result = map2(result, current, xs -> b -> {
                ArrayList<B> next = new ArrayList<>(xs.size() + 1);
                next.addAll(xs);
                next.add(b);
                return List.copyOf(next);
            });
        }
        return result;
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
        public T getOrElseGet(Supplier<T> defaultSupplier) {
            return defaultSupplier.get();
        }

        @Override
        public Result<T> orElse(Supplier<Result<T>> defaultSupplier) {
            return defaultSupplier.get();
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
            this.exception = exception;
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
            return defaultValue;
        }

        @Override
        public T getOrElseGet(Supplier<T> defaultSupplier) {
            return defaultSupplier.get();
        }

        @Override
        public Result<T> orElse(Supplier<Result<T>> defaultSupplier) {
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
            this.value = value;
        }

        @Override
        public <U> Result<U> map(Function<T, U> f) {
            try {
                U next = f.apply(value);
                return next == null ? Result.empty() : success(next);
            } catch (RuntimeException e) {
                return failure(e);
            }
        }

        @Override
        public <U> Result<U> flatMap(Function<T, Result<U>> f) {
            try {
                Result<U> next = f.apply(value);
                return next == null ? Result.empty() : next;
            } catch (RuntimeException e) {
                return failure(e);
            }
        }

        @Override
        public T getOrElse(T defaultValue) {
            return value;
        }

        @Override
        public T getOrElseGet(Supplier<T> defaultSupplier) {
            return value;
        }

        @Override
        public Result<T> orElse(Supplier<Result<T>> defaultSupplier) {
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
