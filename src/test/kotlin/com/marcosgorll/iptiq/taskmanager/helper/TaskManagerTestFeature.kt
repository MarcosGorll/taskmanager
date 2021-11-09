package com.marcosgorll.iptiq.taskmanager.helper

import com.marcosgorll.iptiq.taskmanager.CreationTimeProvider
import com.marcosgorll.iptiq.taskmanager.DefaultCreationTimeProvider
import com.marcosgorll.iptiq.taskmanager.DefaultTaskManager
import com.marcosgorll.iptiq.taskmanager.FifoTaskManager
import com.marcosgorll.iptiq.taskmanager.PriorityTaskManager
import com.marcosgorll.iptiq.taskmanager.model.AbstractProcess
import com.marcosgorll.iptiq.taskmanager.model.Priority
import com.marcosgorll.iptiq.taskmanager.model.Process
import java.time.Instant
import java.util.*

fun newDefaultTaskManager(
    capacity: Int,
    vararg priorities: Priority,
)
        : Pair<DefaultTaskManager, List<AbstractProcess>> {
    return newDefaultTaskManager(capacity, DefaultCreationTimeProvider(), *priorities)
}

fun newDefaultTaskManager(
    capacity: Int,
    creationTimeProvider: CreationTimeProvider,
    vararg priorities: Priority,
)
        : Pair<DefaultTaskManager, List<AbstractProcess>> {
    val taskManager = DefaultTaskManager(capacity, creationTimeProvider)
    val processes = createProcesses(*priorities)
    for (p in processes) {
        taskManager.addProcess(p)
    }
    return Pair(taskManager, processes)
}

fun newFifoTaskManager(
    capacity: Int,
    vararg priorities: Priority,
)
        : Pair<FifoTaskManager, List<AbstractProcess>> {
    return newFifoTaskManager(capacity, DefaultCreationTimeProvider(), *priorities)
}

fun newFifoTaskManager(
    capacity: Int,
    creationTimeProvider: CreationTimeProvider,
    vararg priorities: Priority,
)
        : Pair<FifoTaskManager, List<AbstractProcess>> {
    val taskManager = FifoTaskManager(capacity, creationTimeProvider)
    val processes = createProcesses(*priorities)
    for (p in processes) {
        taskManager.addProcess(p)
    }
    return Pair(taskManager, processes)
}

fun newPriorityTaskManager(
    capacity: Int,
    vararg priorities: Priority,
)
        : Pair<PriorityTaskManager, List<AbstractProcess>> {
    return newPriorityTaskManager(capacity, DefaultCreationTimeProvider(), *priorities)
}

fun newPriorityTaskManager(
    capacity: Int,
    creationTimeProvider: CreationTimeProvider,
    vararg priorities: Priority,
)
        : Pair<PriorityTaskManager, List<AbstractProcess>> {
    val taskManager = PriorityTaskManager(capacity, creationTimeProvider)
    val processes = createProcesses(*priorities)
    for (p in processes) {
        taskManager.addProcess(p)
    }
    return Pair(taskManager, processes)
}

private fun createProcesses(vararg priorities: Priority): List<AbstractProcess> {
    val processes = LinkedList<AbstractProcess>()
    for (priority in priorities) {
        val p = TestProcess(Process(priority))
        processes.add(p);
    }
    return processes
}

/**
 * Creates a sequential creation time provider allowing a deterministic sequence of time for tests
 */
class SequentialCreationTimeProvider : CreationTimeProvider {

    override fun now(): Instant {
        // Here we have DMC DeLorean
        return Instant.now().plusMillis(10);
    }

}

/**
 * Test process to verify if the process was killed, that's
 * if the #kill() method was called
 */
class TestProcess(private val p : Process) : AbstractProcess {

    private var wasKilled = false

    override fun getPid(): String {
        return p.getPid()
    }

    override fun getPriority(): Priority {
       return p.getPriority()
    }

    override fun kill() {
        wasKilled = true
    }

    fun wasKilled() : Boolean {
        return wasKilled
    }

}