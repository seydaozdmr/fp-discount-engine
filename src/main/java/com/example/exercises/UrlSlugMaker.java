package com.example.exercises;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class UrlSlugMaker {

    private static Map<Character,Character> turkishLowercase;

    static {
        turkishLowercase = new HashMap<>();
        turkishLowercase.put('ç','c');
        turkishLowercase.put('ğ','g');
        turkishLowercase.put('ı','i');
        turkishLowercase.put('ö','o');
        turkishLowercase.put('ü','u');
        turkishLowercase.put('ş','s');
    }

    public static Function<String,String> trim = String::trim;

    public static Function<String,String> lower = s -> s.toLowerCase(new Locale("TR"));

    public static Function<String,String> turkishToAscii = s -> s.chars()
            .mapToObj(c -> (char)c)
            .map(c-> turkishLowercase.getOrDefault(c,c))
            .map(String::valueOf)
            .collect(Collectors.joining());

    public static Function<String,String> removePunctuation = s -> s.replaceAll("[^a-z0-9\\s-]", "");

    public static Function<String,String> spacesToHyphen = s -> s.replaceAll("[\\s-]+", "-");

    public static Function<String,String> trimHyphens = s -> s.replaceAll("^-+|-+$", "");

    public static Function<String,String> slugify = trim
                                                        .andThen(lower)
                                                        .andThen(turkishToAscii)
                                                        .andThen(removePunctuation)
                                                        .andThen(spacesToHyphen)
                                                        .andThen(trimHyphens);

    public static void main(String[] args) {
        String test = "çok güzel şeyler";
        System.out.println("slug : "+slugify.apply(test));
    }

}
