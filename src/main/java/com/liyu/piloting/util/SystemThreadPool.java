package com.liyu.piloting.util;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class SystemThreadPool {
    private ThreadPoolExecutor threadPoolExecutor;

    public SystemThreadPool(ThreadPoolExecutor threadPoolExecutor) {
        this.threadPoolExecutor = threadPoolExecutor;
    }

    public static <T> FutureTask<T> doExecute(Callable<T> callable) {
        FutureTask<T> task = new FutureTask<T>(callable);
        getInstance().threadPoolExecutor.execute(task);
        return task;
    }

    public static void doExecute(Runnable r) {getInstance().threadPoolExecutor.execute(r);}

    private static SystemThreadPool getInstance() {return InstanceHolder.systemThreadPool;}

    private static class InstanceHolder {
        private static final int MAX_THREAD_SIZE = 100;
        private static final int CORE_THREAD_SIZE = 5;
        private static final long KEEP_ALIVE_TIME = 2l;
        private static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;
        private static final LinkedBlockingQueue WORK_QUEUE = new LinkedBlockingQueue(50);
        private static final ThreadFactory threadFactory = new ThreadFactory() {
            private final AtomicInteger serialNumber = new AtomicInteger();

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "SystemThreadPool: " + serialNumber.getAndIncrement());
            }
        };


        static SystemThreadPool systemThreadPool
                = new SystemThreadPool(new ThreadPoolExecutor(CORE_THREAD_SIZE, MAX_THREAD_SIZE, KEEP_ALIVE_TIME, TIME_UNIT, WORK_QUEUE, threadFactory));
    }
}
