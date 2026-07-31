package com.example.data.repository

import com.example.data.api.SecMailApi
import com.example.data.db.CachedMessage
import com.example.data.db.TempMailDao
import com.example.data.db.TempMailbox
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class TempMailRepository(private val dao: TempMailDao) {

    private val api: SecMailApi by lazy {
        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl("https://www.1secmail.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(SecMailApi::class.java)
    }

    val allMailboxes: Flow<List<TempMailbox>> = dao.getAllMailboxes()

    fun getMessagesForMailbox(email: String): Flow<List<CachedMessage>> {
        return dao.getMessagesForMailbox(email)
    }

    suspend fun getDomainList(): List<String> = withContext(Dispatchers.IO) {
        try {
            val response = api.getDomainList()
            if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                response.body()!!
            } else {
                listOf("1secmail.com", "1secmail.org", "1secmail.net")
            }
        } catch (e: Exception) {
            listOf("1secmail.com", "1secmail.org", "1secmail.net")
        }
    }

    suspend fun generateRandomMailbox(): TempMailbox = withContext(Dispatchers.IO) {
        try {
            val response = api.genRandomMailbox(1)
            if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                val email = response.body()!![0]
                val parts = email.split("@")
                val username = parts.getOrNull(0) ?: "user${System.currentTimeMillis() % 10000}"
                val domain = parts.getOrNull(1) ?: "1secmail.com"
                val mailbox = TempMailbox(emailAddress = email, username = username, domain = domain)
                dao.insertMailbox(mailbox)
                // Add welcome email
                insertWelcomeEmailIfNeeded(mailbox)
                return@withContext mailbox
            }
        } catch (e: Exception) {
            // Network fallback
        }
        val fallbackUser = "temp_${(100000..999999).random()}"
        val fallbackDomain = "1secmail.com"
        val fallbackEmail = "$fallbackUser@$fallbackDomain"
        val mailbox = TempMailbox(emailAddress = fallbackEmail, username = fallbackUser, domain = fallbackDomain)
        dao.insertMailbox(mailbox)
        insertWelcomeEmailIfNeeded(mailbox)
        mailbox
    }

    suspend fun createCustomMailbox(username: String, domain: String): TempMailbox = withContext(Dispatchers.IO) {
        val cleanUser = username.trim().lowercase().replace(Regex("[^a-z0-9._-]"), "")
        val validUser = if (cleanUser.isEmpty()) "user${System.currentTimeMillis() % 10000}" else cleanUser
        val email = "$validUser@$domain"
        val mailbox = TempMailbox(emailAddress = email, username = validUser, domain = domain)
        dao.insertMailbox(mailbox)
        insertWelcomeEmailIfNeeded(mailbox)
        mailbox
    }

    suspend fun refreshMessages(mailbox: TempMailbox): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val response = api.getMessages(mailbox.username, mailbox.domain)
            if (response.isSuccessful) {
                val summaries = response.body() ?: emptyList()
                val newMessages = mutableListOf<CachedMessage>()

                for (summary in summaries) {
                    val compositeId = "${mailbox.emailAddress}_${summary.id}"
                    // Check if message details already read
                    val detailResp = try {
                        api.readMessage(mailbox.username, mailbox.domain, summary.id)
                    } catch (e: Exception) {
                        null
                    }

                    val detail = detailResp?.body()
                    val bodyText = detail?.textBody?.ifBlank { detail.body } ?: detail?.body ?: ""
                    val bodyHtml = detail?.htmlBody ?: detail?.body ?: ""
                    val otp = extractOtp(summary.subject + " " + bodyText)

                    newMessages.add(
                        CachedMessage(
                            compositeId = compositeId,
                            messageId = summary.id,
                            mailboxAddress = mailbox.emailAddress,
                            fromAddress = summary.from,
                            subject = summary.subject,
                            dateString = summary.date,
                            bodyText = bodyText,
                            bodyHtml = bodyHtml,
                            extractedOtp = otp,
                            isRead = false
                        )
                    )
                }

                if (newMessages.isNotEmpty()) {
                    dao.insertMessages(newMessages)
                }
                Result.success(newMessages.size)
            } else {
                Result.failure(Exception("API HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun insertDemoMessage(mailbox: TempMailbox, sender: String, subject: String, body: String) = withContext(Dispatchers.IO) {
        val id = System.currentTimeMillis()
        val compositeId = "${mailbox.emailAddress}_$id"
        val otp = extractOtp("$subject $body")
        val msg = CachedMessage(
            compositeId = compositeId,
            messageId = id,
            mailboxAddress = mailbox.emailAddress,
            fromAddress = sender,
            subject = subject,
            dateString = "Just Now",
            bodyText = body,
            bodyHtml = "<p>$body</p>",
            extractedOtp = otp,
            isRead = false
        )
        dao.insertMessage(msg)
    }

    private suspend fun insertWelcomeEmailIfNeeded(mailbox: TempMailbox) {
        val id = 1001L
        val compositeId = "${mailbox.emailAddress}_$id"
        val welcomeSubject = "Welcome to Temp Mail! 🎉 / টেম্প মেইলে স্বাগতম!"
        val welcomeBody = """
            Hello! Your temporary email address '${mailbox.emailAddress}' is active and ready to receive emails.
            
            Key Features:
            - Instant verification code (OTP) extraction
            - Dynamic QR code for mobile scanning
            - Auto-refresh inbox timer
            - Room database local history storage
            - Dual language support (Bangla 🇧🇩 / English 🇬🇧)
            
            You can use this email address for online signups, verification codes, or temporary testing.
            --------------------------------------------------
            হ্যালো! আপনার অস্থায়ী ইমেইল ঠিকানায় আপনাকে স্বাগতম। আপনি এখন থেকে যে কোনো রেজিস্ট্রেশন বা ওটিপি (OTP) কোড গ্রহণের জন্য এটি ব্যবহার করতে পারবেন।
        """.trimIndent()

        val welcomeMessage = CachedMessage(
            compositeId = compositeId,
            messageId = id,
            mailboxAddress = mailbox.emailAddress,
            fromAddress = "support@tempmail.app",
            subject = welcomeSubject,
            dateString = "Now",
            bodyText = welcomeBody,
            bodyHtml = "<p>${welcomeBody.replace("\n", "<br>")}</p>",
            extractedOtp = null,
            isRead = false
        )
        dao.insertMessage(welcomeMessage)
    }

    suspend fun markAsRead(compositeId: String) = withContext(Dispatchers.IO) {
        dao.markMessageAsRead(compositeId)
    }

    suspend fun deleteMessage(compositeId: String) = withContext(Dispatchers.IO) {
        dao.deleteMessage(compositeId)
    }

    suspend fun deleteMailbox(email: String) = withContext(Dispatchers.IO) {
        dao.clearMessagesForMailbox(email)
        dao.deleteMailbox(email)
    }

    suspend fun toggleFavorite(email: String, currentFav: Boolean) = withContext(Dispatchers.IO) {
        dao.updateFavorite(email, !currentFav)
    }

    private fun extractOtp(text: String): String? {
        val pattern = Pattern.compile("\\b(\\d{4,8})\\b")
        val matcher = pattern.matcher(text)
        if (matcher.find()) {
            return matcher.group(1)
        }
        return null
    }
}
