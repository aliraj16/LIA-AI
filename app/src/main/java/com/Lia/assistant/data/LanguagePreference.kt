package com.Lia.assistant.data

enum class LanguagePreference(val displayName: String, val description: String) {
    AUTO("Auto-detect", "Follows the language you use (Bangla, Hindi, Hinglish or English)"),
    BANGLA("বাংলা", "সবসময় স্বাভাবিক বাংলায় উত্তর দেয়"),
    HINDI("Hindi", "Always responds in natural Hindi"),
    HINGLISH("Hinglish", "Conversational blend of Hindi and English"),
    ENGLISH("English", "Clear and natural English");

    companion object {
        fun fromString(value: String): LanguagePreference {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: AUTO
        }
    }
}
