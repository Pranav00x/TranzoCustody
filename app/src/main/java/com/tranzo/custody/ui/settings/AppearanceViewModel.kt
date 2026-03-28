package com.tranzo.custody.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tranzo.custody.data.local.ThemePreferencesManager
import com.tranzo.custody.ui.theme.AppFontId
import com.tranzo.custody.ui.theme.AppThemeId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AppearanceUiState(
    val selectedTheme: AppThemeId = AppThemeId.CLASSIC,
    val selectedFont: AppFontId = AppFontId.INTER
)

@HiltViewModel
class AppearanceViewModel @Inject constructor(
    private val themePreferencesManager: ThemePreferencesManager
) : ViewModel() {

    private val _state = MutableStateFlow(AppearanceUiState())
    val state: StateFlow<AppearanceUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            themePreferencesManager.selectedTheme.collect { theme ->
                _state.value = _state.value.copy(selectedTheme = theme)
            }
        }
        viewModelScope.launch {
            themePreferencesManager.selectedFont.collect { font ->
                _state.value = _state.value.copy(selectedFont = font)
            }
        }
    }

    fun selectTheme(themeId: AppThemeId) {
        viewModelScope.launch {
            themePreferencesManager.setTheme(themeId)
        }
    }

    fun selectFont(fontId: AppFontId) {
        viewModelScope.launch {
            themePreferencesManager.setFont(fontId)
        }
    }
}
