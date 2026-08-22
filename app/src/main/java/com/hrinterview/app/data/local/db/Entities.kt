package com.hrinterview.app.data.local.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "competences")
data class CompetenceEntity(
    @PrimaryKey val id: String,
    val name: String,
    val isBuiltIn: Boolean,
    val isEnabled: Boolean
)

@Entity(
    tableName = "questions",
    indices = [Index("competenceId")]
)
data class QuestionEntity(
    @PrimaryKey val id: String,
    val text: String,
    val competenceId: String,
    val questionType: String,
    val positions: String,
    val isBuiltIn: Boolean,
    val isEnabled: Boolean,
    val sortKey: Int
)

@Entity(tableName = "interviews")
data class InterviewEntity(
    @PrimaryKey val id: String,
    val candidateName: String,
    val vacancy: String,
    val positionType: String,
    val createdAt: Long,
    val overallScore: Float,
    val finalComment: String,
    val selectedCompetenceIds: String
)

@Entity(
    tableName = "answers",
    indices = [Index("interviewId")]
)
data class AnswerEntity(
    @PrimaryKey val id: String,
    val interviewId: String,
    val questionId: String,
    val questionText: String,
    val competenceId: String,
    val competenceName: String,
    val questionType: String,
    val score: Int,
    val comment: String,
    val orderIndex: Int
)
