package com.hrinterview.app.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CompetenceDao {
    @Query("SELECT * FROM competences ORDER BY isBuiltIn DESC, name ASC")
    fun observeAll(): Flow<List<CompetenceEntity>>

    @Query("SELECT * FROM competences ORDER BY isBuiltIn DESC, name ASC")
    suspend fun getAll(): List<CompetenceEntity>

    @Query("SELECT COUNT(*) FROM competences")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<CompetenceEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: CompetenceEntity)

    @Query("DELETE FROM competences WHERE id = :id AND isBuiltIn = 0")
    suspend fun deleteUser(id: String)
}

@Dao
interface QuestionDao {
    @Query("SELECT * FROM questions ORDER BY sortKey ASC, id ASC")
    fun observeAll(): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions ORDER BY sortKey ASC, id ASC")
    suspend fun getAll(): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE id = :id")
    suspend fun getById(id: String): QuestionEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<QuestionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: QuestionEntity)

    @Update
    suspend fun update(item: QuestionEntity)

    @Query("UPDATE questions SET isEnabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: String, enabled: Boolean)

    @Query("DELETE FROM questions WHERE id = :id AND isBuiltIn = 0")
    suspend fun deleteUser(id: String)

    @Query("SELECT COUNT(*) FROM questions")
    suspend fun count(): Int
}

@Dao
interface InterviewDao {
    @Query("SELECT * FROM interviews ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<InterviewEntity>>

    @Query("SELECT * FROM interviews WHERE id = :id")
    suspend fun getById(id: String): InterviewEntity?

    @Query("SELECT COUNT(*) FROM interviews")
    fun observeCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: InterviewEntity)

    @Query("DELETE FROM interviews")
    suspend fun deleteAll()

    @Query("DELETE FROM interviews WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface AnswerDao {
    @Query("SELECT * FROM answers WHERE interviewId = :interviewId ORDER BY orderIndex ASC")
    suspend fun getForInterview(interviewId: String): List<AnswerEntity>

    @Query("SELECT * FROM answers WHERE interviewId = :interviewId ORDER BY orderIndex ASC")
    fun observeForInterview(interviewId: String): Flow<List<AnswerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<AnswerEntity>)

    @Query("DELETE FROM answers")
    suspend fun deleteAll()

    @Query("DELETE FROM answers WHERE interviewId = :interviewId")
    suspend fun deleteForInterview(interviewId: String)
}
