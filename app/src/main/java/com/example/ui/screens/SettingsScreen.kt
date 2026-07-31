package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    isBangla: Boolean,
    autoRefreshSec: Int,
    onLanguageChange: (String) -> Unit,
    onAutoRefreshChange: (Int) -> Unit,
    onSendTestEmail: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Language Option Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isBangla) "অ্যাপের ভাষা (Language)" else "App Language",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = isBangla,
                        onClick = { onLanguageChange("BN") },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) {
                        Text("বাংলা 🇧🇩")
                    }
                    SegmentedButton(
                        selected = !isBangla,
                        onClick = { onLanguageChange("EN") },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) {
                        Text("English 🇬🇧")
                    }
                }
            }
        }

        // Auto Refresh Rate Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (isBangla) "স্বয়ংক্রিয় রিফ্রেশ সময়" else "Auto-Refresh Interval",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(5, 10, 30, 0).forEach { sec ->
                        val selected = autoRefreshSec == sec
                        val label = when (sec) {
                            5 -> "5s"
                            10 -> "10s"
                            30 -> "30s"
                            else -> if (isBangla) "বন্ধ" else "Off"
                        }
                        FilterChip(
                            selected = selected,
                            onClick = { onAutoRefreshChange(sec) },
                            label = { Text(label) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Test Inbox Feature
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MarkEmailUnread,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (isBangla) "টেস্ট ইমেইল ও ওটিপি পরীক্ষা" else "Test Inbox & OTP Extraction",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isBangla) "ইনবক্সে তাৎক্ষণিক একটি পরীক্ষা মূলক ওটিপি কোড ইমেইল পাঠাতে নিচের বাটনে ট্যাপ করুন:"
                    else "Tap the button below to send a sample email with verification code to test your inbox:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onSendTestEmail,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.BugReport, contentDescription = null)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(if (isBangla) "টেস্ট ইমেইল পাঠান (Send Test Mail)" else "Send Test Mail")
                }
            }
        }

        // About & Tech Specs
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (isBangla) "অ্যাপ সম্পর্কে" else "About Temp Mail",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isBangla)
                        "টেম্প মেইল একটি নিরাপদ এবং ফাস্ট টেম্পোরারি ইমেইল সার্ভিস। এতে রয়েছে রিয়েল-টাইম অটো ইনবক্স আপডেট, কিউআর কোড স্ক্যানার এবং স্বয়ংক্রিয় ওটিপি কোড ডিটেক্টর।"
                    else
                        "Temp Mail is a fast, safe temporary mailbox generator. Features include real-time inbox refresh, QR code generator, and automatic OTP verification code detection.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
