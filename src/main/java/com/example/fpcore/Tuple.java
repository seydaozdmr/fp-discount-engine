package com.example.fpcore;

import java.util.Objects;

/** Immutable pair used across List/Result/Option composition (chapter 8). */
public final class Tuple<T, U> {

    public final T _1;
    public final U _2;

    public Tuple(T t, U u) {
        this._1 = Objects.requireNonNull(t);
        this._2 = Objects.requireNonNull(u);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tuple<?, ?> that)) return false;
        return _1.equals(that._1) && _2.equals(that._2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_1, _2);
    }

    @Override
    public String toString() {
        return String.format("(%s,%s)", _1, _2);
    }
}
