package com.example.discount.simple;

import com.example.fpcore.CollectionUtils;
import com.example.fpcore.Folds;

import java.util.List;

public class RangeExample {
    public static void main(String[] args) {

        List<Integer> range = CollectionUtils.unfold(0, x -> x + 1 , x -> x < 10);
        System.out.println(range);

        List<Integer> range2 = CollectionUtils.recursivelyRange(0, 10);

        List<Integer> range3 = Folds.mapViaFoldLeft(CollectionUtils.range(0, 10) , x -> x * 2);
        System.out.println(range3);
    }
}
