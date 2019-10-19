# java8 异步编程实战（一）

# 目录
[Future](#Future)

[CompletableFuture介绍](#CompletableFuture介绍)

[CompletableFuture特性](#CompletableFuture特性)

[计算结果完成时的处理](#计算结果完成时的处理)

[转换](#转换)

## Future
    JDK 5引入了Future模式。Future接口是Java多线程Future模式的实现，在java.util.concurrent包中，可以来进行异步计算。
    
    Future模式是多线程设计常用的一种设计模式。Future模式可以理解成：我有一个任务，提交给了Future，Future替我完成这个任务。
    期间我自己可以去做任何想做的事情。一段时间之后，我就便可以从Future那儿取出结果。
    Future的接口很简单，只有五个方法。
```java
public interface Future<V> {

    boolean cancel(boolean mayInterruptIfRunning);

    boolean isCancelled();

    boolean isDone();

    V get() throws InterruptedException, ExecutionException;

    V get(long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException;
}
```
Future接口的方法介绍如下：

* boolean cancel (boolean mayInterruptIfRunning) 取消任务的执行。参数指定是否立即中断任务执行，或者等等任务结束
* boolean isCancelled () 任务是否已经取消，任务正常完成前将其取消，则返回 true
* boolean isDone () 任务是否已经完成。需要注意的是如果任务正常终止、异常或取消，都将返回true
* V get () throws InterruptedException, ExecutionException 等待任务执行结束，然后获得V类型的结果。InterruptedException 线程被中断异常， ExecutionException任务执行异常，如果任务被取消，还会抛出CancellationException
* V get (long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException 同上面的get功能一样，多了设置超时时间。参数timeout指定超时时间，uint指定时间的单位，在枚举类TimeUnit中有相关的定义。如果计 算超时，将抛出TimeoutException

一般情况下，我们会结合Callable和Future一起使用，通过ExecutorService的submit方法执行Callable，并返回Future。

```java
    ExecutorService executor = Executors.newCachedThreadPool();
    //Lambda 是一个 callable， 提交后便立即执行，这里返回的是 FutureTask 实例
    Future<String> future = executor.submit(() -> { 
        System.out.println("running task");
        Thread.sleep(10000);
        return "return task";
    });

    try {
        Thread.sleep(1000);
    } catch (InterruptedException e) {
    }
    //前面的的 Callable 在其他线程中运行着，可以做一些其他的事情
    System.out.println("do something else");

    try {
        System.out.println(future.get());  //等待 future 的执行结果，执行完毕之后打印出来
    } catch (InterruptedException e) {
    } catch (ExecutionException e) {

    } finally {
        executor.shutdown();
    }
```
比起future.get()，其实更推荐使用get (long timeout, TimeUnit unit) 方法，设置了超时时间可以防止程序无限制的等待future的结果。

## CompletableFuture介绍

### 2.1 Future模式的缺点
* Future虽然可以实现获取异步执行结果的需求，但是它没有提供通知的机制，我们无法得知Future什么时候完成。

* 要么使用阻塞，在future.get()的地方等待future返回的结果，这时又变成同步操作。要么使用isDone()轮询地判断Future是否完成，这样会耗费CPU的资源。
### 2.2 CompletableFuture介绍
Netty、Guava分别扩展了Java 的 Future 接口，方便异步编程。

Java 8新增的CompletableFuture类正是吸收了所有Google Guava中ListenableFuture和SettableFuture的特征，还提供了其它强大的功能，让Java拥有了完整的非阻塞编程模型：Future、Promise 和 Callback(在Java8之前，只有无Callback 的Future)。

CompletableFuture能够将回调放到与任务不同的线程中执行，也能将回调作为继续执行的同步函数，在与任务相同的线程中执行。它避免了传统回调最大的问题，那就是能够将控制流分离到不同的事件处理器中。

CompletableFuture弥补了Future模式的缺点。在异步的任务完成后，需要用其结果继续操作时，无需等待。可以直接通过thenAccept、thenApply、thenCompose等方式将前面异步处理的结果交给另外一个异步事件处理线程来处理。

## CompletableFuture特性
### 3.1 创建CompletableFuture对象

```java
//使用ForkJoinPool.commonPool()作为它的线程池执行异步代码。
public static CompletableFuture<Void> runAsync(Runnable runnable)
//使用指定的thread pool执行异步代码。
public static CompletableFuture<Void> runAsync(Runnable runnable, Executor executor)
//使用ForkJoinPool.commonPool()作为它的线程池执行异步代码，异步操作有返回值
public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier)
//使用指定的thread pool执行异步代码，异步操作有返回值
public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier, Executor executor)
```
runAsync 和 supplyAsync 方法的区别是runAsync返回的CompletableFuture是没有返回值的。

```java
@Test
public void test02() throws InterruptedException {
    CompletableFuture<Void> cf = CompletableFuture.runAsync(() -> {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    });

    logger.info("isDone 1 " + cf.isDone());
    Thread.sleep(2000);
    logger.info("isDone 2 " + cf.isDone());
    logger.info("result {}", cf.join());
}
```
执行结果：
```java
17:11:43.143 [main] INFO completable_future.p3.CompletableFutureTest - isDone 1 false
17:11:45.150 [main] INFO completable_future.p3.CompletableFutureTest - isDone 2 true
17:11:45.150 [main] INFO completable_future.p3.CompletableFutureTest - result null
```
而supplyAsync返回的CompletableFuture是由返回值的，下面的代码打印了future的返回值。

```java
@Test
public void test02_1() throws InterruptedException {
    CompletableFuture<String> cf = CompletableFuture.supplyAsync(() -> {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "hello";
    });

    logger.info("isDone 1 " + cf.isDone());
    Thread.sleep(2000);
    logger.info("isDone 2 " + cf.isDone());
    logger.info("result {}", cf.join());
}
```
执行结果：
```java
17:11:52.902 [main] INFO completable_future.p3.CompletableFutureTest - isDone 1 false
17:11:54.907 [main] INFO completable_future.p3.CompletableFutureTest - isDone 2 true
17:11:54.907 [main] INFO completable_future.p3.CompletableFutureTest - result hello
```
### 3.2 Completable
|方法名|描述|
|---|---|
|complete(T t)	|完成异步执行，并返回future的结果|
|completeExceptionally(Throwable ex)	|异步执行不正常的结束|

future.get()在等待执行结果时，程序会一直block，如果此时调用complete(T t)会立即执行。
```java
        CompletableFuture<String> future  = CompletableFuture.supplyAsync(() -> "Hello");

        future.complete("World");

        try {
            System.out.println(future.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
```
执行结果：World

可以看到future调用complete(T t)会立即执行。但是complete(T t)只能调用一次，后续的重复调用会失效。
如果future已经执行完毕能够返回结果，此时再调用complete(T t)则会无效。
```java
    CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> "Hello");

    try {
        Thread.sleep(5000);
    } catch (InterruptedException e) {
        e.printStackTrace();
    }

    future.complete("World");

    try {
        System.out.println(future.get());
    } catch (InterruptedException e) {
        e.printStackTrace();
    } catch (ExecutionException e) {
        e.printStackTrace();
    }
```
执行结果： Hello

如果使用completeExceptionally(Throwable ex)则抛出一个异常，而不是一个成功的结果。
```java
CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> "Hello");

future.completeExceptionally(new Exception());

try {
    System.out.println(future.get());
} catch (InterruptedException e) {
    e.printStackTrace();
} catch (ExecutionException e) {
    e.printStackTrace();
}
```
执行结果：
```java
java.util.concurrent.ExecutionException: java.lang.Exception
...
```
## 计算结果完成时的处理
当CompletableFuture的计算结果完成，或者抛出异常的时候，我们可以执行特定的Action。主要是下面的方法：
```java
public CompletableFuture<T> whenComplete(BiConsumer<? super T, ? super Throwable> action)
public CompletableFuture<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> action)
public CompletableFuture<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> action, Executor executor)
public CompletableFuture<T> exceptionally(Function<Throwable, ? extends T> fn)
```
可以看到Action的类型是BiConsumer<? super T, ? super Throwable>，它可以处理正常的计算结果，或者异常情况。

方法不以Async结尾，意味着Action使用相同的线程执行，而Async可能会使用其他的线程去执行（如果使用相同的线程池，也可能会被同一个线程选中执行）。

注意这几个方法都会返回CompletableFuture，当Action执行完毕后它的结果返回原始的CompletableFuture的计算结果或者返回异常。

whenComplete方法的使用方式如下所示：
```java
@Test
public void test03() {
    CompletableFuture<String> cf = CompletableFuture.supplyAsync(() -> {
        logger.info("start");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "hello";
    });
    CompletableFuture<String> cf1 = cf.whenComplete((s, throwable) -> {
        if (throwable == null) {
            logger.info(s);
        }
    });

    logger.info(cf.join());
    logger.info(cf1.join());
}
```
执行结果:
```java
18:54:03.213 [ForkJoinPool.commonPool-worker-1] INFO completable_future.p3.CompletableFutureTest - start
18:54:04.220 [ForkJoinPool.commonPool-worker-1] INFO completable_future.p3.CompletableFutureTest - hello
18:54:04.220 [main] INFO completable_future.p3.CompletableFutureTest - hello
18:54:04.220 [main] INFO completable_future.p3.CompletableFutureTest - hello
```
可以看到，正常情况下whenComplete返回supplyAsync执行的结果。

如果执行过程中抛出异常，whenComplete也可以接收到异常然后处理：
```java
@Test
public void test03_1() {
    CompletableFuture<String> cf = CompletableFuture.supplyAsync(() -> {
        logger.info("start");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (true) {
            throw new RuntimeException("exception");
        }
        return "hello";
    });
    cf.whenComplete((s, throwable) -> {
        if (throwable == null) {
            logger.info(s);
        } else {
            logger.error(throwable.getMessage());
        }
    });

    while (!cf.isDone()) {}
}
```
执行结果如下：
```java
18:15:30.632 [ForkJoinPool.commonPool-worker-1] INFO completable_future.p3.CompletableFutureTest - start
18:15:31.635 [ForkJoinPool.commonPool-worker-1] ERROR completable_future.p3.CompletableFutureTest - java.lang.RuntimeException: exception
```
exceptionally方法返回一个新的CompletableFuture，当原始的CompletableFuture抛出异常的时候，就会触发这个CompletableFuture的计算，调用function计算值，否则如果原始的CompletableFuture正常计算完后，这个新的CompletableFuture也计算完成，它的值和原始的CompletableFuture的计算的值相同。也就是这个exceptionally方法用来处理异常的情况。

exceptionally方法的使用方式如下所示：
```java
@Test
public void test05() {
    CompletableFuture<String> cf = CompletableFuture.supplyAsync(() -> {
        logger.info("start");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (true) {
            throw new RuntimeException("exception");
        }
        return "hello";
    });
    cf.whenComplete((s, throwable) -> {
        if (throwable == null) {
            logger.info(s);
        } else {
            logger.error(throwable.getMessage());
        }
    });
    CompletableFuture<String> cf1 = cf.exceptionally(throwable -> {
        logger.error(throwable.getMessage());
        return "exception happened";
    });

    logger.info(cf1.join());
}
```
执行结果如下:
```java
18:38:31.461 [ForkJoinPool.commonPool-worker-1] INFO completable_future.p3.CompletableFutureTest - start
18:38:32.467 [ForkJoinPool.commonPool-worker-1] ERROR completable_future.p3.CompletableFutureTest - java.lang.RuntimeException: exception
18:38:32.467 [ForkJoinPool.commonPool-worker-1] ERROR completable_future.p3.CompletableFutureTest - java.lang.RuntimeException: exception
18:38:32.467 [main] INFO completable_future.p3.CompletableFutureTest - exception happened
```
可以看到，当执行过程抛出异常时，会触发exceptionally的执行，并返回exceptionally的返回值。

如果执行过程中没有抛出异常：
```java
@Test
public void test05() {
    CompletableFuture<String> cf = CompletableFuture.supplyAsync(() -> {
        logger.info("start");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "hello";
    });
    cf.whenComplete((s, throwable) -> {
        if (throwable == null) {
            logger.info(s);
        } else {
            logger.error(throwable.getMessage());
        }
    });
    CompletableFuture<String> cf1 = cf.exceptionally(throwable -> {
        logger.error(throwable.getMessage());
        return "exception happened";
    });

    logger.info(cf1.join());
}
```
执行结果：
```java
18:42:55.469 [ForkJoinPool.commonPool-worker-1] INFO completable_future.p3.CompletableFutureTest - start
18:42:56.476 [ForkJoinPool.commonPool-worker-1] INFO completable_future.p3.CompletableFutureTest - hello
18:42:56.476 [main] INFO completable_future.p3.CompletableFutureTest - hello
```
可以看到，如果执行过程中没有抛出异常exceptionally不会触发，它返回的值就是supplyAsync执行返回的原始值。

下面一组方法虽然也返回CompletableFuture对象，但是对象的值和原来的CompletableFuture计算的值不同。当原先的CompletableFuture的值计算完成或者抛出异常的时候，会触发这个CompletableFuture对象的计算，结果由BiFunction参数计算而得。因此这组方法兼有whenComplete和转换的两个功能。

```java

public <U> CompletableFuture<U> handle(BiFunction<? super T, Throwable, ? extends U> fn)
public <U> CompletableFuture<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> fn)
public <U> CompletableFuture<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> fn, Executor executor)

```
同样，不以Async结尾的方法由原来的线程计算，以Async结尾的方法由默认的线程池ForkJoinPool.commonPool()或者指定的线程池executor运行。

handle方法的使用方式如下所示
```java
@Test
public void test06() {
    CompletableFuture<String> cf = CompletableFuture.supplyAsync(() -> {
        logger.info("start");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "hello";
    });
    CompletableFuture<String> cf1 = cf.handle((s, throwable) -> {
        if (throwable == null) {
            logger.info(s);
            return s + " world";
        } else {
            logger.error(throwable.getMessage());
            return "exception happened";
        }

    });

    logger.info(cf1.join());
}
```
执行结果如下：
```java
18:47:03.524 [ForkJoinPool.commonPool-worker-1] INFO completable_future.p3.CompletableFutureTest - start
18:47:04.529 [ForkJoinPool.commonPool-worker-1] INFO completable_future.p3.CompletableFutureTest - hello
18:47:04.530 [main] INFO completable_future.p3.CompletableFutureTest - hello world
```
如果执行过程抛出异常：
```java
@Test
public void test06() {
    CompletableFuture<String> cf = CompletableFuture.supplyAsync(() -> {
        logger.info("start");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (true) {
            throw new RuntimeException("exception");
        }
        return "hello";
    });
    CompletableFuture<String> cf1 = cf.handle((s, throwable) -> {
        if (throwable == null) {
            logger.info(s);
            return s + " world";
        } else {
            logger.error(throwable.getMessage());
            return "exception happened";
        }

    });

    logger.info(cf1.join());
}
```
执行结果如下：
```java
18:48:25.769 [ForkJoinPool.commonPool-worker-1] INFO completable_future.p3.CompletableFutureTest - start
18:48:26.775 [ForkJoinPool.commonPool-worker-1] ERROR completable_future.p3.CompletableFutureTest - java.lang.RuntimeException: exception
18:48:26.776 [main] INFO completable_future.p3.CompletableFutureTest - exception happened
```
可以看到，handle方法接收执行结果和异常，处理之后返回新的结果。

## 转换
CompletableFuture可以作为monad和functor。由于回调风格的实现，我们不必因为等待一个计算完成而阻塞着调用线程，而是告诉CompletableFuture当计算完成的时候请执行某个function。而且我们还可以将这些操作串联起来，或者将CompletableFunction组合起来。

```java
public <U> CompletableFuture<U> thenApply(Function<? super T,? extends U> fn)
public <U> CompletableFuture<U> thenApplyAsync(Function<? super T,? extends U> fn)
public <U> CompletableFuture<U> thenApplyAsync(Function<? super T,? extends U> fn, Executor executor)
```

这一组函数的功能是当原来的CompletableFuture计算完后，将结果传递给函数fn，将fn的结果作为新的CompletableFuture计算果。

因此它的功能相当于将CompletableFuture<T>转换成CompletableFuture<U>。
需要注意的是，这些转换并不是马上执行的，也不会阻塞，而是在前一个stage完成后继续执行。
它们与handle方法的区别在于handle方法会处理正常计算值和异常，因此它可以屏蔽异常，避免异常继续抛出。而thenApply方法只是用来处理正常值，因此一旦有异常就会抛出。
thenApply方法的使用方式如下：
```java
@Test
public void test07() {
    CompletableFuture<String> cf = CompletableFuture.supplyAsync(() -> {
        logger.info("start");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "hello";
    });
    CompletableFuture<String> cf1 = cf.thenApply(new Function<String, String>() {
        @Override
        public String apply(String s) {
            logger.info(s);
            return s + " world";
        }
    });

    logger.info(cf1.join());
}
```
执行结果如下：
```java
20:22:24.537 [ForkJoinPool.commonPool-worker-1] INFO completable_future.p3.CompletableFutureTest - start
20:22:25.542 [ForkJoinPool.commonPool-worker-1] INFO completable_future.p3.CompletableFutureTest - hello
20:22:25.543 [main] INFO completable_future.p3.CompletableFutureTest - hello world
```
## 纯消费(执行Action)
上面的方法是当计算完成的时候，会生成新的计算结果（thenApply, handle），或者返回同样的计算结果whenComplete。CompletableFuture还提供了一种处理结果的方法，只对结果执行Action，而不返回新的计算值。因此计算值为Void：
```java
public CompletableFuture<Void> thenAccept(Consumer<? super T> action)
public CompletableFuture<Void> thenAcceptAsync(Consumer<? super T> action)
public CompletableFuture<Void> thenAcceptAsync(Consumer<? super T> action, Executor executor)
```
看它的参数类型也就明白了，它们是函数式接口Consumer，这个接口只有输入，没有返回值。

thenAccept方法的使用方式如下：
```java
@Test
public void test08() {
    CompletableFuture<String> cf = CompletableFuture.supplyAsync(() -> {
        logger.info("start");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "hello";
    });
    cf.thenAccept(new Consumer<String>() {
        @Override
        public void accept(String s) {
            logger.info(s);
        }
    });

    cf.join();
}
```
执行结果如下:
```java
20:28:30.580 [ForkJoinPool.commonPool-worker-1] INFO completable_future.p3.CompletableFutureTest - start
20:28:31.588 [ForkJoinPool.commonPool-worker-1] INFO completable_future.p3.CompletableFutureTest - hello

```
## thenAcceptBoth
```java
public <U> CompletableFuture<Void> thenAcceptBoth(CompletionStage<? extends U> other, BiConsumer<? super T, ? super U> action)
public <U> CompletableFuture<Void> thenAcceptBothAsync(CompletionStage<? extends U> other, BiConsumer<? super T, ? super U> action)
public <U> CompletableFuture<Void> thenAcceptBothAsync(CompletionStage<? extends U> other, BiConsumer<? super T, ? super U> action, Executor executor)

```
thenAcceptBoth接收另一个CompletionStage和action，当两个CompletionStage都正常完成计算后，就会执行提供的action，它用来组合另外一个异步的结果。

thenAcceptBoth方法的使用方式如下：
```java
@Test
public void test09() {
    CompletableFuture<String> cf = CompletableFuture.supplyAsync(() -> {
        logger.info("start");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "hello";
    });
    CompletableFuture<String> cf1 = CompletableFuture.supplyAsync(() -> {
        logger.info("start");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "world";
    });

    cf.thenAcceptBoth(cf1, (s, s2) -> {
        logger.info(s + " " + s2);
    }).join();
}
```
执行结果如下：
```java
20:31:40.335 [ForkJoinPool.commonPool-worker-2] INFO completable_future.p3.CompletableFutureTest - start
20:31:40.335 [ForkJoinPool.commonPool-worker-1] INFO completable_future.p3.CompletableFutureTest - start
20:31:42.344 [ForkJoinPool.commonPool-worker-2] INFO completable_future.p3.CompletableFutureTest - hello world
```
## runAfterBoth

```java
public CompletableFuture<Void> runAfterBoth(CompletionStage<?> other, Runnable action)
public CompletableFuture<Void> runAfterBothAsync(CompletionStage<?> other, Runnable action)
public CompletableFuture<Void> runAfterBothAsync(CompletionStage<?> other, Runnable action, Executor executor)

```
runAfterBoth是当两个CompletionStage都正常完成计算的时候，执行一个Runnable，这个Runnable并不使用计算的结果。

runAfterBoth方法的使用方式如下：
```java
@Test
public void test10() {
    CompletableFuture<String> cf = CompletableFuture.supplyAsync(() -> {
        logger.info("start");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "hello";
    });
    CompletableFuture<String> cf1 = CompletableFuture.supplyAsync(() -> {
        logger.info("start");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "world";
    });

    cf.runAfterBoth(cf1, () -> {
        logger.info("end");
    }).join();
}
```
执行结果如下：
```java
20:34:07.085 [ForkJoinPool.commonPool-worker-2] INFO completable_future.p3.CompletableFutureTest - start
20:34:07.085 [ForkJoinPool.commonPool-worker-1] INFO completable_future.p3.CompletableFutureTest - start
20:34:09.094 [ForkJoinPool.commonPool-worker-2] INFO completable_future.p3.CompletableFutureTest - end

```
## thenRun
```java
public CompletableFuture<Void> thenRun(Runnable action)
public CompletableFuture<Void> thenRunAsync(Runnable action)
public CompletableFuture<Void> thenRunAsync(Runnable action, Executor executor)

```

thenRun当计算完成的时候会执行一个Runnable，与thenAccept不同，Runnable并不使用CompletableFuture计算的结果。

因此先前的CompletableFuture计算的结果被忽略，返回Completable<Void>类型的对象。

thenRun方法的使用方式如下：
```java
@Test
public void test11() {
    CompletableFuture<String> cf = CompletableFuture.supplyAsync(() -> {
        logger.info("start");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "hello";
    });

    cf.thenRun(() -> logger.info("end")).join();
}
```
执行结果如下：
```java
20:36:05.833 [ForkJoinPool.commonPool-worker-1] INFO completable_future.p3.CompletableFutureTest - start
20:36:06.843 [ForkJoinPool.commonPool-worker-1] INFO completable_future.p3.CompletableFutureTest - end
```
## 组合

```java
public <U> CompletableFuture<U> thenCompose(Function<? super T, ? extends CompletionStage<U>> fn)
public <U> CompletableFuture<U> thenComposeAsync(Function<? super T, ? extends CompletionStage<U>> fn)
public <U> CompletableFuture<U> thenComposeAsync(Function<? super T, ? extends CompletionStage<U>> fn, Executor executor)

```
这一组方法接受一个Function作为参数，这个Function的输入是当前的CompletableFuture的计算值，返回结果将是一个新的CompletableFuture，这个新的CompletableFuture会组合原来的CompletableFuture和函数返回的CompletableFuture。因此它的功能类似于： **A +--> B +---> C**

thenCompose返回的对象并不一定是函数fn返回的对象，如果原来的CompletableFuture还没有计算出来，它就会生成一个新的组合后的CompletableFuture。

thenCompose方法的使用方式如下：
```java
@Test
public void test12() {
    CompletableFuture<String> cf = CompletableFuture.supplyAsync(() -> {
        logger.info("start");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "hello";
    });
    CompletableFuture<String> cf2 = cf.thenCompose(new Function<String, CompletionStage<String>>() {
        @Override
        public CompletionStage<String> apply(String s) {
            return CompletableFuture.supplyAsync(() -> {
                logger.info(s);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return s + " world";
            });
        }
    });
    logger.info(cf2.join());
}
```
执行结果如下:
```java
20:41:39.242 [ForkJoinPool.commonPool-worker-1] INFO completable_future.p3.CompletableFutureTest - start
20:41:40.253 [ForkJoinPool.commonPool-worker-1] INFO completable_future.p3.CompletableFutureTest - hello
20:41:42.255 [main] INFO completable_future.p3.CompletableFutureTest - hello world
```

## thenCombine

```java
public <U,V> CompletableFuture<V> thenCombine(CompletionStage<? extends U> other, BiFunction<? super T,? super U,? extends V> fn)
public <U,V> CompletableFuture<V> thenCombineAsync(CompletionStage<? extends U> other, BiFunction<? super T,? super U,? extends V> fn)
public <U,V> CompletableFuture<V> thenCombineAsync(CompletionStage<? extends U> other, BiFunction<? super T,? super U,? extends V> fn, Executor executor)

```
thenCombine用来复合另外一个CompletionStage的结果，它的功能类似：
```java
A +
  |
  +------> C
  +------^
B +
```
两个CompletionStage是并行执行的，他们之间没有先后依赖顺序，other并不会等待先前的CompletableFuture执行完毕后再执行。

从功能上来讲，它们的功能更类似thenAcceptBoth，只不过thenAcceptBoth是纯消费，它的函数参数没有返回值，而thenCombine的函数参数fn有返回值。

thenCombine方法的使用方式如下：

```java
@Test
public void test13() {
    CompletableFuture<String> cf = CompletableFuture.supplyAsync(() -> {
        logger.info("start");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "hello";
    });
    CompletableFuture<String> cf1 = CompletableFuture.supplyAsync(() -> {
        logger.info("start");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "world";
    });

    CompletableFuture<String> cf2 = cf.thenCombine(cf1, (s, s2) -> s + " " + s2);
    logger.info(cf2.join());
}
```
执行结果如下：
```java
20:45:01.018 [ForkJoinPool.commonPool-worker-1] INFO completable_future.p3.CompletableFutureTest - start
20:45:01.018 [ForkJoinPool.commonPool-worker-2] INFO completable_future.p3.CompletableFutureTest - start
20:45:03.028 [main] INFO completable_future.p3.CompletableFutureTest - hello world
```
## AcceptEither 和 applyToEither
thenAcceptBoth和runAfterBoth是当两个CompletableFuture都计算完成，而下面的方法是当任意一个CompletableFuture计算完成的时候就会执行。
```java
public CompletableFuture<Void> acceptEither(CompletionStage<? extends T> other, Consumer<? super T> action)
public CompletableFuture<Void> acceptEitherAsync(CompletionStage<? extends T> other, Consumer<? super T> action)
public CompletableFuture<Void> acceptEitherAsync(CompletionStage<? extends T> other, Consumer<? super T> action, Executor executor)

public <U> CompletableFuture<U> applyToEither(CompletionStage<? extends T> other, Function<? super T, U> fn)
public <U> CompletableFuture<U> applyToEitherAsync(CompletionStage<? extends T> other, Function<? super T, U> fn)
public <U> CompletableFuture<U> applyToEitherAsync(CompletionStage<? extends T> other, Function<? super T, U> fn, Executor executor)

```
acceptEither方法是当任意一个CompletionStage完成的时候，action这个消费者就会被执行。这个方法返回CompletableFuture<Void>

applyToEither方法是当任意一个CompletionStage完成的时候，fn会被执行，它的返回值会当做新的CompletableFuture<U>的计算结果


acceptEither方法的使用方式如下：
```java
@Test
public void test14() {
    CompletableFuture<String> cf = CompletableFuture.supplyAsync(() -> {
        logger.info("start");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "hello";
    });
    CompletableFuture<String> cf1 = CompletableFuture.supplyAsync(() -> {
        logger.info("start");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "world";
    });

    cf.acceptEither(cf1, s -> logger.info(s)).join();
}
```
执行结果如下：
```java
21:21:36.031 [ForkJoinPool.commonPool-worker-1] INFO completable_future.p3.CompletableFutureTest - start
21:21:36.031 [ForkJoinPool.commonPool-worker-2] INFO completable_future.p3.CompletableFutureTest - start
21:21:37.039 [ForkJoinPool.commonPool-worker-1] INFO completable_future.p3.CompletableFutureTest - hello

```
可以看到，当cf执行完毕后，acceptEither方法就被触发执行了

applyToEither方法的使用方式如下：

```java
@Test
public void test15() {
    CompletableFuture<String> cf = CompletableFuture.supplyAsync(() -> {
        logger.info("start");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "hello";
    });
    CompletableFuture<String> cf1 = CompletableFuture.supplyAsync(() -> {
        logger.info("start");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "world";
    });

    CompletableFuture<String> cf2 = cf.applyToEither(cf1, s -> {
        logger.info(s);
        return s + " end";
    });
    logger.info(cf2.join());
}
```
执行结果如下：
```java
21:25:43.441 [ForkJoinPool.commonPool-worker-1] INFO completable_future.p3.CompletableFutureTest - start
21:25:43.441 [ForkJoinPool.commonPool-worker-2] INFO completable_future.p3.CompletableFutureTest - start
21:25:44.447 [ForkJoinPool.commonPool-worker-1] INFO completable_future.p3.CompletableFutureTest - hello
21:25:44.448 [main] INFO completable_future.p3.CompletableFutureTest - hello end
```

allOf 和 anyOf
```java
public static CompletableFuture<Void> allOf(CompletableFuture<?>... cfs)
public static CompletableFuture<Object> anyOf(CompletableFuture<?>... cfs)

```
allOf方法是当所有的CompletableFuture都执行完后执行计算

anyOf方法是当任意一个CompletableFuture执行完后就会执行计算，计算的结果相同

anyOf和applyToEither不同，anyOf接受任意多的CompletableFuture但是applyToEither只是判断两个CompletableFuture。
anyOf返回值的计算结果是参数中其中一个CompletableFuture的计算结果，applyToEither返回值的计算结果却是要经过fn处理的。当然还有静态方法的区别，线程池的选择等。

### allOf方法的使用方式如下：
```java
@Test
public void test16() {
    CompletableFuture<String> cf = CompletableFuture.supplyAsync(() -> {
        logger.info("start");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "hello";
    });
    CompletableFuture<String> cf1 = CompletableFuture.supplyAsync(() -> {
        logger.info("start");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "world";
    });

    CompletableFuture.allOf(cf, cf1).whenComplete((v, throwable) -> {
        List<String> list = new ArrayList<>();
        list.add(cf.join());
        list.add(cf1.join());
        logger.info("result {}", list);
    }).join();
}
```
执行结果如下：

```java
21:36:36.938 [ForkJoinPool.commonPool-worker-1] INFO completable_future.p3.CompletableFutureTest - start
21:36:36.938 [ForkJoinPool.commonPool-worker-2] INFO completable_future.p3.CompletableFutureTest - start
21:36:38.951 [ForkJoinPool.commonPool-worker-2] INFO completable_future.p3.CompletableFutureTest - result [hello, world]

```
### anyOf方法的使用方式如下：
```java
@Test
public void test17() {
    CompletableFuture<String> cf = CompletableFuture.supplyAsync(() -> {
        logger.info("start");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "hello";
    });
    CompletableFuture<String> cf1 = CompletableFuture.supplyAsync(() -> {
        logger.info("start");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "world";
    });

    CompletableFuture<Object> cf2 = CompletableFuture.anyOf(cf, cf1).whenComplete((o, throwable) -> logger.info("result {}", o));
    logger.info("result {}", cf2.join());
}
```
执行结果如下：
```java
21:43:12.562 [ForkJoinPool.commonPool-worker-2] INFO completable_future.p3.CompletableFutureTest - start
21:43:12.562 [ForkJoinPool.commonPool-worker-1] INFO completable_future.p3.CompletableFutureTest - start
21:43:13.573 [ForkJoinPool.commonPool-worker-1] INFO completable_future.p3.CompletableFutureTest - result hello
21:43:13.576 [main] INFO completable_future.p3.CompletableFutureTest - result hello
```
