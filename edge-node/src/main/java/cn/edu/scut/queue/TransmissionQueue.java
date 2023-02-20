package cn.edu.scut.queue;

import cn.edu.scut.bean.Task;
import cn.edu.scut.thread.TransmissionRunnable;
import cn.edu.scut.utils.SpringBeanUtils;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class TransmissionQueue {
    // transmission queue based on the queue in thread pool
    ThreadPoolExecutor thread = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());

    public void add(Task task) {
        TransmissionRunnable runnable = SpringBeanUtils.applicationContext.getBean(TransmissionRunnable.class);
        runnable.setTask(task);
        thread.execute(runnable);
    }
}
