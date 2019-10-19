package com.soil.completable.future;

import com.soil.completable.future.service.DefaultAsyncService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CompletablefutureApplication.class)
@Slf4j
public class DefaultAsyncServiceTest {
    @Autowired
    private DefaultAsyncService defaultAsyncService;

    /**
     * 同步调用
     */
    @Test
    public void syncCale() {
        Integer cale = defaultAsyncService.cale(10);
    }

    /**
     * 异步调用
     * 1、计算结果完成时的处理
     */
    @Test
    public void asyncCale() throws ExecutionException,InterruptedException {
//        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> defaultAsyncService.cale(10));
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {return defaultAsyncService.cale(10);});
        /*Integer result = future.get();
        log.info("join=>>>{}",result);*/
        Future<Integer> f = future.whenCompleteAsync((v, e)->{
            log.error("默认线程池线程名称：{}",Thread.currentThread().getName());
            log.info("结果：{}",v);
        });

        log.info("主线程：{},计算结果：{}",Thread.currentThread().getName(),f.get());
    }

    /**
     * CompletableFuture中的异常处理
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void asyncException()throws ExecutionException ,InterruptedException{
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(()->{
            //方案1
            /*try {
                return defaultAsyncService.caleException(5);
            } catch (Exception e) {
                log.error("【测试异常】>>>",e);
                return 0;
            }*/
            //方案2
            return defaultAsyncService.caleException(5);
        }).exceptionally(ex->{
            log.error("【计算出现异常】{}",ex);
            return 0;
        });

        log.info("最终结果:{}",future.get());
    }

    /**
     * 组和多个CompletableFuture的方法是thenCombine(),它的签名如下:
     * public <U,V> CompletableFuture<V> thenCombine(CompletionStage<? extends U> other,BiFunction<? super T,? super U,? extends V> fn)
     */
    @Test
    public void combined()throws ExecutionException ,InterruptedException{
        long startTime = System.currentTimeMillis();
        /*方法thenCombine()首先完成当前CompletableFuture和other的执行,
        接着,将这两者的执行结果传递给BiFunction(该接口接受两个参数,并有一个返回值),
        并返回代表BiFuntion实例的CompletableFuture对象:*/
        CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(()->defaultAsyncService.cale(5));
        CompletableFuture<Integer> future2 = CompletableFuture.supplyAsync(()->defaultAsyncService.cale(10));

        CompletableFuture<String> stringCompletableFuture = future1.thenCombine(future2, (i, j) -> {
            log.info("i={},j={}", i, j);
            return i + j;
        }).thenApply(result -> {
            log.info("异步回调转换-线程名称：{}",Thread.currentThread().getName());
            return "计算结果：" + result;
        });

        log.info(stringCompletableFuture.get());
        log.info("耗时：{}",System.currentTimeMillis() - startTime);
    }
}
