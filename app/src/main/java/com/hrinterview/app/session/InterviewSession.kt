package com.hrinterview.app.session

import com.hrinterview.app.domain.AnswerDraft
import com.hrinterview.app.domain.InterviewQuestion
import com.hrinterview.app.domain.InterviewSummary
import com.hrinterview.app.domain.PositionType
import com.hrinterview.app.domain.SavedAnswer
import java.util.UUID

data class InterviewSession(
    val candidateName: String = "",
    val vacancy: String = "",
    val positionType: PositionType = PositionType.SPECIALIST,
    val selectedCompetenceIds: List<String> = emptyList(),
    val drafts: List<AnswerDraft> = emptyList(),
    val currentIndex: Int = 0,
    val finalComment: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val savedId: String? = null
) {
    val overallScore: Float
        get() {
            val scored = drafts.map { it.score }.filter { it in 1..5 }
            return if (scored.isEmpty()) 0f else scored.average().toFloat()
        }

    fun toSummary(id: String = savedId ?: UUID.randomUUID().toString()): InterviewSummary {
        return InterviewSummary(
            id = id,
            candidateName = candidateName,
            vacancy = vacancy,
            positionType = positionType,
            createdAt = createdAt,
            overallScore = overallScore,
            finalComment = finalComment,
            selectedCompetenceIds = selectedCompetenceIds,
            answers = drafts.mapIndexed { index, draft ->
                SavedAnswer(
                    id = UUID.randomUUID().toString(),
                    questionId = draft.question.id,
                    questionText = draft.question.text,
                    competenceId = draft.question.competenceId,
                    competenceName = draft.question.competenceName,
                    questionType = draft.question.questionType,
                    score = draft.score,
                    comment = draft.comment,
                    orderIndex = index
                )
            }
        )
    }

    companion object {
        fun start(
            candidateName: String,
            vacancy: String,
            positionType: PositionType,
            selectedCompetenceIds: List<String>,
            questions: List<InterviewQuestion>
        ) = InterviewSession(
            candidateName = candidateName.trim(),
            vacancy = vacancy.trim(),
            positionType = positionType,
            selectedCompetenceIds = selectedCompetenceIds,
            drafts = questions.map { AnswerDraft(it) },
            currentIndex = 0,
            createdAt = System.currentTimeMillis()
        )
    }
}
