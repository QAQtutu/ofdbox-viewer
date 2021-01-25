package com.ofdbox.viewer.utils;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Wapper<T> {

    private int code=200;
    private String msg;
    private T data;

    public static <T> Wapper<T>  success(){
        return new Wapper<>(200,"",null);
    }
    public static <T> Wapper<T>  success(T data){
        return new Wapper<T>(200,"",data);
    }

    public static <T> Wapper<T>  success(String msg,T data){
        return new Wapper<T>(200,msg,data);
    }

    public static <T> Wapper<T>  fail(){
        return new Wapper<T>(500,"",null);
    }
    public static <T> Wapper<T>  fail(T data){
        return new Wapper<T>(500,"",data);
    }
    public static Wapper  fail(String msg){
        return new Wapper(500,msg,null);
    }
    public static <T> Wapper<T>  fail(String msg,T data){
        return new Wapper<T>(500,msg,data);
    }
}
