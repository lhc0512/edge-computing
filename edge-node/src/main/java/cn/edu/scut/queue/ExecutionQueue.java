package cn.edu.scut.queue;

import cn.edu.scut.bean.Task;
import cn.edu.scut.thread.ExecutionRunnable;
import cn.edu.scut.utils.SpringBeanUtils;
import lombok.Getter;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ExecutionQueue {
    // execution queue based on the queue in thread pool
    @Getter
    private ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());

    public void add(Task task) {
        ExecutionRunnable runnable = SpringBeanUtils.applicationContext.getBean(ExecutionRunnable.class);
        runnable.setTask(task);
        executor.execute(runnable);
    }
    public int getSize(){
        return executor.getQueue().size();
    }
}
