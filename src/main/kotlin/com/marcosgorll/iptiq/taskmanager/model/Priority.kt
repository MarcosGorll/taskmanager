package com.marcosgorll.iptiq.taskmanager.model

/**
 * Defines priority constants for processes
 */
enum class Priority(val priorityAsInt : Int) {

    /**
     * Low process priority
     */
    LOW(1),

    /**
     * Medium process priority
     */
    MEDIUM(5),

    /**
     * High process priority
     */
    HIGH(10)

}