package com.shipping.payment_service.exception;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponse (LocalDateTime time, int status, String error, String messag, Map<String,String> validationError) {
    public static ErrorResponse of(int status,String error,String message){
        return new ErrorResponse(LocalDateTime.now(),status,error,message,null);
    }
    public static ErrorResponse ofValidation(int status,String error,Map<String,String> validationError){
        return new ErrorResponse(LocalDateTime.now(),status,error,"Validation Failed",validationError);
    }
}
