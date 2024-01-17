package com.alifba.alifba.models

import com.alifba.alifba.R

data class Lesson(
    val id: Int,
    val title: String,
    val segments: List<LessonSegment>
)

sealed class LessonSegment {
    data class CommonLesson(val image: Int, val description: String, val speech: Int) :
        LessonSegment()

    data class MCQLessonItem(val question: String, val choices: List<McqItem>, val speech: Int) :
        LessonSegment()
}

data class McqItem(
    val choice: String,
    val answer: Boolean
)

val sampleLessons = listOf(
    Lesson(
        id = 101,
        title = "Allah's Creation and Love",
        segments = listOf(
            LessonSegment.CommonLesson(
                image = R.drawable.river,
                description = "Hello, little friends! Today, we're going on a fun adventure to see the beautiful world Allah has made. Let's find out how He created everything and how much He loves us and everything He made!",
                speech = R.raw.intro
            ),

            LessonSegment.CommonLesson(
                image = R.drawable.laila_butterfly,
                description = "Nana, look at these tiny wonders!, the butterflies are so beautiful, Who made them so beautiful?",
                speech = R.raw.nana_look_at_these
            ),
            LessonSegment.CommonLesson(
                image = R.drawable.every_creature,
                description = "Laila, every creature, from the ladybug to the trees, is made by Allah, our Creator.",
                speech = R.raw.every_creature
            ),
            LessonSegment.CommonLesson(
                image = R.drawable.every_creature,
                description = "Laila, every creature, from the ladybug to the trees, is made by Allah, our Creator.",
                speech = R.raw.every_creature
            ),
            LessonSegment.CommonLesson(
                image = R.drawable.laila_butterfly,
                description = "Allah made all this?",
                speech = R.raw.allah_made_all_this
            ),
            LessonSegment.CommonLesson(
                image = R.drawable.every_creature,
                description = "Yes, dear. Imagine a being whose kindness is deeper than the oceans and whose power is higher than the mountains. He created everything you see, the sky above us, and the warmth of the sun on our skin.",
                speech = R.raw.yes_dear
            ),
            LessonSegment.CommonLesson(
                image = R.drawable.laila_nana_juice,
                description = "So, everything around us, the flowers, clouds, sunsets, and even the fizzy apple juice we love, are gifts from Allah?",
                speech = R.raw.gifts_from_allah
            ),
            LessonSegment.CommonLesson(
                image = R.drawable.laila_final,
                description = "Laila now looks at the world with a new sense of wonder, recognizing Allah's presence in all aspects of nature, including the simple joys of life like fizzy apple juice.",
                speech = R.raw.laila_final
            ),
            LessonSegment.MCQLessonItem(
                question = "Who created this amazing world, the beautiful sky, the flowing river and the shining stars?",
                choices = listOf(
                    McqItem("Humans", false),
                    McqItem("Allah", true),
                    McqItem("Someone else", false)
                ),
                speech = R.raw.mcq_sample
            )
        )
    ),
    Lesson(
        id = 102,
        title = "Allah's Creation and Love",
        segments = listOf(
            LessonSegment.CommonLesson(
                image = R.drawable.river,
                description = "Hello, little friends! Today, we're going on a fun adventure to see the beautiful world Allah has made. Let's find out how He created everything and how much He loves us and everything He made!",
                speech = R.raw.intro
            ),
            LessonSegment.MCQLessonItem(
                question = "Who created this amazing world, the beautiful sky, the flowing river and the shining stars?",
                choices = listOf(
                    McqItem("Humans", false),
                    McqItem("Allah", true),
                    McqItem("Someone else", false)
                ),
                speech = R.raw.mcq_sample
            )
        )
    )
)
