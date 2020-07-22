package com.fuzamei.common.utils.task

/**
 * @author zhengjy
 * @since 2019/12/12
 * Description:
 */
abstract class Task {

    internal var mTaskManager: TaskManager? = null

    abstract fun work()

    fun done() {
        mTaskManager?.nextTask()
    }
}
