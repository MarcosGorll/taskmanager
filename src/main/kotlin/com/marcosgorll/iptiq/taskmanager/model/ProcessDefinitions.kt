package com.marcosgorll.iptiq.taskmanager.model

import mu.KotlinLogging
import java.util.UUID

private val logger = KotlinLogging.logger {}

/**
 * Defines the contract of a process to the TaskManager
 */
interface AbstractProcess {

    /**
     * Unique ID of the process
     */
    fun getPid() : String

    /**
     * Priority of the process
     */
    fun getPriority() : Priority

    /**
     * Function called to terminate the process
     */
    fun kill()
}

/**
 * Default implementation of a process to the TaskManager
 */
class Process(private val priority: Priority) : AbstractProcess {

    //Honestly I'm not a big fan of having String UUIDs has UUIDs for processes
    //But using as long (like unix/linux/bsd/etc.) will require changing the class definition
    //to have the pid created by the task manager, not directly in the process - to avoid collision
    //so, for now I'll use a string uuid.
    private val pid : String = UUID.randomUUID().toString()

    override fun getPid(): String {
        return pid
    }

    override fun getPriority(): Priority {
        return priority
    }

    override fun kill() {
        logger.info { "Killing process $pid with priority $priority" }
    }

    override fun toString(): String {
        return "Process pid:${pid} with priority:${priority}"
    }
}