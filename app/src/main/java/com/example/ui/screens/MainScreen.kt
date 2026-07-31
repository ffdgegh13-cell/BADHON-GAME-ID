package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.db.CachedMessage
import com.example.ui.components.OTPBadge
import com.example.ui.theme.EmeraldActive
import com.example.ui.viewmodel.TempMailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: TempMailViewModel) {
    val context = LocalContext.current
    val currentMailbox by viewModel.currentMailbox.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val allMailboxes by viewModel.allMailboxes.collectAsStateWithLifecycle()
    val availableDomains by viewModel.availableDomains.collectAsStateWithLifecycle()
    val unreadCount by viewModel.unreadCount.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val autoRefreshSec by viewModel.autoRefreshInterval.collectAsStateWithLifecycle()
    val countdown by viewModel.refreshCountdown.collectAsStateWithLifecycle()
    val language by viewModel.language.collectAsStateWithLifecycle()
    val selectedMessage by viewModel.selectedMessage.collectAsStateWithLifecycle()
    val showQRCodeDialog by viewModel.showQRCodeDialog.collectAsStateWithLifecycle()
    val showCustomEmailDialog by viewModel.showCustomEmailDialog.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

    val isBangla = language == "BN"
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearToast()
        }
    }

    // Detail Screen Overlay
    selectedMessage?.let { msg ->
        MessageDetailScreen(
            message = msg,
            isBangla = isBangla,
            onBack = { viewModel.closeMessageDetail() },
            onDelete = { viewModel.deleteMessage(msg) },
            onCopyOtp = { otp -> viewModel.copyToClipboard(context, otp, "OTP Code") }
        )
        return
    }

    // Dialogs
    if (showQRCodeDialog && currentMailbox != null) {
        QRCodeDialog(
            emailAddress = currentMailbox!!.emailAddress,
            isBangla = isBangla,
            onDismiss = { viewModel.showQRCode(false) },
            onCopy = { viewModel.copyToClipboard(context, currentMailbox!!.emailAddress) }
        )
    }

    if (showCustomEmailDialog) {
        CustomEmailDialog(
            domains = availableDomains,
            isBangla = isBangla,
            onDismiss = { viewModel.showCustomEmailDialog(false) },
            onCreate = { user, domain -> viewModel.createCustomMailbox(user, domain) }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(EmeraldActive)
                        )
                        Text(
                            text = if (isBangla) "টেম্প মেইল (Temp Mail)" else "Temp Mail",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        )
                    }
                },
                actions = {
                    // Quick Language Switch Button
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clickable { viewModel.toggleLanguage() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "Language",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = if (isBangla) "বাংলা" else "English",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        BadgedBox(badge = {
                            if (unreadCount > 0) {
                                Badge { Text("$unreadCount") }
                            }
                        }) {
                            Icon(imageVector = Icons.Default.Inbox, contentDescription = "Inbox")
                        }
                    },
                    label = { Text(if (isBangla) "ইনবক্স" else "Inbox") }
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(imageVector = Icons.Default.History, contentDescription = "Mailboxes") },
                    label = { Text(if (isBangla) "সংরক্ষিত" else "Mailboxes") }
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text(if (isBangla) "সেটিংস" else "Settings") }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> {
                    // Active Email Banner Card
                    ActiveEmailBanner(
                        currentMailbox = currentMailbox,
                        isBangla = isBangla,
                        isRefreshing = isRefreshing,
                        isLoading = isLoading,
                        autoRefreshSec = autoRefreshSec,
                        countdown = countdown,
                        onCopy = {
                            currentMailbox?.let { viewModel.copyToClipboard(context, it.emailAddress) }
                        },
                        onQRCode = { viewModel.showQRCode(true) },
                        onNewRandom = { viewModel.generateNewRandomMailbox() },
                        onCustomEmail = { viewModel.showCustomEmailDialog(true) },
                        onRefreshInbox = { viewModel.refreshInboxManually() }
                    )

                    // Search & Inbox Message List
                    val filteredMessages = remember(messages, searchQuery) {
                        if (searchQuery.isBlank()) messages
                        else messages.filter {
                            it.subject.contains(searchQuery, ignoreCase = true) ||
                                    it.fromAddress.contains(searchQuery, ignoreCase = true) ||
                                    it.bodyText.contains(searchQuery, ignoreCase = true)
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(12.dp))

                        // Search Field
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text(if (isBangla) "ইনবক্সে ইমেইল খুঁজুন..." else "Search inbox...") },
                            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        if (filteredMessages.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.MarkEmailUnread,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = if (isBangla) "ইনবক্স খালি! ইমেইল আসা পর্যন্ত অপেক্ষা করা হচ্ছে..." else "Inbox is empty! Waiting for incoming emails...",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    OutlinedButton(
                                        onClick = { viewModel.sendTestEmail() }
                                    ) {
                                        Text(if (isBangla) "টেস্ট ইমেইল পান (Get Test Email)" else "Get Test Email")
                                    }
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(filteredMessages, key = { it.compositeId }) { item ->
                                    InboxMessageCard(
                                        message = item,
                                        isBangla = isBangla,
                                        onClick = { viewModel.openMessageDetail(item) },
                                        onCopyOtp = { otp ->
                                            viewModel.copyToClipboard(context, otp, "OTP Code")
                                        },
                                        onDelete = { viewModel.deleteMessage(item) }
                                    )
                                }
                                item { Spacer(modifier = Modifier.height(16.dp)) }
                            }
                        }
                    }
                }

                1 -> {
                    SavedMailboxesScreen(
                        mailboxes = allMailboxes,
                        activeMailbox = currentMailbox,
                        isBangla = isBangla,
                        onSelectMailbox = { viewModel.selectMailbox(it) },
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                        onDeleteMailbox = { viewModel.deleteMailbox(it) }
                    )
                }

                2 -> {
                    SettingsScreen(
                        isBangla = isBangla,
                        autoRefreshSec = autoRefreshSec,
                        onLanguageChange = { viewModel.setLanguage(it) },
                        onAutoRefreshChange = { viewModel.setAutoRefreshInterval(it) },
                        onSendTestEmail = { viewModel.sendTestEmail() }
                    )
                }
            }
        }
    }
}

@Composable
fun ActiveEmailBanner(
    currentMailbox: com.example.data.db.TempMailbox?,
    isBangla: Boolean,
    isRefreshing: Boolean,
    isLoading: Boolean,
    autoRefreshSec: Int,
    countdown: Int,
    onCopy: () -> Unit,
    onQRCode: () -> Unit,
    onNewRandom: () -> Unit,
    onCustomEmail: () -> Unit,
    onRefreshInbox: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isBangla) "আপনার সক্রিয় অস্থায়ী ইমেইল:" else "Your Temporary Email Address:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                if (autoRefreshSec > 0) {
                    Text(
                        text = if (isBangla) "অটো রিফ্রেশ: ${countdown}s" else "Auto refresh: ${countdown}s",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Email address display container
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text(
                            text = currentMailbox?.emailAddress ?: "...",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = onCopy) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = onQRCode) {
                            Icon(
                                imageVector = Icons.Default.QrCode2,
                                contentDescription = "QR Code",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onNewRandom,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Autorenew,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isBangla) "নতুন ইমেইল" else "New Email",
                        fontSize = 13.sp
                    )
                }

                OutlinedButton(
                    onClick = onCustomEmail,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isBangla) "কাস্টম" else "Custom",
                        fontSize = 13.sp
                    )
                }

                IconButton(
                    onClick = onRefreshInbox,
                    modifier = Modifier.background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    )
                ) {
                    if (isRefreshing) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            if (autoRefreshSec > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                val progress = (countdown.toFloat() / autoRefreshSec.toFloat()).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun InboxMessageCard(
    message: CachedMessage,
    isBangla: Boolean,
    onClick: () -> Unit,
    onCopyOtp: (String) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (!message.isRead) MaterialTheme.colorScheme.surfaceVariant
            else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    if (!message.isRead) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                    Text(
                        text = message.fromAddress,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (!message.isRead) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = message.dateString,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = message.subject.ifBlank { if (isBangla) "(বিষয়বস্তু নেই)" else "(No Subject)" },
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            val previewText = message.bodyText.ifBlank { message.bodyHtml.replace(Regex("<[^>]*>"), "") }
            if (previewText.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = previewText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            message.extractedOtp?.let { otp ->
                Spacer(modifier = Modifier.height(8.dp))
                OTPBadge(
                    otpCode = otp,
                    onCopyClick = { onCopyOtp(otp) }
                )
            }
        }
    }
}
