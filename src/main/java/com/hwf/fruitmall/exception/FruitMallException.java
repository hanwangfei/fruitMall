package com.hwf.fruitmall.exception;

/**
 * 统一异常
 */
public class FruitMallException extends RuntimeException{
    private final Integer code;
    private final String message;

    public FruitMallException(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public FruitMallException(FruitMallExceptionEnum exceptionEnum){
        this(exceptionEnum.getCode(),exceptionEnum.getMsg());
    }

    public Integer getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
