package com.hrinterview.app.data.repository

import com.hrinterview.app.data.local.db.AnswerEntity
import com.hrinterview.app.data.local.db.AppDatabase
import com.hrinterview.app.data.local.db.CompetenceEntity
import com.hrinterview.app.data.local.db.InterviewEntity
import com.hrinterview.app.data.local.db.QuestionEntity
import com.hrinterview.app.data.seed.QuestionBankSeed
import com.hrinterview.app.domain.Competence
import com.hrinterview.app.domain.InterviewQuestion
import com.hrinterview.app.domain.InterviewSummary
import com.hrinterview.app.domain.PositionType
import com.hrinterview.app.domain.QuestionType
import com.hrinterview.app.domain.SavedAnswer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class HrRepository(private val db: AppDatabase) {

    suspend fun ensureSeeded() {
        if (db.competenceDao().count() == 0) {
            db.competenceDao().insertAll(QuestionBankSeed.competences)
        }
        if (db.questionDao().count() == 0) {
            db.questionDao().insertAll(QuestionBankSeed.questions)
        }
    }

    fun observeCompetences(): Flow<List<Competence>> =
        db.competenceDao().observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun getCompetences(): List<Competence> =
        db.competenceDao().getAll().map { it.toDomain() }

    suspend fun saveCompetence(item: Competence) {
        db.competenceDao().upsert(
            CompetenceEntity(item.id, item.name, item.isBuiltIn, item.isEnabled)
        )
    }

    suspend fun deleteUserCompetence(id: String) {
        db.competenceDao().deleteUser(id)
    }

    fun observeQuestions(): Flow<List<InterviewQuestion>> =
        kotlinx.coroutines.flow.combine(
            db.questionDao().observeAll(),
            db.competenceDao().observeAll()
        ) { questions, comps ->
            val names = comps.associate { it.id to it.name }
            questions.map { it.toDomain(names[it.competenceId] ?: it.competenceId) }
        }

    suspend fun getQuestions(): List<InterviewQuestion> {
        val names = db.competenceDao().getAll().associate { it.id to it.name }
        return db.questionDao().getAll().map { it.toDomain(names[it.competenceId] ?: it.competenceId) }
    }

    suspend fun getQuestion(id: String): InterviewQuestion? {
        val entity = db.questionDao().getById(id) ?: return null
        val name = db.competenceDao().getAll().firstOrNull { it.id == entity.competenceId }?.name
            ?: entity.competenceId
        return entity.toDomain(name)
    }

    suspend fun saveQuestion(question: InterviewQuestion) {
        db.questionDao().upsert(
            QuestionEntity(
                id = question.id,
                text = question.text,
                competenceId = question.competenceId,
                questionType = question.questionType.name,
                positions = question.positionTypes.joinToString(",") { it.name },
                isBuiltIn = question.isBuiltIn,
                isEnabled = question.isEnabled,
                sortKey = question.sortKey
            )
        )
    }

    suspend fun setQuestionEnabled(id: String, enabled: Boolean) {
        db.questionDao().setEnabled(id, enabled)
    }

    suspend fun deleteUserQuestion(id: String) {
        db.questionDao().deleteUser(id)
    }

    fun observeInterviews(): Flow<List<InterviewSummary>> =
        db.interviewDao().observeAll().map { list ->
            list.map { it.toSummary(emptyList()) }
        }

    fun observeInterviewCount(): Flow<Int> = db.interviewDao().observeCount()

    suspend fun getInterview(id: String): InterviewSummary? {
        val interview = db.interviewDao().getById(id) ?: return null
        val answers = db.answerDao().getForInterview(id).map { it.toDomain() }
        return interview.toSummary(answers)
    }

    suspend fun saveInterview(summary: InterviewSummary) {
        db.interviewDao().upsert(
            InterviewEntity(
                id = summary.id,
                candidateName = summary.candidateName,
                vacancy = summary.vacancy,
                positionType = summary.positionType.name,
                createdAt = summary.createdAt,
                overallScore = summary.overallScore,
                finalComment = summary.finalComment,
                selectedCompetenceIds = summary.selectedCompetenceIds.joinToString(",")
            )
        )
        db.answerDao().deleteForInterview(summary.id)
        db.answerDao().insertAll(
            summary.answers.map {
                AnswerEntity(
                    id = it.id,
                    interviewId = summary.id,
                    questionId = it.questionId,
                    questionText = it.questionText,
                    competenceId = it.competenceId,
                    competenceName = it.competenceName,
                    questionType = it.questionType.name,
                    score = it.score,
                    comment = it.comment,
                    orderIndex = it.orderIndex
                )
            }
        )
    }

    suspend fun clearHistory() {
        db.answerDao().deleteAll()
        db.interviewDao().deleteAll()
    }

    companion object {
        fun newId(): String = UUID.randomUUID().toString()
    }
}

private fun CompetenceEntity.toDomain() = Competence(id, name, isBuiltIn, isEnabled)

private fun QuestionEntity.toDomain(competenceName: String) = InterviewQuestion(
    id = id,
    text = text,
    competenceId = competenceId,
    competenceName = competenceName,
    questionType = runCatching { QuestionType.valueOf(questionType) }.getOrDefault(QuestionType.COMPETENCY),
    positionTypes = positions.split(",").filter { it.isNotBlank() }.mapNotNull {
        runCatching { PositionType.valueOf(it) }.getOrNull()
    }.toSet(),
    isBuiltIn = isBuiltIn,
    isEnabled = isEnabled,
    sortKey = sortKey
)

private fun InterviewEntity.toSummary(answers: List<SavedAnswer>) = InterviewSummary(
    id = id,
    candidateName = candidateName,
    vacancy = vacancy,
    positionType = runCatching { PositionType.valueOf(positionType) }.getOrDefault(PositionType.SPECIALIST),
    createdAt = createdAt,
    overallScore = overallScore,
    finalComment = finalComment,
    selectedCompetenceIds = selectedCompetenceIds.split(",").filter { it.isNotBlank() },
    answers = answers
)

private fun AnswerEntity.toDomain() = SavedAnswer(
    id = id,
    questionId = questionId,
    questionText = questionText,
    competenceId = competenceId,
    competenceName = competenceName,
    questionType = runCatching { QuestionType.valueOf(questionType) }.getOrDefault(QuestionType.COMPETENCY),
    score = score,
    comment = comment,
    orderIndex = orderIndex
)
