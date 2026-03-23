package com.example.discount.simple.helper;


import java.util.function.Supplier;

public class Case <T> extends Tuple<Supplier<Boolean>, Supplier<Result<T>>>{

    public Case(Supplier<Boolean> booleanSupplier, Supplier<Result<T>> resultSupplier) {
        super(booleanSupplier, resultSupplier);
    }

    public static <T> Case<T> mcase(Supplier<Boolean> booleanSupplier, Supplier<Result<T>> resultSupplier) {
        return new Case<>(booleanSupplier, resultSupplier);
    }

    private static class DefaultCase<T> extends Case<T>{
        private DefaultCase(Supplier<Boolean> booleanSupplier, Supplier<Result<T>> resultSupplier) {
            super(booleanSupplier, resultSupplier);
        }
    }

    public static <T> DefaultCase<T> mcase(Supplier<Result<T>> resultSupplier) {
        return new DefaultCase<>(() -> true, resultSupplier);
    }

    public static <T> Result<T> match(DefaultCase<T> defaultCase, Case<T>... matches) {
        for (Case<T> match : matches) {
            if (match.get_1().get()) {
                return match.get_2().get();
            }
        }
        return defaultCase.get_2().get();
    }

}
