package com.fuzamei.common.executor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author zhengjy
 * @since 2018/12/18
 * Description:线程池工具类
 */
public class AppExecutors {

    /**
     * 在Android7.0及以上的华为手机（EmotionUI_5.0及以上）最大线程数限制为500，
     * 因此数据库操作的线程池不能太大，否则会报{@link OutOfMemoryError}错误
     *
     * @return  用于数据库操作的线程池
     */
    public static ExecutorService databaseThreadPool() {
        return DatabaseThreadPoolHolder.databaseThreadPool;
    }

    /**
     * 消息发送线程池
     *
     * @return  用于消息发送操作的线程池
     */
    public static ExecutorService messageThreadPool() {
        return MessageThreadPoolHolder.messageThreadPool;
    }

    private static class DatabaseThreadPoolHolder {
        private static ExecutorService
                databaseThreadPool = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),
                300, 30L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(),
                new AppThreadFactory("AppDatabaseThread", Thread.NORM_PRIORITY));
    }

    private static class MessageThreadPoolHolder {
        private static ExecutorService
                messageThreadPool = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),
                10, 20L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(),
                new AppThreadFactory("AppMessageThread", Thread.NORM_PRIORITY));
    }
}
