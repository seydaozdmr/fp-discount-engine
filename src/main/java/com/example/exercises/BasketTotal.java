package com.example.exercises;

import com.example.fpcore.List;
import com.example.fpcore.Tuple;

import java.math.BigDecimal;

public class BasketTotal {

    public record BasketLineItem(BigDecimal unitPrice, int quantity){}

    static Tuple<BigDecimal, Integer> totals(List<BasketLineItem> basketLineItemList){
        return basketLineItemList.foldLeft(new Tuple<>(BigDecimal.ZERO,0),
                acc -> line ->
                        new Tuple<>(acc._1.add(line.unitPrice().multiply(BigDecimal.valueOf(line.quantity))),
                        acc._2 + line.quantity()));
    }

    public static void main(String[] args) {
        List<BasketLineItem> lines = List.list(new BasketLineItem(BigDecimal.valueOf(100),2),new BasketLineItem(BigDecimal.valueOf(22.5),3));

        System.out.println("total : " + totals(lines));
    }
}
