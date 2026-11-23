package com.example.exercises;

import java.math.BigDecimal;
import java.util.function.Function;

import static java.math.BigDecimal.ONE;
import static java.math.RoundingMode.HALF_UP;

public class Basic {

    public static Function<BigDecimal, BigDecimal> addVat(BigDecimal rate) {
        return price -> price.multiply(ONE.add(rate)).setScale(2, HALF_UP);
    }


    public static void main(String[] args) {
        Function<BigDecimal,BigDecimal> fiveVat = addVat(BigDecimal.valueOf(0.05));
        BigDecimal result = applyVat(fiveVat,BigDecimal.valueOf(100));

        System.out.println(String.format("applied vat : %s", result));

    }

    public static BigDecimal applyVat(Function<BigDecimal, BigDecimal> vat, BigDecimal price){
        return vat.apply(price);
    }



}
