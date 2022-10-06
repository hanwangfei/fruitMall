package com.hwf.fruitmall.exception;

import com.hwf.fruitmall.common.ApiRestResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.ArrayList;
import java.util.List;


/**
 * 处理统一异常的handler
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    private final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Object handleException(Exception e){
        log.error("Default Exception: ",e);
        return ApiRestResponse.error(FruitMallExceptionEnum.SYSTEM_ERROR);
    }


    /**
     * 处理自定义业务逻辑异常
     * @param e
     * @return
     */
    @ExceptionHandler(FruitMallException.class)
    @ResponseBody
    public Object handleFruitMallException(FruitMallException e){
        log.error("FruitMall Exception: ",e);
        return ApiRestResponse.error(e.getCode(),e.getMessage());
    }


    /**
     * 文件上传大小超出限制异常
     * @param e
     * @return
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseBody
    public Object handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e){
        log.error("MaxUploadSizeExceededException: ",e);
        return ApiRestResponse.error(FruitMallExceptionEnum.UPLOAD_FILE_TOO_BIG);
    }


    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ApiRestResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException e){
        log.error("MethodArgumentNotValidException : ",e);

        return handleBindingResult(e.getBindingResult());
    }

    private ApiRestResponse handleBindingResult(BindingResult result){

        //吧异常处理为对外暴露的提示
        List<String> list=new ArrayList<>();
        if(result.hasErrors()){
            List<ObjectError> allErrors = result.getAllErrors();
            for(ObjectError objectError:allErrors){
                String message = objectError.getDefaultMessage();
                list.add(message);
            }
        }
        if(list.size()==0){
            return ApiRestResponse.error(FruitMallExceptionEnum.REQUEST_PARAM_ERROR);
        }
        return ApiRestResponse.error(FruitMallExceptionEnum.REQUEST_PARAM_ERROR.getCode(),list.toString());
    }
}
