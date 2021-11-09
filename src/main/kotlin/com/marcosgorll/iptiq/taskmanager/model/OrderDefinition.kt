package com.marcosgorll.iptiq.taskmanager.model

import com.marcosgorll.iptiq.taskmanager.RunningProcess

/**
 * Sorting helper to list processes on TaskManagers
 */
enum class OrderDefinition(val comparator: Comparator<RunningProcess>) {

    /**
     * Sorting by process PID ascending
     */
    BY_PID_ASC(compareBy<RunningProcess> { it.getPid() }),

    /**
     * Sorting by process PID descending
     */
    BY_PID_DESC(compareByDescending<RunningProcess> { it.getPid() }),

    /**
     * Sorting by process Creating Time ascending
     */
    BY_CREATION_TIME_ASC(compareBy<RunningProcess> { it.createdAt }),

    /**
     * Sorting by process Creating Time descending
     */
    BY_CREATION_TIME_DESC(compareByDescending<RunningProcess> { it.createdAt }),

    /**
     * Sorting by process Priority ascending
     */
    BY_PRIORITY_ASC(Comparator.comparingInt<RunningProcess> { it.getPriority().priorityAsInt }),

    /**
     * Sorting by process Priority ascending
     */
    BY_PRIORITY_DESC(Comparator.comparingInt<RunningProcess> { -it.getPriority().priorityAsInt })

}