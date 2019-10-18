package com.soil.async.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class AsyncFutureTaskService {
    /**
     *  异常调用返回Future
     *  对于返回值是Future，不会被AsyncUncaughtExceptionHandler处理，需要我们在方法中捕获异常并处理
     *  或者在调用方在调用Futrue.get时捕获异常进行处理
     */
    @Async
    public Future<String> doTaskOne(long callTime){
        long startTime = System.currentTimeMillis();
        Future<String> future;
        try {
            TimeUnit.SECONDS.sleep(2);
            future = new AsyncResult<>("Task1-SUCCESS");
            long endTime = System.currentTimeMillis();
            log.info("【任务一】>>>线程名称:{},调用时间:{},开始时间:{},结束时间:{},耗时:{}",
                    Thread.currentThread().getName(),callTime,startTime,endTime,(endTime - startTime));
            throw new IllegalArgumentException("任务一 abc");
        } catch (InterruptedException e) {
            future = new AsyncResult<String>("error");
        } catch(IllegalArgumentException e){
            future = new AsyncResult<String>(e.getMessage());
        }
        return future;
    }

    @Async
    public Future<String> doTaskTwo(long callTime){
        long startTime = System.currentTimeMillis();
        Future<String> future;
        try {TimeUnit.SECONDS.sleep(3); } catch (InterruptedException e) { e.printStackTrace(); }
        long endTime = System.currentTimeMillis();
        future = new AsyncResult<>("task2-SUCCESS");
        log.info("【任务二】>>>线程名称:{},调用时间:{},开始时间:{},结束时间:{},耗时:{}",Thread.currentThread().getName(),callTime,startTime,endTime,(endTime - startTime));
        return future;
    }

    @Async
    public Future<String> doTaskThree(long callTime){
        long startTime = System.currentTimeMillis();
        Future<String> future;
        try {TimeUnit.SECONDS.sleep(1); } catch (InterruptedException e) { e.printStackTrace(); }
        long endTime = System.currentTimeMillis();
        future = new AsyncResult<>("task3-SUCCESS");
        log.info("【任务三】>>>线程名称:{},调用时间:{},开始时间:{},结束时间:{},耗时:{}",Thread.currentThread().getName(),callTime,startTime,endTime,(endTime - startTime));
        return future;
    }
}
