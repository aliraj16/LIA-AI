package com.Lia.assistant.action

import android.content.Context
import android.content.Intent
import kotlin.math.abs

object AppLauncher {

    /**
     * Resolves an app query against a map of lowercase app labels to package names.
     * Rules:
     * 1. Exact label match first.
     * 2. Candidates with key length >= 3 where key startsWith query or query startsWith key
     *    or every query word prefix-matches some key word.
     * 3. Pick the candidate with closest label length to the query.
     * Never loose substring matching.
     */
    fun findApp(query: String, apps: Map<String, String>): String? {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return null

        // 1. Exact match
        if (apps.containsKey(q)) {
            return apps[q]
        }

        val queryWords = q.split(Regex("\\s+")).filter { it.isNotBlank() }
        val candidates = mutableListOf<String>()

        for (label in apps.keys) {
            if (label.length < 3) continue

            val startsWith = label.startsWith(q) || q.startsWith(label)
            if (startsWith) {
                candidates.add(label)
                continue
            }

            // Word-by-word prefix match
            val labelWords = label.split(Regex("\\s+")).filter { it.isNotBlank() }
            if (queryWords.isNotEmpty() && labelWords.isNotEmpty()) {
                val allQueryWordsMatch = queryWords.all { qw ->
                    labelWords.any { lw -> lw.startsWith(qw) }
                }
                if (allQueryWordsMatch) {
                    candidates.add(label)
                }
            }
        }

        if (candidates.isEmpty()) return null

        // Pick candidate with closest label length
        val bestLabel = candidates.minByOrNull { abs(it.length - q.length) } ?: return null
        return apps[bestLabel]
    }

    fun launch(context: Context, packageName: String): Boolean {
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName) ?: return false
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }
}
