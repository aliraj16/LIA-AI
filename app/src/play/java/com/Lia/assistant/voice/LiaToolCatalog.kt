package com.Lia.assistant.voice

import org.json.JSONArray

object LiaToolCatalog {
    fun declarations(): JSONArray {
        val array = JSONArray()

        array.put(
            GeminiToolUtils.functionDecl(
                name = "open_app",
                description = "Opens an installed application on the phone by its name.",
                params = mapOf("app_name" to "The name of the app to launch (e.g. YouTube, WhatsApp, Camera)"),
                required = listOf("app_name")
            )
        )

        array.put(
            GeminiToolUtils.functionDecl(
                name = "call_contact",
                description = "Places a voice call to a phone contact by name or direct number.",
                params = mapOf("contact_name" to "The contact name or phone number to call"),
                required = listOf("contact_name")
            )
        )

        array.put(
            GeminiToolUtils.functionDecl(
                name = "message_contact",
                description = "Prepares a message to a contact in SMS or WhatsApp. The composer opens for user review.",
                params = mapOf(
                    "contact_name" to "Name or phone number of the recipient",
                    "message" to "Text body of the message",
                    "app" to "Optional target app: 'whatsapp' or 'sms' (defaults to 'sms')"
                ),
                required = listOf("contact_name", "message")
            )
        )

        array.put(
            GeminiToolUtils.functionDecl(
                name = "build_website",
                description = "Launches the Website Forge to design, code, and animate a complete website based on a description.",
                params = mapOf("prompt" to "Detailed description of the website to build (e.g., 'Modern cafe landing page with menu and 3D coffee cup')"),
                required = listOf("prompt")
            )
        )

        return array
    }
}
