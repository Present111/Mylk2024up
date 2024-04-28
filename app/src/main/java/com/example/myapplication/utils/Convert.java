package com.example.myapplication.utils;

public class Convert {
    public static String convertName(String name) {
        StringBuilder initials = new StringBuilder();
        String[] parts = name.split("\\s+");
        for (String part : parts) {
            if (!part.isEmpty()) {
                initials.append(part.charAt(0));
            }
        }
        return initials.toString().toUpperCase();
    }
}
