package com.ofdbox.viewer.utils;

import java.util.UUID;

public class Utils {
    public static String uuid(){
        return UUID.randomUUID().toString().replaceAll("-","");
    }
}
