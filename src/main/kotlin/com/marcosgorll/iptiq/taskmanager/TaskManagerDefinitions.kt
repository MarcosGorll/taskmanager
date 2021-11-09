package com.marcosgorll.iptiq.taskmanager

import com.marcosgorll.iptiq.taskmanager.model.AbstractProcess
import com.marcosgorll.iptiq.taskmanager.model.Priority
import com.marcosgorll.iptiq.taskmanager.model.OrderDefinition
import java.time.Instant

/**
 * Defines the contract of a TaskManager
 */
interface TaskManager {

    /**
     * Ads a process to the task manager
     */
    fun addProcess(process: AbstractProcess)

    /**
     * List running processes. @see OrderDefinition
     */
    fun listRunningProcesses(sortBy: OrderDefinition) : List<AbstractProcess>

    /**
     * Kill a process by its PID
     * @throws ProcessNotFoundException if the PID does not exist in the task manager
     */
    fun killProcess(pid: String)

    /**
     * Kill 0 or more process by the priority level
     */
    fun killGroupProcesses(priority: Priority)

    /**
     * Kill all processes within the task manager
     */
    fun killAllProcesses()

}


/**
 *  Defines a process running inside some TaskManager.
 *  It adds the `time` the process was created
 */
class RunningProcess(val createdAt : Instant, private val process: AbstractProcess) : AbstractProcess {

    override fun getPid(): String {
        return process.getPid()
    }

    override fun getPriority(): Priority {
        return process.getPriority()
    }

    override fun kill() {
        process.kill()
    }

    override fun toString(): String {
        return "Process pid:${getPid()} with priority:${getPriority()} created at:$createdAt"
    }

}

/**
 *  Defines a read-only process, which cannot be killed by calling the kill method directly.
 */
class ReadonlyProcess(private val process: AbstractProcess) : AbstractProcess {

    override fun getPid(): String {
        return process.getPid()
    }

    override fun getPriority(): Priority {
        return process.getPriority()
    }

    override fun kill() {
        //protect against listing a process and killing it
        //directly by process.kill() instead of using some of hte
        //TaskManager.killXXX() possibilities. If this is allowed,
        //some processes would be killed, but keep inside the TaskManager
        //data structure.
        throw UnsupportedOperationException()
    }

    override fun toString(): String {
        return process.toString()
    }

}

/**
 * Defines a creation time instant provider. This helps on testing, as it will allow
 * the test to create a deterministic order for the processes added to the TaskManager
 */
interface CreationTimeProvider {

    /**
     * Returns the exact time of now
     */
    fun now() : Instant

}

/**
 * Default implementation for creation time provider. It uses #Instant diretly
 */
class DefaultCreationTimeProvider : CreationTimeProvider {
    override fun now() : Instant {
        return Instant.now()
    }
}



