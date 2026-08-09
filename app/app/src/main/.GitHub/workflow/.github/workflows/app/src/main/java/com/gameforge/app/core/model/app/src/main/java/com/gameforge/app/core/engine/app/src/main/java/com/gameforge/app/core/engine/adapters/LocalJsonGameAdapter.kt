package com.gameforge.app.core.engine.adapters

import com.gameforge.app.core.engine.GameAdapter
import com.gameforge.app.core.model.*
import org.json.JSONObject
import java.io.File

class LocalJsonGameAdapter(
    override val manifest: GameManifest,
    private val targetConfigFile: File
) : GameAdapter {

    private var cachedParams = mutableMapOf<String, GameParameter>()

    override suspend fun checkConnection(): ConnectionStatus {
        return if (targetConfigFile.exists() && targetConfigFile.canRead() && targetConfigFile.canWrite()) {
            ConnectionStatus.CONNECTED
        } else {
            ConnectionStatus.UNSUPPORTED
        }
    }

    override suspend fun fetchParameters(): Result<List<GameParameter>> {
        return try {
            if (!targetConfigFile.exists()) {
                return Result.failure(Exception("Game configuration file not found at: ${targetConfigFile.absolutePath}"))
            }

            val jsonContent = targetConfigFile.readText()
            val jsonObj = JSONObject(jsonContent)

            val params = listOf(
                GameParameter(
                    id = "p_level",
                    keyPath = "progression.level",
                    displayName = "Player Level",
                    category = ParameterCategory.PROGRESSION,
                    type = ParameterType.INT,
                    currentValue = ParameterValue.IntVal(jsonObj.optJSONObject("progression")?.optInt("level", 1) ?: 1),
                    originalValue = ParameterValue.IntVal(jsonObj.optJSONObject("progression")?.optInt("level", 1) ?: 1),
                    validationRule = ValidationRule(minValue = 1.0, maxValue = 100.0)
                ),
                GameParameter(
                    id = "p_xp",
                    keyPath = "progression.xp",
                    displayName = "Current XP",
                    category = ParameterCategory.PROGRESSION,
                    type = ParameterType.INT,
                    currentValue = ParameterValue.IntVal(jsonObj.optJSONObject("progression")?.optInt("xp", 0) ?: 0),
                    originalValue = ParameterValue.IntVal(jsonObj.optJSONObject("progression")?.optInt("xp", 0) ?: 0)
                ),
                GameParameter(
                    id = "c_coins",
                    keyPath = "economy.coins",
                    displayName = "Coins",
                    category = ParameterCategory.CURRENCY,
                    type = ParameterType.INT,
                    currentValue = ParameterValue.IntVal(jsonObj.optJSONObject("economy")?.optInt("coins", 0) ?: 0),
                    originalValue = ParameterValue.IntVal(jsonObj.optJSONObject("economy")?.optInt("coins", 0) ?: 0)
                ),
                GameParameter(
                    id = "c_gems",
                    keyPath = "economy.gems",
                    displayName = "Gems",
                    category = ParameterCategory.CURRENCY,
                    type = ParameterType.INT,
                    currentValue = ParameterValue.IntVal(jsonObj.optJSONObject("economy")?.optInt("gems", 0) ?: 0),
                    originalValue = ParameterValue.IntVal(jsonObj.optJSONObject("economy")?.optInt("gems", 0) ?: 0)
                ),
                GameParameter(
                    id = "s_health",
                    keyPath = "stats.maxHealth",
                    displayName = "Max Health",
                    category = ParameterCategory.STATS,
                    type = ParameterType.FLOAT,
                    currentValue = ParameterValue.FloatVal(jsonObj.optJSONObject("stats")?.optDouble("maxHealth", 100.0)?.toFloat() ?: 100f),
                    originalValue = ParameterValue.FloatVal(jsonObj.optJSONObject("stats")?.optDouble("maxHealth", 100.0)?.toFloat() ?: 100f),
                    validationRule = ValidationRule(minValue = 1.0, maxValue = 10000.0)
                ),
                GameParameter(
                    id = "g_speed",
                    keyPath = "gameplay.moveSpeed",
                    displayName = "Movement Speed Multiplier",
                    category = ParameterCategory.GAMEPLAY,
                    type = ParameterType.FLOAT,
                    currentValue = ParameterValue.FloatVal(jsonObj.optJSONObject("gameplay")?.optDouble("moveSpeed", 1.0)?.toFloat() ?: 1.0f),
                    originalValue = ParameterValue.FloatVal(jsonObj.optJSONObject("gameplay")?.optDouble("moveSpeed", 1.0)?.toFloat() ?: 1.0f),
                    validationRule = ValidationRule(minValue = 0.1, maxValue = 10.0)
                )
            )

            params.forEach { cachedParams[it.id] = it }
            Result.success(params)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun applyParameters(parameters: List<GameParameter>): Result<Boolean> {
        return try {
            val jsonContent = if (targetConfigFile.exists()) targetConfigFile.readText() else "{}"
            val rootObj = JSONObject(jsonContent)

            for (param in parameters) {
                val keys = param.keyPath.split(".")
                var currentObj = rootObj
                for (i in 0 until keys.size - 1) {
                    if (!currentObj.has(keys[i])) {
                        currentObj.put(keys[i], JSONObject())
                    }
                    currentObj = currentObj.getJSONObject(keys[i])
                }

                val lastKey = keys.last()
                when (val v = param.currentValue) {
                    is ParameterValue.IntVal -> currentObj.put(lastKey, v.value)
                    is ParameterValue.FloatVal -> currentObj.put(lastKey, v.value.toDouble())
                    is ParameterValue.BoolVal -> currentObj.put(lastKey, v.value)
                    is ParameterValue.StringVal -> currentObj.put(lastKey, v.value)
                }
            }

            targetConfigFile.writeText(rootObj.toString(4))
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createBackup(): Result<String> {
        return try {
            val backupFile = File(targetConfigFile.parent, "${targetConfigFile.name}.bak_${System.currentTimeMillis()}")
            targetConfigFile.copyTo(backupFile, overwrite = true)
            Result.success(backupFile.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun restoreBackup(backupId: String): Result<Boolean> {
        return try {
            val backupFile = File(backupId)
            if (!backupFile.exists()) return Result.failure(Exception("Backup snapshot not found"))
            backupFile.copyTo(targetConfigFile, overwrite = true)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
