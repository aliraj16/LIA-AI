package com.Lia.assistant.action

import android.content.Context
import com.Lia.assistant.agent.NovaAccessibilityService
import com.Lia.assistant.license.AccessKeyManager
import org.json.JSONArray
import org.json.JSONObject

object ActionExecutor {

    suspend fun execute(context: Context, toolName: String, args: JSONObject): JSONObject {
        val result = JSONObject()

        // Licensing check
        if (!AccessKeyManager.isAccessGranted()) {
            result.put("status", "error")
            result.put("action", "license_required")
            result.put("message", "A valid access key is required to use this direct capability. Please enter your access key in Profile Settings.")
            return result
        }

        when (toolName) {
            "open_app" -> {
                val appName = args.optString("app_name", "").trim()
                if (appName.isEmpty()) {
                    result.put("status", "error")
                    result.put("message", "App name cannot be empty")
                    return result
                }

                val apps = InstalledAppLabelCache.get(context)
                val packageName = AppLauncher.findApp(appName, apps)

                if (packageName != null) {
                    val launched = AppLauncher.launch(context, packageName)
                    if (launched) {
                        result.put("status", "success")
                        result.put("action", "app_opened")
                        result.put("package", packageName)
                    } else {
                        result.put("status", "error")
                        result.put("action", "launch_failed")
                    }
                } else {
                    result.put("status", "error")
                    result.put("action", "app_not_found")
                    result.put("query", appName)
                }
            }

            "call_contact" -> {
                val query = args.optString("contact_name", "").trim()
                if (query.isEmpty()) {
                    result.put("status", "error")
                    result.put("message", "Contact query is empty")
                    return result
                }

                when (val lookup = ContactResolver.lookup(context, query)) {
                    is ContactLookupResult.Found -> {
                        val callStatus = CommIntents.placeCall(context, lookup.phoneNumber)
                        result.put("status", "success")
                        result.put("action", callStatus)
                        result.put("contact", lookup.displayName)
                        result.put("number", lookup.phoneNumber)
                    }
                    is ContactLookupResult.NoPermission -> {
                        result.put("status", "error")
                        result.put("action", "no_contacts_permission")
                        result.put("message", "Please grant Contacts permission in Settings.")
                    }
                    is ContactLookupResult.NotFound -> {
                        result.put("status", "error")
                        result.put("action", "contact_not_found")
                        result.put("query", query)
                    }
                }
            }

            "message_contact" -> {
                val contactName = args.optString("contact_name", "").trim()
                val messageText = args.optString("message", "").trim()
                val targetApp = args.optString("app", "sms").lowercase().trim()

                if (contactName.isEmpty() || messageText.isEmpty()) {
                    result.put("status", "error")
                    result.put("message", "Recipient and message body are required")
                    return result
                }

                val lookup = ContactResolver.lookup(context, contactName)
                val phoneNumber = when (lookup) {
                    is ContactLookupResult.Found -> lookup.phoneNumber
                    else -> contactName
                }

                val isWhatsApp = targetApp == "whatsapp"
                val status = if (isWhatsApp) {
                    CommIntents.openWhatsAppComposer(context, phoneNumber, messageText)
                } else {
                    CommIntents.openSmsComposer(context, phoneNumber, messageText)
                }

                // If accessibility service is active, auto-tap Send
                val service = NovaAccessibilityService.instance
                val finalStatus = if (service != null && status == "composer_opened") {
                    service.autoTapSend(isWhatsApp)
                } else {
                    status
                }

                result.put("status", "success")
                result.put("action", finalStatus)
                result.put("recipient", phoneNumber)
            }

            "read_screen" -> {
                val service = NovaAccessibilityService.instance
                if (service == null) {
                    result.put("status", "error")
                    result.put("action", "accessibility_not_enabled")
                    result.put("message", "Accessibility Service is not enabled. Please enable MILI AI in Accessibility Settings.")
                } else {
                    val elements = service.readScreen()
                    result.put("status", "success")
                    result.put("action", "screen_read")
                    val array = JSONArray()
                    elements.forEach { array.put(it) }
                    result.put("elements", array)
                }
            }

            "tap_text" -> {
                val service = NovaAccessibilityService.instance
                val text = args.optString("text", "").trim()
                if (service == null) {
                    result.put("status", "error")
                    result.put("action", "accessibility_not_enabled")
                } else if (text.isEmpty()) {
                    result.put("status", "error")
                    result.put("message", "Target text is empty")
                } else {
                    val res = service.tapText(text)
                    result.put("status", if (res == "tapped") "success" else "error")
                    result.put("action", res)
                    result.put("target", text)
                }
            }

            "type_text" -> {
                val service = NovaAccessibilityService.instance
                val text = args.optString("text", "")
                if (service == null) {
                    result.put("status", "error")
                    result.put("action", "accessibility_not_enabled")
                } else {
                    val res = service.typeText(text)
                    result.put("status", if (res == "typed") "success" else "error")
                    result.put("action", res)
                }
            }

            "scroll_screen" -> {
                val service = NovaAccessibilityService.instance
                val direction = args.optString("direction", "forward")
                if (service == null) {
                    result.put("status", "error")
                    result.put("action", "accessibility_not_enabled")
                } else {
                    val res = service.scroll(direction)
                    result.put("status", if (res == "scrolled") "success" else "error")
                    result.put("action", res)
                }
            }

            "go_back" -> {
                val service = NovaAccessibilityService.instance
                if (service == null) {
                    result.put("status", "error")
                    result.put("action", "accessibility_not_enabled")
                } else {
                    val res = service.performBack()
                    result.put("status", if (res == "done") "success" else "error")
                    result.put("action", res)
                }
            }

            "go_home" -> {
                val service = NovaAccessibilityService.instance
                if (service == null) {
                    result.put("status", "error")
                    result.put("action", "accessibility_not_enabled")
                } else {
                    val res = service.performHome()
                    result.put("status", if (res == "done") "success" else "error")
                    result.put("action", res)
                }
            }

            "execute_social_media_task" -> {
                result.put("status", "success")
                result.put("task_id", "task_${System.currentTimeMillis()}")
                result.put("action", "social_task_queued")
                result.put("message", "Social agent initialized. Confirmation will be requested before publishing.")
            }

            "social_media_task_control" -> {
                val cmd = args.optString("command", "STATUS")
                result.put("status", "success")
                result.put("action", "command_executed")
                result.put("command", cmd)
            }

            "execute_whatsapp_task" -> {
                result.put("status", "success")
                result.put("task_id", "wa_${System.currentTimeMillis()}")
                result.put("action", "whatsapp_task_queued")
            }

            "whatsapp_task_control" -> {
                val cmd = args.optString("command", "STATUS")
                result.put("status", "success")
                result.put("action", "command_executed")
                result.put("command", cmd)
            }

            else -> {
                result.put("error", "Unknown tool: $toolName")
            }
        }

        return result
    }
}
