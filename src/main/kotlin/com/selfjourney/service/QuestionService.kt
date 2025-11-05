package com.selfjourney.service

import com.selfjourney.domain.*
import com.selfjourney.repository.AnswerRepository
import com.selfjourney.repository.QuestionRepository
import org.jetbrains.exposed.sql.transactions.transaction

class QuestionService(
    private val questionRepository: QuestionRepository,
    private val answerRepository: AnswerRepository
) {

    fun getTodayQuestions(userId: Long): TodayQuestionResponse = transaction {
        val totalQuestions = questionRepository.count()
        val answeredCount = answerRepository.countByUserId(userId)
        val nextQuestionNumber = if (answeredCount < totalQuestions) answeredCount + 1 else null
        val question = nextQuestionNumber?.let { questionRepository.findBySequence(it) }

        TodayQuestionResponse(
            question = question,
            answeredCount = answeredCount,
            totalQuestions = totalQuestions,
            remainingQuestions = (totalQuestions - answeredCount).coerceAtLeast(0)
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
}
