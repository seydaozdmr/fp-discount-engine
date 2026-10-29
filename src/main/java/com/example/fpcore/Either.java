package com.example.fpcore;

import java.util.function.Function;
import java.util.function.Supplier;

public abstract class Either<T,U> {

    public abstract <R2> Either<T,R2> map (Function<U,R2> mapper);
    public abstract <R2> Either<T,R2> flatMap (Function<U, Either<T,R2>> mapper);
    public abstract U getOrElse(Supplier<U> defaultSupplier);

    public Either<T,U> orElse(Supplier<Either<T,U>> defaultValue) {
        return map(x -> this).getOrElse(defaultValue);
    }


    private static class Left<T,U> extends Either<T,U> {
        private final T value;
        private Left(T value) {this.value = value;}
        @Override public <R2> Either<T, R2> map(Function<U, R2> f)                 { return new Left<>(value); }
        @Override public <R2> Either<T, R2> flatMap(Function<U, Either<T, R2>> f)  { return new Left<>(value); }
        @Override public U getOrElse(Supplier<U> defaultValue)                    { return defaultValue.get(); }
        public String toString() {return String.format("Left(%s)", value);}
    }

    private static class Right<T,U> extends Either<T,U> {
        private final U value;
        private Right(U value) {this.value = value;}

        public String toString() {return String.format("Right(%s)", value);}

        @Override
        public <R2> Either<T, R2> map(Function<U, R2> mapper) {
            return new Right<>(mapper.apply(value));
        }

        @Override
        public <R2> Either<T, R2> flatMap(Function<U, Either<T, R2>> mapper) {
            return mapper.apply(value);
        }

        @Override
        public U getOrElse(Supplier<U> defaultSupplier) {
            return value;
        }
    }

    public static <T,U> Either<T,U> left(T value) {return new Left<>(value);}
    public static <T,U> Either<T,U> right(U value) {return new Right<>(value);}

    public static <A extends Comparable<A>> Function<List<A>,Either<String,A>> max(){
        return xs -> xs.isEmpty() ?
                Either.left("max called on an empty list") :
                Either.right(xs.foldLeft(xs.head(),x->y->x.compareTo(y) < 0 ? x : y));
    }
}
