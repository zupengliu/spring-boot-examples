package com.soil.completable.future.conf;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@Component
@Slf4j
public class CustomizeThreadPool {
    @Autowired
    private CustomizeThreadPoolYaml customizeThreadPoolYaml;
    /**
     * 1.这种形式的线程池配置是需要在使用的方法上面@Async("taskExecutor"),
     * 2.如果在使用的方法上面不加该注解那么spring就会使用默认的线程池
     * 3.所以如果加@Async注解但是不指定使用的线程池，又想自己定义线程池那么就可以重写spring默认的线程池
     * 4.所以第二个方法就是重写默认线程池
     * 注意：完全可以把线程池的参数写到配置文件中
     */
    @Bean("taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //核心线程池大小
        executor.setCorePoolSize(customizeThreadPoolYaml.getCorePoolSize());
        //最大线程数
        executor.setMaxPoolSize(customizeThreadPoolYaml.getMaxPoolSize());
        //队列容量
        executor.setQueueCapacity(customizeThreadPoolYaml.getQueueCapacity());
        //活跃时间
        executor.setKeepAliveSeconds(customizeThreadPoolYaml.getKeepAliveSeconds());
        //线程名字前缀
        executor.setThreadNamePrefix("CustomizeThreadPool-");

        // setRejectedExecutionHandler：当pool已经达到max size的时候，如何处理新任务
        // CallerRunsPolicy：不在新线程中执行任务，而是由调用者所在的线程来执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();
        log.info("**********初始化线程池**********");
        return executor;
    }
}
