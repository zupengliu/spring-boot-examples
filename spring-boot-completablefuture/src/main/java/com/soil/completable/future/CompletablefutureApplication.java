package com.soil.completable.future;

import com.soil.completable.future.conf.CustomizeThreadPoolYaml;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EnableConfigurationProperties({CustomizeThreadPoolYaml.class} ) // 开启配置属性支持
public class CompletablefutureApplication {

    public static void main(String[] args) {
        SpringApplication.run(CompletablefutureApplication.class, args);
    }

}
