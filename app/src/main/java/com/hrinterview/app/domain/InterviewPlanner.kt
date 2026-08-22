package com.hrinterview.app.domain

import com.hrinterview.app.data.seed.QuestionBankSeed

object InterviewPlanner {
    const val TARGET_COUNT = 12

    fun selectQuestions(
        positionType: PositionType,
        selectedCompetenceIds: List<String>,
        bank: List<InterviewQuestion>
    ): List<InterviewQuestion> {
        val selected = selectedCompetenceIds.toSet()
        val filtered = bank
            .filter { it.isEnabled }
            .filter { positionType in it.positionTypes }
            .filter { it.competenceId in selected }
            .distinctBy { it.id }

        if (filtered.isEmpty()) {
            return bank
                .filter { it.isEnabled && positionType in it.positionTypes }
                .distinctBy { it.id }
                .shuffled()
                .take(TARGET_COUNT)
        }

        val queues = filtered
            .groupBy { it.competenceId }
            .mapValues { (_, questions) -> ArrayDeque(questions.shuffled()) }
        val order = selectedCompetenceIds.distinct().filter { it in queues.keys }
        val result = mutableListOf<InterviewQuestion>()
        val usedIds = mutableSetOf<String>()

        while (result.size < TARGET_COUNT) {
            var added = false
            for (cid in order) {
                val queue = queues[cid] ?: continue
                while (queue.isNotEmpty()) {
                    val next = queue.removeFirst()
                    if (next.id in usedIds) continue
                    usedIds += next.id
                    result += next
                    added = true
                    break
                }
                if (result.size == TARGET_COUNT) break
            }
            if (!added) break
        }
        return result
    }

    fun suggestedIds(type: PositionType): List<String> = QuestionBankSeed.suggestedCompetences(type)

    fun competenceScores(answers: List<SavedAnswer>): List<CompetenceScore> {
        return answers.groupBy { it.competenceId }.map { (id, items) ->
            CompetenceScore(
                competenceId = id,
                name = items.first().competenceName,
                average = items.map { it.score }.average().toFloat()
            )
        }.sortedByDescending { it.average }
    }
}
