package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.db.TempMailbox

@Composable
fun SavedMailboxesScreen(
    mailboxes: List<TempMailbox>,
    activeMailbox: TempMailbox?,
    isBangla: Boolean,
    onSelectMailbox: (TempMailbox) -> Unit,
    onToggleFavorite: (TempMailbox) -> Unit,
    onDeleteMailbox: (TempMailbox) -> Unit,
    modifier: Modifier = Modifier
) {
    if (mailboxes.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isBangla) "কোনো সংরক্ষিত ইমেইল পাওয়া যায়নি" else "No saved temporary mailboxes",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    text = if (isBangla) "আপনার পূর্বে তৈরি ইমেইলসমূহ:" else "Your Temp Mail History:",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(mailboxes, key = { it.emailAddress }) { mailbox ->
                val isActive = activeMailbox?.emailAddress == mailbox.emailAddress

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        else MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectMailbox(mailbox) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isActive) Icons.Default.CheckCircle else Icons.Default.Email,
                            contentDescription = null,
                            tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = mailbox.emailAddress,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isActive) (if (isBangla) "সক্রিয় ইনবক্স" else "Active Mailbox") else (if (isBangla) "ট্যাপ করে পরিবর্তন করুন" else "Tap to switch"),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(onClick = { onToggleFavorite(mailbox) }) {
                            Icon(
                                imageVector = if (mailbox.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                                contentDescription = "Favorite",
                                tint = if (mailbox.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(onClick = { onDeleteMailbox(mailbox) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }
    }
}
