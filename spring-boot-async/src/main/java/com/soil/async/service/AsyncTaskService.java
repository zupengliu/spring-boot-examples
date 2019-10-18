package com.soil.async.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class AsyncTaskService {

    @Async
    public void doTaskOne(long callTime){
        long startTime = System.currentTimeMillis();
        try {TimeUnit.SECONDS.sleep(2); } catch (InterruptedException e) { e.printStackTrace(); }
        long endTime = System.currentTimeMillis();
        log.info("【任务一】>>>线程名称:{},调用时间:{},开始时间:{},结束时间:{},耗时:{}",Thread.currentThread().getName(),callTime,startTime,endTime,(endTime - startTime));
        throw new IllegalArgumentException(callTime+"");
    }

    @Async
    public void doTaskTwo(long callTime){
        long startTime = System.currentTimeMillis();
        try {TimeUnit.SECONDS.sleep(3); } catch (InterruptedException e) { e.printStackTrace(); }
        long endTime = System.currentTimeMillis();
        log.info("【任务二】>>>线程名称:{},调用时间:{},开始时间:{},结束时间:{},耗时:{}",Thread.currentThread().getName(),callTime,startTime,endTime,(endTime - startTime));
    }

    @Async
    public void doTaskThree(long callTime){
        long startTime = System.currentTimeMillis();
        try {TimeUnit.SECONDS.sleep(1); } catch (InterruptedException e) { e.printStackTrace(); }
        long endTime = System.currentTimeMillis();
        log.info("【任务三】>>>线程名称:{},调用时间:{},开始时间:{},结束时间:{},耗时:{}",Thread.currentThread().getName(),callTime,startTime,endTime,(endTime - startTime));
    }
}
