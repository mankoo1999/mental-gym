package com.mentalgym.app.data.remote.groq

import com.mentalgym.app.domain.content.ExercisePrompts
import com.mentalgym.app.domain.model.ExerciseInteractionKind
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.math.abs
import kotlin.random.Random
import javax.inject.Inject
import javax.inject.Singleton

/** Result of checking / coaching a user response. [correct] is null for open-ended coaching only. */
data class ExerciseEvaluation(
    val correct: Boolean?,
    val feedback: String,
    val usedAi: Boolean
)

data class LoadedInteractiveChallenge(
    val kind: ExerciseInteractionKind,
    val title: String,
    val body: String,
    val multilineInput: Boolean,
    /** For arithmetic AI evaluation path. */
    val expectedNumber: Int?,
    /** Normalized expected string (digits-only for sequences, lowercase trimmed for logic/pattern). */
    val expectedTextKey: String?,
    val categoryLabel: String?,
    val usedAi: Boolean,
    /** Sequence recall: show [body] with digits only while countdown runs, then show [recallPrompt]. */
    val memorizeSeconds: Int = 0,
    val recallPrompt: String? = null
)

@Singleton
class GroqExerciseCoach @Inject constructor(
    private val groq: GroqChatClient,
    private val json: Json
) {

    private data class GenCtx(
        val exerciseId: String,
        val exerciseTitle: String,
        val sessionNonce: Long
    ) {
        fun rng(kind: ExerciseInteractionKind, mix: Int = 0): Random {
            val seed = sessionNonce xor
                (exerciseId.hashCode().toLong() shl 16) xor
                (kind.ordinal.toLong() shl 32) xor
                (mix.toLong() shl 40)
            return Random(seed)
        }
    }

    suspend fun loadChallenge(
        kind: ExerciseInteractionKind,
        difficultyLevel: Int,
        exerciseId: String,
        exerciseTitle: String,
        exerciseDescription: String,
        exerciseInstructions: String,
        apiKey: String?,
        sessionNonce: Long,
        recentSnippets: List<String> = emptyList()
    ): LoadedInteractiveChallenge {
        val key = apiKey?.trim().orEmpty()
        val d = difficultyLevel.coerceIn(1, 10)
        val ctx = GenCtx(exerciseId, exerciseTitle, sessionNonce)
        return when (kind) {
            ExerciseInteractionKind.AI_TEXT_COACH ->
                loadTextCoachChallenge(
                    key,
                    d,
                    ctx,
                    exerciseDescription,
                    exerciseInstructions,
                    recentSnippets
                )
            ExerciseInteractionKind.AI_ARITHMETIC ->
                loadArithmetic(key, d, ctx)
            ExerciseInteractionKind.AI_SEQUENCE_RECALL ->
                loadSequence(key, d, ctx, retryStep = 0)
            ExerciseInteractionKind.AI_LOGIC_SHORT ->
                loadLogic(key, d, ctx, retryStep = 0)
            ExerciseInteractionKind.AI_PATTERN ->
                loadPattern(key, d, ctx, retryStep = 0)
            ExerciseInteractionKind.AI_CATEGORY_SPRINT ->
                loadCategory(key, d, ctx, retryStep = 0)
            else -> error("not an AI challenge kind")
        }
    }

    suspend fun evaluate(
        kind: ExerciseInteractionKind,
        challenge: LoadedInteractiveChallenge,
        userInput: String,
        apiKey: String?
    ): ExerciseEvaluation {
        val key = apiKey?.trim().orEmpty()
        return when (kind) {
            ExerciseInteractionKind.AI_ARITHMETIC -> {
                val n = challenge.expectedNumber
                    ?: return ExerciseEvaluation(false, "Missing problem data.", false)
                evaluateArithmetic(key, challenge.body, n, userInput)
            }
            ExerciseInteractionKind.AI_SEQUENCE_RECALL -> evaluateSequence(key, challenge, userInput)
            ExerciseInteractionKind.AI_LOGIC_SHORT,
            ExerciseInteractionKind.AI_PATTERN -> evaluateTextAnswer(key, challenge, userInput)
            ExerciseInteractionKind.AI_CATEGORY_SPRINT -> evaluateCategoryList(key, challenge, userInput)
            ExerciseInteractionKind.AI_TEXT_COACH -> coachOpenText(key, challenge.title, challenge.body, userInput)
            else -> ExerciseEvaluation(null, "Unsupported.", false)
        }
    }

    private suspend fun loadArithmetic(key: String, d: Int, ctx: GenCtx): LoadedInteractiveChallenge {
        val rng = ctx.rng(ExerciseInteractionKind.AI_ARITHMETIC)
        if (key.isEmpty()) {
            val (expr, ans) = LocalArithmeticGenerator.generate(d, rng)
            return LoadedInteractiveChallenge(
                ExerciseInteractionKind.AI_ARITHMETIC,
                "Mental math",
                expr,
                multilineInput = false,
                expectedNumber = ans,
                expectedTextKey = null,
                categoryLabel = null,
                usedAi = false
            )
        }
        return try {
            val raw = groq.completeChat(
                apiKey = key,
                model = ExercisePrompts.MODEL_GENERATE,
                systemPrompt = ExercisePrompts.ARITH_PROBLEM_SYSTEM,
                userPrompt = """
                    Difficulty (1-10): $d
                    session_nonce: ${ctx.sessionNonce}
                    ${ExercisePrompts.themeGuard(ctx.exerciseTitle, ctx.exerciseId, "working memory / calculation")}
                    One JSON object only.
                """.trimIndent()
            )
            val dto = json.decodeFromString<ArithmeticProblemDto>(stripFences(raw))
            LoadedInteractiveChallenge(
                ExerciseInteractionKind.AI_ARITHMETIC,
                "Mental math",
                dto.expression.trim(),
                false,
                dto.answer,
                null,
                null,
                true
            )
        } catch (_: Exception) {
            val (expr, ans) = LocalArithmeticGenerator.generate(d, rng)
            LoadedInteractiveChallenge(
                ExerciseInteractionKind.AI_ARITHMETIC,
                "Mental math",
                expr,
                false,
                ans,
                null,
                null,
                false
            )
        }
    }

    private suspend fun evaluateArithmetic(
        key: String,
        expression: String,
        expected: Int,
        userRaw: String
    ): ExerciseEvaluation {
        if (key.isEmpty()) {
            val e = LocalArithmeticGenerator.evaluateLocally(expression, expected, userRaw)
            return ExerciseEvaluation(e.correct, e.feedback, false)
        }
        return try {
            val raw = groq.completeChat(
                apiKey = key,
                model = ExercisePrompts.MODEL_EVAL,
                systemPrompt = ExercisePrompts.ARITH_EVAL_SYSTEM,
                userPrompt = """
                    Problem: $expression
                    Correct answer: $expected
                    User: ${userRaw.trim()}
                """.trimIndent()
            )
            val dto = json.decodeFromString<EvalBoolDto>(stripFences(raw))
            ExerciseEvaluation(
                dto.correct,
                dto.feedback.trim().ifEmpty { if (dto.correct) "Nice." else "Expected $expected." },
                true
            )
        } catch (_: Exception) {
            val e = LocalArithmeticGenerator.evaluateLocally(expression, expected, userRaw)
            ExerciseEvaluation(e.correct, e.feedback, false)
        }
    }

    private suspend fun loadSequence(key: String, d: Int, ctx: GenCtx, retryStep: Int): LoadedInteractiveChallenge {
        val rng = ctx.rng(ExerciseInteractionKind.AI_SEQUENCE_RECALL, retryStep)
        val len = (4 + d).coerceIn(5, 14)
        if (key.isEmpty()) {
            val digits = List(len) { rng.nextInt(0, 10) }
            return sequenceChallengeFromDigits(digits, false, d)
        }
        if (retryStep > 15) {
            val digits = List(len) { rng.nextInt(0, 10) }
            return sequenceChallengeFromDigits(digits, false, d)
        }
        return try {
            val raw = groq.completeChat(
                apiKey = key,
                model = ExercisePrompts.MODEL_GENERATE,
                systemPrompt = ExercisePrompts.SEQ_GEN_SYSTEM,
                userPrompt = """
                    Exactly $len single-digit numbers 0-9 in order. Difficulty feel ~$d.
                    session_nonce: ${ctx.sessionNonce}
                    retry_attempt: $retryStep
                    ${ExercisePrompts.themeGuard(ctx.exerciseTitle, ctx.exerciseId, "working memory / digit span")}
                """.trimIndent()
            )
            val dto = json.decodeFromString<SequenceDto>(stripFences(raw))
            val canonical = canonicalDigitsFromAi(dto)
            val minLen = (len - 1).coerceAtLeast(5)
            if (canonical.length < minLen) {
                return loadSequence("", d, ctx.copy(sessionNonce = ctx.sessionNonce + 31L * (retryStep + 1)), retryStep + 1)
            }
            sequenceChallengeFromCanonicalString(canonical, true, d)
        } catch (_: Exception) {
            loadSequence("", d, ctx.copy(sessionNonce = ctx.sessionNonce + 17L * (retryStep + 1)), retryStep + 1)
        }
    }

    private fun canonicalDigitsFromAi(dto: SequenceDto): String {
        val raw = dto.sequenceDisplay.trim()
        val beforeEllipsis = raw.substringBefore("...").substringBefore("…").trim()
        val fromDisplay = beforeEllipsis.filter { it.isDigit() }
        val fromDto = dto.normalizedAnswer.trim().filter { it.isDigit() }
        return when {
            fromDisplay.isNotEmpty() && fromDto.isNotEmpty() && fromDisplay != fromDto -> fromDisplay
            fromDisplay.isNotEmpty() -> fromDisplay
            fromDto.isNotEmpty() -> fromDto
            else -> ""
        }
    }

    private fun sequenceChallengeFromDigits(
        digits: List<Int>,
        usedAi: Boolean,
        d: Int
    ): LoadedInteractiveChallenge =
        sequenceChallengeFromCanonicalString(digits.joinToString("") { it.toString() }, usedAi, d)

    private fun sequenceChallengeFromCanonicalString(
        canonical: String,
        usedAi: Boolean,
        d: Int
    ): LoadedInteractiveChallenge {
        val displayComma = canonical.asIterable().joinToString(", ") { it.toString() }
        val memSec = (6 + d / 2).coerceIn(6, 14)
        val recall =
            "Digits are hidden. Type every digit in order with no spaces or commas."
        return LoadedInteractiveChallenge(
            ExerciseInteractionKind.AI_SEQUENCE_RECALL,
            "Sequence recall",
            "Memorize this ($memSec s — clock is below; pause the exercise timer if you need longer). Then digits hide:\n\n$displayComma",
            false,
            null,
            canonical,
            null,
            usedAi,
            memSec,
            recall
        )
    }

    private suspend fun evaluateSequence(
        key: String,
        challenge: LoadedInteractiveChallenge,
        userRaw: String
    ): ExerciseEvaluation {
        val norm = challenge.expectedTextKey ?: return ExerciseEvaluation(false, "Missing sequence.", false)
        val userDigits = userRaw.filter { it.isDigit() }
        if (key.isEmpty()) {
            val ok = userDigits == norm
            return ExerciseEvaluation(
                ok,
                if (ok) "Perfect recall." else "Expected digit order: $norm",
                false
            )
        }
        return try {
            val raw = groq.completeChat(
                apiKey = key,
                model = ExercisePrompts.MODEL_EVAL,
                systemPrompt = ExercisePrompts.SEQ_EVAL_SYSTEM,
                userPrompt = """
                    Shown sequence digits in order (canonical): $norm
                    User typed: ${userRaw.trim()}
                """.trimIndent()
            )
            val dto = json.decodeFromString<EvalBoolDto>(stripFences(raw))
            ExerciseEvaluation(dto.correct, dto.feedback.trim(), true)
        } catch (_: Exception) {
            val ok = userDigits == norm
            ExerciseEvaluation(ok, if (ok) "Perfect recall." else "Expected: $norm", false)
        }
    }

    private suspend fun loadLogic(key: String, d: Int, ctx: GenCtx, retryStep: Int): LoadedInteractiveChallenge {
        if (key.isEmpty()) {
            val r = ctx.rng(ExerciseInteractionKind.AI_LOGIC_SHORT, retryStep)
            val (q, a) = LocalLogicPattern.logicRiddle(r.nextInt())
            return LoadedInteractiveChallenge(
                ExerciseInteractionKind.AI_LOGIC_SHORT,
                "Logic",
                q,
                false,
                null,
                a,
                null,
                false
            )
        }
        return try {
            val raw = groq.completeChat(
                apiKey = key,
                model = ExercisePrompts.MODEL_GENERATE,
                systemPrompt = ExercisePrompts.LOGIC_GEN_SYSTEM,
                userPrompt = """
                    Difficulty ~$d.
                    session_nonce: ${ctx.sessionNonce}
                    retry: $retryStep
                    ${ExercisePrompts.themeGuard(ctx.exerciseTitle, ctx.exerciseId, "reasoning / deduction")}
                    Invent a novel micro-puzzle; avoid famous classics (no river-crossing, no wolf-goat-cabbage).
                """.trimIndent()
            )
            val dto = json.decodeFromString<LogicDto>(stripFences(raw))
            LoadedInteractiveChallenge(
                ExerciseInteractionKind.AI_LOGIC_SHORT,
                "Logic",
                dto.question.trim(),
                false,
                null,
                dto.answer.trim().lowercase(),
                null,
                true
            )
        } catch (_: Exception) {
            loadLogic("", d, ctx, retryStep + 1)
        }
    }

    private suspend fun loadPattern(key: String, d: Int, ctx: GenCtx, retryStep: Int): LoadedInteractiveChallenge {
        if (key.isEmpty()) {
            val r = ctx.rng(ExerciseInteractionKind.AI_PATTERN, retryStep)
            val (series, next) = LocalLogicPattern.numericPattern(d, r)
            return LoadedInteractiveChallenge(
                ExerciseInteractionKind.AI_PATTERN,
                "Pattern",
                "What comes next?\n\n$series",
                false,
                null,
                next,
                null,
                false
            )
        }
        return try {
            val raw = groq.completeChat(
                apiKey = key,
                model = ExercisePrompts.MODEL_GENERATE,
                systemPrompt = ExercisePrompts.PATTERN_GEN_SYSTEM,
                userPrompt = """
                    Difficulty ~$d. Simple numeric or obvious word pattern; one clear next term.
                    session_nonce: ${ctx.sessionNonce}
                    ${ExercisePrompts.themeGuard(ctx.exerciseTitle, ctx.exerciseId, "pattern / induction")}
                """.trimIndent()
            )
            val dto = json.decodeFromString<PatternDto>(stripFences(raw))
            LoadedInteractiveChallenge(
                ExerciseInteractionKind.AI_PATTERN,
                "Pattern",
                "What comes next?\n\n${dto.series.trim()}",
                false,
                null,
                dto.next.trim().lowercase(),
                null,
                true
            )
        } catch (_: Exception) {
            loadPattern("", d, ctx, retryStep + 1)
        }
    }

    private suspend fun loadCategory(key: String, d: Int, ctx: GenCtx, retryStep: Int): LoadedInteractiveChallenge {
        val rng = ctx.rng(ExerciseInteractionKind.AI_CATEGORY_SPRINT, retryStep)
        val localCat = LocalLogicPattern.randomCategory(d, rng)
        if (key.isEmpty()) {
            return LoadedInteractiveChallenge(
                ExerciseInteractionKind.AI_CATEGORY_SPRINT,
                "Rapid naming",
                "Category: $localCat\n\nList as many as you can (comma-separated or line breaks). Tap Check — offline mode only counts items roughly.",
                true,
                null,
                null,
                localCat,
                false
            )
        }
        return try {
            val raw = groq.completeChat(
                apiKey = key,
                model = ExercisePrompts.MODEL_GENERATE,
                systemPrompt = ExercisePrompts.CAT_GEN_SYSTEM,
                userPrompt = """
                    Pick one concrete category; difficulty ~$d (harder = narrower niche).
                    About half the time choose something natural for someone in India (states/UTs, festivals, cricket, Indian cinema, street food, languages, monuments).
                    session_nonce: ${ctx.sessionNonce}
                    ${ExercisePrompts.themeGuard(ctx.exerciseTitle, ctx.exerciseId, "fluency / rapid naming")}
                """.trimIndent()
            )
            val dto = json.decodeFromString<CategoryDto>(stripFences(raw))
            val cat = dto.category.trim()
            LoadedInteractiveChallenge(
                ExerciseInteractionKind.AI_CATEGORY_SPRINT,
                "Rapid naming",
                "List as many things in this category as you can: $cat (comma-separated or line breaks).",
                true,
                null,
                null,
                cat,
                true
            )
        } catch (_: Exception) {
            loadCategory("", d, ctx, retryStep + 1)
        }
    }

    private suspend fun evaluateTextAnswer(
        key: String,
        challenge: LoadedInteractiveChallenge,
        userRaw: String
    ): ExerciseEvaluation {
        val canon = challenge.expectedTextKey ?: return ExerciseEvaluation(false, "Missing key.", false)
        val u = userRaw.trim().lowercase()
        if (key.isEmpty()) {
            val ok = u == canon || u.removeSpaces() == canon.removeSpaces()
            return ExerciseEvaluation(ok, if (ok) "Matches." else "Looking for something like: $canon", false)
        }
        return try {
            val raw = groq.completeChat(
                apiKey = key,
                model = ExercisePrompts.MODEL_EVAL,
                systemPrompt = ExercisePrompts.SHORT_EVAL_SYSTEM,
                userPrompt = """
                    Context: ${challenge.title}
                    Expected answer (canonical): $canon
                    User: ${userRaw.trim()}
                """.trimIndent()
            )
            val dto = json.decodeFromString<EvalBoolDto>(stripFences(raw))
            ExerciseEvaluation(dto.correct, dto.feedback.trim(), true)
        } catch (_: Exception) {
            val ok = u == canon
            ExerciseEvaluation(ok, if (ok) "Matches." else "Expected: $canon", false)
        }
    }

    private suspend fun evaluateCategoryList(
        key: String,
        challenge: LoadedInteractiveChallenge,
        userRaw: String
    ): ExerciseEvaluation {
        val cat = challenge.categoryLabel ?: "items"
        val lines = userRaw.split(Regex("[,\n;]+")).map { it.trim() }.filter { it.length > 1 }
        val count = lines.size
        if (key.isEmpty()) {
            return ExerciseEvaluation(
                null,
                "Offline: you listed about $count line(s). Aim for speed and variety in \"$cat\".",
                false
            )
        }
        return try {
            val raw = groq.completeChat(
                apiKey = key,
                model = ExercisePrompts.MODEL_EVAL,
                systemPrompt = ExercisePrompts.CAT_EVAL_SYSTEM,
                userPrompt = """
                    Category: $cat
                    User list:
                    ${userRaw.trim()}
                """.trimIndent()
            )
            val dto = json.decodeFromString<EvalCoachDto>(stripFences(raw))
            ExerciseEvaluation(null, dto.feedback.trim(), true)
        } catch (_: Exception) {
            ExerciseEvaluation(
                null,
                "Rough count: $count. Add a Groq key for richer feedback on \"$cat\".",
                false
            )
        }
    }

    private suspend fun coachOpenText(
        key: String,
        exerciseTitle: String,
        contextHint: String,
        userText: String
    ): ExerciseEvaluation {
        if (key.isEmpty()) {
            return ExerciseEvaluation(
                null,
                "Save a Groq API key in Settings for AI coaching on your writing. Until then: re-read what you wrote and fix one unclear sentence.",
                false
            )
        }
        return try {
            val raw = groq.completeChat(
                apiKey = key,
                model = ExercisePrompts.MODEL_EVAL,
                systemPrompt = ExercisePrompts.TEXT_COACH_SYSTEM,
                userPrompt = """
                    Exercise: $exerciseTitle
                    Guidance: $contextHint

                    User wrote:
                    ${userText.trim()}
                """.trimIndent()
            )
            val dto = json.decodeFromString<EvalCoachDto>(stripFences(raw))
            ExerciseEvaluation(null, dto.feedback.trim(), true)
        } catch (_: Exception) {
            ExerciseEvaluation(null, "Could not reach the coach — try again.", false)
        }
    }

    private suspend fun loadTextCoachChallenge(
        key: String,
        d: Int,
        ctx: GenCtx,
        exerciseDescription: String,
        exerciseInstructions: String,
        recentSnippets: List<String>
    ): LoadedInteractiveChallenge {
        val r = ctx.rng(ExerciseInteractionKind.AI_TEXT_COACH)
        val salt = r.nextInt()
        val exerciseId = ctx.exerciseId
        val exerciseTitle = ctx.exerciseTitle
        if (key.isEmpty()) {
            return LocalWritingPrompts.challengeOffline(
                exerciseId,
                exerciseTitle,
                d,
                salt,
                recentSnippets
            )
        }
        val system = buildString {
            append(ExercisePrompts.TEXT_TOPIC_SYSTEM_BASE)
            append("\n\n")
            when (exerciseId) {
                "idea_generation" -> append(ExercisePrompts.TEXT_TOPIC_IDEA_EXTRA)
                "perspective_switching" -> append(ExercisePrompts.TEXT_TOPIC_PERSPECTIVE_EXTRA)
                "memory_palace" -> append(ExercisePrompts.TEXT_TOPIC_MEMORY_PALACE_EXTRA)
                else -> append(
                    "Match the exercise id: give one focused assignment only; avoid stacking unrelated tasks."
                )
            }
        }
        return try {
            val raw = groq.completeChat(
                apiKey = key,
                model = ExercisePrompts.MODEL_GENERATE,
                systemPrompt = system,
                userPrompt = """
                    exercise_id: $exerciseId
                    exercise_name: $exerciseTitle
                    difficulty_1_to_10: $d
                    session_nonce: ${ctx.sessionNonce}
                    app_description: ${exerciseDescription.trim()}
                    coach_steps (optional context only): ${exerciseInstructions.trim()}
                    ${ExercisePrompts.themeGuard(exerciseTitle, exerciseId)}
                    ${ExercisePrompts.drillFreshnessHint(ExercisePrompts.isoWeekYear(), recentSnippets)}

                    analogy_creation → name two concepts and ask for exactly one metaphor bridging them.
                    category_switching → list 4–6 concrete objects to re-sort under different rules.
                    complex_problem → one messy planning scenario to break into steps.

                    User often lives in India; use Indian examples only when they fit naturally.
                """.trimIndent()
            )
            val dto = json.decodeFromString<TextTopicDto>(stripFences(raw))
            val body = buildString {
                append(dto.task.trim())
                append("\n\nTap Coach feedback when ready.")
            }
            LoadedInteractiveChallenge(
                kind = ExerciseInteractionKind.AI_TEXT_COACH,
                title = dto.panelTitle.trim().ifEmpty { exerciseTitle },
                body = body,
                multilineInput = true,
                expectedNumber = null,
                expectedTextKey = null,
                categoryLabel = null,
                usedAi = true
            )
        } catch (_: Exception) {
            LocalWritingPrompts.challengeOffline(
                exerciseId,
                exerciseTitle,
                d,
                salt,
                recentSnippets
            )
        }
    }

    private fun String.removeSpaces(): String = filter { !it.isWhitespace() }

    private fun stripFences(raw: String): String {
        var s = raw.trim()
        if (s.startsWith("```")) {
            val nl = s.indexOf('\n')
            if (nl >= 0) s = s.drop(nl + 1)
            val end = s.lastIndexOf("```")
            if (end >= 0) s = s.take(end)
        }
        return s.trim()
    }
}

@Serializable
private data class ArithmeticProblemDto(val expression: String, val answer: Int)

@Serializable
private data class EvalBoolDto(val correct: Boolean, val feedback: String = "")

@Serializable
private data class EvalCoachDto(val feedback: String)

@Serializable
private data class SequenceDto(
    @SerialName("sequence_display") val sequenceDisplay: String,
    @SerialName("normalized_answer") val normalizedAnswer: String
)

@Serializable
private data class LogicDto(val question: String, val answer: String)

@Serializable
private data class PatternDto(val series: String, val next: String)

@Serializable
private data class CategoryDto(val category: String)

@Serializable
private data class TextTopicDto(
    @SerialName("panel_title") val panelTitle: String,
    val task: String
)

/** Built-in writing prompts when offline or the topic API fails. */
private object LocalWritingPrompts {

    private fun pickAvoiding(pool: List<String>, recentLower: List<String>, rng: Random): String {
        if (pool.isEmpty()) return ""
        val order = pool.indices.shuffled(rng)
        fun tooClose(choice: String): Boolean {
            val c = choice.lowercase()
            if (c.length < 6) return false
            return recentLower.any { r ->
                if (r.length < 10) return@any false
                r.contains(c) || c.contains(r.trim().take(minOf(28, r.length)))
            }
        }
        for (i in order) {
            val pick = pool[i]
            if (!tooClose(pick)) return pick
        }
        return pool[order[0]]
    }

    fun challengeOffline(
        exerciseId: String,
        defaultTitle: String,
        d: Int,
        salt: Int,
        recentSnippets: List<String> = emptyList()
    ): LoadedInteractiveChallenge {
        val weekMix = ExercisePrompts.isoWeekYear().hashCode().toLong()
        val rng = Random(
            (exerciseId.hashCode().toLong() shl 32) xor salt.toLong() xor d.toLong() xor weekMix
        )
        val recentLower = recentSnippets.map { it.lowercase() }
        val (title, task) = when (exerciseId) {
            "perspective_switching" -> {
                val claim = pickAvoiding(perspectiveClaims, recentLower, rng)
                "Debate topic" to
                    "Claim to work with: $claim\n\nIn one reply: argue for it, then against it, then one nuance a third person might add " +
                    "(keep each part short)."
            }
            "idea_generation" -> {
                val t = pickAvoiding(brainstormTopics, recentLower, rng)
                "Brainstorm topic" to
                    "Only task: list as many ideas as you can for: $t\n\nQuantity first; you can organize later."
            }
            "analogy_creation" -> {
                val (a, b) = analogyPairs[analogyPairs.indices.shuffled(rng).first()]
                "One metaphor" to
                    "Compare \"$a\" with \"$b\" in one paragraph: start with one similarity, end with one metaphor."
            }
            "category_switching" -> {
                val items = pickAvoiding(objectBundles, recentLower, rng)
                "Re-sort" to
                    "Objects: $items\n\nName three different sorting rules; under each rule, say how you’d group these objects."
            }
            "memory_palace" -> {
                val list = pickAvoiding(indiaMemoryLists, recentLower, rng)
                "Memory palace (long-term memory)" to
                    "Technique: pick a familiar route (e.g. home → metro). Place one vivid image per stop.\n\n" +
                    "Memorize this ordered list: $list\n\n" +
                    "Write the route and what you placed at each location (short phrases)."
            }
            "complex_problem" -> {
                val s = pickAvoiding(planningScenarios, recentLower, rng)
                "Plan in steps" to
                    "Scenario: $s\n\nSketch a plan: sub-problems, then ordered steps (bullets are fine)."
            }
            else -> defaultTitle to
                "${defaultTitle}: follow the exercise steps from the card above, then reflect in your own words below."
        }
        val body = buildString {
            append(task)
            append("\n\nCoach feedback needs a Groq key in Settings; offline, tighten one unclear sentence yourself.")
        }
        return LoadedInteractiveChallenge(
            kind = ExerciseInteractionKind.AI_TEXT_COACH,
            title = title,
            body = body,
            multilineInput = true,
            expectedNumber = null,
            expectedTextKey = null,
            categoryLabel = null,
            usedAi = false
        )
    }

    private val perspectiveClaims = listOf(
        "Public transport in big Indian cities should be free at the point of use.",
        "Bollywood should rely less on remakes and more on original scripts.",
        "India should adopt a four-day work week for office jobs where productivity allows.",
        "The IPL should cap team spending harder to level the playing field.",
        "NEP-style multi-language schooling is workable for most states.",
        "High-speed rail should be prioritized over more new expressways.",
        "Privacy should trump convenience for Aadhaar-linked services in sensitive domains.",
        "Crop burning bans need compensation, not only fines.",
        "Work-from-home should remain a default option where the job allows.",
        "Classical language study should be optional, not mandated nationally.",
        "Private coaching for board exams should be regulated more tightly."
    )

    private val brainstormTopics = listOf(
        "Uses for a ₹500 note besides buying things",
        "Improvements to your nearest railway station",
        "Ways to cut wedding waste without killing joy",
        "Monsoon hacks for a small apartment",
        "New snacks you could sell from a food cart",
        "Apps India still needs but doesn’t have",
        "Cheap ways to stay fit without a gym",
        "Icebreaker games for a mixed-language team",
        "How to make a local park safer after dark",
        "Side hustles that work with unreliable power/wifi"
    )

    private val analogyPairs = listOf(
        "monsoon" to "stock market volatility",
        "Indian thali" to "project timeline",
        "metro train" to "operating system",
        "cricket over" to "work sprint",
        "spice box (masala dabba)" to "developer toolchain",
        "kite festival" to "team morale",
        "filter coffee" to "daily standup"
    )

    private val objectBundles = listOf(
        "steel tiffin, bamboo basket, USB cable, kolhapuri chappal, metro smart card",
        "cricket ball, tabla, pressure cooker, rangoli stencil, laptop charger",
        "mango, kulhad, calendar, bicycle bell, board exam admit card",
        "dupatta, whiteboard marker, umbrella, cricket stump, spice jar"
    )

    private val indiaMemoryLists = listOf(
        "Tiger, Peacock, Lotus, Mango, Hockey, Banyan (national symbols themes)",
        "Sachin, Milkha, PT Usha, Viswanathan Anand, Mary Kom (sport firsts)",
        "Chennai, Kolkata, Mumbai, Bengaluru, Hyderabad (capitals you’ve visited — swap for yours)",
        "Diwali, Eid, Onam, Baisakhi, Christmas (festivals — replace with your own list)",
        "Roti, Dal, Chutney, Pickle, Papad (thali elements in order)"
    )

    private val planningScenarios = listOf(
        "You promised to help a cousin move flats during a week you already have deadlines.",
        "Two festivals land on the same long weekend and family expects you in two cities.",
        "Your building’s water supply fails the day before board exams at home.",
        "You must coordinate a team across IST, CET, and US Eastern for one release.",
        "Train cancellations strand you halfway to an interview — you have 90 minutes."
    )
}

/** Kept from previous arithmetic module. */
object LocalArithmeticGenerator {

    fun generate(difficulty: Int): Pair<String, Int> = generate(difficulty, Random.Default)

    fun generate(difficulty: Int, rng: Random): Pair<String, Int> {
        val d = difficulty.coerceIn(1, 10)
        return when (rng.nextInt(4)) {
            0 -> {
                val a = rng.nextInt(10, 10 + d * 8)
                val b = rng.nextInt(10, 10 + d * 8)
                "$a + $b" to a + b
            }
            1 -> {
                val a = rng.nextInt(20 + d * 5, 80 + d * 15)
                val b = rng.nextInt(5, minOf(a - 1, 20 + d * 4))
                "$a - $b" to a - b
            }
            2 -> {
                val a = rng.nextInt(3, 4 + d / 2)
                val b = rng.nextInt(3, 10 + d)
                "$a × $b" to a * b
            }
            else -> {
                val b = rng.nextInt(2, 6 + d / 2)
                val quotient = rng.nextInt(3, 8 + d)
                val a = b * quotient
                "$a ÷ $b" to quotient
            }
        }
    }

    data class ArithEval(val correct: Boolean, val feedback: String)

    fun evaluateLocally(expression: String, expected: Int, userRaw: String): ArithEval {
        val cleaned = userRaw.trim()
            .replace(",", "")
            .replaceFirst(Regex("(?i)^\\s*minus\\s*"), "-")
        val num = cleaned.toDoubleOrNull()
            ?: cleaned.filter { it.isDigit() || it == '-' || it == '.' }.toDoubleOrNull()
        val correct = num != null && abs(num - expected.toDouble()) < 1e-6
        val feedback = if (correct) "Correct — nice work."
        else "Not quite — the answer was $expected."
        return ArithEval(correct, feedback)
    }
}

private object LocalLogicPattern {
    /** Each question is self-contained with all needed facts; answer is short and checkable. */
    private val logicBank: List<Pair<String, String>> = listOf(
        "Premise 1: Every bloop is a razzle. Premise 2: Some razzles are toops. Question: Does it necessarily follow that some bloops are toops? Reply with yes or no only." to "no",
        "Two people A and B each make one statement. A says: 'Exactly one of us is lying.' B says: 'A is telling the truth.' Assuming each is either always truthful or always lying, who is lying? Reply with a or b only." to "b",
        "You flip a fair coin twice (order matters). What is the probability you get at least one heads? Reply as a fraction like 3/4." to "3/4",
        "Floor function: ⌊x⌋ is the greatest integer ≤ x. If ⌊x⌋ = 5 for a real number x, give one possible integer value for ⌊2x⌋ (just the number)." to "10",
        "Ordering facts: Lily is taller than Max. Aria is shorter than Max. Is Aria shorter than Lily? Reply yes or no." to "yes",
        "A factory has identical machines working at the same rate: 5 machines make 5 widgets in 5 minutes. How many minutes do 100 machines need to make 100 widgets? (Assume work can be parallelized across machines.)" to "5",
        "How many integers n satisfy 3 < n < 7 ? Reply with the count only." to "3",
        "A snack costs ₹37. You pay with a ₹50 note. The clerk returns change using only ₹5, ₹2, and ₹1 coins and uses as few coins as possible. How many rupees did you get back in total?" to "13",
        "Find two different prime numbers whose product is 15. What is their sum?" to "8",
        "The sequence lists primes in order: 2, 3, 5, 7, 11, ? What is the next term?" to "13",
        "Convert 2.5 minutes into seconds. Reply with an integer." to "150",
        "Solve for x when x + y = 12 and x − y = 4. Give only x." to "8",
        "Logical implication: if (NOT A) implies B, and A is false, must B necessarily be true? Reply yes or no (material implication in classical logic)." to "yes",
        "Compute 1+2+3+4+5+6+7+8+9. Reply with the integer." to "45",
        "A drawer has 10 blue socks and 10 red socks, all loose. You pull socks blindly one by one. What is the minimum number you must pull to guarantee you have a matching pair?" to "3",
        "A train 120 m long passes a pole in 4 seconds at constant speed. How fast is the train in m/s?" to "30"
    )

    fun logicRiddle(salt: Int): Pair<String, String> {
        val idx = ((salt xor 0x9E3779B9.toInt()) and 0x7FFFFFFF) % logicBank.size
        return logicBank[idx]
    }

    fun numericPattern(d: Int, rng: Random): Pair<String, String> {
        if (rng.nextBoolean()) {
            val start = rng.nextInt(2, 5)
            val step = 2 + (d / 4)
            val a = start
            val b = start + step
            val c = start + step * 2
            val next = (start + step * 3).toString()
            return "$a, $b, $c, ?" to next
        }
        val a = rng.nextInt(2, 5)
        val t0 = a
        val t1 = a * 2
        val t2 = a * 4
        val t3 = a * 8
        val next = (a * 16).toString()
        return "$t0, $t1, $t2, $t3, ?" to next
    }

    fun randomCategory(d: Int, rng: Random): String {
        val india = listOf(
            "states or union territories of India",
            "Indian sweets or mithai",
            "Test cricketers who played for India",
            "languages spoken widely in India",
            "Indian spices used in cooking",
            "Bollywood movies from the 2000s",
            "games children play on Indian streets",
            "South Indian breakfast dishes",
            "freedom fighters or movement leaders",
            "Classical dance forms of India",
            "IIT or IISc campuses",
            "rivers of India",
            "metro cities with rapid transit"
        )
        val global = listOf(
            "mammals", "programming languages", "musical instruments",
            "pieces of furniture", "types of weather", "vegetables", "sports played with a ball"
        )
        val pickIndia = (d + rng.nextInt(3)) % 5 != 0
        return if (pickIndia) india[(d + rng.nextInt()).mod(india.size)]
        else global[(d + rng.nextInt()).mod(global.size)]
    }
}
