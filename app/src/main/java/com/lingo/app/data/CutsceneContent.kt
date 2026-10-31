package com.lingo.app.data

data class SparkChip(
    val label: String,
    val example: String,
)

data class LessonSpark(
    val prompt: String,
    val chips: List<SparkChip>,
)

/**
 * One open creative prompt per lesson. The chips lower the blank-page cost
 * without turning the warm-up into another exercise or another saved field.
 */
object CutsceneContent {

    internal val sparks: Map<String, LessonSpark> = mapOf(
        "u1-l1" to LessonSpark(
            prompt = "What is one small detail that makes a stranger feel welcome?",
            chips = listOf(
                SparkChip("A tiny fact", "I just moved here, so I'm still finding my favourite café."),
                SparkChip("A shared place", "I live near the park — do you know the little bakery there?"),
                SparkChip("A curious question", "What's the best thing about this neighbourhood?"),
            ),
        ),
        "u1-l2" to LessonSpark(
            prompt = "Invent a café order with one small surprise.",
            chips = listOf(
                SparkChip("A comfort order", "Could I have a flat white and a warm croissant, please?"),
                SparkChip("A menu question", "Is the soup vegetarian? My friend can't eat meat."),
                SparkChip("A tiny twist", "Could I have the usual, but with oat milk today?"),
            ),
        ),
        "u1-l3" to LessonSpark(
            prompt = "What landmark would you invent to help someone find your street?",
            chips = listOf(
                SparkChip("A colour", "Turn left at the blue door just past the bakery."),
                SparkChip("A sound", "Follow the music until you reach the small square."),
                SparkChip("A story", "You'll see a wall of old clocks — my street is right behind it."),
            ),
        ),
        "u2-l1" to LessonSpark(
            prompt = "What is one friendly question that would calm a nervous traveller?",
            chips = listOf(
                SparkChip("About the trip", "Where are you flying today, and is it a special trip?"),
                SparkChip("About the bags", "How many bags will you check in today?"),
                SparkChip("About the next step", "Would you like a window seat while we sort this out?"),
            ),
        ),
        "u2-l2" to LessonSpark(
            prompt = "What is one small detail that makes a hotel feel welcoming?",
            chips = listOf(
                SparkChip("A useful fact", "Breakfast is on the second floor until ten."),
                SparkChip("A local tip", "The night market is two streets over — it's worth a wander."),
                SparkChip("A kind question", "Is there anything you need after your journey?"),
            ),
        ),
        "u2-l3" to LessonSpark(
            prompt = "What would you ask a local to turn a quick chat into a real conversation?",
            chips = listOf(
                SparkChip("The moment", "The light is beautiful here — is this your favourite time of day?"),
                SparkChip("The place", "What's one place nearby that visitors always miss?"),
                SparkChip("The story", "How long have you lived here? What's changed most?"),
            ),
        ),
        "u3-l1" to LessonSpark(
            prompt = "What is one sentence that makes a work email feel human?",
            chips = listOf(
                SparkChip("Warm opener", "Hope your week is going well — no rush on this."),
                SparkChip("Clear ask", "Could you send the final figures by Thursday?"),
                SparkChip("Kind close", "Thanks for keeping this moving; I owe you a coffee."),
            ),
        ),
        "u3-l2" to LessonSpark(
            prompt = "What is one phrase that lets you enter a meeting without interrupting?",
            chips = listOf(
                SparkChip("Hold the floor", "Could I add one thing before we move on?"),
                SparkChip("Build on it", "I'd like to build on what Priya just said."),
                SparkChip("Ask for space", "May I take thirty seconds to lay out an alternative?"),
            ),
        ),
        "u3-l3" to LessonSpark(
            prompt = "What is one trade-off you could offer instead of just saying no?",
            chips = listOf(
                SparkChip("A smaller piece", "I can send the summary by Tuesday, but the full report by Thursday."),
                SparkChip("A condition", "I can hit Friday if I get the figures by Wednesday noon."),
                SparkChip("A question", "Which matters more this week — speed or the full analysis?"),
            ),
        ),
        "u4-l1" to LessonSpark(
            prompt = "What everyday object could become a literal idiom?",
            chips = listOf(
                SparkChip("Break the ice", "The first person to crack the frozen bowl gets the last slice."),
                SparkChip("Spill the beans", "The bag split on the stairs, and beans rolled into the meeting."),
                SparkChip("On the same page", "We all opened the same book and read the same line."),
            ),
        ),
        "u4-l2" to LessonSpark(
            prompt = "What is the strongest fair reason someone might disagree with you?",
            chips = listOf(
                SparkChip("Their concern", "Some people worry this change will make the room feel less spontaneous."),
                SparkChip("A shared value", "We both want people to feel heard, even if we disagree on how."),
                SparkChip("A small yes", "I still think we should try it, but let's keep the old format for big decisions."),
            ),
        ),
        "u4-l3" to LessonSpark(
            prompt = "What is one small detail that could turn a story in a new direction?",
            chips = listOf(
                SparkChip("A sound", "Then the phone rang, and nobody recognised the number."),
                SparkChip("An object", "A blue glove was sitting on the seat where she had been."),
                SparkChip("A question", "Why was the light still on in the empty office?"),
            ),
        ),
        "u5-l1" to LessonSpark(
            prompt = "How would you describe a symptom so a pharmacist can help quickly?",
            chips = listOf(
                SparkChip("Since when", "I've had a sore throat since Monday and it's worse at night."),
                SparkChip("What you tried", "I've tried honey and paracetamol, but the fever is still there."),
                SparkChip("A safety question", "Should I see a doctor if this lasts more than a week?"),
            ),
        ),
        "u5-l2" to LessonSpark(
            prompt = "What is one question you would ask before joining a new class?",
            chips = listOf(
                SparkChip("Level", "Is this class suitable if I've only just started?"),
                SparkChip("Practical", "What should I bring, and how long does each session run?"),
                SparkChip("People", "What do people usually do after the class?"),
            ),
        ),
        "u5-l3" to LessonSpark(
            prompt = "What is one way to invite a friend without pressure?",
            chips = listOf(
                SparkChip("Specific", "There's a market on Sunday morning — want to wander round?"),
                SparkChip("Flexible", "I'm free Saturday or Tuesday, whichever is easier."),
                SparkChip("Easy out", "No worries at all if you're already booked up."),
            ),
        ),
        "u6-l1" to LessonSpark(
            prompt = "What is one open question that could unlock a better conversation?",
            chips = listOf(
                SparkChip("What", "What was the most surprising part of your week?"),
                SparkChip("How", "How did you end up doing that?"),
                SparkChip("Why", "Why does that matter to you?"),
            ),
        ),
        "u6-l2" to LessonSpark(
            prompt = "How would you explain one idea to someone who is tired and busy?",
            chips = listOf(
                SparkChip("One sentence", "The app saves as you type, so you can close it safely."),
                SparkChip("A because", "It feels slow because too many ideas are arriving at once."),
                SparkChip("An example", "Think of it like a receipt: one line per thing you bought."),
            ),
        ),
        "u6-l3" to LessonSpark(
            prompt = "How would you say the same thing in a warmer, simpler way?",
            chips = listOf(
                SparkChip("Warmer", "The meeting moved because our client couldn't make it."),
                SparkChip("Shorter", "We pushed the meeting back; the client was unavailable."),
                SparkChip("Different angle", "The client needed more time, so the meeting is now later."),
            ),
        ),
        "u6-l4" to LessonSpark(
            prompt = "What small problem would you turn into a pitch?",
            chips = listOf(
                SparkChip("A daily friction", "The lift queue at 8 a.m. makes everyone late for the same meeting."),
                SparkChip("A tiny fix", "What if the building opened one extra lift ten minutes earlier?"),
                SparkChip("A clear ask", "Could we trial it for two weeks and measure the queue?"),
            ),
        ),
        "u6-l5" to LessonSpark(
            prompt = "What could you say when you need a second to think?",
            chips = listOf(
                SparkChip("Clarify", "Let me make sure I understand the two parts of that question."),
                SparkChip("Buy time", "That's a good question — may I take a moment to think?"),
                SparkChip("Be honest", "I don't know yet, but here's how I'd find out."),
            ),
        ),
    )

    fun sparkFor(lessonId: String): LessonSpark? = sparks[lessonId]
}
