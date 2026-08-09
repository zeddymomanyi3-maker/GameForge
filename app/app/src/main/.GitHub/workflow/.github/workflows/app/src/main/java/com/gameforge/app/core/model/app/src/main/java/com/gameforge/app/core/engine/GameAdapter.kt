package com.gameforge.app.core.engine

import com.gameforge.app.core.model.*

interface GameAdapter {
    val manifest: GameManifest
    suspend fun checkConnection(): ConnectionStatus
    suspend fun fetchParameters(): Result<List<GameParameter>>
    suspend fun applyParameters(parameters: List<GameParameter>): Result<Boolean>
    suspend fun createBackup(): Result<String>
    suspend fun restoreBackup(backupId: String): Result<Boolean>
}
