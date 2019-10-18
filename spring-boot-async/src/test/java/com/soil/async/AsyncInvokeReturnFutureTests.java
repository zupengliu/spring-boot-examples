package com.soil.async;

import com.soil.async.service.AsyncFutureTaskService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = AsyncApplication.class)
@Slf4j
public class AsyncInvokeReturnFutureTests {
    @Autowired
    private AsyncFutureTaskService asyncFutureTaskService;
    @Test
    public void futureAsyncTest(){
        long callTime = System.currentTimeMillis();
        Future<String> futureOne = asyncFutureTaskService.doTaskOne(callTime);
        Future<String> futureTwo = asyncFutureTaskService.doTaskTwo(callTime);
        Future<String> futureThree = asyncFutureTaskService.doTaskThree(callTime);

        long waiteTime = 4000L;
        //超时等待未来时间
        long futureTime = waiteTime + System.currentTimeMillis();
        long remaining = waiteTime;
        while (remaining > 0){
            if(futureOne.isDone() && futureTwo.isDone() && futureThree.isDone()){
                long endTime = System.currentTimeMillis();
                try {
                    log.info(futureOne.get());
                    log.info(futureTwo.get());
                    log.info(futureThree.get());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

                log.info("耗时：{}",endTime - callTime);
                break;
            }

            //计算超时时间
            remaining = futureTime - System.currentTimeMillis();
        }

    }
}
