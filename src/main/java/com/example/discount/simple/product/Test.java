package com.example.discount.simple.product;

import java.util.List;

import static com.example.fpcore.CollectionUtils.list;
import static com.example.fpcore.Folds.fold;

public class Test {

    public static void main(String[] args) {

        Product tootPaste = new Product("tootpaste", new Price(10), new Weight(100));
        Product tootBrush = new Product("tootbrush", new Price(95), new Weight(10));

        List<OrderLine> orders = list(new OrderLine(1,tootBrush), new OrderLine(1,tootPaste));

        Price price = fold(orders, Price.ZERO, Price.sum);
        Weight weight = fold(orders, Weight.ZERO, Weight.sum);

        System.out.printf("total price: %s%n", price);
        System.out.println("-----------------------");
        System.out.printf("total weight: %s" , weight);



    }
}
