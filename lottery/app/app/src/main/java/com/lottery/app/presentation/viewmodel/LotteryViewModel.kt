package com.lottery.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lottery.app.domain.model.DrawResult
import com.lottery.app.domain.model.HotColdStatistics
import com.lottery.app.domain.model.LotteryRecord
import com.lottery.app.domain.model.PredictionResult
import com.lottery.app.domain.model.PrizeLevels
import com.lottery.app.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 彩票记录 ViewModel
 */
@HiltViewModel
class LotteryViewModel @Inject constructor(
    private val getAllRecordsUseCase: GetAllRecordsUseCase,
    private val getRecordByIdUseCase: GetRecordByIdUseCase,
    private val saveRecordUseCase: SaveRecordUseCase,
    private val deleteRecordUseCase: DeleteRecordUseCase,
    private val checkWinStatusUseCase: CheckWinStatusUseCase,
    private val syncDrawsUseCase: SyncDrawsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LotteryUiState())
    val uiState: StateFlow<LotteryUiState> = _uiState.asStateFlow()

    init {
        loadRecords()
    }

    private fun loadRecords() {
        viewModelScope.launch {
            getAllRecordsUseCase().collect { records ->
                _uiState.update { it.copy(records = records, isLoading = false) }
            }
        }
    }

    fun saveRecord(record: LotteryRecord) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val id = saveRecordUseCase(record)
                
                // 检查是否已开奖
                val savedRecord = record.copy(id = id)
                if (savedRecord.drawDate != null) {
                    checkWinStatus(savedRecord)
                }
                
                _uiState.update { it.copy(isLoading = false, message = "保存成功") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun deleteRecord(id: Long) {
        viewModelScope.launch {
            try {
                deleteRecordUseCase(id)
                _uiState.update { it.copy(message = "删除成功") }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun checkWinStatus(record: LotteryRecord) {
        viewModelScope.launch {
            try {
                val updated = checkWinStatusUseCase(record)
                _uiState.update { 
                    it.copy(message = if (updated.isWin) {
                        "恭喜！中${updated.prizeLevel}，奖金 ${updated.prizeAmount} 元"
                    } else {
                        "未中奖"
                    })
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun syncDraws() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = syncDrawsUseCase()
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    message = result.getOrNull()?.let { "同步成功，更新 $it 期开奖数据" } ?: "同步失败"
                )
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null, error = null) }
    }

    /**
     * 创建新的彩票记录
     */
    fun createNewRecord(): LotteryRecord {
        return LotteryRecord(
            period = generateNextPeriod(),
            purchaseDate = System.currentTimeMillis(),
            redBalls = emptyList(),
            blueBall = 0
        )
    }

    private fun generateNextPeriod(): String {
        // 生成下一期期号（简化实现）
        val calendar = java.util.Calendar.getInstance()
        val year = calendar.get(java.util.Calendar.YEAR)
        val week = (calendar.get(java.util.Calendar.DAY_OF_YEAR) / 7) + 1
        return String.format("%d%02d", year, week)
    }
}

data class LotteryUiState(
    val records: List<LotteryRecord> = emptyList(),
    val isLoading: Boolean = true,
    val message: String? = null,
    val error: String? = null
) {
    val totalWinnings: Double
        get() = records.filter { it.isWin }.sumOf { it.prizeAmount ?: 0.0 }
    
    val winCount: Int
        get() = records.count { it.isWin }
}