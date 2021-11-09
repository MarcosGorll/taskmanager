package com.marcosgorll.iptiq.taskmanager

/**
 * When the maximum capacity of a TaskManager is reached and a new process is added
 */
class MaximumCapacityReachedException(message: String) : Exception(message) {

}

/**
 * When trying to operate over a process that a TaskManager is not aware of
 */
class ProcessNotFoundException(message: String) : Exception(message) {

}
