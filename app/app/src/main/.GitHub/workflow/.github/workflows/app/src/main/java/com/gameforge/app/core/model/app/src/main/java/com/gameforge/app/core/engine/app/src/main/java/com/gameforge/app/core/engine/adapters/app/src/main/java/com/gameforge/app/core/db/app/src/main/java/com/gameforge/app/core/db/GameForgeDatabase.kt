package com.gameforge.app.core.db

import androidx.room.*

@Dao
interface GameDao {
    @Query("SELECT * FROM games")
    suspend fun getAllGames(): List<GameEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: GameEntity)

    @Query("SELECT * FROM backups WHERE gameId = :gameId ORDER BY timestamp DESC")
    suspend fun getBackupsForGame(gameId: String): List<BackupEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBackup(backup: BackupEntity)

    @Query("SELECT * FROM custom_adapters")
    suspend fun getCustomAdapters(): List<CustomAdapterSchemaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomAdapter(schema: CustomAdapterSchemaEntity)
}

@Database(entities = [GameEntity::class, BackupEntity::class, CustomAdapterSchemaEntity::class], version = 1)
abstract class GameForgeDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
}
