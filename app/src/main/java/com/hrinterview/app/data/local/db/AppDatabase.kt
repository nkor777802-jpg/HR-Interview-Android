package com.hrinterview.app.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        CompetenceEntity::class,
        QuestionEntity::class,
        InterviewEntity::class,
        AnswerEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun competenceDao(): CompetenceDao
    abstract fun questionDao(): QuestionDao
    abstract fun interviewDao(): InterviewDao
    abstract fun answerDao(): AnswerDao

    companion object {
        fun create(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, "hr_interview.db")
                .fallbackToDestructiveMigration(true)
                .build()
        }
    }
}
