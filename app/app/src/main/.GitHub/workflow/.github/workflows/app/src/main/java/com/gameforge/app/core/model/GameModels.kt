package com.gameforge.app.core.model

enum class ParameterCategory {
    PROGRESSION,
    CURRENCY,
    STATS,
    INVENTORY,
    GAMEPLAY,
    LEVEL
}

enum class ParameterType {
    INT,
    FLOAT,
    BOOLEAN,
    STRING,
    ENUM
}

data class ValidationRule(
    val minValue: Double? = null,
    val maxValue: Double? = null,
    val allowedValues: List<String>? = null,
    val regexPattern: String? = null
)

sealed class ParameterValue {
    data class IntVal(val value: Int) : ParameterValue()
    data class FloatVal(val value: Float) : ParameterValue()
    data class BoolVal(val value: Boolean) : ParameterValue()
    data class StringVal(val value: String) : ParameterValue()
}

data class GameParameter(
    val id: String,
    val keyPath: String,
    val displayName: String,
    val category: ParameterCategory,
    val type: ParameterType,
    var currentValue: ParameterValue,
    val originalValue: ParameterValue,
    val validationRule: ValidationRule = ValidationRule(),
    val isReadOnly: Boolean = false,
    val description: String = ""
)

enum class ConnectionStatus {
    CONNECTED,
    DISCONNECTED,
    UNSUPPORTED,
    COMPATIBILITY_WARNING
}

data class GameManifest(
    val id: String,
    val title: String,
    val developer: String,
    val gameVersion: String,
    val adapterVersion: String,
    val engine: String,
    val connectionType: String,
    val iconUrl: String = ""
)
