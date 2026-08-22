package com.hrinterview.app.ui.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hrinterview.app.container
import com.hrinterview.app.data.repository.HrRepository
import com.hrinterview.app.domain.Competence
import com.hrinterview.app.domain.InterviewPlanner
import com.hrinterview.app.domain.InterviewQuestion
import com.hrinterview.app.domain.InterviewSummary
import com.hrinterview.app.domain.PositionType
import com.hrinterview.app.domain.QuestionType
import com.hrinterview.app.domain.ThemeMode
import com.hrinterview.app.session.InterviewSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = app.container.repository
    val interviews = repo.observeInterviews()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val count = repo.observeInterviewCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
}

class SetupViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = app.container.repository
    private val container = app.container

    val competences = repo.observeCompetences()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val candidate = MutableStateFlow("")
    val vacancy = MutableStateFlow("")
    val positionType = MutableStateFlow(PositionType.SPECIALIST)
    val selectedIds = MutableStateFlow(InterviewPlanner.suggestedIds(PositionType.SPECIALIST).toSet())
    val error = MutableStateFlow<String?>(null)

    fun onPositionChange(type: PositionType) {
        positionType.value = type
        selectedIds.value = InterviewPlanner.suggestedIds(type).toSet()
    }

    fun toggleCompetence(id: String) {
        selectedIds.update { current ->
            if (id in current) current - id else current + id
        }
    }

    suspend fun startInterview(): Boolean {
        val name = candidate.value.trim()
        val job = vacancy.value.trim()
        if (name.isEmpty() || job.isEmpty()) {
            error.value = "Укажите кандидата и вакансию"
            return false
        }
        if (selectedIds.value.isEmpty()) {
            error.value = "Выберите хотя бы одну компетенцию"
            return false
        }
        error.value = null
        val questions = InterviewPlanner.selectQuestions(
            positionType.value,
            selectedIds.value.toList(),
            repo.getQuestions()
        )
        if (questions.isEmpty()) {
            error.value = "Нет активных вопросов для выбранных условий. Проверьте банк вопросов."
            return false
        }
        container.setSession(
            InterviewSession.start(name, job, positionType.value, selectedIds.value.toList(), questions)
        )
        return true
    }
}

class SessionViewModel(app: Application) : AndroidViewModel(app) {
    val session = app.container.session
        .stateIn(viewModelScope, SharingStarted.Eagerly, InterviewSession())
    private val container = app.container

    fun setScore(score: Int) {
        container.updateSession { s ->
            val i = s.currentIndex
            val drafts = s.drafts.toMutableList()
            if (i in drafts.indices) drafts[i] = drafts[i].copy(score = score)
            s.copy(drafts = drafts)
        }
    }

    fun setComment(text: String) {
        container.updateSession { s ->
            val i = s.currentIndex
            val drafts = s.drafts.toMutableList()
            if (i in drafts.indices) drafts[i] = drafts[i].copy(comment = text)
            s.copy(drafts = drafts)
        }
    }

    fun back() {
        container.updateSession { s -> s.copy(currentIndex = (s.currentIndex - 1).coerceAtLeast(0)) }
    }

    fun nextOrFinish(): Boolean {
        val s = session.value
        val current = s.drafts.getOrNull(s.currentIndex) ?: return false
        if (current.score !in 1..5) return false
        return if (s.currentIndex >= s.drafts.lastIndex) {
            true
        } else {
            container.updateSession { it.copy(currentIndex = it.currentIndex + 1) }
            false
        }
    }
}

class ResultViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = app.container.repository
    private val container = app.container
    val session = container.session.stateIn(viewModelScope, SharingStarted.Eagerly, InterviewSession())
    val saved = MutableStateFlow(false)

    fun setFinalComment(text: String) {
        container.updateSession { it.copy(finalComment = text) }
    }

    fun save(onDone: () -> Unit) {
        viewModelScope.launch {
            val current = session.value
            val id = current.savedId ?: HrRepository.newId()
            repo.saveInterview(current.copy(savedId = id).toSummary(id))
            container.updateSession { it.copy(savedId = id) }
            saved.value = true
            onDone()
        }
    }
}

class HistoryViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = app.container.repository
    val query = MutableStateFlow("")
    val items = combine(repo.observeInterviews(), query) { list, q ->
        val needle = q.trim().lowercase()
        if (needle.isEmpty()) list
        else list.filter {
            it.candidateName.lowercase().contains(needle) ||
                it.vacancy.lowercase().contains(needle)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

class DetailViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = app.container.repository
    val interview = MutableStateFlow<InterviewSummary?>(null)

    fun load(id: String) {
        viewModelScope.launch { interview.value = repo.getInterview(id) }
    }
}

class BankViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = app.container.repository
    val competenceFilter = MutableStateFlow<String?>(null)
    val positionFilter = MutableStateFlow<PositionType?>(null)
    val competences = repo.observeCompetences()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val questions = combine(
        repo.observeQuestions(),
        competenceFilter,
        positionFilter
    ) { list, competence, position ->
        list.filter { q ->
            (competence == null || q.competenceId == competence) &&
                (position == null || position in q.positionTypes)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun toggle(question: InterviewQuestion) {
        viewModelScope.launch { repo.setQuestionEnabled(question.id, !question.isEnabled) }
    }

    fun deleteUser(id: String) {
        viewModelScope.launch { repo.deleteUserQuestion(id) }
    }
}

class QuestionEditorViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = app.container.repository
    val competences = repo.observeCompetences()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val text = MutableStateFlow("")
    val competenceId = MutableStateFlow("")
    val type = MutableStateFlow(QuestionType.SITUATIONAL)
    val positions = MutableStateFlow(PositionType.entries.toSet())
    val enabled = MutableStateFlow(true)
    val isBuiltIn = MutableStateFlow(false)
    val editingId = MutableStateFlow<String?>(null)
    val sortKey = MutableStateFlow(10_000)

    fun load(id: String?) {
        viewModelScope.launch {
            val comps = repo.getCompetences()
            if (competenceId.value.isEmpty()) competenceId.value = comps.firstOrNull()?.id.orEmpty()
            if (id == null) return@launch
            val q = repo.getQuestion(id) ?: return@launch
            editingId.value = q.id
            text.value = q.text
            competenceId.value = q.competenceId
            type.value = q.questionType
            positions.value = q.positionTypes
            enabled.value = q.isEnabled
            isBuiltIn.value = q.isBuiltIn
            sortKey.value = q.sortKey
        }
    }

    suspend fun save(): Boolean {
        val body = text.value.trim()
        if (body.isEmpty() || competenceId.value.isEmpty() || positions.value.isEmpty()) return false
        val id = editingId.value ?: "user_${HrRepository.newId()}"
        repo.saveQuestion(
            InterviewQuestion(
                id = id,
                text = body,
                competenceId = competenceId.value,
                competenceName = "",
                questionType = type.value,
                positionTypes = positions.value,
                isBuiltIn = isBuiltIn.value,
                isEnabled = enabled.value,
                sortKey = if (isBuiltIn.value) sortKey.value else 10_000 + body.hashCode().and(0x7fff)
            )
        )
        return true
    }
}

class SettingsViewModel(app: Application) : AndroidViewModel(app) {
    private val settings = app.container.settings
    private val repo = app.container.repository
    val theme = settings.themeMode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemeMode.SYSTEM)

    fun setTheme(mode: ThemeMode) {
        viewModelScope.launch { settings.setThemeMode(mode) }
    }

    fun clearHistory(onDone: () -> Unit) {
        viewModelScope.launch {
            repo.clearHistory()
            onDone()
        }
    }
}

class CompetencesViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = app.container.repository
    val items = repo.observeCompetences()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val newName = MutableStateFlow("")

    fun add() {
        val name = newName.value.trim()
        if (name.isEmpty()) return
        viewModelScope.launch {
            repo.saveCompetence(
                Competence(
                    id = "user_comp_${HrRepository.newId()}",
                    name = name,
                    isBuiltIn = false,
                    isEnabled = true
                )
            )
            newName.value = ""
        }
    }

    fun delete(id: String) {
        viewModelScope.launch { repo.deleteUserCompetence(id) }
    }
}

class LegalViewModel(app: Application) : AndroidViewModel(app) {
    private val settings = app.container.settings
    val accepted = settings.agreementAccepted
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun accept(onDone: () -> Unit) {
        viewModelScope.launch {
            settings.acceptAgreement()
            onDone()
        }
    }
}
