package com.example.exercises;

import com.example.fpcore.List;
import java.util.function.Function;

public class RunningBalance {

    public static <A, B> List<B> scanLeft(List<A> list, B zero, Function<B, Function<A, B>> f) {
        List<B> start = List.<B>list().cons(zero);                      // (1) boş listede bile sonuç [zero] olmalı
        List<B> reversed = list.foldLeft(start, acc -> a -> acc.cons(f.apply(acc.head()).apply(a)));                // (2) yeni bakiyeyi hesapla ve biriktiriciye ekle
        return reversed.reverse();                               // (3)
    }

    public static List<Integer> runningBalance(int opening, List<Integer> transactions) {
        return scanLeft(transactions, opening, balance -> tx -> balance + tx);
    }






}
