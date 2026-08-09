package com.gameforge.app.core.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey val id: String,
    val title: String,
    val developer: String,
    val gameVersion: String,
    val adapterVersion: String,
    val engine: String,
    val connectionType: String,
    val configPath: String,
    val isSupported: Boolean = true
)

@Entity(tableName = "backups")
data class BackupEntity(
    @PrimaryKey val backupId: String,
    val gameId: String,
    val timestamp: Long,
    val filePath: String,
    val note: String
)

@Entity(tableName = "custom_adapters")
data class CustomAdapterSchemaEntity(
    @PrimaryKey val adapterId: String,
    val gameTitle: String,
    val jsonSchema: String,
    val createdAt: Long
)
