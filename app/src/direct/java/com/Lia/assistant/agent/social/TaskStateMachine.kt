package com.Lia.assistant.agent.social

enum class TaskState {
    IDLE,
    OPENING_APP,
    OBSERVING,
    RESOLVING_TARGET,
    VALIDATING_TARGET,
    EXECUTING_ACTION,
    WAITING_FOR_UI,
    VERIFYING,
    NEXT_STEP,
    RETRY,
    REOBSERVE,
    WAITING_FOR_CONFIRMATION,
    WAITING_FOR_USER,
    COMPLETED,
    FAILED,
    CANCELLED
}

class TaskStateMachine {
    private var currentState: TaskState = TaskState.IDLE
    private val stateHistory = mutableListOf<TaskState>(TaskState.IDLE)
    private val lock = Any()

    val state: TaskState
        get() = synchronized(lock) { currentState }

    val history: List<TaskState>
        get() = synchronized(lock) { ArrayList(stateHistory) }

    fun isTerminal(): Boolean {
        return synchronized(lock) {
            currentState == TaskState.COMPLETED ||
            currentState == TaskState.FAILED ||
            currentState == TaskState.CANCELLED
        }
    }

    fun moveTo(next: TaskState) {
        synchronized(lock) {
            if (isTerminal()) {
                throw IllegalStateException("Cannot transition from terminal state $currentState to $next")
            }

            // FAILED and CANCELLED are reachable from any non-terminal state
            if (next == TaskState.FAILED || next == TaskState.CANCELLED) {
                currentState = next
                stateHistory.add(next)
                return
            }

            val isLegal = when (currentState) {
                TaskState.IDLE -> next == TaskState.OPENING_APP
                TaskState.OPENING_APP -> next == TaskState.OBSERVING || next == TaskState.WAITING_FOR_USER
                TaskState.OBSERVING -> next == TaskState.RESOLVING_TARGET || next == TaskState.WAITING_FOR_USER
                TaskState.RESOLVING_TARGET -> next == TaskState.VALIDATING_TARGET || next == TaskState.REOBSERVE
                TaskState.VALIDATING_TARGET -> next == TaskState.EXECUTING_ACTION || next == TaskState.WAITING_FOR_CONFIRMATION
                TaskState.WAITING_FOR_CONFIRMATION -> next == TaskState.EXECUTING_ACTION
                TaskState.EXECUTING_ACTION -> next == TaskState.WAITING_FOR_UI
                TaskState.WAITING_FOR_UI -> next == TaskState.VERIFYING
                TaskState.VERIFYING -> next == TaskState.NEXT_STEP || next == TaskState.RETRY || next == TaskState.COMPLETED
                TaskState.NEXT_STEP -> next == TaskState.OBSERVING || next == TaskState.COMPLETED
                TaskState.RETRY -> next == TaskState.RESOLVING_TARGET || next == TaskState.REOBSERVE
                TaskState.REOBSERVE -> next == TaskState.OBSERVING
                TaskState.WAITING_FOR_USER -> next == TaskState.OBSERVING
                TaskState.COMPLETED, TaskState.FAILED, TaskState.CANCELLED -> false
            }

            if (!isLegal) {
                throw IllegalStateException("Illegal state transition from $currentState to $next")
            }

            currentState = next
            stateHistory.add(next)
        }
    }
}
