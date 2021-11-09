# TaskManager
iptiQ TaskManager assessment

## TaskManager assessment definition
The full definition of the assessment can be found [here](Task%20Manager.pdf).

## Some information before explaining the solution
I'm still a lot more Java developer than Kotlin developer, but I thought it would be a good
idea to implement this assessment using Kotlin as it is the main language at iptiQ. So, during the
review of this code, please point out any items that were written more in Java style than Kotlin style.
I'm sure there are a few, at least.

## Libraries
The only libraries used were microutils kotlin logging and JUnit 5. Libraries for mocking tests seeme a bit overkill
given that I would use them only to spy/verify if one or another method was invoked (such as kill in the process class).
I decided to solve this by having an extension of the process which I could "ask" if the method was invoked.

## Design of the solution
The design of the solution followed exactly the specification of the assessment. An immutable Process class holding both
PID and PRIORITY data. An interface AbstractProcess defining the methods required by any process class. A decorator called
RunningProcess, which complies with AbstractProcess interface and adds the creation time of a process. A second decorator
was implemented, called ReadonlyProcess, which is used to safely return process through the list operations on the TaskManagers.
This seemed to be a good idea in order to avoid the kill method to be called from the process listed, thus, having a process
killed but not removed from the TaskManager.

The TaskManagers also followed exactly the specification, where 3 are implemented:
* DefaultTaskManager: with the default behaviour of not allowing new processes to be added if the capacity was reached
* FifoTaskManager: extends the DefaultTaskManager and overrides the add process operation, removing the oldest (first) process if the capacity was reached
* PriorityTaskManager: extends the DefaultTaskManager and overrides the add process operation, removing a lower priority process if needed and possible

## Some last notes
Some items where considered to be implemented, but they seemed to break the specification. Those items are:
* PID as long value: it seemed more natural to have a long value for processes PIDs, but to do that in a safe way without collisions,
it will be required to have some PID provider class within the TaskManager, and once the process is built, a PID would be given to the process.
Allowing the process to create its own PID works safely only with UUID
* TaskManager Kill APIs and Process Kill API could work together: If the process could have access to some internal API of the TaskManager, like
a callback, to "let the TaskManager knows" about a process being killed would be nice to have. So, if a process is killed directly through its API,
it could let the TaskManager be aware of it to remove the process from the running processes. This would require, also, changes to avoid an infinite
loop between calling some of the Kill APIs on the TaskManager and the Kill method on the process.

