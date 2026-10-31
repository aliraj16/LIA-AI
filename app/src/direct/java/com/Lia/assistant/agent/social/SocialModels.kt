package com.Lia.assistant.agent.social

import android.net.Uri
import org.json.JSONObject

enum class Platform {
    INSTAGRAM,
    FACEBOOK
}

enum class SocialAction {
    CREATE_POST,
    CREATE_REEL,
    CREATE_STORY,
    TEXT_POST
}

enum class TaskMode {
    PUBLISH,
    DRAFT
}

enum class TaskStatus {
    IDLE,
    RUNNING,
    WAITING_FOR_CONFIRMATION,
    WAITING_FOR_USER,
    COMPLETED,
    SUBMITTED_UNVERIFIED,
    FAILED,
    CANCELLED
}

enum class ControlCommand {
    CONFIRM,
    REJECT,
    RESUME,
    CANCEL,
    STATUS
}

data class SocialTaskRequest(
    val platform: Platform,
    val action: SocialAction,
    val mediaUri: Uri?,
    val caption: String,
    val mode: TaskMode,
    val targetAccount: String?
) {
    companion object {
        fun parse(args: JSONObject): SocialTaskRequest {
            val platformStr = args.optString("platform", "INSTAGRAM").uppercase()
            val platform = runCatching { Platform.valueOf(platformStr) }.getOrDefault(Platform.INSTAGRAM)

            val actionStr = args.optString("action", "CREATE_POST").uppercase()
            val action = runCatching { SocialAction.valueOf(actionStr) }.getOrDefault(SocialAction.CREATE_POST)

            val uriStr = args.optString("media_uri", "")
            val mediaUri = if (uriStr.isNotBlank()) Uri.parse(uriStr) else null

            val caption = args.optString("caption", "")

            val modeStr = args.optString("mode", "DRAFT").uppercase()
            val mode = runCatching { TaskMode.valueOf(modeStr) }.getOrDefault(TaskMode.DRAFT)

            val targetAccount = args.optString("target_account", "").ifBlank { null }

            return SocialTaskRequest(platform, action, mediaUri, caption, mode, targetAccount)
        }
    }
}

data class PlanStep(
    val stepId: Int,
    val description: String,
    val targetSpec: String,
    val isIrreversible: Boolean = false
)

data class TaskReport(
    val taskId: String,
    val status: TaskStatus,
    val platform: Platform,
    val action: SocialAction,
    val message: String
)
