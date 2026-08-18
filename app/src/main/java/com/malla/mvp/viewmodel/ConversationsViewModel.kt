package com.malla.mvp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.malla.mvp.data.AppDatabase
import com.malla.mvp.data.entity.ConversationEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ConversationsViewModel(application: Application) : AndroidViewModel(application) {
    private val _conversations = MutableStateFlow<List<ConversationEntity>>(emptyList())
    val conversations: StateFlow<List<ConversationEntity>> = _conversations.asStateFlow()

    init {
        val db = AppDatabase.getInstance(application)
        viewModelScope.launch {
            db?.conversationDao()?.getAllVisibleConversations()
                ?.distinctUntilChanged()
                ?.catch { emit(emptyList()) }
                ?.collect { list ->
                    _conversations.value = list
                }
        }
    }
}
