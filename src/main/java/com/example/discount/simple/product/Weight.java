package com.example.discount.simple.product;

import java.util.function.Function;

public class Weight {

    public static final Weight ZERO = new Weight(0);
    private final int weight;


    public Weight(int weight) {
        this.weight = weight;
    }

    public int getWeight() {
        return weight;
    }

    public Weight add(Weight that) {
        return new Weight(this.weight + that.weight);
    }

    public Weight multiply(int factor) {
        return new Weight(this.weight * factor);
    }

    public static Function<Weight, Function<OrderLine, Weight>> sum = x -> y -> x.add(y.getWeight());

    public String toString() {
        return String.format("%d", weight);
    }
}
