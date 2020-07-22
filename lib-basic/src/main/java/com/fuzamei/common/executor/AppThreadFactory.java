package com.fuzamei.common.executor;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zhengjy
 * @since 2018/12/18
 * Description:
 */
public final class AppThreadFactory extends AtomicInteger implements ThreadFactory {

    final String prefix;

    final int priority;

    public AppThreadFactory(String prefix) {
        this(prefix, Thread.NORM_PRIORITY);
    }

    public AppThreadFactory(String prefix, int priority) {
        this.prefix = prefix;
        this.priority = priority;
    }

    @Override
    public Thread newThread(Runnable r) {
        String name = prefix + '-' + incrementAndGet();
        Thread t = new Thread(r, name);
        t.setPriority(priority);
        t.setDaemon(true);
        return t;
    }

    @Override
    public String toString() {
        return "AppThreadFactory[" + prefix + "]";
    }
}

