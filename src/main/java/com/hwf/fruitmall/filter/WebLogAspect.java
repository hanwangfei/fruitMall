package com.hwf.fruitmall.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

/**
 * 打印请求和响应信息
 */
@Aspect
@Component
public class WebLogAspect {
    private final Logger log = LoggerFactory.getLogger(WebLogAspect.class);
    @Pointcut("execution(public * com.hwf.fruitmall.controller.*.*(..))")  //配置拦截点为controller包下的所有类的所有方法
    public void webLog(){

    }
    @Before("webLog()")
    public void doBefore(JoinPoint joinPoint){
        //收到请求，记录请求内容
        ServletRequestAttributes attributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest(); //拿到当前请求

        log.info("URL ： "+request.getRequestURI().toString());
        log.info("HTTP_METHOD :" +request.getMethod());  //请求方法
        log.info("IP : "+request.getRemoteAddr());  //拿到ip
        log.info("CLASS_METHOD : " +joinPoint.getSignature().getDeclaringTypeName()+"."+joinPoint.getSignature().getName());  //拿到类名和方法名
        log.info("ARGS : "+ Arrays.toString(joinPoint.getArgs()));  //拿到参数

    }
    @AfterReturning(returning = "res",pointcut = "webLog()")
    public void doAfterReturning(Object res){
        //处理完请求，返回内容
        try {
            log.info("RESPONSE : "+new ObjectMapper().writeValueAsString(res));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

    }
}
