package com.example.examples;

import com.example.fpcore.Either;
import com.example.fpcore.List;

import java.util.ArrayList;

public class ListImpl {

    public static void main(String[] args) {
        List<Integer> list = List.list(1,2,3,4,5,6);
        List<Integer> result = list.drop(2);
        System.out.println(result);

        List<Integer> concat = List.concat(list, result);
        System.out.println(concat);

        List<Integer> gtFive = list.dropWhile(x->x <= 5);
        System.out.println(gtFive);

        List<Integer> reversed = list.reverse();
        System.out.println(reversed);

        List<Integer> foldRight = List.foldRight(List.list(1,2,3),List.list(),x->y->y.cons(x));
        System.out.println(foldRight);

        List<Integer> multiplyTwo = list.map(x->x*2);
        System.out.println(multiplyTwo);

        //System.out.println(Either.max().apply(list));
    }
}
