package com.mentalgym.app.domain.content

import java.time.LocalDate
import java.time.temporal.IsoFields

/**
 * All Groq system prompts and generation constraints live here so copy stays consistent,
 * on-theme, and easy to tune without hunting through the coach implementation.
 */
object ExercisePrompts {

    /** ISO week-based year + week, e.g. `2026-W12`, so prompts naturally rotate over weeks. */
    fun isoWeekYear(): String {
        val d = LocalDate.now()
        val y = d.get(IsoFields.WEEK_BASED_YEAR)
        val w = d.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)
        return "${y}-W${w.toString().padStart(2, '0')}"
    }

    /** Injected into topic-generation user prompts when we have history from prior sessions. */
    fun drillFreshnessHint(isoWeekYear: String, recentSnippets: List<String>): String {
        if (recentSnippets.isEmpty()) {
            return "Freshness anchor: $isoWeekYear — choose a genuinely different angle than a generic brainstorm or debate."
        }
        return buildString {
            append("Freshness anchor: $isoWeekYear. ")
            append("These prompts were already used recently for this same exercise — do NOT repeat or trivially paraphrase them; change domain and stance:\n")
            recentSnippets.forEachIndexed { i, s ->
                append("${i + 1}. ${s.take(180).trim()}\n")
            }
        }
    }

    const val MODEL_GENERATE = "openai/gpt-oss-120b"
    const val MODEL_EVAL = "openai/gpt-oss-20b"

    fun themeGuard(exerciseTitle: String, exerciseId: String, cognitiveHint: String = ""): String = """
        App: Mental Gym — structured cognitive workout (not general chat).
        Exercise title: "$exerciseTitle"
        Exercise id: $exerciseId
        ${if (cognitiveHint.isNotBlank()) "Cognitive focus: $cognitiveHint" else ""}
        Keep outputs concrete, appropriate for a short drill, and aligned with this exercise.
    """.trimIndent()

    val ARITH_PROBLEM_SYSTEM = """
        Reply JSON only: {"expression":"...","answer":<integer>}.
        Mental arithmetic only; expression readable on a phone; numbers reasonable for mental math.
    """.trimIndent()

    val ARITH_EVAL_SYSTEM = """
        Reply JSON only: {"correct":<bool>,"feedback":"<short>"}.
        Judge numeric equivalence generously (spacing, commas, words like "minus").
    """.trimIndent()

    val SEQ_GEN_SYSTEM = """
        Reply JSON only: {"sequence_display":"3, 1, 8, 2","normalized_answer":"3182"}.
        sequence_display MUST list every single digit separated by ", " (comma space).
        NEVER use "...", "…", or ellipsis — the user must see the full sequence to memorize.
        normalized_answer must be exactly those digits concatenated, same order, no spaces.
    """.trimIndent()

    val SEQ_EVAL_SYSTEM = """
        Reply JSON only: {"correct":<bool>,"feedback":"<short>"}.
        User types digits in order; accept if order matches even with extra spaces or punctuation.
    """.trimIndent()

    val LOGIC_GEN_SYSTEM = """
        Reply JSON only: {"question":"<full puzzle>","answer":"<lowercase short answer>"}.
        The question MUST be fully self-contained: include every rule, premise, and definition the solver needs.
        No acronyms without expansion. No references to "the list above" or missing context.
        One clear question at the end. Answer: one word or a short phrase (lowercase) checkable from the text alone.
        Avoid famous puzzle names; write an original micro-scenario.
    """.trimIndent()

    val PATTERN_GEN_SYSTEM = """
        Reply JSON only: {"series":"<visible terms>","next":"<expected next term lowercase>"}.
        series: comma-separated, pattern obvious from visible terms; no trick wording.
        next: the single next term only (lowercase if words).
    """.trimIndent()

    val CAT_GEN_SYSTEM = """
        Reply JSON only: {"category":"<short noun phrase>"}.
        Category must be listable in 1–2 minutes (examples: "Indian sweets", "sports with a ball").
    """.trimIndent()

    val CAT_EVAL_SYSTEM = """
        Reply JSON only: {"feedback":"<2-4 sentences>"}.
        Mention roughly how many plausible items and encourage variety. No "correct" field.
    """.trimIndent()

    val SHORT_EVAL_SYSTEM = """
        Reply JSON only: {"correct":<bool>,"feedback":"<short>"}.
        Fair to synonyms and minor formatting for short logic/pattern answers.
    """.trimIndent()

    val TEXT_COACH_SYSTEM = """
        Reply JSON only: {"feedback":"<3-5 sentences constructive coaching>"}.
        No numeric grade; encourage clarity and one concrete improvement.
    """.trimIndent()

    val TEXT_TOPIC_SYSTEM_BASE = """
        Reply JSON only: {"panel_title":"<short label>","task":"<assignment>"}.
        No markdown fences. The task must be ONE focused thing the user can start immediately on a phone.
    """.trimIndent()

    /** Extra rules appended by exercise id in code (idea generation, perspective, etc.). */
    val TEXT_TOPIC_IDEA_EXTRA = """
        For idea_generation only:
        The task must be exactly ONE brainstorming topic (one sentence).
        Do NOT ask for a numbered multi-part list. Do NOT bundle 4–5 separate deliverables.
        Optional: say "list as many ideas as you can in a few minutes" only — nothing else.
        If the user message lists recent topics, you must pick something clearly different in domain and wording.
    """.trimIndent()

    val TEXT_TOPIC_PERSPECTIVE_EXTRA = """
        For perspective_switching only:
        State ONE concrete controversial claim the user should debate (one sentence).
        Do not add extra research steps or multiple unrelated questions.
        If recent claims are listed, avoid repeating the same policy domain and stance (e.g. don’t swap only one word).
    """.trimIndent()

    val TEXT_TOPIC_MEMORY_PALACE_EXTRA = """
        For memory_palace (method of loci — long-term memory technique):
        Suggest ONE short ordered list of 6–10 concrete items (not abstract themes) to memorize,
        and remind them to place each item along a familiar route (home, college, metro stops).
        Name the technique once ("memory palace / method of loci").
    """.trimIndent()
}
