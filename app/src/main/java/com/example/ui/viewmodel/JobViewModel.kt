package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppliedJobEntity
import com.example.data.local.JobEntity
import com.example.data.local.SavedJobEntity
import com.example.data.model.JobDetailDto
import com.example.data.model.JobDto
import com.example.data.model.PreferencesDto
import com.example.data.model.ResumeAnalysisDto
import com.example.data.model.SubscriptionPlanDto
import com.example.data.repository.JobRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class JobUiState(
    val isLoading: Boolean = false,
    val isSearching: Boolean = false,
    val searchResults: List<JobDto> = emptyList(),
    val aiSearchSummary: String? = null,
    val resumeAnalysis: ResumeAnalysisDto? = null,
    val selectedJobDetail: JobDetailDto? = null,
    val plans: List<SubscriptionPlanDto> = emptyList(),
    val preferences: PreferencesDto? = null,
    val stats: Map<String, Int> = mapOf("total_applied" to 0, "interviews" to 0, "offers" to 0, "pending" to 0),
    val message: String? = null,
    val errorMessage: String? = null
)

class JobViewModel(private val repository: JobRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(JobUiState())
    val uiState: StateFlow<JobUiState> = _uiState.asStateFlow()

    val todayJobs: StateFlow<List<JobEntity>> = repository.todayJobs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recommendedJobs: StateFlow<List<JobEntity>> = repository.recommendedJobs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedJobs: StateFlow<List<SavedJobEntity>> = repository.savedJobs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val appliedJobs: StateFlow<List<AppliedJobEntity>> = repository.appliedJobs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadInitialData()
    }

    fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.refreshTodayJobs()
            repository.refreshRecommendedJobs()
            repository.refreshSavedJobs()
            repository.refreshApplications()

            val statsResult = repository.getApplicationStats()
            statsResult.onSuccess { st ->
                _uiState.update { it.copy(stats = st) }
            }

            val plansResult = repository.getPlans()
            plansResult.onSuccess { pl ->
                _uiState.update { it.copy(plans = pl) }
            }

            val prefResult = repository.getPreferences()
            prefResult.onSuccess { pr ->
                _uiState.update { it.copy(preferences = pr) }
            }

            // Also populate search results initially
            repository.searchJobs().onSuccess { initialJobs ->
                _uiState.update { it.copy(searchResults = initialJobs) }
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun search(
        keyword: String? = null,
        employmentType: String? = null,
        location: String? = null,
        remoteType: String? = null,
        experienceMax: Double? = null,
        postedDays: Int? = null,
        sortBy: String = "newest"
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true, aiSearchSummary = null) }
            val result = repository.searchJobs(
                keyword = keyword,
                employmentType = employmentType,
                location = location,
                remoteType = remoteType,
                experienceMax = experienceMax,
                postedDays = postedDays,
                sortBy = sortBy
            )
            result.onSuccess { jobs ->
                _uiState.update { it.copy(isSearching = false, searchResults = jobs) }
            }.onFailure { err ->
                _uiState.update { it.copy(isSearching = false, errorMessage = err.localizedMessage) }
            }
        }
    }

    fun searchRealWebJobs(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true) }
            val result = repository.searchRealWebJobs(query)
            result.onSuccess { jobs ->
                _uiState.update {
                    it.copy(
                        isSearching = false,
                        searchResults = jobs,
                        aiSearchSummary = "Loaded ${jobs.size} verified live openings from tech company ATS feeds"
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(isSearching = false, errorMessage = "Live web search: ${err.localizedMessage}")
                }
            }
        }
    }

    fun runAiSearch(prompt: String) {
        if (prompt.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true) }
            val result = repository.aiSearch(prompt)
            result.onSuccess { res ->
                _uiState.update {
                    it.copy(
                        isSearching = false,
                        searchResults = res.results,
                        aiSearchSummary = res.summary
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(isSearching = false, errorMessage = "AI search failed: ${err.localizedMessage}")
                }
            }
        }
    }

    fun loadJobDetail(jobId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, selectedJobDetail = null) }
            val result = repository.getJobDetail(jobId)
            result.onSuccess { detail ->
                _uiState.update { it.copy(isLoading = false, selectedJobDetail = detail) }
            }.onFailure { err ->
                _uiState.update { it.copy(isLoading = false, errorMessage = err.localizedMessage) }
            }
        }
    }

    fun saveJob(job: JobDto, category: String = "Saved", notes: String? = null) {
        viewModelScope.launch {
            repository.saveJob(job, category, notes)
            _uiState.update { it.copy(message = "Job saved to $category") }
        }
    }

    fun saveJob(job: JobDetailDto, category: String = "Saved", notes: String? = null) {
        viewModelScope.launch {
            repository.saveJobDetail(job, category, notes)
            _uiState.update { it.copy(message = "Job saved to $category") }
        }
    }

    fun unsaveJob(jobId: String) {
        viewModelScope.launch {
            repository.deleteSavedJob(jobId)
            _uiState.update { it.copy(message = "Job removed from saved") }
        }
    }

    fun markApplied(jobId: String, status: String = "Applied", notes: String? = null) {
        viewModelScope.launch {
            repository.applyJob(jobId, status, notes)
            val st = repository.getApplicationStats().getOrNull()
            _uiState.update {
                it.copy(
                    message = "Application tracked!",
                    stats = st ?: it.stats
                )
            }
        }
    }

    fun updateApplicationStatus(id: String, status: String, remarks: String? = null, notes: String? = null) {
        viewModelScope.launch {
            repository.updateApplicationStatus(id, status, remarks, notes)
            repository.refreshApplications()
            val st = repository.getApplicationStats().getOrNull()
            _uiState.update {
                it.copy(
                    message = "Application status updated to $status",
                    stats = st ?: it.stats
                )
            }
        }
    }

    fun analyzeResume(text: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.analyzeResume(text)
            result.onSuccess { analysis ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        resumeAnalysis = analysis,
                        message = "Resume analyzed! Roles detected."
                    )
                }
            }.onFailure { err ->
                _uiState.update { it.copy(isLoading = false, errorMessage = err.localizedMessage) }
            }
        }
    }

    fun updatePreferences(newPrefs: PreferencesDto) {
        viewModelScope.launch {
            repository.updatePreferences(newPrefs).onSuccess { saved ->
                _uiState.update { it.copy(preferences = saved, message = "Preferences updated!") }
                repository.refreshRecommendedJobs()
            }
        }
    }

    fun subscribePlan(planTier: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.subscribePlan(planTier)
            result.onSuccess { sub ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = "Upgraded to ${sub.plan.name}!"
                    )
                }
                repository.refreshRecommendedJobs()
            }.onFailure { err ->
                _uiState.update { it.copy(isLoading = false, errorMessage = err.localizedMessage) }
            }
        }
    }

    fun clearFeedback() {
        _uiState.update { it.copy(message = null, errorMessage = null) }
    }

    class Factory(private val repository: JobRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(JobViewModel::class.java)) {
                return JobViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
