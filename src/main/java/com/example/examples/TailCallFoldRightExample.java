package com.example.examples;

import com.example.fpcore.TailCall;

import java.util.List;
import java.util.function.Function;

public final class TailCallFoldRightExample {

    public static void main(String[] args) {
        List<Integer> numbers = List.of(1, 2, 3, 4, 5);

        Integer sum = foldRight(numbers, 0, value -> acc -> value + acc);
        String expression = foldRight(numbers, "zero", value -> acc -> "(" + value + " + " + acc + ")");

        System.out.println("sum = " + sum);
        System.out.println("expression = " + expression);
    }

    public static <A, B> B foldRight(List<A> list, B identity, Function<A, Function<B, B>> f) {
        return foldRight(list, list.size() - 1, identity, f).eval();
    }

    private static <A, B> TailCall<B> foldRight(
            List<A> list,
            int index,
            B acc,
            Function<A, Function<B, B>> f
    ) {
        if (index < 0) {
            return TailCall.done(acc);
        }
        B nextAcc = f.apply(list.get(index)).apply(acc);
        return TailCall.call(() -> foldRight(list, index - 1, nextAcc, f));
    }
}
