package cn.edu.scut.service;

import cn.edu.scut.thread.UserRunnable;
import cn.edu.scut.utils.SpringBeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {

    @Value("${edgeComputing.timeSlot}")
    private int timeSlot;

    private ScheduledThreadPoolExecutor threadPoolExecutor = new ScheduledThreadPoolExecutor(1);

    public void start() {
        UserRunnable userRunnable = SpringBeanUtils.applicationContext.getBean(UserRunnable.class);
        threadPoolExecutor.scheduleAtFixedRate(userRunnable, 0, timeSlot, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        threadPoolExecutor.shutdown();
    }

    public void restart() {
        threadPoolExecutor = new ScheduledThreadPoolExecutor(1);
        UserRunnable userRunnable = SpringBeanUtils.applicationContext.getBean(UserRunnable.class);
        userRunnable.setTimeSlot(0);
        threadPoolExecutor.scheduleAtFixedRate(userRunnable, 0, timeSlot, TimeUnit.MILLISECONDS);
    }
}
