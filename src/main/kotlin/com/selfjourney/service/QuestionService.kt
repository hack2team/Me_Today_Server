package com.selfjourney.service

import com.selfjourney.domain.*
import com.selfjourney.repository.AnswerRepository
import com.selfjourney.repository.QuestionRepository
import com.selfjourney.repository.UserRepository
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.math.max
import kotlin.math.roundToInt

class QuestionService(
    private val questionRepository: QuestionRepository,
    private val answerRepository: AnswerRepository,
    private val userRepository: UserRepository
) {

    fun getTodayQuestions(userId: Long): TodayQuestionResponse = transaction {
        val baseCount = questionRepository.count()
        val totalAnswers = answerRepository.countByUserId(userId)
        if (baseCount == 0) {
            return@transaction TodayQuestionResponse(
                question = null,
                answeredCount = totalAnswers,
                totalQuestions = 0,
                remainingQuestions = 0
            )
        }

        val planMonths = userRepository.getQuestionCycleMonths(userId)
        val cycleSize = computeCycleQuestionCount(baseCount, planMonths)
        val answeredInCycle = if (cycleSize == 0) 0 else totalAnswers % cycleSize
        val nextIndex = answeredInCycle + 1
        val normalizedIndex = ((nextIndex - 1) % baseCount) + 1
        val question = questionRepository.findById(normalizedIndex.toLong())
        val remaining = if (cycleSize == 0) 0 else {
            val remainingWithinCycle = cycleSize - answeredInCycle
            if (remainingWithinCycle == 0) cycleSize else remainingWithinCycle
        }

        TodayQuestionResponse(
            question = question,
            answeredCount = totalAnswers,
            totalQuestions = cycleSize,
            remainingQuestions = remaining
        )
    }

    fun getAllQuestions(): List<QuestionDTO> = transaction {
        questionRepository.findAll()
    }

    fun getQuestionById(id: Long): QuestionDTO? = transaction {
        questionRepository.findById(id)
    }

    fun getQuestionsByInterestId(interestId: Long): List<QuestionDTO> = transaction {
        questionRepository.findByInterestId(interestId)
    }

    fun createQuestion(request: CreateQuestionRequest): Long = transaction {
        questionRepository.create(request)
    }

    private fun computeCycleQuestionCount(baseCount: Int, planDurationMonths: Int): Int {
        if (baseCount <= 0) return 0
        val months = planDurationMonths.coerceAtLeast(1)
        val raw = (baseCount * months / 12.0).roundToInt()
        return max(1, raw)
    }
}
