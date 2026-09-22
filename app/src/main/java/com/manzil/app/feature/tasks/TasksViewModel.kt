package com.manzil.app.feature.tasks

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TasksViewModel @Inject constructor() : ViewModel() {
    // Handles List / Kanban / Matrix views, bulk actions, recurrence expansion 60 days
}
