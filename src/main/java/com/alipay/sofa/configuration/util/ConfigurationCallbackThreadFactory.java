package com.alipay.sofa.configuration.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

public class ConfigurationCallbackThreadFactory implements ThreadFactory {

    private final AtomicLong threadNumber = new AtomicLong(1);

    private final boolean daemon;

    private static final ThreadGroup THREAD_GROUP = new ThreadGroup("Sofa-Configuration");

    public static ThreadGroup getThreadGroup() {
        return THREAD_GROUP;
    }

    public static ThreadFactory create(boolean daemon) {
        return new ConfigurationCallbackThreadFactory(daemon);
    }

    private ConfigurationCallbackThreadFactory(boolean daemon) {
        this.daemon = daemon;
    }

    public Thread newThread(Runnable runnable) {
        Thread thread = new Thread(THREAD_GROUP, runnable,
                THREAD_GROUP.getName() + "-" + threadNumber.getAndIncrement());
        thread.setDaemon(daemon);
        if (thread.getPriority() != Thread.NORM_PRIORITY) {
            thread.setPriority(Thread.NORM_PRIORITY);
        }
        return thread;
    }
}
