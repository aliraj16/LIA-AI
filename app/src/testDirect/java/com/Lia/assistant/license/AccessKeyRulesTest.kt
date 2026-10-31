package com.Lia.assistant.license

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AccessKeyRulesTest {

    @Test
    fun testBlockedAlwaysInactive() {
        val res = AccessKeyRules.evaluate(
            plan = "Pro",
            expiresAt = System.currentTimeMillis() + 100000L,
            isBlocked = true,
            hasKey = true
        )
        assertFalse(res.isActive)
        assertTrue(res.isBlocked)
        assertEquals("Access Blocked by Administrator", res.statusText)
    }

    @Test
    fun testExpiredKeyIsInactive() {
        val now = 1_000_000L
        val res = AccessKeyRules.evaluate(
            plan = "Pro",
            expiresAt = 900_000L,
            isBlocked = false,
            hasKey = true,
            currentTime = now
        )
        assertFalse(res.isActive)
        assertTrue(res.isExpired)
        assertEquals("Subscription Expired", res.statusText)
    }

    @Test
    fun testValidKeyIsActive() {
        val now = 1_000_000L
        val res = AccessKeyRules.evaluate(
            plan = "Pro",
            expiresAt = 2_000_000L,
            isBlocked = false,
            hasKey = true,
            currentTime = now
        )
        assertTrue(res.isActive)
        assertFalse(res.isExpired)
        assertFalse(res.isBlocked)
        assertEquals("Pro Plan Active", res.statusText)
    }

    @Test
    fun testMissingKeyIsInactive() {
        val res = AccessKeyRules.evaluate(
            plan = "Free",
            expiresAt = 0L,
            isBlocked = false,
            hasKey = false
        )
        assertFalse(res.isActive)
        assertEquals("No Access Key Configured", res.statusText)
    }
}
