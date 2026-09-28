package com.example.kitchen.utils;

import java.util.Base64;


public class BASE64Encoder {

    public String encode(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }
}
