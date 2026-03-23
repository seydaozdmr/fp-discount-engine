package com.example.discount.simple;

import com.example.discount.simple.helper.Effect;
import com.example.discount.simple.helper.Result;

import java.util.function.Function;
import java.util.regex.Pattern;

import static com.example.discount.simple.helper.Case.match;
import static com.example.discount.simple.helper.Case.mcase;
import static com.example.discount.simple.helper.Result.failure;
import static com.example.discount.simple.helper.Result.success;


public class EmailValidation {

    static Pattern emailPattern = Pattern.compile("^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,4}$");

    static Effect<String> successEffect = s -> System.out.println("Mail send to " + s);
    static Effect<String> failureEffect = s -> System.out.println("Error message logged: " + s);

    // emailChecker fonksiyonu, verilen string'in email formatına uygun olup olmadığını kontrol eder ve buna göre success veya failure Result döner
    static Function<String, Result<String>> emailChecker = s -> match(
            mcase(() -> success(s)),
            mcase(() -> s == null, () -> failure("email must not be null")),
            mcase(()-> s.length() == 0, () -> failure("email must not be empty")),
            mcase(()-> !emailPattern.matcher(s).matches(), () -> failure("email : " + s + " is invalid")));


    // duruma göre bind olan success veya failure effect'ini çalıştırır
    public static void main(String[] args) {

        // message mcase ile kontrol edilir ve success veya failure Result döner, ardından bind ile uygun effect çalıştırılır
        emailChecker.apply("this.is@my.email").bind(successEffect, failureEffect);
    }
}
