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

    public Result<T> filter(Predicate<T> predicate) {
        return filter(predicate, "Condition not matched");
    }

    public Result<T> filter(Predicate<T> predicate, String message) {
        Objects.requireNonNull(predicate, "predicate");
        return flatMap(v -> predicate.test(v) ? this : Result.failure(message));
    }

    public boolean exists(Predicate<T> predicate) {
        return map(predicate::test).getOrElse(false);
    }

    public Result<T> mapFailure(String message) {
        if (this instanceof Failure<T> f) {
            return Result.failure(new IllegalStateException(message, f.exception));
        }
        return this;
    }

    public Result<T> mapFailure(String message, Exception cause) {
        if (this instanceof Failure<T> f) {
            return Result.failure(new IllegalStateException(message, f.exception));
        }
        return this;
    }

    public Result<T> mapFailure(Exception replacement) {
        if (this instanceof Failure<T>) {
            return Result.failure(replacement);
        }
        return this;
    }

    public void forEach(Effect<T> ef) {
        if (this instanceof Success<T> s) {
            ef.apply(s.value);
        }
        // Empty and Failure: do nothing.
    }

    public void forEachOrThrow(Effect<T> ef) {
        if (this instanceof Success<T> s) {
            ef.apply(s.value);
        } else if (this instanceof Failure<T> f) {
            throw f.exception;
        }
        // Empty: do nothing, Empty isn't an error.
    }

    /**
     * Applies the effect on Success and returns Empty; on Failure returns the error message
     * as a Success so a second effect (logging, printing) can handle it. Not pure: it runs ef.
     */
    public Result<String> forEachOrFail(Effect<T> ef) {
        if (this instanceof Success<T> s) {
            ef.apply(s.value);
            return Result.empty();
        }
        if (this instanceof Failure<T> f) {
            return Result.success(String.valueOf(f.exception.getMessage()));
        }
        return Result.empty();
    }

    public Result<RuntimeException> forEachOrException(Effect<T> ef) {
        if (this instanceof Success<T> s) {
            ef.apply(s.value);
            return Result.empty();
        }
        if (this instanceof Failure<T> f) {
            return Result.success(f.exception);
        }
        return Result.empty();
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

    public static <T> Result<T> of(T value) {
        return value != null ? success(value) : Result.failure("Null value");
    }

    public static <T> Result<T> of(T value, String message) {
        return value != null ? success(value) : Result.failure(message);
    }

    public static <T> Result<T> of(Predicate<T> predicate, T value) {
        try {
            return predicate.test(value) ? success(value) : empty();
        } catch (Exception e) {
            String errMessage = String.format("Exception while evaluating predicate: %s", value);
            return Result.failure(new IllegalStateException(errMessage, e));
        }
    }

    public static <T> Result<T> of(Predicate<T> predicate, T value, String message) {
        try {
            return predicate.test(value) ? success(value) : Result.failure(String.format(message, value));
        } catch (Exception e) {
            String errMessage = String.format("Exception while evaluating predicate: %s",
                    String.format(message, value));
            return Result.failure(new IllegalStateException(errMessage, e));
        }
    }

    public static <A, B> Function<Result<A>, Result<B>> lift(Function<A, B> f) {
        return ra -> ra.map(f);
    }

    public static <A, B, C> Function<Result<A>, Function<Result<B>, Result<C>>>
            lift2(Function<A, Function<B, C>> f) {
        return ra -> rb -> ra.map(f).flatMap(rb::map);
    }

    public static <A, B, C, D> Function<Result<A>, Function<Result<B>, Function<Result<C>, Result<D>>>>
            lift3(Function<A, Function<B, Function<C, D>>> f) {
        return ra -> rb -> rc -> ra.map(f).flatMap(rb::map).flatMap(rc::map);
    }

    public static <A, B, C> Result<C> map2(Result<A> ra, Result<B> rb, Function<A, Function<B, C>> f) {
        return lift2(f).apply(ra).apply(rb);
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
