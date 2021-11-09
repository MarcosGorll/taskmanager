package com.marcosgorll.iptiq.taskmanager

import com.marcosgorll.iptiq.taskmanager.model.AbstractProcess
import mu.KotlinLogging
import java.util.LinkedList
import kotlin.concurrent.write

private val logger = KotlinLogging.logger {}

/**
 * A First In First Out (FIFO) implementation of TaskManager
 */
class FifoTaskManager
    (capacity: Int, creationTimeProvider: CreationTimeProvider = DefaultCreationTimeProvider()) :
    DefaultTaskManager(capacity, LinkedList(), creationTimeProvider) {

    /**
     * Reimplements the add process to remove the oldest running process (FIFO) in the TaskManager
     * if the capacity limit is reached
     */
    override fun addProcess(process: AbstractProcess) {
        logger.info { "Adding process ${process.getPid()}" }
        lock.write {
            if (runningProcesses.size == capacity) {
                val oldestProcess = (runningProcesses as LinkedList).pop()
                logger.info { "Maximum capacity reached, removing process ${oldestProcess.getPid()}" }
                oldestProcess.kill()
            }
            runningProcesses.add(RunningProcess(creationTimeProvider.now(), process))
        }
    }

}