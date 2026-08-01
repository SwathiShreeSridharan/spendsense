package com.spendsense.exception;

public class DuplicateGroupException extends RuntimeException{
    public DuplicateGroupException(String message){
        super(message);
    }
}
