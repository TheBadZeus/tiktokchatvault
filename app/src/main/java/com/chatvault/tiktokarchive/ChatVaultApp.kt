package com.chatvault.tiktokarchive

import android.app.Application
import android.content.Context
import com.chatvault.tiktokarchive.data.database.ChatVaultDatabase
import com.chatvault.tiktokarchive.data.prefs.SettingsStore
import com.chatvault.tiktokarchive.data.repository.ArchiveRepository

/** Hand-rolled dependency container (no DI framework needed at this size). */
class AppContainer(private val context: Context) {
    val database: ChatVaultDatabase by lazy { ChatVaultDatabase.build(context) }
    val repository: ArchiveRepository by lazy { ArchiveRepository(database) }
    val settings: SettingsStore by lazy { SettingsStore(context) }

    fun databaseSizeBytes(): Long {
        var total = 0L
        for (suffix in listOf("", "-wal", "-shm")) {
            val f = context.getDatabasePath(ChatVaultDatabase.FILE_NAME + suffix)
            if (f.exists()) total += f.length()
        }
        return total
    }
}

class ChatVaultApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
