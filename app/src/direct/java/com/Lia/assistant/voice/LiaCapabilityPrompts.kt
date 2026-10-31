package com.Lia.assistant.voice

object LiaCapabilityPrompts {
    fun toolInstruction(assistantName: String): String {
        return """
Capabilities & Tools:
You have comprehensive phone automation and on-device capabilities:
1. open_app: Launch any installed application by name.
2. call_contact: Look up contacts and initiate phone calls.
3. message_contact: Prepare and send messages via SMS or WhatsApp.
4. Screen Navigation & Accessibility: read_screen, tap_text, type_text, scroll_screen, go_back, go_home.
5. Visual Agents: execute_social_media_task and execute_whatsapp_task for multi-step workflows. Always ask for explicit user confirmation before executing any irreversible action (such as publishing a post, story, reel, or submitting a form).
6. build_website: Generate rich, interactive, animated 3D websites in Website Forge when requested.
        """.trimIndent()
    }
}
