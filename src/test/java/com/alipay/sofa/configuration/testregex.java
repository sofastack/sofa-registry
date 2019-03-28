package com.alipay.sofa.configuration;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class testregex {
    public static void main(String[] args) {
        Pattern pattern = Pattern.compile("order-.*");

        Matcher matcher = pattern.matcher("orderservice");

        System.out.println(matcher.matches());
    }
}
