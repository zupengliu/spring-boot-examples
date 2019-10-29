package com.soil.completable.future.conf;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "soil.thread.pool")
@Data
public class CustomizeThreadPoolYaml {

    private int corePoolSize = 1;
    private int maxPoolSize = 2147483647;
    private int keepAliveSeconds = 60;
    private int queueCapacity = 2147483647;
}
