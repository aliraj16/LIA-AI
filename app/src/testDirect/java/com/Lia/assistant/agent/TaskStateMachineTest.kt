package com.Lia.assistant.agent

import com.Lia.assistant.agent.social.TaskState
import com.Lia.assistant.agent.social.TaskStateMachine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class TaskStateMachineTest {

    @Test
    fun testLegalTransitionsSequence() {
        val sm = TaskStateMachine()
        assertEquals(TaskState.IDLE, sm.state)

        sm.moveTo(TaskState.OPENING_APP)
        sm.moveTo(TaskState.OBSERVING)
        sm.moveTo(TaskState.RESOLVING_TARGET)
        sm.moveTo(TaskState.VALIDATING_TARGET)
        sm.moveTo(TaskState.WAITING_FOR_CONFIRMATION)
        sm.moveTo(TaskState.EXECUTING_ACTION)
        sm.moveTo(TaskState.WAITING_FOR_UI)
        sm.moveTo(TaskState.VERIFYING)
        sm.moveTo(TaskState.COMPLETED)

        assertEquals(TaskState.COMPLETED, sm.state)
        assertTrue(sm.isTerminal())
    }

    @Test
    fun testIllegalTransitionThrows() {
        val sm = TaskStateMachine()
        try {
            sm.moveTo(TaskState.EXECUTING_ACTION)
            fail("Should throw on illegal transition from IDLE to EXECUTING_ACTION")
        } catch (_: IllegalStateException) {}
    }

    @Test
    fun testCancelAndFailReachableFromAnyState() {
        val sm1 = TaskStateMachine()
        sm1.moveTo(TaskState.OPENING_APP)
        sm1.moveTo(TaskState.CANCELLED)
        assertTrue(sm1.isTerminal())

        val sm2 = TaskStateMachine()
        sm2.moveTo(TaskState.OPENING_APP)
        sm2.moveTo(TaskState.OBSERVING)
        sm2.moveTo(TaskState.FAILED)
        assertTrue(sm2.isTerminal())
    }

    @Test
    fun testTerminalStatesCannotTransition() {
        val sm = TaskStateMachine()
        sm.moveTo(TaskState.OPENING_APP)
        sm.moveTo(TaskState.CANCELLED)
        try {
            sm.moveTo(TaskState.IDLE)
            fail("Terminal state cannot transition")
        } catch (_: IllegalStateException) {}
    }
}
