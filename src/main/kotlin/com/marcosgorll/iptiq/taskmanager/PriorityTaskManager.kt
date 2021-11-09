package com.marcosgorll.iptiq.taskmanager

import com.marcosgorll.iptiq.taskmanager.model.AbstractProcess
import mu.KotlinLogging
import java.time.Instant
import java.util.PriorityQueue
import kotlin.Comparator
import kotlin.concurrent.write

private val logger = KotlinLogging.logger {}

/**
 * A Priority based Task Manager
 */
class PriorityTaskManager(capacity: Int, creationTimeProvider: CreationTimeProvider = DefaultCreationTimeProvider()) :
    DefaultTaskManager(capacity, PriorityQueue(priorityComparator), creationTimeProvider) {

    // static object to keep the comparator first based by the priority, then by creation time
    companion object {
        val priorityComparator = Comparator.comparingInt<RunningProcess> { it.getPriority().priorityAsInt }
            .then(compareBy<RunningProcess> { it.createdAt })
    }

    /**
     * Ads a process, if the Task Manager reached the maximum capacity, it will try to remove
     * a process which priority is lower than the new one. In case of multiple processes with
     * lower priority, the oldest one will be removed
     */
    override fun addProcess(process: AbstractProcess) {
        logger.info { "Adding process ${process.getPid()}" }
        lock.write {
            if (runningProcesses.size == capacity) {
                val queue = (runningProcesses as PriorityQueue<RunningProcess>)
                val lowestAndOldest = queue.remove()
                if (lowestAndOldest.getPriority().priorityAsInt < process.getPriority().priorityAsInt) {
                    logger.info { "Maximum capacity reached, removing process ${lowestAndOldest.getPid()}" }
                    lowestAndOldest.kill()
                    runningProcesses.add(RunningProcess(Instant.now(), process))
                } else {
                    logger.info { "No process with lower priority found, new process not being added" }
                    runningProcesses.add(lowestAndOldest)
                }
            } else {
                runningProcesses.add(RunningProcess(creationTimeProvider.now(), process))
            }
        }
    }
}