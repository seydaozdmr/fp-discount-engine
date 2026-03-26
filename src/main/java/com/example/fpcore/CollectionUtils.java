package com.example.fpcore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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


}
