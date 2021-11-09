package com.marcosgorll.iptiq.taskmanager

import com.marcosgorll.iptiq.taskmanager.model.AbstractProcess
import com.marcosgorll.iptiq.taskmanager.model.OrderDefinition
import com.marcosgorll.iptiq.taskmanager.model.Priority
import mu.KotlinLogging
import java.util.LinkedList
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

private val logger = KotlinLogging.logger {}

/**
 * A simple task manager that allows #capacity process to run simultaneously
 * It is thread safe and can be used in parallel.
 *
 * Open to extension as base to more advanced TaskManagers
 */
open class DefaultTaskManager : TaskManager {
    // Protected to be accessible to sub-classes
    protected val capacity: Int

    // Protected to be extensible and used on the specialized classes
    protected val runningProcesses: MutableCollection<RunningProcess>

    // A read//write lock to allow all readers threads do safely read if there isn't
    // another thread writing (adding or killing processes)
    protected val lock: ReentrantReadWriteLock

    // Protected to be extensible and used on the specialized classes
    protected val creationTimeProvider: CreationTimeProvider

    /**
     * Creates a new DefaultTaskManager with the maximum capacity
     */
    constructor(capacity: Int, creationTimeProvider: CreationTimeProvider = DefaultCreationTimeProvider())
            : this(capacity, LinkedList<RunningProcess>(), creationTimeProvider)

    /**
     * Creates a new DefaultTaskManager with the maximum capacity and an implementation of
     * MutableCollection to keep this process
     */
    protected constructor(
        capacity: Int,
        runningProcess: MutableCollection<RunningProcess>,
        creationTimeProvider: CreationTimeProvider = DefaultCreationTimeProvider()
    ) {
        require(capacity > 0) {
            throw IllegalArgumentException("TaskManager Capacity has to be bigger than 0. It was $capacity")
        }
        this.capacity = capacity
        this.runningProcesses = runningProcess
        this.creationTimeProvider = creationTimeProvider
        this.lock = ReentrantReadWriteLock()
    }

    override fun addProcess(process: AbstractProcess) {
        logger.info { "Adding process ${process.getPid()}" }
        lock.write {
            ensureCapacity()
            runningProcesses.add(RunningProcess(creationTimeProvider.now(), process))
        }
    }

    override fun listRunningProcesses(sortBy: OrderDefinition): List<AbstractProcess> {
        logger.info { "Listing running processes process" }
        lock.read {
            // Returning a new list decorated with ReadonlyProcess to avoid the call of process.kill
            return runningProcesses.sortedWith(sortBy.comparator).map { ReadonlyProcess(it) }
        }
    }

    override fun killProcess(pid: String) {
        logger.info { "Killing process $pid" }
        lock.write {
            val process = runningProcesses.firstOrNull { it.getPid() == pid }
                ?: throw ProcessNotFoundException("Process $pid not found")
            process.kill()
            runningProcesses.remove(process)
        }
    }

    override fun killGroupProcesses(priority: Priority) {
        logger.info { "Killing all processes with priority $priority" }
        lock.write {
            val processes = runningProcesses.filter { it.getPriority() == priority }
            processes.forEach { it.kill() }
            runningProcesses.removeAll(processes)
        }
    }

    override fun killAllProcesses() {
        logger.info { "Killing all processes" }
        lock.write {
            runningProcesses.forEach { it.kill() }
            runningProcesses.clear()
        }
    }

    private fun ensureCapacity() {
        if (runningProcesses.size + 1 > capacity) {
            throw MaximumCapacityReachedException("Maximum capacity reached. Maximum capacity is $capacity")
        }
    }

}