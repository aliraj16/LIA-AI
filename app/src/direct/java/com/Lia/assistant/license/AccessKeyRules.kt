package com.Lia.assistant.license

object AccessKeyRules {

    data class EvaluationResult(
        val isActive: Boolean,
        val statusText: String,
        val isBlocked: Boolean,
        val isExpired: Boolean
    )

    fun evaluate(
        plan: String,
        expiresAt: Long,
        isBlocked: Boolean,
        hasKey: Boolean,
        currentTime: Long = System.currentTimeMillis()
    ): EvaluationResult {
        if (isBlocked) {
            return EvaluationResult(
                isActive = false,
                statusText = "Access Blocked by Administrator",
                isBlocked = true,
                isExpired = false
            )
        }

        if (!hasKey) {
            return EvaluationResult(
                isActive = false,
                statusText = "No Access Key Configured",
                isBlocked = false,
                isExpired = false
            )
        }

        if (expiresAt > 0L && currentTime > expiresAt) {
            return EvaluationResult(
                isActive = false,
                statusText = "Subscription Expired",
                isBlocked = false,
                isExpired = true
            )
        }

        return EvaluationResult(
            isActive = true,
            statusText = "$plan Plan Active",
            isBlocked = false,
            isExpired = false
        )
    }
}
