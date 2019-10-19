package com.soil.async;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;


@SpringBootApplication
@EnableAsync
public class AsyncApplication {

    public static void main(String[] args) {
        SpringApplication.run(AsyncApplication.class, args);
    }

}

/**
 * //https://www.cnblogs.com/fingerboy/p/9948736.html
 * //https://www.sczyh30.com/posts/Java/java-8-completable-future/#%E6%9E%84%E9%80%A0CompletableFuture%E5%AF%B9%E8%B1%A1
 */