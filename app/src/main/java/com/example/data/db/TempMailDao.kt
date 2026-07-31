package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TempMailDao {

    // Mailboxes
    @Query("SELECT * FROM temp_mailboxes ORDER BY createdAt DESC")
    fun getAllMailboxes(): Flow<List<TempMailbox>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMailbox(mailbox: TempMailbox)

    @Query("DELETE FROM temp_mailboxes WHERE emailAddress = :email")
    suspend fun deleteMailbox(email: String)

    @Query("UPDATE temp_mailboxes SET isFavorite = :isFav WHERE emailAddress = :email")
    suspend fun updateFavorite(email: String, isFav: Boolean)

    // Messages
    @Query("SELECT * FROM cached_messages WHERE mailboxAddress = :email ORDER BY messageId DESC")
    fun getMessagesForMailbox(email: String): Flow<List<CachedMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<CachedMessage>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: CachedMessage)

    @Query("UPDATE cached_messages SET isRead = 1 WHERE compositeId = :compositeId")
    suspend fun markMessageAsRead(compositeId: String)

    @Query("DELETE FROM cached_messages WHERE compositeId = :compositeId")
    suspend fun deleteMessage(compositeId: String)

    @Query("DELETE FROM cached_messages WHERE mailboxAddress = :email")
    suspend fun clearMessagesForMailbox(email: String)
}
