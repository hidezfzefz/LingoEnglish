package com.lingo.app.data

import java.time.LocalDate
import java.time.temporal.WeekFields

/**
 * The social English ladder. XP levels and CEFR levels are deliberately
 * separate: XP measures time on task, while CEFR measures demonstrated skill.
 * No level can be skipped.
 */
enum class CefrLevel(
    val label: String,
    val title: String,
    val xpGate: Int,
    val activeDaysGate: Int,
    val sticker: StickerKind,
) {
    A1("A1", "First steps", 0, 0, StickerKind.SPROUT),
    A2("A2", "Everyday English", 150, 2, StickerKind.COFFEE),
    B1("B1", "Independent", 450, 7, StickerKind.COMPASS),
    B2("B2", "Confident", 900, 14, StickerKind.CHAT),
    C1("C1", "Advanced", 1500, 30, StickerKind.SPEAK),
    C2("C2", "Mastery", 2200, 45, StickerKind.BOOK),
    TOP("TOP", "Top flight", 3000, 60, StickerKind.TROPHY);

    val next: CefrLevel? get() = entries.getOrNull(ordinal + 1)
    val article: String get() = if (this == A1 || this == A2) "an" else "a"

    companion object {
        fun fromKey(key: String?): CefrLevel =
            entries.firstOrNull { it.name == key } ?: A1
    }
}

/** What a test item actually measures. Every exam samples all four. */
enum class TestSkill(val label: String) {
    GRAMMAR("Grammar"),
    VOCABULARY("Vocabulary"),
    READING("Reading"),
    PRAGMATICS("Use of English"),
}

data class LevelQuestion(
    val prompt: String,
    val options: List<String>,
    val correctIndex: Int,
    val skill: TestSkill = TestSkill.GRAMMAR,
    val explanation: String = "",
)

data class PreparedQuestion(
    val prompt: String,
    val options: List<String>,
    val correctIndex: Int,
    val skill: TestSkill,
    val explanation: String,
)

enum class TestKind { LEVEL, WEEKLY }

object LingoLevels {
    const val LEVEL_QUESTION_COUNT = 12
    const val LEVEL_PASS_SCORE = 10
    const val LEVEL_MINUTES = 18
    const val WEEKLY_QUESTION_COUNT = 10
    const val WEEKLY_MINUTES = 10
    const val RETRY_DAYS = 7L

    /**
     * A1 is used for the weekly challenge and as the retention half of the A2
     * weekly test. It still samples all four skills so early learners are not
     * only drilled on verb forms.
     */
    private val a1Warmup = listOf(
        LevelQuestion("Hello, ____ name is Ana.", listOf("I", "my", "me", "mine"), 1),
        LevelQuestion("How ____ you?", listOf("is", "are", "am", "be"), 1),
        LevelQuestion("She ____ coffee every morning.", listOf("drink", "drinks", "drinking", "drank"), 1),
        LevelQuestion("There ____ two chairs in the room.", listOf("is", "are", "am", "be"), 1),
        LevelQuestion("Where ____ you live?", listOf("do", "does", "are", "is"), 0),
        LevelQuestion(
            "I'm very ____. Could I have some water?",
            listOf("thirsty", "hungry", "sleepy", "angry"),
            0,
            TestSkill.VOCABULARY,
        ),
        LevelQuestion(
            "The opposite of “cheap” is ____.",
            listOf("expensive", "free", "small", "quiet"),
            0,
            TestSkill.VOCABULARY,
        ),
        LevelQuestion(
            "You can buy medicine at a ____.",
            listOf("pharmacy", "bakery", "library", "station"),
            0,
            TestSkill.VOCABULARY,
        ),
        LevelQuestion(
            "Read: “Hi Sam, I'm at the café next to the bank. See you at 3.” Where is the café?",
            listOf("Next to the bank", "Inside the bank", "Next to the station", "At Sam's house"),
            0,
            TestSkill.READING,
        ),
        LevelQuestion(
            "Read: “The pool is open Tuesday to Sunday, 9 a.m. to 6 p.m. It is closed on Mondays.” When can you swim?",
            listOf("Tuesday morning", "Monday morning", "Sunday at 7 p.m.", "Every day"),
            0,
            TestSkill.READING,
        ),
        LevelQuestion(
            "Someone says: “Thank you so much!” What is the best reply?",
            listOf("You're welcome.", "Yes, please.", "I'm sorry.", "Goodbye."),
            0,
            TestSkill.PRAGMATICS,
        ),
        LevelQuestion(
            "You want a coffee in a café. What is the most polite request?",
            listOf(
                "Could I have a coffee, please?",
                "I want coffee.",
                "Give me coffee.",
                "Coffee now.",
            ),
            0,
            TestSkill.PRAGMATICS,
        ),
        LevelQuestion(
            "Read: “The shop is open from 9 a.m. to 6 p.m. It is closed on Sundays.” When is the shop closed?",
            listOf("On Sundays", "At 9 a.m.", "At 6 p.m.", "Every afternoon"),
            0,
            TestSkill.READING,
        ),
        LevelQuestion(
            "Someone speaks too quickly. What is the best thing to say?",
            listOf(
                "Could you say that again, please?",
                "Speak slowly.",
                "What?",
                "Stop talking.",
            ),
            0,
            TestSkill.PRAGMATICS,
        ),
    )

    private val exams: Map<CefrLevel, List<LevelQuestion>> = mapOf(
        CefrLevel.A2 to listOf(
            LevelQuestion("I ____ coffee every morning.", listOf("drink", "drinks", "drinking", "drank"), 0),
            LevelQuestion("She has lived here ____ 2020.", listOf("since", "for", "from", "at"), 0),
            LevelQuestion("There isn't ____ milk in the fridge.", listOf("some", "any", "many", "a"), 1),
            LevelQuestion("We ____ to the cinema last night.", listOf("go", "went", "gone", "going"), 1),
            LevelQuestion("If it rains, we ____ at home.", listOf("stay", "will stay", "stayed", "would stay"), 1),
            LevelQuestion(
                "The room was ____ — clean, quiet, and comfortable.",
                listOf("spotless", "crowded", "noisy", "expensive"),
                0,
                TestSkill.VOCABULARY,
            ),
            LevelQuestion(
                "The opposite of “depart” is ____.",
                listOf("arrive", "leave", "board", "delay"),
                0,
                TestSkill.VOCABULARY,
            ),
            LevelQuestion(
                "Could you ____ the meaning of this word?",
                listOf("explain", "tell", "say", "speak"),
                0,
                TestSkill.VOCABULARY,
                "You explain a meaning. “Tell” needs a person: tell me the meaning.",
            ),
            LevelQuestion(
                "Read: “Hi, I'm running 20 minutes late. Could we meet at the café instead of the office? — Sam” What does Sam want to change?",
                listOf("The place", "The day", "The price", "The people"),
                0,
                TestSkill.READING,
            ),
            LevelQuestion(
                "Read: “Library: quiet study area. Phones silent. Drinks with lids only.” What is allowed?",
                listOf("A covered drink", "A phone call", "A loud conversation", "An open cup of coffee"),
                0,
                TestSkill.READING,
            ),
            LevelQuestion(
                "A colleague says: “Could you give me a hand with this report?” What is the best reply?",
                listOf("Of course. What do you need?", "No, I am busy.", "Why?", "You can do it."),
                0,
                TestSkill.PRAGMATICS,
            ),
            LevelQuestion(
                "Which sentence is the most polite request?",
                listOf(
                    "Could you speak more slowly, please?",
                    "Speak more slowly.",
                    "You must speak slowly.",
                    "Slowly!",
                ),
                0,
                TestSkill.PRAGMATICS,
            ),
            LevelQuestion(
                "Read: “Hi Priya, the train is delayed by 30 minutes. I'll be at the station around 6:15. — Leo” Why will Leo arrive later?",
                listOf(
                    "His train is delayed",
                    "He missed the train",
                    "He is already at the station",
                    "He is meeting Priya at work",
                ),
                0,
                TestSkill.READING,
            ),
            LevelQuestion(
                "A friend invites you to dinner, but you already have plans. What is the best reply?",
                listOf(
                    "Thanks for asking, but I'm busy tonight. Maybe next week?",
                    "No.",
                    "I don't want to come.",
                    "Why did you invite me?",
                ),
                0,
                TestSkill.PRAGMATICS,
            ),
        ),
        CefrLevel.B1 to listOf(
            LevelQuestion("I ____ my keys. I can't find them anywhere.", listOf("lost", "have lost", "lose", "had lost"), 1),
            LevelQuestion("She suggested ____ a taxi instead of walking.", listOf("take", "taking", "to take", "took"), 1),
            LevelQuestion("This is the café ____ we first met.", listOf("which", "where", "who", "when"), 1),
            LevelQuestion("By the time we arrived, the concert ____.", listOf("started", "had started", "has started", "was starting"), 1),
            LevelQuestion("He's looking forward to ____ from you.", listOf("hear", "hearing", "heard", "be heard"), 1),
            LevelQuestion(
                "The team ____ a solution that pleased everyone.",
                listOf("came up with", "came across", "came down with", "came over"),
                0,
                TestSkill.VOCABULARY,
            ),
            LevelQuestion(
                "He was ____ to admit that he was wrong.",
                listOf("reluctant", "reliable", "curious", "polite"),
                0,
                TestSkill.VOCABULARY,
            ),
            LevelQuestion(
                "The film was so boring that I ____ asleep.",
                listOf("fell", "felt", "fell down", "fall"),
                0,
                TestSkill.VOCABULARY,
            ),
            LevelQuestion(
                "Read: “Thanks for the draft. The structure works, but the opening is too long. Could you cut it to two sentences and send it back by noon?” What does the writer want?",
                listOf("A shorter opening", "A new structure", "A later deadline", "A longer draft"),
                0,
                TestSkill.READING,
            ),
            LevelQuestion(
                "Read: “The museum's new wing opens next month. Entry is free on Sundays, but timed tickets are required.” What must visitors do on Sundays?",
                listOf("Book a timed ticket", "Pay an entry fee", "Come after noon", "Join a tour"),
                0,
                TestSkill.READING,
            ),
            LevelQuestion(
                "You need to add something in a meeting. What is the most polite phrase?",
                listOf(
                    "Sorry to interrupt, but could I add something?",
                    "Stop talking.",
                    "Listen!",
                    "I interrupt now.",
                ),
                0,
                TestSkill.PRAGMATICS,
            ),
            LevelQuestion(
                "A friend asks: “Can you help me move on Saturday?” You can't. What is the best reply?",
                listOf(
                    "I'd love to help, but I'm away on Saturday. Could we do Sunday?",
                    "No.",
                    "I can't.",
                    "Moving is boring.",
                ),
                0,
                TestSkill.PRAGMATICS,
            ),
            LevelQuestion(
                "Read: “The course is for people who can already hold a short conversation but want to sound more natural in meetings. Classes run in the evening.” Who is the course for?",
                listOf(
                    "Learners who want more natural meeting English",
                    "Complete beginners",
                    "People who only write English",
                    "People who are free during the day",
                ),
                0,
                TestSkill.READING,
            ),
            LevelQuestion(
                "In a meeting, you do not understand a technical term. What is the most professional response?",
                listOf(
                    "Could you explain what you mean by that term?",
                    "I don't know this word.",
                    "Speak more simply.",
                    "Everyone knows that.",
                ),
                0,
                TestSkill.PRAGMATICS,
            ),
        ),
        CefrLevel.B2 to listOf(
            LevelQuestion("Had I known about the delay, I ____ earlier.", listOf("would leave", "would have left", "had left", "will leave"), 1),
            LevelQuestion("Despite ____ hard, he didn't pass the exam.", listOf("study", "studying", "studied", "to study"), 1),
            LevelQuestion("She's not used to ____ up so early.", listOf("get", "getting", "got", "be getting"), 1),
            LevelQuestion("No sooner ____ the door than the phone rang.", listOf("I had closed", "had I closed", "I closed", "did I close"), 1),
            LevelQuestion("The proposal is worth ____ more carefully.", listOf("consider", "considering", "to consider", "considered"), 1),
            LevelQuestion(
                "The report was ____; it gave no clear answer.",
                listOf("inconclusive", "incisive", "decisive", "transparent"),
                0,
                TestSkill.VOCABULARY,
            ),
            LevelQuestion(
                "She has a ____ understanding of the market.",
                listOf("nuanced", "neutral", "notable", "numerous"),
                0,
                TestSkill.VOCABULARY,
            ),
            LevelQuestion(
                "The evidence was ____; it supported the theory from every angle.",
                listOf("compelling", "compulsory", "competent", "complacent"),
                0,
                TestSkill.VOCABULARY,
            ),
            LevelQuestion(
                "Read: “The film is gorgeous to look at, but its characters never quite earn our sympathy. Still, the final scene is unforgettable.” What is the reviewer's overall view?",
                listOf(
                    "Visually strong but emotionally uneven",
                    "A complete failure",
                    "A perfect film",
                    "Too short to judge",
                ),
                0,
                TestSkill.READING,
            ),
            LevelQuestion(
                "Read: “The council approved the plan, provided that construction begins before June and traffic studies are completed. If either condition is missed, the approval lapses.” What happens if a condition is missed?",
                listOf("The approval ends", "The plan starts anyway", "The council pays a fine", "The deadline moves"),
                0,
                TestSkill.READING,
            ),
            LevelQuestion(
                "A colleague presents an idea you disagree with. What is the most constructive response?",
                listOf(
                    "I see your point. What evidence would change your mind?",
                    "That's wrong.",
                    "I don't agree.",
                    "Everyone knows that won't work.",
                ),
                0,
                TestSkill.PRAGMATICS,
            ),
            LevelQuestion(
                "You need a busy manager to approve something today. What is the best message?",
                listOf(
                    "I know today is busy. Could you approve this by 4 p.m. so we can meet the deadline? I'm happy to summarise it in two lines.",
                    "Approve this now.",
                    "Please approve.",
                    "Why haven't you approved this?",
                ),
                0,
                TestSkill.PRAGMATICS,
            ),
            LevelQuestion(
                "Read: “The pilot scheme will run in three schools before any national rollout. Participating schools will receive training but no extra funding.” What limitation does the scheme have?",
                listOf(
                    "Schools receive training but no additional money",
                    "It is already national",
                    "Only three teachers are involved",
                    "It has already ended",
                ),
                0,
                TestSkill.READING,
            ),
            LevelQuestion(
                "A client wants a report tomorrow, but you need three days. What is the best response?",
                listOf(
                    "I can't deliver tomorrow, but I can send a full draft on Thursday and the final version on Friday.",
                    "No.",
                    "That's impossible.",
                    "You should have asked earlier.",
                ),
                0,
                TestSkill.PRAGMATICS,
            ),
        ),
        CefrLevel.C1 to listOf(
            LevelQuestion("Rarely ____ such a compelling argument.", listOf("I have heard", "have I heard", "I heard", "did I heard"), 1),
            LevelQuestion("I'd rather you ____ the news to anyone yet.", listOf("didn't mention", "don't mention", "hadn't mentioned", "wouldn't mention"), 0),
            LevelQuestion("____ it not been for her support, I would have given up.", listOf("If", "Had", "Should", "Were"), 1),
            LevelQuestion(
                "The committee, ____ members disagreed, postponed the decision.",
                listOf("whose", "which", "who", "what"),
                0,
            ),
            LevelQuestion(
                "Little ____ that the decision would change everything.",
                listOf("did she know", "she knew", "she did know", "she had known"),
                0,
            ),
            LevelQuestion(
                "The findings ____ doubt on the earlier research.",
                listOf("cast", "make", "put", "give"),
                0,
                TestSkill.VOCABULARY,
            ),
            LevelQuestion(
                "The two theories are not mutually exclusive; they can ____.",
                listOf("coexist", "coincide", "collaborate", "compound"),
                0,
                TestSkill.VOCABULARY,
            ),
            LevelQuestion(
                "The argument is ____; it has no logical gaps.",
                listOf("watertight", "waterlogged", "waterproof", "watered"),
                0,
                TestSkill.VOCABULARY,
            ),
            LevelQuestion(
                "Read: “The author praises the proposal's ambition, yet questions whether its costs have been fully counted.” The tone is best described as…",
                listOf("Supportive but cautious", "Openly hostile", "Entirely neutral", "Enthusiastically uncritical"),
                0,
                TestSkill.READING,
            ),
            LevelQuestion(
                "Read: “The study's sample was small, and its findings have not been replicated. The authors themselves describe the results as preliminary.” What is the authors' attitude?",
                listOf("Cautious", "Confident", "Dismissive", "Amused"),
                0,
                TestSkill.READING,
            ),
            LevelQuestion(
                "You must criticise a colleague's proposal in a meeting. Which is most diplomatic?",
                listOf(
                    "I think the direction is strong. Could we stress-test the cost assumptions before we commit?",
                    "Your proposal is unrealistic.",
                    "This will never work.",
                    "Who approved this?",
                ),
                0,
                TestSkill.PRAGMATICS,
            ),
            LevelQuestion(
                "A client asks for a discount you cannot give. What is the best reply?",
                listOf(
                    "I understand the budget pressure. I can't reduce the price, but I can adjust the scope to fit. Shall we look at that?",
                    "No.",
                    "That's impossible.",
                    "Why do you need a discount?",
                ),
                0,
                TestSkill.PRAGMATICS,
            ),
            LevelQuestion(
                "Read: “The report stops short of recommending a ban, arguing that the evidence is insufficient. It does, however, call for tighter monitoring.” What does the report recommend?",
                listOf(
                    "Tighter monitoring rather than a ban",
                    "An immediate ban",
                    "No action at all",
                    "Less monitoring",
                ),
                0,
                TestSkill.READING,
            ),
            LevelQuestion(
                "A senior colleague dismisses your proposal in front of the team. What is the most effective response?",
                listOf(
                    "I'd like to understand which part concerns you most. Could we review the evidence after the meeting?",
                    "You're wrong.",
                    "You always do this.",
                    "Fine, forget it.",
                ),
                0,
                TestSkill.PRAGMATICS,
            ),
        ),
        CefrLevel.C2 to listOf(
            LevelQuestion(
                "Were the results ____, the theory would gain considerable support.",
                listOf("to be replicated", "replicated", "replicating", "be replicated"),
                0,
            ),
            LevelQuestion(
                "____ had the announcement been made than the market reacted.",
                listOf("No sooner", "Scarcely", "Hardly", "Only"),
                0,
            ),
            LevelQuestion(
                "The professor, along with her students, ____ attending the conference.",
                listOf("is", "are", "were", "be"),
                0,
            ),
            LevelQuestion(
                "The minister's statement was so ____ that it could be read as either an apology or a denial.",
                listOf("equivocal", "unequivocal", "vehement", "transparent"),
                0,
                TestSkill.VOCABULARY,
            ),
            LevelQuestion(
                "The novel's ending is deliberately ____, leaving readers to draw their own conclusions.",
                listOf("explicit", "ambiguous", "predictable", "didactic"),
                1,
                TestSkill.VOCABULARY,
            ),
            LevelQuestion(
                "His argument ____ a number of questionable assumptions.",
                listOf("rests on", "rests in", "rests for", "rests with"),
                0,
                TestSkill.VOCABULARY,
            ),
            LevelQuestion(
                "The policy was ____ by critics as a short-sighted compromise.",
                listOf("excoriated", "extolled", "exonerated", "exaggerated"),
                0,
                TestSkill.VOCABULARY,
            ),
            LevelQuestion(
                "Read: “The reform was sold as a simplification, yet it added three new forms and two new exemptions. Its defenders insist the complexity is temporary.” What does the writer imply?",
                listOf(
                    "The reform may have made things more complex",
                    "The reform was simple",
                    "Critics were ignored",
                    "The exemptions are temporary",
                ),
                0,
                TestSkill.READING,
            ),
            LevelQuestion(
                "Read: “The data, although incomplete, still point to a clear trend. The authors are careful not to overstate their case.” What is the function of the second sentence?",
                listOf("To qualify the first", "To contradict it", "To repeat it", "To give an example"),
                0,
                TestSkill.READING,
            ),
            LevelQuestion(
                "Read: “She writes with the confidence of someone who has never been contradicted — and the reader is left to decide whether that is strength or blindness.” The tone is…",
                listOf("Wryly critical", "Openly admiring", "Entirely neutral", "Confused"),
                0,
                TestSkill.READING,
            ),
            LevelQuestion(
                "At a conference, someone challenges your data in front of the room. What is the best response?",
                listOf(
                    "That's a fair challenge. The sample is small, which is why I'd treat the result as indicative rather than conclusive.",
                    "You're wrong.",
                    "I don't have time for this.",
                    "Everyone here agrees with me.",
                ),
                0,
                TestSkill.PRAGMATICS,
            ),
            LevelQuestion(
                "You must reject a vendor's proposal while preserving the relationship. Which is best?",
                listOf(
                    "We're grateful for the work you put in. The proposal doesn't fit our current priorities, but I'd like to stay in touch for the next round.",
                    "We reject your proposal.",
                    "Your proposal is too expensive.",
                    "Don't contact us again.",
                ),
                0,
                TestSkill.PRAGMATICS,
            ),
            LevelQuestion(
                "Read: “The proposal is attractive in theory; the difficulty lies in the transition, when existing systems must run alongside the new one. It is here that most reforms fail.” What does the writer identify as the main risk?",
                listOf(
                    "The transition period",
                    "The underlying theory",
                    "The cost of new systems",
                    "Public opposition",
                ),
                0,
                TestSkill.READING,
            ),
            LevelQuestion(
                "At a public panel, a questioner misrepresents your position. What is the most effective response?",
                listOf(
                    "I think that's a different claim from the one I made. To be precise, I argued that the policy is premature, not that it is unnecessary.",
                    "You weren't listening.",
                    "That is a stupid question.",
                    "I have already answered that.",
                ),
                0,
                TestSkill.PRAGMATICS,
            ),
        ),
        CefrLevel.TOP to listOf(
            LevelQuestion(
                "The committee's findings are ____ with the original hypothesis.",
                listOf("consistent", "consist", "consisting", "consistency"),
                0,
                TestSkill.VOCABULARY,
            ),
            LevelQuestion(
                "No amount of rhetoric can ____ the fact that the policy failed.",
                listOf("obscure", "obstruct", "oblige", "observe"),
                0,
                TestSkill.VOCABULARY,
            ),
            LevelQuestion(
                "The professor's critique was ____, sparing none of the paper's flawed methodology.",
                listOf("incisive", "indecisive", "insipid", "inept"),
                0,
                TestSkill.VOCABULARY,
            ),
            LevelQuestion(
                "The reforms were ____ rather than radical.",
                listOf("incremental", "incipient", "incidental", "incessant"),
                0,
                TestSkill.VOCABULARY,
            ),
            LevelQuestion(
                "The transition was so abrupt that the argument felt ____.",
                listOf("disjointed", "meticulous", "coherent", "seamless"),
                0,
                TestSkill.VOCABULARY,
            ),
            LevelQuestion(
                "Choose the sentence with the most natural academic register.",
                listOf(
                    "The results were super good.",
                    "The results were highly significant.",
                    "The results were really great.",
                    "The results were kind of significant.",
                ),
                1,
                TestSkill.PRAGMATICS,
            ),
            LevelQuestion(
                "You chair a panel and a speaker overruns. What is the most diplomatic intervention?",
                listOf(
                    "That's a fascinating point. I'm conscious of time, so could you bring it to a close with one takeaway?",
                    "Stop now.",
                    "You're over time.",
                    "Next speaker.",
                ),
                0,
                TestSkill.PRAGMATICS,
            ),
            LevelQuestion(
                "So ____ was the evidence that the committee reversed its decision.",
                listOf("compelling", "compelled", "compellingly", "compel"),
                0,
            ),
            LevelQuestion(
                "Not until the final chapter ____ the author reveal the truth.",
                listOf("does", "did", "is", "has"),
                0,
            ),
            LevelQuestion(
                "Read: “The policy's defenders call it pragmatic; its critics call it unprincipled. Both descriptions assume the same thing: that the choice was deliberate.” What does the writer suggest?",
                listOf(
                    "The debate misses a shared assumption",
                    "The policy was accidental",
                    "One side is clearly right",
                    "The critics are unprincipled",
                ),
                0,
                TestSkill.READING,
            ),
            LevelQuestion(
                "Read: “The biography is exhaustively researched but curiously lifeless; every fact is present, yet the subject never comes into focus.” What is the reviewer's main criticism?",
                listOf(
                    "The book lacks a vivid central portrait",
                    "The book is too short",
                    "The research is thin",
                    "The subject is uninteresting",
                ),
                0,
                TestSkill.READING,
            ),
            LevelQuestion(
                "Read: “To call the novel experimental is not to say it is difficult, but to say that it treats structure as a question rather than a given.” What does “experimental” mean here?",
                listOf(
                    "It questions conventional structure",
                    "It is hard to read",
                    "It has no plot",
                    "It is unfinished",
                ),
                0,
                TestSkill.READING,
            ),
            LevelQuestion(
                "It is not so much the cost ____ the principle that concerns them.",
                listOf("as", "like", "than", "that"),
                0,
            ),
            LevelQuestion(
                "A journalist asks a leading question that invites you to criticise a colleague. What is the most professional response?",
                listOf(
                    "I won't speak for my colleague, but I can tell you what our review found and what we're changing.",
                    "That's a ridiculous question.",
                    "Off the record, yes.",
                    "No comment.",
                ),
                0,
                TestSkill.PRAGMATICS,
            ),
        ),
    )

    fun examFor(target: CefrLevel): List<LevelQuestion> =
        balancedSelection(bankFor(target), LEVEL_QUESTION_COUNT)

    fun weeklyQuestions(level: CefrLevel): List<LevelQuestion> {
        val currentBank = bankFor(level)
        val previousBank = when (level) {
            CefrLevel.A1 -> emptyList()
            CefrLevel.A2 -> a1Warmup
            else -> exams[level.previous()].orEmpty()
        }

        // Take six at the current level first. Then build the retention half
        // from everything not already chosen, so the same prompt can never
        // appear twice in one weekly test.
        val currentPick = balancedSelection(currentBank, 6)
        val usedPrompts = currentPick.map { it.prompt }.toSet()
        val retentionPool = (previousBank + currentBank)
            .filter { it.prompt !in usedPrompts }
            .distinctBy { it.prompt }
        val retentionPick = balancedSelection(
            retentionPool,
            WEEKLY_QUESTION_COUNT - currentPick.size,
        )

        return (currentPick + retentionPick)
            .distinctBy { it.prompt }
            .shuffled()
            .take(WEEKLY_QUESTION_COUNT)
    }

    fun prepare(questions: List<LevelQuestion>): List<PreparedQuestion> =
        questions.shuffled().map { question ->
            val options = question.options.shuffled()
            PreparedQuestion(
                prompt = question.prompt,
                options = options,
                correctIndex = options.indexOf(question.options[question.correctIndex]),
                skill = question.skill,
                explanation = question.explanation,
            )
        }

    fun currentWeekId(date: LocalDate = LocalDate.now()): String {
        val fields = WeekFields.ISO
        val year = date.get(fields.weekBasedYear())
        val week = date.get(fields.weekOfWeekBasedYear())
        return "$year-W%02d".format(week)
    }

    fun retryDate(epochDay: Int): LocalDate? =
        if (epochDay <= 0) null else LocalDate.ofEpochDay(epochDay.toLong())

    private fun bankFor(level: CefrLevel): List<LevelQuestion> =
        if (level == CefrLevel.A1) a1Warmup else exams[level].orEmpty()

    /**
     * Sample fairly across skills before filling any remaining slots. This
     * prevents a bank with many grammar items from turning every weekly test
     * into a grammar test by accident.
     */
    private fun balancedSelection(pool: List<LevelQuestion>, count: Int): List<LevelQuestion> {
        val unique = pool.distinctBy { it.prompt }
        if (count <= 0) return emptyList()
        if (unique.size <= count) return unique.shuffled()

        val groups = TestSkill.entries.map { skill ->
            unique.filter { it.skill == skill }.shuffled()
        }
        val chosen = mutableListOf<LevelQuestion>()
        var round = 0
        while (chosen.size < count) {
            var added = false
            for (group in groups) {
                val next = group.getOrNull(round) ?: continue
                if (chosen.size >= count) break
                chosen += next
                added = true
            }
            if (!added) break
            round += 1
        }
        if (chosen.size < count) {
            chosen += unique
                .filterNot { candidate -> chosen.any { it.prompt == candidate.prompt } }
                .shuffled()
                .take(count - chosen.size)
        }
        return chosen.shuffled()
    }
}

private fun CefrLevel.previous(): CefrLevel = CefrLevel.entries[(ordinal - 1).coerceAtLeast(0)]
