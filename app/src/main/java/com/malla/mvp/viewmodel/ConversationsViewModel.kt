package com.malla.mvp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.malla.mvp.data.AppDatabase
import com.malla.mvp.data.entity.ConversationEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ConversationsViewModel : ViewModel() {
    private val _conversations = MutableStateFlow<List<ConversationEntity>>(emptyList())
    val conversations: StateFlow<List<ConversationEntity>> = _conversations.asStateFlow()

    fun startObserving(db: AppDatabase?) {
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
