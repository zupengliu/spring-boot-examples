package com.soil.async;

import com.soil.async.service.AsyncTaskService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AsyncApplication.class)
@Slf4j
public class AsyncApplicationTests {
    @Autowired
    private AsyncTaskService asyncTaskService;

    /***
     * 无返回的异步调用，使用try catch 捕获不到异常，需要使用 AsyncUncaughtExceptionHandler 捕获异常并打印
     */
    @Test
    public void testVoidAsync() {
        long callTime = System.currentTimeMillis();
        asyncTaskService.doTaskOne(callTime);
        asyncTaskService.doTaskTwo(callTime);
        asyncTaskService.doTaskThree(callTime);
        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
