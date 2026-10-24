package com.example.ui.project

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AiCodeService
import com.example.ai.AiGenerationState
import com.example.ai.ModelTier
import com.example.data.model.ChatMessage
import com.example.data.model.ConsoleLogEntry
import com.example.data.model.LogLevel
import com.example.data.model.Project
import com.example.data.model.ProjectTemplates
import com.example.data.repository.ProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class FileTab {
    HTML, CSS, JS, PYTHON, JAVA
}

enum class WorkspaceView {
    SPLIT, EDITOR, PREVIEW
}

class ProjectViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProjectRepository(application)
    private val aiService = AiCodeService()

    val projectsList: StateFlow<List<Project>> = repository.observeProjects()
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    private val _activeProject = MutableStateFlow<Project?>(null)
    val activeProject: StateFlow<Project?> = _activeProject.asStateFlow()

    private val _activeFileTab = MutableStateFlow(FileTab.HTML)
    val activeFileTab: StateFlow<FileTab> = _activeFileTab.asStateFlow()

    private val _activeWorkspaceView = MutableStateFlow(WorkspaceView.SPLIT)
    val activeWorkspaceView: StateFlow<WorkspaceView> = _activeWorkspaceView.asStateFlow()

    private val _modelTier = MutableStateFlow(ModelTier.HIGH_THINKING)
    val modelTier: StateFlow<ModelTier> = _modelTier.asStateFlow()

    private val _isSearchGrounding = MutableStateFlow(true)
    val isSearchGrounding: StateFlow<Boolean> = _isSearchGrounding.asStateFlow()

    private val _aiState = MutableStateFlow<AiGenerationState>(AiGenerationState.Idle)
    val aiState: StateFlow<AiGenerationState> = _aiState.asStateFlow()

    private val _consoleLogs = MutableStateFlow<List<ConsoleLogEntry>>(emptyList())
    val consoleLogs: StateFlow<List<ConsoleLogEntry>> = _consoleLogs.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isConsoleDrawerOpen = MutableStateFlow(false)
    val isConsoleDrawerOpen: StateFlow<Boolean> = _isConsoleDrawerOpen.asStateFlow()

    private val _reloadTrigger = MutableStateFlow(0)
    val reloadTrigger: StateFlow<Int> = _reloadTrigger.asStateFlow()

    fun selectProject(project: Project) {
        _activeProject.value = project
        _consoleLogs.value = emptyList()
        _aiState.value = AiGenerationState.Idle
        _reloadTrigger.value += 1
        observeProjectMessages(project.id)
    }

    fun clearActiveProject() {
        _activeProject.value = null
        _consoleLogs.value = emptyList()
        _aiState.value = AiGenerationState.Idle
    }

    private fun observeProjectMessages(projectId: String) {
        if (projectId.isBlank()) return
        viewModelScope.launch {
            repository.observeMessages(projectId)
                .catch { emit(emptyList()) }
                .collectLatest { list ->
                    _chatMessages.value = list
                }
        }
    }

    fun setFileTab(tab: FileTab) {
        _activeFileTab.value = tab
    }

    fun setWorkspaceView(view: WorkspaceView) {
        _activeWorkspaceView.value = view
    }

    fun setModelTier(tier: ModelTier) {
        _modelTier.value = tier
    }

    fun toggleSearchGrounding() {
        _isSearchGrounding.value = !_isSearchGrounding.value
    }

    fun toggleConsoleDrawer() {
        _isConsoleDrawerOpen.value = !_isConsoleDrawerOpen.value
    }

    fun forceReloadPreview() {
        _reloadTrigger.value += 1
    }

    fun updateActiveCode(code: String) {
        val current = _activeProject.value ?: return
        val updated = when (_activeFileTab.value) {
            FileTab.HTML -> current.copy(htmlContent = code)
            FileTab.CSS -> current.copy(cssContent = code)
            FileTab.JS -> current.copy(jsContent = code)
            FileTab.PYTHON -> current.copy(description = if (current.description.contains("python")) current.description else current.description + " [Python Snippet]")
            FileTab.JAVA -> current.copy(description = if (current.description.contains("java")) current.description else current.description + " [Java Snippet]")
        }
        _activeProject.value = updated
        _reloadTrigger.value += 1
    }

    fun saveCurrentProject() {
        val current = _activeProject.value ?: return
        viewModelScope.launch {
            if (current.id.isNotBlank()) {
                repository.updateProject(current)
            } else {
                val newId = repository.createProject(current)
                _activeProject.value = current.copy(id = newId)
            }
        }
    }

    fun createNewProject(title: String = "New App", template: Project = ProjectTemplates.CYBERPUNK_TIMER) {
        viewModelScope.launch {
            val newProject = template.copy(
                id = "",
                title = title.ifBlank { template.title },
                createdAt = null,
                updatedAt = null
            )
            val docId = repository.createProject(newProject)
            val created = newProject.copy(id = docId)
            selectProject(created)
        }
    }

    fun deleteProject(projectId: String) {
        viewModelScope.launch {
            repository.deleteProject(projectId)
            if (_activeProject.value?.id == projectId) {
                _activeProject.value = null
            }
        }
    }

    fun addConsoleLog(level: LogLevel, message: String) {
        val entry = ConsoleLogEntry(level = level, message = message)
        _consoleLogs.value = (_consoleLogs.value + entry).takeLast(100)
    }

    fun clearConsoleLogs() {
        _consoleLogs.value = emptyList()
    }

    fun sendPrompt(userPrompt: String) {
        val project = _activeProject.value ?: return
        if (userPrompt.isBlank()) return

        // 1. Add user message locally and to repository
        viewModelScope.launch {
            if (project.id.isNotBlank()) {
                repository.addMessage(project.id, sender = "user", content = userPrompt)
            }
        }

        // 2. Select appropriate model tier
        val tier = if (_isSearchGrounding.value && _modelTier.value != ModelTier.HIGH_THINKING) {
            ModelTier.GENERAL_SEARCH
        } else {
            _modelTier.value
        }

        viewModelScope.launch {
            aiService.generateCode(userPrompt, project, tier).collectLatest { state ->
                _aiState.value = state

                if (state is AiGenerationState.Success) {
                    // Update active project with new files
                    var updated = project
                    if (!state.updatedHtml.isNullOrBlank()) updated = updated.copy(htmlContent = state.updatedHtml)
                    if (!state.updatedCss.isNullOrBlank()) updated = updated.copy(cssContent = state.updatedCss)
                    if (!state.updatedJs.isNullOrBlank()) updated = updated.copy(jsContent = state.updatedJs)

                    _activeProject.value = updated
                    _reloadTrigger.value += 1

                    // Persist updated project & model reply
                    if (updated.id.isNotBlank()) {
                        repository.updateProject(updated)
                        repository.addMessage(
                            projectId = updated.id,
                            sender = "model",
                            content = state.explanation,
                            thinking = state.thinking
                        )
                    }
                }
            }
        }
    }

    fun diagnoseAndFixError(errorMessage: String) {
        val prompt = "Diagnose and fix this runtime JavaScript error: $errorMessage. Update the HTML, CSS, or JS code to eliminate this bug."
        sendPrompt(prompt)
    }
}
