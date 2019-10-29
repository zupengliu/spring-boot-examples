package com.soil.completable.future.service;

import com.alibaba.fastjson.JSON;
import com.soil.completable.future.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

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
    public int caleException(Integer para) {
        return para / 0;
    }

    public Long save(User user){
        try {
            TimeUnit.MILLISECONDS.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        log.info("线程名称：{},save新增用户信息：{}", Thread.currentThread().getName(),JSON.toJSONString(user));
        return 123L;
    }

    /***
     * 获取用户信息
     * @return
     * @throws InterruptedException
     */
    public User getUserInfo(long id){
        try {
            TimeUnit.MILLISECONDS.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        log.info("查询用户信息，线程名称：{}", Thread.currentThread().getName());
        if(id==123L) {
            return User.builder().id(123L).userName("liuzp").age(20).sex(1).build();
        }else{
            return null;
        }
    }
}
