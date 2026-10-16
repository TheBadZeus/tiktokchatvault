package com.chatvault.tiktokarchive.data.database

import androidx.room.migration.Migration

/**
 * One Migration per schema version bump. This database holds user evidence, so
 * fallbackToDestructiveMigration() must never be used.
 */
object Migrations {
    val ALL: Array<Migration> = arrayOf()
}
