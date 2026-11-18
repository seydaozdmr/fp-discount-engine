package com.example.discount.simple.product;

import java.util.function.Function;

public class Price {

    private final double price;
    public static final Price ZERO = new Price(0.0);

    public Price(double price) {
        this.price = price;
    }

    public static Price price(double price) {
        if (price < 0) throw new IllegalArgumentException("price cannot be negative");
        return new Price(price);
    }

    public double getPrice() {
        return price;
    }

    public Price add(Price that) {
        return new Price(this.price + that.price);
    }

    public Price multiply(double factor) {
        return new Price(this.price * factor);
    }

    public static Function<Price, Function<OrderLine, Price>> sum = x -> y -> x.add(y.getAmount());

    public String toString() {
        return String.format("%.2f", price);
    }

}
