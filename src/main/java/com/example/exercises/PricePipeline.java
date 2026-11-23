package com.example.exercises;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.Function;

import static java.math.BigDecimal.ONE;
import static java.math.RoundingMode.HALF_UP;

public class PricePipeline {

    public static Function<BigDecimal,BigDecimal> discount(BigDecimal rate) {
        return price -> price.multiply(BigDecimal.ONE.subtract(rate)) ;
    }

    public static Function<BigDecimal, BigDecimal> round = price -> price.setScale(2, RoundingMode.HALF_UP);

    public static Function<BigDecimal, Function<BigDecimal,BigDecimal>> fixedAmountDiscount =
            amount ->
                price ->
                            price.subtract(amount);

    public static Function<BigDecimal, BigDecimal> addVat(BigDecimal rate) {
        return price -> price.multiply(ONE.add(rate)).setScale(2, HALF_UP);
    }

    public static Function<BigDecimal, Function<BigDecimal, Function<BigDecimal, BigDecimal>>> fixedPipeline =
            amount ->
                    rate ->
                            fixedAmountDiscount.apply(amount).andThen(addVat(rate)).andThen(round);

    public static Function<BigDecimal,BigDecimal> pipeline = discount(new BigDecimal("0.10")).andThen(addVat(new BigDecimal("0.20"))).andThen(round);



    public static void main(String[] args) {
        System.out.println("pipeline sonucu :" +pipeline.apply(BigDecimal.valueOf(100)));

        Function<BigDecimal,BigDecimal> tenFixedDiscount = fixedAmountDiscount.apply(BigDecimal.valueOf(10));

        System.out.println("fixed amount discount 100 : " + tenFixedDiscount.apply(BigDecimal.valueOf(100)));

        System.out.println("fixed pipeline: " + fixedPipeline.apply(new BigDecimal("10")).apply(new BigDecimal("0.20")).apply(new BigDecimal("100")));
        fixedAmountDiscount.apply(new BigDecimal("10"));

//        rate -> fixedAmountDiscount.apply(new BigDecimal("10")).andThen(addVat(rate)).andThen(round);
//        fixedAmountDiscount.apply(new BigDecimal("10")).andThen(addVat("0.20")).andThen(round);
//        price ->
//                price.subtract(amount).setScale(2,HALF_UP)
//
//                x -> round ( addVat( fixedAmountDiscount(x)))
    }



}
