package com.example.discount.simple;

import java.util.List;

import static com.example.fpcore.Folds.fold;

public class FoldExample {

    public static void main(String[] args) {

        List<Integer> list = List.of(1,2,3,4,5);

        Integer result = fold(list, 0 , acc -> y -> acc + y);

        System.out.println(result);
    }
}
