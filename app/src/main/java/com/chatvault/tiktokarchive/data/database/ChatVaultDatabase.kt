package com.chatvault.tiktokarchive.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.chatvault.tiktokarchive.data.dao.AppSettingDao
import com.chatvault.tiktokarchive.data.dao.AttachmentDao
import com.chatvault.tiktokarchive.data.dao.CaptureEventDao
import com.chatvault.tiktokarchive.data.dao.ContactDao
import com.chatvault.tiktokarchive.data.dao.ConversationDao
import com.chatvault.tiktokarchive.data.dao.EvidenceExportDao
import com.chatvault.tiktokarchive.data.dao.MessageDao
import com.chatvault.tiktokarchive.data.entities.AppSetting
import com.chatvault.tiktokarchive.data.entities.ArchivedMessage
import com.chatvault.tiktokarchive.data.entities.CaptureEvent
import com.chatvault.tiktokarchive.data.entities.Contact
import com.chatvault.tiktokarchive.data.entities.Conversation
import com.chatvault.tiktokarchive.data.entities.EvidenceExport
import com.chatvault.tiktokarchive.data.entities.MessageAttachment

@Database(
    entities = [
        Contact::class,
        Conversation::class,
        ArchivedMessage::class,
        MessageAttachment::class,
        CaptureEvent::class,
        EvidenceExport::class,
        AppSetting::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class ChatVaultDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun attachmentDao(): AttachmentDao
    abstract fun captureEventDao(): CaptureEventDao
    abstract fun exportDao(): EvidenceExportDao
    abstract fun settingDao(): AppSettingDao

    companion object {
        const val FILE_NAME = "chatvault.db"

        fun build(context: Context): ChatVaultDatabase =
            Room.databaseBuilder(context.applicationContext, ChatVaultDatabase::class.java, FILE_NAME)
                .addMigrations(*Migrations.ALL)
                .build()
    }
}
