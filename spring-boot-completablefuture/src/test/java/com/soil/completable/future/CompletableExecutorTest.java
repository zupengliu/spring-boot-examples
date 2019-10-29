package com.soil.completable.future;

import com.alibaba.fastjson.JSON;
import com.soil.completable.future.conf.CustomizeThreadPool;
import com.soil.completable.future.domain.User;
import com.soil.completable.future.service.DefaultAsyncService;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.implementation.bytecode.Throw;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CompletablefutureApplication.class)
@Slf4j
public class CompletableExecutorTest {
    @Autowired
    private DefaultAsyncService defaultAsyncService;
    @Autowired
    private CustomizeThreadPool customizeThreadPool;
    /**
     * 指定线程池执行异步任务
     */
    @Test
    public void executeOfCustomizeThread(){

        for (int i=0;i<200;i++) {
            CompletableFuture<Long> future = CompletableFuture.supplyAsync(new Supplier<Long>() {
                @Override
                public Long get() {
                    return defaultAsyncService.save(User.builder().id(123L).userName("liuzp").build());
                }
            }, customizeThreadPool.taskExecutor());

            CompletableFuture<User> userCompletableFuture =
                    future.thenCompose(userId -> CompletableFuture.supplyAsync(() -> {

                        return defaultAsyncService.getUserInfo(userId);

                    }, customizeThreadPool.taskExecutor()));

            userCompletableFuture.whenComplete((result, throwable) -> {
                if (null == throwable) {
                    log.info("用户信息:{}", result.getUserName());
                } else {
                    log.error("程序异常:{}", throwable);
                }
            });
        }
    }

    @Test
    public void executeOfCustomizeThreadB(){

            CompletableFuture<User> future = CompletableFuture.supplyAsync(new Supplier<Long>() {
                @Override
                public Long get() {
                    return defaultAsyncService.save(User.builder().id(123L).userName("liuzp").build());
                }
            }, customizeThreadPool.taskExecutor())
            .thenCompose(userId -> CompletableFuture.supplyAsync(() -> {

                return defaultAsyncService.getUserInfo(userId);

            }, customizeThreadPool.taskExecutor()))
            .whenComplete((v,e)->{
                if (null == e) {
                    log.info("用户信息:{}", v.getUserName());
                } else {
                    log.error("程序异常:{}", e);
                }
            });
            future.join();
    }

}
