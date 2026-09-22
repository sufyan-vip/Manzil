@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.manzil.app.feature.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.manzil.app.core.design.ChipRow
import com.manzil.app.core.design.KeyValueRow
import com.manzil.app.core.design.ManzilCard
import com.manzil.app.core.design.ManzilTextField
import com.manzil.app.core.design.SectionHeader
import com.manzil.app.core.theme.ManzilColors

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        LinearProgressIndicator(
            progress = { (state.step + 1) / 4f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "Step ${state.step + 1} of 4",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (state.step) {
                0 -> {
                    item {
                        Text(
                            "Welcome to Manzil",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Manzil means destination. This app turns one big goal into today's work — " +
                                "and it keeps running till the goal is DONE, not until a fixed year.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    item {
                        ManzilCard {
                            SectionHeader(title = "Who is this plan for?")
                            Spacer(Modifier.height(10.dp))
                            ManzilTextField(
                                value = state.name,
                                onValueChange = viewModel::updateName,
                                label = "Your name",
                                placeholder = "Sufyan"
                            )
                            Spacer(Modifier.height(10.dp))
                            ManzilTextField(
                                value = state.university,
                                onValueChange = viewModel::updateUniversity,
                                label = "University",
                                placeholder = "COMSATS Sahiwal"
                            )
                            Spacer(Modifier.height(10.dp))
                            ManzilTextField(
                                value = state.semester,
                                onValueChange = viewModel::updateSemester,
                                label = "Semester",
                                placeholder = "3rd"
                            )
                        }
                    }
                }
                1 -> {
                    item {
                        Text(
                            "What are you building?",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Write the goal you actually want — software house, freelancing, remote job, anything.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    item {
                        ManzilCard {
                            ManzilTextField(
                                value = state.mainGoal,
                                onValueChange = viewModel::updateMainGoal,
                                label = "Main goal",
                                placeholder = "Software house in Sahiwal",
                                singleLine = false,
                                minLines = 2
                            )
                            Spacer(Modifier.height(10.dp))
                            ManzilTextField(
                                value = state.targetDate,
                                onValueChange = viewModel::updateTargetDate,
                                label = "Target (flexible)",
                                placeholder = "2028, or when I hit 3.5L/month"
                            )
                        }
                    }
                    item {
                        ManzilCard(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.Flag,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Perpetual goal engine",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "If a day goes wrong, work is not lost — it rolls to tomorrow with the reason written down. " +
                                    "The plan adjusts to your real life.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
                2 -> {
                    item {
                        Text(
                            "Skills & time",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Manzil uses this to size tasks realistically.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    item {
                        ManzilCard {
                            ManzilTextField(
                                value = state.skills,
                                onValueChange = viewModel::updateSkills,
                                label = "Current skills",
                                placeholder = "HTML, CSS, JavaScript",
                                singleLine = false,
                                minLines = 2
                            )
                            Spacer(Modifier.height(12.dp))
                            Text("Daily deep work hours", style = MaterialTheme.typography.labelMedium)
                            Spacer(Modifier.height(6.dp))
                            ChipRow(
                                options = listOf("1h", "2h", "3h", "4h", "6h"),
                                selectedIndex = listOf(1, 2, 3, 4, 6).indexOf(state.dailyHours).coerceAtLeast(2),
                                onSelect = { index ->
                                    viewModel.setDailyHours(listOf(1, 2, 3, 4, 6)[index])
                                }
                            )
                        }
                    }
                    item {
                        ManzilCard {
                            SectionHeader(title = "Where will you hunt clients?")
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Free channels only — no marketplace fees. Pick what you will actually use.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(10.dp))
                            ChipRow(
                                options = state.allPlatforms,
                                selectedIndex = -1,
                                onSelect = { index -> viewModel.togglePlatform(state.allPlatforms[index]) }
                            )
                            Spacer(Modifier.height(8.dp))
                            state.selectedPlatforms.forEach { platform ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = ManzilColors.success,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(platform, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
                else -> {
                    item {
                        Text(
                            "How should I remind you?",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "One morning briefing, one evening review. Nothing else unless you ask.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    item {
                        ManzilCard {
                            Text(
                                "Morning briefing",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Spacer(Modifier.height(8.dp))
                            ChipRow(
                                options = listOf("06:00", "07:00", "08:00", "09:00"),
                                selectedIndex = listOf("06:00", "07:00", "08:00", "09:00")
                                    .indexOf(state.morningTime).coerceAtLeast(1),
                                onSelect = { index ->
                                    viewModel.setMorning(listOf("06:00", "07:00", "08:00", "09:00")[index])
                                }
                            )
                            Spacer(Modifier.height(14.dp))
                            Text("Evening review", style = MaterialTheme.typography.titleSmall)
                            Spacer(Modifier.height(8.dp))
                            ChipRow(
                                options = listOf("20:00", "21:00", "21:30", "22:00"),
                                selectedIndex = listOf("20:00", "21:00", "21:30", "22:00")
                                    .indexOf(state.eveningTime).coerceAtLeast(2),
                                onSelect = { index ->
                                    viewModel.setEvening(listOf("20:00", "21:00", "21:30", "22:00")[index])
                                }
                            )
                        }
                    }
                    item {
                        ManzilCard {
                            SectionHeader(title = "Ready to start")
                            Spacer(Modifier.height(8.dp))
                            KeyValueRow("Name", state.name.ifBlank { "—" })
                            KeyValueRow("Goal", state.mainGoal.ifBlank { "Software house" })
                            KeyValueRow("Deep work", "${state.dailyHours}h / day")
                            KeyValueRow("Briefing", state.morningTime)
                            KeyValueRow("Review", state.eveningTime)
                        }
                    }
                    item {
                        ManzilCard(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.Lock,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Your data stays on this phone",
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "No account, no tracking, no ads. An OpenRouter key is optional and only used for AI features.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (state.step > 0) {
                TextButton(onClick = viewModel::back) { Text("Back") }
            }
            Spacer(Modifier.weight(1f))
            if (state.step < 3) {
                Button(
                    onClick = viewModel::next,
                    shape = RoundedCornerShape(14.dp)
                ) { Text("Continue") }
            } else {
                Button(
                    onClick = {
                        viewModel.finish()
                        onFinished()
                    },
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Filled.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Start my plan")
                }
            }
        }
    }
}
