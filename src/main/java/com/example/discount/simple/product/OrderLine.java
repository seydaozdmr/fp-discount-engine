package com.example.discount.simple.product;

public class OrderLine {
    private int count;
    private Product product;

    public OrderLine(int count, Product product) {
        this.count = count;
        this.product = product;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getCount() {
        return count;
    }
    public void setCount(int count) {
        this.count = count;
    }

    public Weight getWeight() {
        return this.product.getWeight().multiply(count);
    }

    public Price getAmount() {
        return this.product.getPrice().multiply(count);
    }



}
