package com.example.fpcore;

/**
 * A function that applies a side effect to a value without transforming it.
 * Named {@code Effect} rather than {@code Consumer} because the value isn't
 * consumed: after the effect runs, the value is unchanged and still available
 * for further computations or effects (Saumont, chapter 7.5.4).
 */
@FunctionalInterface
public interface Effect<T> {
    void apply(T t);
}
