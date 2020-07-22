package com.fuzamei.common.utils.task

import java.util.*

/**
 * @author zhengjy
 * @since 2019/12/12
 * Description:按顺序执行一系列任务
 */
class TaskManager {

    companion object {
        @JvmStatic
        fun create(): TaskManager {
            return TaskManager()
        }
    }

    private val taskQueue by lazy { LinkedList<Task>() }

    fun addTask(task: Task): TaskManager {
        task.mTaskManager = this
        taskQueue.add(task)
        return this
    }

    internal fun nextTask() {
        taskQueue.poll()?.apply {
            work()
        }
    }

    fun start() {
        nextTask()
    }
}