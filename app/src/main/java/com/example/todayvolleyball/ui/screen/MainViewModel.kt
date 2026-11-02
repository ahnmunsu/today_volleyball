package com.example.todayvolleyball.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todayvolleyball.data.local.ScheduleChangeEntity
import com.example.todayvolleyball.data.local.VolleyballDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val dao: VolleyballDao
) : ViewModel() {

    private val _unreadChanges = MutableStateFlow<List<ScheduleChangeEntity>>(emptyList())
    val unreadChanges: StateFlow<List<ScheduleChangeEntity>> = _unreadChanges

    init {
        checkUnreadChanges()
    }

    private fun checkUnreadChanges() {
        viewModelScope.launch {
            _unreadChanges.value = dao.getUnreadChanges()
        }
    }

    fun markChangesAsRead(changes: List<ScheduleChangeEntity>) {
        viewModelScope.launch {
            changes.forEach { change ->
                dao.markChangeAsRead(change.id)
            }
            _unreadChanges.value = emptyList()
        }
    }
}
