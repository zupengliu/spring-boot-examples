package com.soil.async.conf;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

import java.lang.reflect.Method;

/**
 * 自定义异步调用异常类
 * 对void方法抛出的异常处理的类AsyncUncaughtExceptionHandler
 */
@Slf4j
public class AsyncCustomUncaughtExceptionHandler implements AsyncUncaughtExceptionHandler {

    @Override
    public void handleUncaughtException(Throwable throwable, Method method, Object... objects) {
        log.error("【ASYNC-异常信息】>>>{}",throwable.getMessage());

        log.error("【ASYNC-异常方法名称】>>>{}", JSON.toJSONString(method));

        for (Object object : objects) {
            log.info("【ASYNC-参数】>>>",object);
        }
    }
}
