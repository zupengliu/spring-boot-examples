# springboot异步调用（spring-boot-async）
参考地址:https://blog.csdn.net/hry2015/article/details/67640534

## 1. TaskExecutor
Spring异步线程池的接口类，其实质是java.util.concurrent.Executor

Spring 已经实现的异常线程池：
1. SimpleAsyncTaskExecutor：不是真的线程池，这个类不重用线程，每次调用都会创建一个新的线程。
2. SyncTaskExecutor：这个类没有实现异步调用，只是一个同步操作。只适用于不需要多线程的地方
3. ConcurrentTaskExecutor：Executor的适配类，不推荐使用。如果ThreadPoolTaskExecutor不满足要求时，才用考虑使用这个类
4. SimpleThreadPoolTaskExecutor：是Quartz的SimpleThreadPool的类。线程池同时被quartz和非quartz使用，才需要使用此类
5. ThreadPoolTaskExecutor ：最常使用，推荐。 其实质是对java.util.concurrent.ThreadPoolExecutor的包装
## 2. @Async
spring对过@Async定义异步任务

异步的方法有3种
1. 最简单的异步调用，返回值为void
2. 带参数的异步调用 异步方法可以传入参数
3. 异常调用返回Future

## 3. 对异步方法的异常处理
在调用方法时，可能出现方法中抛出异常的情况。在异步中主要有有两种异常处理方法：
1. 对于方法返回值是Futrue的异步方法: 
    a) 一种是在调用future的get时捕获异常; 
    b) 在异常方法中直接捕获异常
2. 对于返回值是void的异步方法：通过自定义异常类实现AsyncUncaughtExceptionHandler处理异常
