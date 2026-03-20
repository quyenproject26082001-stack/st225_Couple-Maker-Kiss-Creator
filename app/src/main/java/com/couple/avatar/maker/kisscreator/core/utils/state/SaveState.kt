package com.couple.avatar.maker.kisscreator.core.utils.state

sealed class SaveState {
    data class Success(val path: String) : SaveState()
    data class Error(val exception: Exception) : SaveState()
    object Loading : SaveState()
}