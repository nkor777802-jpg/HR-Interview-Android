package com.hrinterview.app.domain

enum class PositionType(val title: String) {
    MANAGER("Руководитель"),
    SPECIALIST("Специалист"),
    WORKER("Рабочая профессия")
}

enum class QuestionType(val title: String) {
    EXPERIENCE("Опыт"),
    MOTIVATION("Мотивация"),
    COMPETENCY("Компетентностный"),
    BEHAVIORAL("Поведенческий"),
    SITUATIONAL("Ситуационный")
}

enum class ThemeMode(val title: String) {
    SYSTEM("Системная"),
    LIGHT("Светлая"),
    DARK("Тёмная")
}

data class Competence(
    val id: String,
    val name: String,
    val isBuiltIn: Boolean,
    val isEnabled: Boolean = true
)

data class InterviewQuestion(
    val id: String,
    val text: String,
    val competenceId: String,
    val competenceName: String,
    val questionType: QuestionType,
    val positionTypes: Set<PositionType>,
    val isBuiltIn: Boolean,
    val isEnabled: Boolean,
    val sortKey: Int
)

data class AnswerDraft(
    val question: InterviewQuestion,
    val score: Int = 0,
    val comment: String = ""
)

data class InterviewSummary(
    val id: String,
    val candidateName: String,
    val vacancy: String,
    val positionType: PositionType,
    val createdAt: Long,
    val overallScore: Float,
    val finalComment: String,
    val selectedCompetenceIds: List<String>,
    val answers: List<SavedAnswer>
)

data class SavedAnswer(
    val id: String,
    val questionId: String,
    val questionText: String,
    val competenceId: String,
    val competenceName: String,
    val questionType: QuestionType,
    val score: Int,
    val comment: String,
    val orderIndex: Int
)

data class CompetenceScore(
    val competenceId: String,
    val name: String,
    val average: Float
)
