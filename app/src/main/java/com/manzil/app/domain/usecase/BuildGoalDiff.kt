package com.manzil.app.domain.usecase

import com.manzil.app.core.diff.DiffEntry
import javax.inject.Inject

class BuildGoalDiffUseCase @Inject constructor() {
    fun buildDiff(oldGoal: Map<String, String>, newGoal: Map<String, String>): List<DiffEntry> {
        val diffs = mutableListOf<DiffEntry>()
        for ((key, newVal) in newGoal) {
            val oldVal = oldGoal[key]
            if (oldVal != newVal) {
                diffs.add(DiffEntry(key, oldVal ?: "", newVal))
            }
        }
        return diffs
    }
}
