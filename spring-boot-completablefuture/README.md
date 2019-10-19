#Java CompletableFuture 详解
    Java 8 强大的函数式异步编程辅助类

#目录
```
[TOC]

1.<a href="#CF1" target="_self"/>主动完成计算
2.创建CompletableFuture对象。
3.计算结果完成时的处理
4.转换
5.纯消费(执行Action)
6.组合
7.Either
8.辅助方法 allOf 和 anyOf
9.更进一步
10.参考文档
```


<a href="https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/Future.html">Future</a>是Java 5添加的类，用来描述一个异步计算的结果。你可以使用isDone方法检查计算是否完成，或者使用get阻塞住调用线程，直到计算完成返回结果，你也可以使用cancel方法停止任务的执行。

    ```java
    public class BasicFuture {
        public static void main(String[] args) throws ExecutionException, InterruptedException {
            ExecutorService es = Executors.newFixedThreadPool(10);
            Future<Integer> f = es.submit(() ->{
                    // 长时间的异步计算
                    // ……
                    // 然后返回结果
                    return 100;
                });
    //        while(!f.isDone())
    //            ;
            f.get();
        }
    }
    ```
虽然Future以及相关使用方法提供了异步执行任务的能力，但是对于结果的获取却是很不方便，只能通过阻塞或者轮询的方式得到任务的结果。阻塞的方式显然和我们的异步编程的初衷相违背，轮询的方式又会耗费无谓的CPU资源，而且也不能及时地得到计算结果，为什么不能用观察者设计模式当计算结果完成及时通知监听者呢？

很多语言，比如Node.js，采用回调的方式实现异步编程。Java的一些框架，比如Netty，自己扩展了Java的 Future接口，提供了addListener等多个扩展方法：

```js
    ChannelFuture future = bootstrap.connect(new InetSocketAddress(host, port));
    future.addListener(new ChannelFutureListener(){
          @Override
               public void operationComplete(ChannelFuture future) throws Exception
               {
                   if (future.isSuccess()) {
                       // SUCCESS
                   }
                   else {
                       // FAILURE
                   }
               }
    });
```
Google guava也提供了通用的扩展Future:ListenableFuture、SettableFuture 以及辅助类Futures等,方便异步编程。

```java
    final String name = ...;
    inFlight.add(name);
    ListenableFuture<Result> future = service.query(name);
    future.addListener(new Runnable() {
      public void run() {
        processedCount.incrementAndGet();
        inFlight.remove(name);
        lastProcessed.set(name);
        logger.info("Done with {0}", name);
      }
    }, executor);
```
Scala也提供了简单易用且功能强大的Future/Promise异步编程模式。

作为正统的Java类库，是不是应该做点什么，加强一下自身库的功能呢？

在Java 8中, 新增加了一个包含50个方法左右的类: CompletableFuture，提供了非常强大的Future的扩展功能，可以帮助我们简化异步编程的复杂性，提供了函数式编程的能力，可以通过回调的方式处理计算结果，并且提供了转换和组合CompletableFuture的方法。

下面我们就看一看它的功能吧。

## <span id="CF1">主动完成计算