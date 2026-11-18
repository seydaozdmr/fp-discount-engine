package com.example.discount.simple;


import com.example.fpcore.Folds;

import java.util.List;
import java.util.function.Function;

public class ComposeMapExample {

    public static void main(String[] args) {

        Function<Double,Double> addTax = x -> x * 0.9;
        Function<Double,Double> addShipping = x -> x + 3.4;

        List<Double> prices = List.of(10.0, 20.0, 30.0);
        List<Double> priceWithTax = Folds.map(prices, addTax);
        List<Double> pricesWithTaxAndShipping = Folds.map(priceWithTax, addShipping);

        System.out.println(pricesWithTaxAndShipping);
    }
}
