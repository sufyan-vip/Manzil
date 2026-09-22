package com.manzil.app.core.diff

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * Flat, top-level JSON diff used for goal revision history.
 *
 * Keys present in both objects are compared by their rendered value, keys that
 * disappear are reported as an empty [DiffEntry.to] and keys that appear are
 * reported with an empty [DiffEntry.from].
 */
object JsonDiff {
    fun diff(oldJson: String, newJson: String): List<DiffEntry> {
        val oldObject = parseObject(oldJson)
        val newObject = parseObject(newJson)
        val diffs = mutableListOf<DiffEntry>()
        newObject.forEach { (key, newValue) ->
            val oldValue = oldObject[key]
            if (oldValue != newValue) {
                diffs.add(DiffEntry(key, render(oldValue), render(newValue)))
            }
        }
        oldObject.forEach { (key, oldValue) ->
            if (!newObject.containsKey(key)) {
                diffs.add(DiffEntry(key, render(oldValue), ""))
            }
        }
        return diffs
    }

    private fun parseObject(json: String): JsonObject {
        val element = runCatching { Json.parseToJsonElement(json) }.getOrNull()
        return element as? JsonObject ?: JsonObject(emptyMap())
    }

    private fun render(element: JsonElement?): String = when (element) {
        null -> ""
        is JsonPrimitive -> element.content
        else -> element.toString()
    }
}
