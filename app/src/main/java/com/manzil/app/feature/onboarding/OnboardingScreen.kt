package com.manzil.app.feature.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * BS Software Engineering Edition Onboarding
 * Collects: Name, University, Semester, Skills, Main Goal, Daily Hours, Language, Notification Time
 * As per user request: "Starting ma mara name wagara or information sare ly app muj sa"
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = hiltViewModel(),
    onComplete: () -> Unit = {}
) {
    var currentStep by remember { mutableStateOf(0) }
    val totalSteps = 4

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        LinearProgressIndicator(
            progress = { (currentStep + 1) / totalSteps.toFloat() },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(24.dp))

        when (currentStep) {
            0 -> Step1_PersonalInfo(viewModel)
            1 -> Step2_GoalInfo(viewModel)
            2 -> Step3_SkillsAndTime(viewModel)
            3 -> Step4_NotificationsAndFinish(viewModel)
        }

        Spacer(Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (currentStep > 0) {
                OutlinedButton(onClick = { currentStep-- }) {
                    Text("Back")
                }
            } else {
                Spacer(Modifier.width(1.dp))
            }

            Button(
                onClick = {
                    if (currentStep < totalSteps - 1) {
                        currentStep++
                    } else {
                        viewModel.saveOnboarding()
                        onComplete()
                    }
                }
            ) {
                Text(if (currentStep == totalSteps - 1) "Start My Journey" else "Continue")
            }
        }
    }
}

@Composable
fun Step1_PersonalInfo(viewModel: OnboardingViewModel) {
    var name by remember { mutableStateOf("") }
    var university by remember { mutableStateOf("") }
    var semester by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Welcome to Manzil", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("BS Software Engineering Edition — Let's know you", style = MaterialTheme.typography.bodyLarge)

        OutlinedTextField(value = name, onValueChange = { name = it; viewModel.name = it }, label = { Text("Your full name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = university, onValueChange = { university = it; viewModel.university = it }, label = { Text("University (e.g. COMSATS Sahiwal)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = semester, onValueChange = { semester = it; viewModel.semester = it }, label = { Text("Semester (e.g. 1st, 3rd, 5th)") }, modifier = Modifier.fillMaxWidth())
        
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(Modifier.padding(16.dp)) {
                Text("Why we ask?", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text("So AI can create semester-aware tasks. Exams time pe tasks auto-adjust ho jayenge.", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun Step2_GoalInfo(viewModel: OnboardingViewModel) {
    var mainGoal by remember { mutableStateOf("") }
    var targetDate by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("What are you building?", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("No fixed 2030 — Your goal lives till you complete it", style = MaterialTheme.typography.bodyMedium)

        OutlinedTextField(value = mainGoal, onValueChange = { mainGoal = it; viewModel.mainGoal = it }, label = { Text("Main Goal (e.g. Software House, Top Freelancer, Remote Job at FAANG)") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
        OutlinedTextField(value = targetDate, onValueChange = { targetDate = it; viewModel.targetDate = it }, label = { Text("Target Date (Flexible — e.g. 2028, or 'When I hit 5L/month')") }, modifier = Modifier.fillMaxWidth())

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Perpetual Goal Engine", fontWeight = FontWeight.Bold)
                Text("• Goal tab tak chalega jab tak DONE na ho", style = MaterialTheme.typography.bodySmall)
                Text("• AI har week goal condition check karega", style = MaterialTheme.typography.bodySmall)
                Text("• Time ke sath latest info se tasks update honge", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun Step3_SkillsAndTime(viewModel: OnboardingViewModel) {
    var skills by remember { mutableStateOf("") }
    var hours by remember { mutableStateOf("3") }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Skills & Time", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

        OutlinedTextField(value = skills, onValueChange = { skills = it; viewModel.skills = it }, label = { Text("Current Skills (e.g. HTML, CSS, JS, React)") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
        OutlinedTextField(value = hours, onValueChange = { hours = it; viewModel.dailyHours = it.toIntOrNull() ?: 3 }, label = { Text("Daily Deep Work Hours") }, modifier = Modifier.fillMaxWidth())

        Text("Client Hunting Preference (Modern — Not Fiverr/Upwork torture):", style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            listOf("Instagram", "LinkedIn", "Facebook", "X/Twitter", "Reddit", "Discord").forEach {
                FilterChip(selected = viewModel.preferredPlatforms.contains(it), onClick = { viewModel.togglePlatform(it) }, label = { Text(it) })
            }
        }
    }
}

@Composable
fun Step4_NotificationsAndFinish(viewModel: OnboardingViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("How should I remind you?", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Manzil will tell you: Yeh karna hai / Yeh ho gaya / Yeh reh gaya", style = MaterialTheme.typography.bodyLarge)

        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Smart Features Enabled:", fontWeight = FontWeight.Bold)
                Text("✅ Auto-rollover: Unfinished task → Kal ke sath adjust", style = MaterialTheme.typography.bodySmall)
                Text("✅ AI Strict Task Manager: Condition dekh ke next tasks arrange", style = MaterialTheme.typography.bodySmall)
                Text("✅ Latest Info Sync: Market trends se tasks update", style = MaterialTheme.typography.bodySmall)
                Text("✅ Modern Client Hunt: Insta, FB, LinkedIn, X, Reddit, Discord", style = MaterialTheme.typography.bodySmall)
                Text("✅ Offline + Online: 100% offline, AI online", style = MaterialTheme.typography.bodySmall)
                Text("✅ Premium UI: Material 3 Expressive", style = MaterialTheme.typography.bodySmall)
            }
        }

        OutlinedTextField(value = viewModel.morningTime, onValueChange = { viewModel.morningTime = it }, label = { Text("Morning Briefing Time (e.g. 07:00)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = viewModel.eveningTime, onValueChange = { viewModel.eveningTime = it }, label = { Text("Evening Review Time (e.g. 21:30)") }, modifier = Modifier.fillMaxWidth())
    }
}
