package com.steelrain.springboot.lilac.exception;

public class NaruBookExistException extends RuntimeException {
    public NaruBookExistException(String msg){
        super(msg);
    }

    public NaruBookExistException(String msg, Exception ex){
        super(msg, ex);
    }
}
