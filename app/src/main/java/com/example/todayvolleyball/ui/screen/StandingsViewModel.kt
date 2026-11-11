package com.example.todayvolleyball.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todayvolleyball.domain.model.TeamRank
import com.example.todayvolleyball.domain.repository.VolleyballRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StandingsViewModel @Inject constructor(
    private val repository: VolleyballRepository
) : ViewModel() {

    // 201: 정규 (기본), 204: 준플옵, 202: 플옵, 203: 챔결
    private val _selectedStage = MutableStateFlow("201")
    val selectedStage: StateFlow<String> = _selectedStage.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    val teamRanks: StateFlow<List<TeamRank>> = _selectedStage
        .flatMapLatest { stage ->
            repository.getTeamRanksFlow(stage)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        refreshData()
    }

    fun selectStage(leagueCode: String) {
        if (_selectedStage.value != leagueCode) {
            _selectedStage.value = leagueCode
            fetchDataForStage(leagueCode)
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                repository.fetchAndSaveTeamRanks(_selectedStage.value)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    private fun fetchDataForStage(leagueCode: String) {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                repository.fetchAndSaveTeamRanks(leagueCode)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}
