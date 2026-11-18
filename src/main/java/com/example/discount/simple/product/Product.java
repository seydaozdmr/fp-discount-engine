package com.example.discount.simple.product;

public class Product {

    private final String name;
    private final Price price;
    private final Weight weight;

    public Product(String name, Price price, Weight weight) {
        this.name = name;
        this.price = price;
        this.weight = weight;
    }

    public String getName() {
        return name;
    }

    public Price getPrice() {
        return price;
    }

    public Weight getWeight() {
        return weight;
    }
}
