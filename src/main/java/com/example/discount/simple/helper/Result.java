package com.example.discount.simple.helper;

public interface Result <T>{

    void bind(Effect<T> success, Effect<String> failure);

    public static <T> Result<T> failure(String message) {
        return new Failure<>(message);
    }

    public static <T> Result<T> success (T value) {
        return new Success<>(value);
    }

    public class Success<T> implements Result<T> {
        private T value;

        private Success(T value) {
            this.value = value;
        }

        @Override
        public void bind(Effect<T> success, Effect<String> failure) {
            success.apply(value);
        }
    }

    public class Failure<T> implements Result<T> {
        private String message;

        private Failure(String message) {
            this.message = message;
        }

        @Override
        public void bind(Effect<T> success, Effect<String> failure) {
            failure.apply(message);
        }
    }
}
