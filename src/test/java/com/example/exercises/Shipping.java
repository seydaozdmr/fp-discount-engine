package com.example.exercises;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.Function;

public class Shipping {

    static final BigDecimal UNIT = BigDecimal.valueOf(5);


    static Function<Region, Function<Speed, Function<Integer, BigDecimal>>> shippingFee = region ->
            speed ->
                    desi ->
                            region.base.add(
                                    UNIT.multiply(BigDecimal.valueOf(desi)))
                                    .multiply(speed.multiplier)
                                    .setScale(2, RoundingMode.HALF_UP);

    static Function<Speed, Function<Integer, BigDecimal>> marmaraRegionFunction = shippingFee.apply(Region.MARMARA);



    enum Region {
        MARMARA(new BigDecimal("30")),
        DOGU(new BigDecimal("60"));

        final BigDecimal base;

        Region(BigDecimal base) {
            this.base = base;
        }
    }

    enum Speed {
        STANDARD(new BigDecimal("1")),
        EXPRESS(new BigDecimal("1.5"));

        final BigDecimal multiplier;

        Speed(BigDecimal multiplier) {
            this.multiplier = multiplier;
        }
    }

    public static void main(String[] args) {

        System.out.println("Marmara Standard : " + marmaraRegionFunction.apply(Speed.STANDARD).apply(3));
    }

}
