package com.example.fpcore;

import com.example.discount.simple.helper.Effect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static com.example.fpcore.Folds.fold;
import static com.example.fpcore.Folds.foldLeft;

public class CollectionUtils {

    public static <T> List <T> list() {
        return Collections.emptyList();
    }

    public static <T> List <T> list(T t) {
        return Collections.singletonList(t);
    }

    public static <T> List <T> list(List<T> list) {
        return Collections.unmodifiableList(list);
    }

    @SafeVarargs
    public static <T> List <T> list(T... ts) {
        return Collections.unmodifiableList(Arrays.asList(Arrays.copyOf(ts, ts.length)));
    }

    public static <T> T head (List<T> list) {
        if (list.isEmpty()) throw new IllegalArgumentException("list is empty");
        return list.get(0);
    }

    private static <T> List<T> copy (List<T> list) {
        return new ArrayList<>(list);
    }

    public static <T> List<T> tail (List<T> list) {
        if  (list.isEmpty()) throw new IllegalArgumentException("list is empty");

        List<T> workingCopy = copy(list);
        workingCopy.remove(0);
        return Collections.unmodifiableList(workingCopy);
    }

    public static <T> List<T> append(List<T> list, T t) {
        List<T> workingCopy = copy(list);
        workingCopy.add(t);
        return Collections.unmodifiableList(workingCopy);
    }

    public static <T> List<T> prepend(T t, List<T> list) {
        List<T> workingCopy = copy(list);
        workingCopy.add(0, t);
        return Collections.unmodifiableList(workingCopy);
    }

    public static <T> List<T> prepend(List<T> list, T t){
        return fold(list, list(t), x -> y -> append(x,y)); //fold left
    }

    public static <T> List<T> reverse(List<T> list){
        return fold(list, list(), x -> y -> prepend(x,y));
    }

    public static <T> void forEach(Collection<T> collection, Effect<T> effect) {
        for (T t : collection) effect.apply(t);
    }

    public static <T> List<T> unfold (T seed, Function<T, T> f, Function<T, Boolean> p) {
        List<T> result = new ArrayList<>();
        T temp = seed;
        while(p.apply(temp)) {
            result = append(result, temp);
            temp = f.apply(temp);
        }
        return result;
    }

    public static List<Integer> range (int start, int end) {
        return unfold(start, x->x+1, x-> x < end);
    }

    public static List<Integer> recursivelyRange (int start, int end) {
        return end <= start ? list() : prepend(recursivelyRange(start+1, end), start);
    }

}
