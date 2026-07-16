package com.sorobanzen.app.domain

data class PracticeProgress(
    val isActive: Boolean = false,
    val isAwaitingNextProblem: Boolean = false,
    val problem: String = "",
    val score: Int = 0,
    val total: Int = 0
)

data class PracticeSubmission(
    val isCorrect: Boolean,
    val correctAnswer: Long
)

class PracticeSession {
    var progress: PracticeProgress = PracticeProgress()
        private set

    private var answer: Long = 0L

    fun start(problem: String, answer: Long) {
        this.answer = answer
        progress = PracticeProgress(isActive = true, problem = problem)
    }

    fun submit(input: String): PracticeSubmission? {
        if (!progress.isActive || progress.isAwaitingNextProblem) return null
        val submittedAnswer = input.toLongOrNull() ?: return null
        val isCorrect = submittedAnswer == answer
        progress = progress.copy(
            isAwaitingNextProblem = true,
            score = progress.score + if (isCorrect) 1 else 0,
            total = progress.total + 1
        )
        return PracticeSubmission(isCorrect, answer)
    }

    fun advance(problem: String, answer: Long) {
        if (!progress.isActive) return
        this.answer = answer
        progress = progress.copy(
            isAwaitingNextProblem = false,
            problem = problem
        )
    }

    fun stop() {
        progress = progress.copy(isActive = false, isAwaitingNextProblem = false)
    }
}
