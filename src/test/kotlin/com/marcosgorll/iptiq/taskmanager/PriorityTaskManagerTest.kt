package com.marcosgorll.iptiq.taskmanager

import com.marcosgorll.iptiq.taskmanager.helper.SequentialCreationTimeProvider
import com.marcosgorll.iptiq.taskmanager.helper.TestProcess
import com.marcosgorll.iptiq.taskmanager.helper.newDefaultTaskManager
import com.marcosgorll.iptiq.taskmanager.helper.newFifoTaskManager
import com.marcosgorll.iptiq.taskmanager.helper.newPriorityTaskManager
import com.marcosgorll.iptiq.taskmanager.model.OrderDefinition
import com.marcosgorll.iptiq.taskmanager.model.Priority
import com.marcosgorll.iptiq.taskmanager.model.Process
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class PriorityTaskManagerTest {

    @Test
    fun `when Creating PriorityTaskManager given Capacity 0 then throw IllegalArgumentException`() {
        val exception = Assertions.assertThrows(IllegalArgumentException::class.java) {
            newPriorityTaskManager(0)
        }
        Assertions.assertEquals("TaskManager Capacity has to be bigger than 0. It was 0", exception.message)
    }

    @Test
    fun `when listing Process given Trying to kill the Process then throw UnsupportedOperationException`() {
        val (taskManager, _) = newDefaultTaskManager(1, Priority.LOW)
        val processes = taskManager.listRunningProcesses(OrderDefinition.BY_PID_ASC)
        Assertions.assertThrows(UnsupportedOperationException::class.java) {
            processes[0].kill()
        }
    }

    @Test
    fun `when adding Process Higher Priority given Reaching Maximum Capacity then Add New Process and Remove Old`() {
        val (taskManager, _) = newPriorityTaskManager(1, Priority.LOW)
        val process = Process(Priority.HIGH)
        taskManager.addProcess(process)
        val processes = taskManager.listRunningProcesses(OrderDefinition.BY_PID_ASC)
        Assertions.assertEquals(1, processes.size)
        Assertions.assertEquals(process.getPid(), processes[0].getPid())
    }

    @Test
    fun `when adding Process Lower Priority given Reaching Maximum Capacity then Not Add Process`() {
        val (taskManager, _) = newPriorityTaskManager(1, Priority.HIGH)
        val process = Process(Priority.MEDIUM)
        taskManager.addProcess(process)
        val processesAfterAdd = taskManager.listRunningProcesses(OrderDefinition.BY_PID_ASC)
        Assertions.assertEquals(1, processesAfterAdd.size)
        Assertions.assertEquals(0, processesAfterAdd.count { it.getPid() == process.getPid() })
    }

    @Test
    fun `when adding Process Higher Priority given Reaching Maximum Capacity then Add New Process and Remove Oldest`() {
        val (taskManager, _) = newPriorityTaskManager(2, SequentialCreationTimeProvider(), Priority.LOW, Priority.LOW)
        val processesBeforeAdd = taskManager.listRunningProcesses(OrderDefinition.BY_CREATION_TIME_ASC)
        val process = Process(Priority.HIGH)
        taskManager.addProcess(process)
        val processes = taskManager.listRunningProcesses(OrderDefinition.BY_PID_ASC)
        Assertions.assertEquals(2, processes.size)
        Assertions.assertEquals(1, processes.count { it.getPid() == process.getPid() })
        Assertions.assertEquals(0, processes.count { it.getPid() == processesBeforeAdd[0].getPid() })
        Assertions.assertEquals(1, processes.count { it.getPid() == processesBeforeAdd[1].getPid() })
    }

    @Test
    fun `when adding Process Same Priority given Reaching Maximum Capacity then Not Add Process`() {
        val (taskManager, _) = newPriorityTaskManager(1, Priority.HIGH)
        val process = Process(Priority.HIGH)
        val processesBeforeAdd = taskManager.listRunningProcesses(OrderDefinition.BY_PID_ASC)
        taskManager.addProcess(process)
        val processesAfterAdd = taskManager.listRunningProcesses(OrderDefinition.BY_PID_ASC)
        Assertions.assertEquals(1, processesAfterAdd.size)
        Assertions.assertEquals(processesBeforeAdd[0].getPid(), processesBeforeAdd[0].getPid())
    }

    @Test
    fun `when adding Process given Reaching Enough Capacity then Add Process`() {
        val (taskManager, _) = newPriorityTaskManager(2, Priority.LOW)
        val process = Process(Priority.HIGH)
        taskManager.addProcess(process)
        val processes = taskManager.listRunningProcesses(OrderDefinition.BY_PID_ASC)
        Assertions.assertEquals(2, processes.size)
        Assertions.assertEquals(1, processes.count { it.getPid() == process.getPid() })
    }

    @Test
    fun `when list Process by PID ASC given List of Processes then list Return List with Process by PID ASC`() {
        val (taskManager, processes) = newPriorityTaskManager(5,
            Priority.HIGH, Priority.HIGH, Priority.HIGH, Priority.MEDIUM, Priority.LOW)
        Assertions.assertEquals(
            processes.sortedBy { it.getPid() }.map { it.getPid() }.toList(),
            taskManager.listRunningProcesses(OrderDefinition.BY_PID_ASC).map { it.getPid() }.toList()
        )
    }

    @Test
    fun `when list Process by PID DESC given List of Processes then list Return List with Process by PID DESC`() {
        val (taskManager, processes) = newPriorityTaskManager(5,
            Priority.LOW, Priority.LOW, Priority.LOW, Priority.HIGH, Priority.MEDIUM)
        Assertions.assertEquals(
            processes.sortedByDescending { it.getPid() }.map { it.getPid() }.toList(),
            taskManager.listRunningProcesses(OrderDefinition.BY_PID_DESC).map { it.getPid() }.toList()
        )
    }

    @Test
    fun `when list Process by PRIORITY ASC given List of Processes then list Return List with Process by PRIORITY ASC`() {
        val (taskManager, processes) = newPriorityTaskManager(3,
            Priority.HIGH, Priority.MEDIUM, Priority.LOW)
        Assertions.assertEquals(
            processes.sortedBy { it.getPriority().priorityAsInt }.map { it.getPid() }.toList(),
            taskManager.listRunningProcesses(OrderDefinition.BY_PRIORITY_ASC).map { it.getPid() }.toList()
        )
    }

    @Test
    fun `when list Process by PRIORITY DESC given List of Processes then list Return List with Process by PRIORITY DESC`() {
        val (taskManager, processes) = newPriorityTaskManager(3,
            Priority.LOW, Priority.HIGH, Priority.MEDIUM)
        Assertions.assertEquals(
            processes.sortedByDescending { it.getPriority().priorityAsInt }.map { it.getPid() }.toList(),
            taskManager.listRunningProcesses(OrderDefinition.BY_PRIORITY_DESC).map { it.getPid() }.toList()
        )
    }

    @Test
    fun `when list Process by CREATION ASC given List of Processes then list Return List with Process by CREATION ASC`() {
        val (taskManager, processes) = newPriorityTaskManager(
            5,
            SequentialCreationTimeProvider(),
            Priority.HIGH, Priority.HIGH, Priority.HIGH, Priority.MEDIUM, Priority.LOW)
        Assertions.assertEquals(
            processes.map { it.getPid() }.toList(),
            taskManager.listRunningProcesses(OrderDefinition.BY_CREATION_TIME_ASC).map { it.getPid() }.toList()
        )
    }

    @Test
    fun `when list Process by CREATION DESC given List of Processes then list Return List with Process by CREATION DESC`() {
        val (taskManager, processes) = newPriorityTaskManager(
            5,
            SequentialCreationTimeProvider(),
            Priority.HIGH, Priority.HIGH, Priority.HIGH, Priority.MEDIUM, Priority.LOW)
        Assertions.assertEquals(
            processes.reversed().map { it.getPid() }.toList(),
            taskManager.listRunningProcesses(OrderDefinition.BY_CREATION_TIME_DESC).map { it.getPid() }.toList()
        )
    }

    @Test
    fun `when kill Process by PID given PID does not exist then throw ProcessNotFoundException`() {
        val (taskManager, _) = newPriorityTaskManager(1, Priority.LOW)
        val exception = Assertions.assertThrows(ProcessNotFoundException::class.java) {
            taskManager.killProcess("DOES_NOT_EXIST")
        }
        Assertions.assertEquals("Process DOES_NOT_EXIST not found", exception.message)
    }

    @Test
    fun `when kill Process by PID given PID does exist then Kill the Process`() {
        val (taskManager, builtProcesses) = newPriorityTaskManager(2, Priority.LOW, Priority.LOW)
        val processes = taskManager.listRunningProcesses(OrderDefinition.BY_PID_ASC)
        taskManager.killProcess(processes[0].getPid())
        val processesAfterKill = taskManager.listRunningProcesses(OrderDefinition.BY_PID_ASC)
        Assertions.assertEquals(1, processesAfterKill.size)
        Assertions.assertEquals(processes[1].getPid(), processesAfterKill[0].getPid())
        Assertions.assertTrue((builtProcesses.find { it.getPid() == processes[0].getPid() } as TestProcess).wasKilled())
    }

    @Test
    fun `when kill Process by PRIORITY given PRIORIY does exist then Kill the Process`() {
        val (taskManager, builtProcesses) = newPriorityTaskManager(2,
            SequentialCreationTimeProvider(), Priority.LOW, Priority.MEDIUM)
        val processes = taskManager.listRunningProcesses(OrderDefinition.BY_CREATION_TIME_ASC)
        taskManager.killGroupProcesses(Priority.LOW)
        val processesAfterKill = taskManager.listRunningProcesses(OrderDefinition.BY_CREATION_TIME_ASC)
        Assertions.assertEquals(1, processesAfterKill.size)
        Assertions.assertEquals(processes[1].getPid(), processesAfterKill[0].getPid())
        Assertions.assertTrue((builtProcesses.find { it.getPid() == processes[0].getPid() } as TestProcess).wasKilled())
    }

    @Test
    fun `when kill Process by PRIORITY given PRIORITY does not exist then does not Kill the Process`() {
        val (taskManager, _) = newPriorityTaskManager(2, Priority.LOW, Priority.LOW)
        val processes = taskManager.listRunningProcesses(OrderDefinition.BY_CREATION_TIME_ASC)
        taskManager.killGroupProcesses(Priority.MEDIUM)
        val processesAfterKill = taskManager.listRunningProcesses(OrderDefinition.BY_CREATION_TIME_ASC)
        Assertions.assertEquals(2, processesAfterKill.size)
        Assertions.assertEquals(processes[0].getPid(), processesAfterKill[0].getPid())
        Assertions.assertEquals(processes[1].getPid(), processesAfterKill[1].getPid())
    }

    @Test
    fun `when killAll Processes given Processes exist then does Kill the Process`() {
        val (taskManager, builtProcesses) = newPriorityTaskManager(2, Priority.LOW, Priority.LOW)
        taskManager.killAllProcesses()
        val processesAfterKill = taskManager.listRunningProcesses(OrderDefinition.BY_PID_ASC)
        Assertions.assertEquals(0, processesAfterKill.size)
        Assertions.assertTrue((builtProcesses.find { it.getPid() == builtProcesses[0].getPid() } as TestProcess).wasKilled())
        Assertions.assertTrue((builtProcesses.find { it.getPid() == builtProcesses[1].getPid() } as TestProcess).wasKilled())
    }


}