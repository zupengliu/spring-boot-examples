package com.soil.completable.future.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DefaultAsyncService {

    public Integer cale(Integer para) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return para * para;
    }

    /**
     * 捕获异常
     * @param para
     * @return
     * @throws Exception
     */
    public Integer caleException(Integer para) {
        return para / 0;
    }
}
