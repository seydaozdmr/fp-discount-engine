package com.example.discount.simple;

import com.example.discount.simple.helper.Effect;
import com.example.fpcore.Executable;
import com.example.fpcore.Folds;

import java.util.List;
import java.util.function.Function;

import static com.example.fpcore.Folds.fold;

public class EffectExample {
    public static void main(String[] args) {

        Function<Double,Double> addTax = x -> x * 0.8;
        Function<Double,Double> addShipping = x -> x + 3.5;

        List<Double> prices = List.of(10.0, 20.0, 30.0);
        List<Double> priceWithTax = Folds.map(prices, addTax);
        List<Double> pricesWithTaxAndShipping = Folds.map(priceWithTax, addShipping);

        Effect<Double> effect = x -> {
            System.out.printf("%.2f", x);
            System.out.println();
        };
        Function<Executable, Function<Executable, Executable>> compose = x -> y -> ()-> {
            x.execute();
            y.execute();
        };
        Executable program = fold(pricesWithTaxAndShipping, ()->{
            System.out.println("Final prices:");
        }, e-> d-> compose.apply(e).apply(()-> effect.apply(d)));
        program.execute();

    }
}
