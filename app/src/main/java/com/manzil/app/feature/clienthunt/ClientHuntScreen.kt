package com.manzil.app.feature.clienthunt

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ClientHuntScreen(viewModel: ClientHuntViewModel = hiltViewModel()) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Modern Client Hunting", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("No Fiverr/Upwork torture — Latest methods 2026-27", style = MaterialTheme.typography.bodyMedium)

        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Instagram DM Outreach", fontWeight = FontWeight.Bold)
                Text("Search: #smallbusiness #sahiwal #lahorebusiness", style = MaterialTheme.typography.bodySmall)
                Text("Script: 'Hey [Name], saw your [business] — I help similar stores get online orders via WhatsApp bot. Made 1 for [example] — 3x orders. Want free demo?'", style = MaterialTheme.typography.bodySmall)
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("LinkedIn Outbound (20/day personalized)", fontWeight = FontWeight.Bold)
                Text("Search: Founder + e-commerce + Pakistan/US", style = MaterialTheme.typography.bodySmall)
                Text("No spam — value first", style = MaterialTheme.typography.bodySmall)
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Facebook Groups + Reddit + Discord", fontWeight = FontWeight.Bold)
                Text("Sahiwal Business, Pak Freelancers, r/forhire, IndieHackers, Discord dev communities", style = MaterialTheme.typography.bodySmall)
            }
        }

        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
            Column(Modifier.padding(16.dp)) {
                Text("X/Twitter Threads + Product Hunt", fontWeight = FontWeight.Bold)
                Text("Build in public — daily 1 post about your BSSE journey + project", style = MaterialTheme.typography.bodySmall)
            }
        }

        Button(onClick = {}, modifier = Modifier.fillMaxWidth()) {
            Text("Generate Today's Client Action (AI)")
        }
    }
}
