@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.manzil.app.feature.settings

import android.content.Intent
import android.provider.Settings as AndroidSettings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.manzil.app.BuildConfig
import com.manzil.app.core.design.ChipRow
import com.manzil.app.core.design.ConfirmDialog
import com.manzil.app.core.design.Divider
import com.manzil.app.core.design.InfoBanner
import com.manzil.app.core.design.KeyValueRow
import com.manzil.app.core.design.ManzilCard
import com.manzil.app.core.design.ManzilScreenScaffold
import com.manzil.app.core.design.ManzilTextField
import com.manzil.app.core.design.Pill
import com.manzil.app.core.design.SectionHeader
import com.manzil.app.core.design.rememberTimePicker
import com.manzil.app.core.theme.ManzilColors
import com.manzil.app.core.theme.ThemeMode
import java.time.LocalTime

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val exportJson = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> uri?.let(viewModel::exportBackup) }

    val exportCsv = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri -> uri?.let(viewModel::exportCsv) }

    val importJson = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let(viewModel::importBackup) }

    val pickMorning = rememberTimePicker(
        LocalTime.of(state.settings.morningHour, state.settings.morningMinute)
    ) { time -> time?.let { viewModel.setNotificationTime(morning = true, hour = it.hour, minute = it.minute) } }

    val pickEvening = rememberTimePicker(
        LocalTime.of(state.settings.eveningHour, state.settings.eveningMinute)
    ) { time -> time?.let { viewModel.setNotificationTime(morning = false, hour = it.hour, minute = it.minute) } }

    ManzilScreenScaffold(
        title = "Settings",
        subtitle = "Everything stays on this device",
        onBack = onBack
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            state.status?.let { message ->
                item {
                    InfoBanner(
                        text = message,
                        color = if (state.statusIsError) ManzilColors.danger else ManzilColors.success,
                        onClose = viewModel::clearStatus
                    )
                }
            }

            /* ------------------------------------ AI ------------------------------------ */
            item {
                ManzilCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Lock,
                            contentDescription = null,
                            tint = ManzilColors.success,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "OpenRouter API key (optional)",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Free key from openrouter.ai/keys. Stored encrypted with the Android Keystore, " +
                            "excluded from backups and exports, never logged.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    if (state.maskedKey != null) {
                        KeyValueRow("Saved key", state.maskedKey!!)
                        Spacer(Modifier.height(8.dp))
                    }
                    ManzilTextField(
                        value = state.keyInput,
                        onValueChange = viewModel::updateKeyInput,
                        label = if (state.maskedKey == null) "Paste key" else "Replace key",
                        placeholder = "sk-or-v1-…",
                        singleLine = true,
                        trailing = {
                            IconButton(onClick = viewModel::toggleShowKey) {
                                Icon(
                                    if (state.showKey) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = "Toggle key visibility",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = viewModel::saveKey, shape = RoundedCornerShape(12.dp)) {
                            Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Save")
                        }
                        OutlinedButton(
                            onClick = viewModel::testKey,
                            enabled = !state.testing,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (state.testing) {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                                Spacer(Modifier.width(6.dp))
                            }
                            Text("Test connection")
                        }
                        if (state.maskedKey != null) {
                            TextButton(onClick = viewModel::removeKey) {
                                Text("Remove", color = ManzilColors.danger)
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    Divider()
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Model",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            state.selectedModel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = viewModel::loadModels) {
                            Text(if (state.loadingModels) "Loading…" else "Browse models")
                        }
                    }
                }
            }

            if (state.models.isNotEmpty()) {
                item { SectionHeader(title = "Available models (free first)") }
                items(state.models, key = { it.id }) { model ->
                    ManzilCard(
                        onClick = { viewModel.selectModel(model.id) },
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                        containerColor = if (model.id == state.selectedModel) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    model.name.ifBlank { model.id },
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    model.id,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (model.isFree) Pill(text = "free", color = ManzilColors.success)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                model.contextLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            /* --------------------------------- Appearance ------------------------------- */
            item {
                ManzilCard {
                    SectionHeader(title = "Appearance")
                    Spacer(Modifier.height(10.dp))
                    ChipRow(
                        options = listOf("System", "Light", "Dark"),
                        selectedIndex = state.settings.themeMode.ordinal,
                        onSelect = { index -> viewModel.setTheme(ThemeMode.entries[index]) }
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Match phone wallpaper", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "Android 12+ dynamic colour",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = state.settings.dynamicColor,
                            onCheckedChange = viewModel::setDynamicColor
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Divider()
                    Spacer(Modifier.height(12.dp))
                    Text("Language of the app text", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    ChipRow(
                        options = listOf("English", "اردو"),
                        selectedIndex = if (state.settings.language == "ur") 1 else 0,
                        onSelect = { index -> viewModel.setLanguage(if (index == 1) "ur" else "en") }
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "This switches greetings and the briefing between English and Roman Urdu. " +
                            "For a fully Urdu interface, set Urdu as the phone language.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            runCatching {
                                context.startActivity(Intent(AndroidSettings.ACTION_LOCALE_SETTINGS))
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Open phone language settings")
                    }
                }
            }

            /* ------------------------------- Notifications ------------------------------ */
            item {
                ManzilCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Notifications",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Enable notifications", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "Max 6 a day · quiet 23:00-07:00",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = state.settings.notificationsEnabled,
                            onCheckedChange = viewModel::setNotifications
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("AI-written briefing", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "Rewrites the briefing in a warmer tone when a key is set",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = state.settings.aiBriefingEnabled,
                            onCheckedChange = viewModel::setAiBriefing
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { pickMorning() }, shape = RoundedCornerShape(12.dp)) {
                            Text("Morning ${state.settings.morningTime}")
                        }
                        OutlinedButton(onClick = { pickEvening() }, shape = RoundedCornerShape(12.dp)) {
                            Text("Evening ${state.settings.eveningTime}")
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            if (state.exactAlarmsAllowed) {
                                "Exact alarms allowed — briefings land on time."
                            } else {
                                "Exact alarms are blocked. Briefings will be roughly on time."
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (state.exactAlarmsAllowed) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                ManzilColors.warning
                            },
                            modifier = Modifier.weight(1f)
                        )
                        if (!state.exactAlarmsAllowed) {
                            TextButton(onClick = {
                                runCatching {
                                    context.startActivity(
                                        Intent(AndroidSettings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                                    )
                                }
                            }) { Text("Allow") }
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Button(onClick = viewModel::sendTestNotification, shape = RoundedCornerShape(12.dp)) {
                        Text("Send test notification")
                    }
                }
            }

            /* ---------------------------------- Profile --------------------------------- */
            item {
                ManzilCard {
                    SectionHeader(title = "Your details")
                    Spacer(Modifier.height(10.dp))
                    ManzilTextField(
                        value = state.nameInput,
                        onValueChange = viewModel::updateName,
                        label = "Name"
                    )
                    Spacer(Modifier.height(8.dp))
                    ManzilTextField(
                        value = state.universityInput,
                        onValueChange = viewModel::updateUniversity,
                        label = "University"
                    )
                    Spacer(Modifier.height(8.dp))
                    ManzilTextField(
                        value = state.semesterInput,
                        onValueChange = viewModel::updateSemester,
                        label = "Semester"
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("Daily deep work target", style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.height(6.dp))
                    ChipRow(
                        options = listOf("1h", "2h", "3h", "4h", "6h"),
                        selectedIndex = when (state.settings.dailyHours) {
                            1 -> 0; 2 -> 1; 3 -> 2; 4 -> 3; else -> 4
                        },
                        onSelect = { index ->
                            viewModel.setDailyHours(listOf(1, 2, 3, 4, 6)[index])
                        }
                    )
                    Spacer(Modifier.height(10.dp))
                    Text("Weekly deep work target", style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.height(6.dp))
                    ChipRow(
                        options = listOf("10h", "15h", "25h", "35h"),
                        selectedIndex = when (state.settings.deepWorkTargetHours) {
                            10 -> 0; 15 -> 1; 25 -> 2; else -> 3
                        },
                        onSelect = { index ->
                            viewModel.setDeepWorkTarget(listOf(10, 15, 25, 35)[index])
                        }
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = viewModel::saveProfile, shape = RoundedCornerShape(12.dp)) {
                        Text("Save details")
                    }
                }
            }

            /* ----------------------------------- Data ---------------------------------- */
            item {
                ManzilCard {
                    SectionHeader(title = "Data & backup")
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "JSON backup excludes your API key. CSV is handy for a portfolio or a client report.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { exportJson.launch("manzil-backup.json") },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Export JSON")
                        }
                        OutlinedButton(
                            onClick = { exportCsv.launch("manzil-tasks.csv") },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Export CSV")
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { importJson.launch(arrayOf("application/json", "text/*")) },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Import backup")
                    }
                    Spacer(Modifier.height(12.dp))
                    Divider()
                    Spacer(Modifier.height(12.dp))
                    TextButton(onClick = viewModel::loadStarterRoadmap) {
                        Text("Load the starter roadmap (goals + tasks + KPIs)")
                    }
                    Spacer(Modifier.height(6.dp))
                    TextButton(onClick = viewModel::requestErase) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = ManzilColors.danger
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Erase all local data", color = ManzilColors.danger)
                    }
                }
            }

            /* ---------------------------------- About ---------------------------------- */
            item {
                ManzilCard {
                    SectionHeader(title = "About Manzil")
                    Spacer(Modifier.height(8.dp))
                    KeyValueRow("Version", "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
                    KeyValueRow("Storage", "Local Room database")
                    KeyValueRow("Network", "OpenRouter only, with your key")
                    KeyValueRow("Analytics", "None")
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Manzil — your destination, daily tracked. Built for the zero-to-goal path: " +
                            "skills, portfolio, first clients, then a software house.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    if (state.confirmErase) {
        ConfirmDialog(
            title = "Erase everything?",
            message = "Goals, tasks, journal, KPIs and time entries on this device will be deleted. " +
                "Your API key is removed too. This cannot be undone.",
            confirmLabel = "Erase",
            destructive = true,
            onConfirm = viewModel::confirmErase,
            onDismiss = viewModel::cancelErase
        )
    }
}
