package com.example.ui.viewmodel

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.db.CachedMessage
import com.example.data.db.TempMailbox
import com.example.data.repository.TempMailRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TempMailViewModel(private val repository: TempMailRepository) : ViewModel() {

    private val _currentMailbox = MutableStateFlow<TempMailbox?>(null)
    val currentMailbox: StateFlow<TempMailbox?> = _currentMailbox.asStateFlow()

    val allMailboxes: StateFlow<List<TempMailbox>> = repository.allMailboxes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val messages: StateFlow<List<CachedMessage>> = _currentMailbox
        .flatMapLatest { mailbox ->
            if (mailbox != null) {
                repository.getMessagesForMailbox(mailbox.emailAddress)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val unreadCount: StateFlow<Int> = messages
        .map { list -> list.count { !it.isRead } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    private val _availableDomains = MutableStateFlow<List<String>>(listOf("1secmail.com", "1secmail.org", "1secmail.net"))
    val availableDomains: StateFlow<List<String>> = _availableDomains.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _autoRefreshInterval = MutableStateFlow(10) // default 10s
    val autoRefreshInterval: StateFlow<Int> = _autoRefreshInterval.asStateFlow()

    private val _refreshCountdown = MutableStateFlow(10)
    val refreshCountdown: StateFlow<Int> = _refreshCountdown.asStateFlow()

    private val _language = MutableStateFlow("BN") // "BN" or "EN"
    val language: StateFlow<String> = _language.asStateFlow()

    private val _selectedMessage = MutableStateFlow<CachedMessage?>(null)
    val selectedMessage: StateFlow<CachedMessage?> = _selectedMessage.asStateFlow()

    private val _showQRCodeDialog = MutableStateFlow(false)
    val showQRCodeDialog: StateFlow<Boolean> = _showQRCodeDialog.asStateFlow()

    private val _showCustomEmailDialog = MutableStateFlow(false)
    val showCustomEmailDialog: StateFlow<Boolean> = _showCustomEmailDialog.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private var countdownJob: Job? = null

    init {
        loadAvailableDomains()
        initializeDefaultMailbox()
        startCountdownTimer()
    }

    private fun loadAvailableDomains() {
        viewModelScope.launch {
            val domains = repository.getDomainList()
            if (domains.isNotEmpty()) {
                _availableDomains.value = domains
            }
        }
    }

    private fun initializeDefaultMailbox() {
        viewModelScope.launch {
            _isLoading.value = true
            // Check if database already has mailboxes
            repository.allMailboxes.collect { list ->
                if (list.isNotEmpty() && _currentMailbox.value == null) {
                    _currentMailbox.value = list.first()
                    _isLoading.value = false
                    refreshInboxInternal()
                } else if (list.isEmpty() && _currentMailbox.value == null) {
                    val newMailbox = repository.generateRandomMailbox()
                    _currentMailbox.value = newMailbox
                    _isLoading.value = false
                    refreshInboxInternal()
                }
            }
        }
    }

    fun generateNewRandomMailbox() {
        viewModelScope.launch {
            _isLoading.value = true
            val newMailbox = repository.generateRandomMailbox()
            _currentMailbox.value = newMailbox
            _isLoading.value = false
            showToast(if (_language.value == "BN") "নতুন ইমেইল তৈরি হয়েছে!" else "New temp email created!")
            resetCountdown()
            refreshInboxInternal()
        }
    }

    fun createCustomMailbox(username: String, domain: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val newMailbox = repository.createCustomMailbox(username, domain)
            _currentMailbox.value = newMailbox
            _isLoading.value = false
            _showCustomEmailDialog.value = false
            showToast(if (_language.value == "BN") "কাস্টম ইমেইল তৈরি হয়েছে!" else "Custom email created!")
            resetCountdown()
            refreshInboxInternal()
        }
    }

    fun selectMailbox(mailbox: TempMailbox) {
        _currentMailbox.value = mailbox
        resetCountdown()
        refreshInboxInternal()
    }

    fun refreshInboxManually() {
        resetCountdown()
        refreshInboxInternal()
    }

    private fun refreshInboxInternal() {
        val mailbox = _currentMailbox.value ?: return
        viewModelScope.launch {
            _isRefreshing.value = true
            repository.refreshMessages(mailbox)
            _isRefreshing.value = false
        }
    }

    fun sendTestEmail() {
        val mailbox = _currentMailbox.value ?: return
        viewModelScope.launch {
            val testCode = (100000..999999).random().toString()
            val subjects = listOf(
                "Your Verification Code is $testCode / ওটিপি কোড: $testCode",
                "Security Login Alert - Code: $testCode",
                "Account Activation Code: $testCode"
            )
            val senders = listOf(
                "verify@service-login.com",
                "no-reply@security-auth.net",
                "accounts@digital-app.org"
            )
            val subject = subjects.random()
            val sender = senders.random()
            val body = "Hello, your dynamic verification code is $testCode. Please enter this code within 10 minutes to verify your identity.\n\nধন্যবাদ! আপনার ওটিপি কোডটি হলো $testCode"

            repository.insertDemoMessage(mailbox, sender, subject, body)
            showToast(if (_language.value == "BN") "টেস্ট ইমেইল পাঠানো হয়েছে! (OTP: $testCode)" else "Test email sent! (OTP: $testCode)")
        }
    }

    private fun startCountdownTimer() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                val interval = _autoRefreshInterval.value
                if (interval > 0) {
                    val nextVal = _refreshCountdown.value - 1
                    if (nextVal <= 0) {
                        _refreshCountdown.value = interval
                        refreshInboxInternal()
                    } else {
                        _refreshCountdown.value = nextVal
                    }
                }
            }
        }
    }

    private fun resetCountdown() {
        _refreshCountdown.value = _autoRefreshInterval.value
    }

    fun setAutoRefreshInterval(seconds: Int) {
        _autoRefreshInterval.value = seconds
        resetCountdown()
    }

    fun toggleLanguage() {
        _language.value = if (_language.value == "BN") "EN" else "BN"
    }

    fun setLanguage(lang: String) {
        _language.value = lang
    }

    fun openMessageDetail(message: CachedMessage) {
        _selectedMessage.value = message
        if (!message.isRead) {
            viewModelScope.launch {
                repository.markAsRead(message.compositeId)
            }
        }
    }

    fun closeMessageDetail() {
        _selectedMessage.value = null
    }

    fun deleteMessage(message: CachedMessage) {
        viewModelScope.launch {
            repository.deleteMessage(message.compositeId)
            if (_selectedMessage.value?.compositeId == message.compositeId) {
                _selectedMessage.value = null
            }
            showToast(if (_language.value == "BN") "ইমেইল মুছে ফেলা হয়েছে" else "Email deleted")
        }
    }

    fun deleteCurrentMailbox() {
        val mailbox = _currentMailbox.value ?: return
        deleteMailbox(mailbox)
    }

    fun deleteMailbox(mailbox: TempMailbox) {
        viewModelScope.launch {
            repository.deleteMailbox(mailbox.emailAddress)
            if (_currentMailbox.value?.emailAddress == mailbox.emailAddress) {
                _currentMailbox.value = null
            }
            showToast(if (_language.value == "BN") "ইনবক্স মুছে দেওয়া হয়েছে" else "Mailbox deleted")
        }
    }

    fun toggleFavorite(mailbox: TempMailbox) {
        viewModelScope.launch {
            repository.toggleFavorite(mailbox.emailAddress, mailbox.isFavorite)
        }
    }

    fun showQRCode(show: Boolean) {
        _showQRCodeDialog.value = show
    }

    fun showCustomEmailDialog(show: Boolean) {
        _showCustomEmailDialog.value = show
    }

    fun copyToClipboard(context: Context, text: String, label: String = "Temp Mail") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        showToast(if (_language.value == "BN") "কপি করা হয়েছে: $text" else "Copied: $text")
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    private fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    class Factory(private val repository: TempMailRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TempMailViewModel(repository) as T
        }
    }
}
