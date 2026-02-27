package com.example.discount.simple;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.Function;

public class TaxRate {

    private static Function<BigDecimal, Function<BigDecimal, BigDecimal>> taxRateFunction;

    public static void main(String[] args) {

        taxRateFunction = rate -> price -> price.multiply(BigDecimal.valueOf(100).add(rate)).divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP);

        Function<BigDecimal, BigDecimal> rateFunction = taxRateFunction.apply(BigDecimal.valueOf(9.0));

        BigDecimal result = rateFunction.apply(BigDecimal.valueOf(100));

        System.out.println("Final price with tax: " + result);

        Function<BigDecimal,BigDecimal> taxRateFunction2 = createTaxFunction(BigDecimal.valueOf(5.0));

        BigDecimal result2 = priceWithTax(BigDecimal.valueOf(100), taxRateFunction2);

        System.out.println("Final price with tax (using priceWithTax): " + result2);

    }

    private static Function<BigDecimal, BigDecimal> createTaxFunction(BigDecimal rate) {
        return price -> price.multiply(BigDecimal.valueOf(100).add(rate)).divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP);
    }

    private static BigDecimal priceWithTax(BigDecimal price, Function<BigDecimal, BigDecimal> taxRateFunction) {
        return taxRateFunction.apply(price);
    }

}
