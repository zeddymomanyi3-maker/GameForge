package com.gameforge.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gameforge.app.core.engine.GameAdapter
import com.gameforge.app.core.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GameForgeUiState(
    val selectedGame: GameManifest? = null,
    val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val parameters: List<GameParameter> = emptyList(),
    val modifiedParameters: Map<String, ParameterValue> = emptyMap(),
    val selectedCategory: ParameterCategory = ParameterCategory.PROGRESSION,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val message: String? = null
)

class GameForgeViewModel(private val activeAdapter: GameAdapter) : ViewModel() {

    private val _uiState = MutableStateFlow(GameForgeUiState())
    val uiState: StateFlow<GameForgeUiState> = _uiState.asStateFlow()

    init {
        loadGameData()
    }

    fun loadGameData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, selectedGame = activeAdapter.manifest)
            val status = activeAdapter.checkConnection()
            _uiState.value = _uiState.value.copy(connectionStatus = status)

            if (status == ConnectionStatus.CONNECTED) {
                activeAdapter.fetchParameters()
                    .onSuccess { params ->
                        _uiState.value = _uiState.value.copy(
                            parameters = params,
                            isLoading = false
                        )
                    }
                    .onFailure { err ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            message = "Error: ${err.message}"
                        )
                    }
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun onParameterChanged(paramId: String, newValue: ParameterValue) {
        val currentModifications = _uiState.value.modifiedParameters.toMutableMap()
        currentModifications[paramId] = newValue

        val updatedParams = _uiState.value.parameters.map { param ->
            if (param.id == paramId) param.copy(currentValue = newValue) else param
        }

        _uiState.value = _uiState.value.copy(
            parameters = updatedParams,
            modifiedParameters = currentModifications
        )
    }

    fun selectCategory(category: ParameterCategory) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun applyChanges() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            activeAdapter.createBackup()
            activeAdapter.applyParameters(_uiState.value.parameters)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        modifiedParameters = emptyMap(),
                        message = "Changes applied successfully!"
                    )
                }
                .onFailure { err ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = "Failed: ${err.message}"
                    )
                }
        }
    }

    fun resetChanges() {
        val resetParams = _uiState.value.parameters.map { param ->
            param.copy(currentValue = param.originalValue)
        }
        _uiState.value = _uiState.value.copy(
            parameters = resetParams,
            modifiedParameters = emptyMap()
        )
    }
}
