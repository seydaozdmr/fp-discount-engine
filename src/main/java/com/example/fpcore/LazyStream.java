package com.example.fpcore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public abstract class LazyStream<T> {

    private static final LazyStream<?> EMPTY = new Empty<>();

    public abstract boolean isEmpty();

    protected abstract T headUnsafe();

    protected abstract LazyStream<T> tailUnsafe();

    public static <T> LazyStream<T> empty() {
        @SuppressWarnings("unchecked")
        LazyStream<T> e = (LazyStream<T>) EMPTY;
        return e;
    }

    public static <T> LazyStream<T> cons(Supplier<T> head, Supplier<LazyStream<T>> tail) {
        return new Cons<>(Lazy.of(head), Lazy.of(tail));
    }

    public static <T> LazyStream<T> of(T... values) {
        LazyStream<T> acc = empty();
        for (int i = values.length - 1; i >= 0; i--) {
            T current = values[i];
            LazyStream<T> next = acc;
            acc = cons(() -> current, () -> next);
        }
        return acc;
    }

    public static <T> LazyStream<T> fromList(List<T> values) {
        LazyStream<T> acc = empty();
        for (int i = values.size() - 1; i >= 0; i--) {
            T current = values.get(i);
            LazyStream<T> next = acc;
            acc = cons(() -> current, () -> next);
        }
        return acc;
    }

    public static LazyStream<Integer> from(int start) {
        return iterate(start, n -> n + 1);
    }

    public static <T> LazyStream<T> repeat(T value) {
        return cons(() -> value, () -> repeat(value));
    }

    public static <T> LazyStream<T> iterate(T seed, Function<T, T> f) {
        return cons(() -> seed, () -> iterate(f.apply(seed), f));
    }

    public Option<T> headOption() {
        return isEmpty() ? Option.none() : Option.some(headUnsafe());
    }

    public Option<T> find(Predicate<T> predicate) {
        return filter(predicate).headOption();
    }

    public LazyStream<T> tail() {
        return isEmpty() ? this : tailUnsafe();
    }

    public LazyStream<T> take(int n) {
        if (n <= 0 || isEmpty()) {
            return empty();
        }
        return cons(this::headUnsafe, () -> tailUnsafe().take(n - 1));
    }

    public LazyStream<T> drop(int n) {
        LazyStream<T> current = this;
        int remaining = n;
        while (remaining > 0 && !current.isEmpty()) {
            current = current.tailUnsafe();
            remaining--;
        }
        return current;
    }

    public LazyStream<T> takeWhile(Predicate<T> predicate) {
        Objects.requireNonNull(predicate, "predicate");
        if (isEmpty()) {
            return empty();
        }
        T h = headUnsafe();
        if (!predicate.test(h)) {
            return empty();
        }
        return cons(() -> h, () -> tailUnsafe().takeWhile(predicate));
    }

    public LazyStream<T> dropWhile(Predicate<T> predicate) {
        Objects.requireNonNull(predicate, "predicate");
        LazyStream<T> current = this;
        while (!current.isEmpty() && predicate.test(current.headUnsafe())) {
            current = current.tailUnsafe();
        }
        return current;
    }

    public <U> LazyStream<U> map(Function<T, U> mapper) {
        Objects.requireNonNull(mapper, "mapper");
        if (isEmpty()) {
            return empty();
        }
        return LazyStream.cons(() -> mapper.apply(headUnsafe()), () -> tailUnsafe().map(mapper));
    }

    public LazyStream<T> filter(Predicate<T> predicate) {
        Objects.requireNonNull(predicate, "predicate");
        LazyStream<T> stream = dropWhile(predicate.negate());
        if (stream.isEmpty()) {
            return stream;
        }
        return cons(stream::headUnsafe, () -> stream.tailUnsafe().filter(predicate));
    }

    public LazyStream<T> append(Supplier<LazyStream<T>> other) {
        Objects.requireNonNull(other, "other");
        if (isEmpty()) {
            return other.get();
        }
        return cons(this::headUnsafe, () -> tailUnsafe().append(other));
    }

    public <U> LazyStream<U> flatMap(Function<T, LazyStream<U>> mapper) {
        Objects.requireNonNull(mapper, "mapper");
        if (isEmpty()) {
            return empty();
        }
        return mapper.apply(headUnsafe()).append(() -> tailUnsafe().flatMap(mapper));
    }

    public <U> U foldRight(Supplier<U> zero, Function<T, Function<Supplier<U>, U>> f) {
        if (isEmpty()) {
            return zero.get();
        }
        return f.apply(headUnsafe()).apply(() -> tailUnsafe().foldRight(zero, f));
    }

    public List<T> toList() {
        ArrayList<T> out = new ArrayList<>();
        LazyStream<T> current = this;
        while (!current.isEmpty()) {
            out.add(current.headUnsafe());
            current = current.tailUnsafe();
        }
        return List.copyOf(out);
    }

    private static final class Empty<T> extends LazyStream<T> {
        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        protected T headUnsafe() {
            throw new IllegalStateException("head on empty stream");
        }

        @Override
        protected LazyStream<T> tailUnsafe() {
            throw new IllegalStateException("tail on empty stream");
        }
    }

    private static final class Cons<T> extends LazyStream<T> {
        private final Lazy<T> head;
        private final Lazy<LazyStream<T>> tail;

        private Cons(Lazy<T> head, Lazy<LazyStream<T>> tail) {
            this.head = head;
            this.tail = tail;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        protected T headUnsafe() {
            return head.get();
        }

        @Override
        protected LazyStream<T> tailUnsafe() {
            return tail.get();
        }
    }
}
