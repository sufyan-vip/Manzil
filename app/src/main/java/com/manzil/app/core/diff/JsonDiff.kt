package com.manzil.app.core.diff

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * Field level JSON diff used by GOAL PULSE: "mere goal mein kya change aaya hai".
 * Flattens both documents to `path -> value` and reports every difference.
 */
object JsonDiff {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    fun diff(oldJson: String, newJson: String): List<DiffEntry> {
        val oldMap = flatten(parse(oldJson))
        val newMap = flatten(parse(newJson))
        val keys = (oldMap.keys + newMap.keys).toSortedSet()
        return keys.mapNotNull { key ->
            val before = oldMap[key]
            val after = newMap[key]
            if (before != after) DiffEntry(key, before ?: "", after ?: "") else null
        }
    }

    private fun parse(raw: String): JsonElement =
        runCatching { json.parseToJsonElement(raw.ifBlank { "{}" }) }.getOrElse { JsonObject(emptyMap()) }

    private fun flatten(element: JsonElement, prefix: String = ""): Map<String, String> {
        val out = mutableMapOf<String, String>()
        when (element) {
            is JsonObject -> element.forEach { (key, value) ->
                val path = if (prefix.isEmpty()) key else "$prefix.$key"
                when (value) {
                    is JsonObject, is JsonArray -> out += flatten(value, path)
                    is JsonNull -> out[path] = ""
                    is JsonPrimitive -> out[path] = value.content
                }
            }
            is JsonArray -> element.forEachIndexed { index, value ->
                val path = if (prefix.isEmpty()) "[$index]" else "$prefix[$index]"
                when (value) {
                    is JsonObject, is JsonArray -> out += flatten(value, path)
                    is JsonNull -> out[path] = ""
                    is JsonPrimitive -> out[path] = value.content
                }
            }
            is JsonNull -> out[prefix] = ""
            is JsonPrimitive -> out[prefix] = element.content
        }
        return out
    }

    /** Human readable label for a diff path: `metricTarget` -> "Metric target". */
    fun label(path: String): String {
        val leaf = path.substringAfterLast('.')
        val spaced = leaf
            .replace(Regex("([a-z0-9])([A-Z])"), "$1 $2")
            .replace('_', ' ')
            .trim()
        if (spaced.isEmpty()) return path
        return spaced.substring(0, 1).uppercase() + spaced.substring(1)
    }
}
