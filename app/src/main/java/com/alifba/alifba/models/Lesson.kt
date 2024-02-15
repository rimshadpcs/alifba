package com.alifba.alifba.models

import androidx.annotation.DrawableRes
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

    data class DragAndDropExperiment(
        val question:String,
        val dragItemList: List<DragItem>,
        val dropItemList: List<DropItem>):LessonSegment()

}



data class DragItem(val id: Int, val answer: String, @DrawableRes val image: Int)

data class DropItem(val id: Int, val name: String)

val dragItems = listOf(
    DragItem(1, "Allah",  R.drawable.moon),
    DragItem(2, "Allah",  R.drawable.sun),
    DragItem(3, "Allah",  R.drawable.animals),
    DragItem(4, "Allah",  R.drawable.flower),
    DragItem(5, "Allah",  R.drawable.rivers),
)

val dropItems = listOf(
    DropItem(1, "Allah"),
)
data class McqItem(
    val choice: String,
    val answer: Boolean
)

val sampleLessons = listOf(
    Lesson(
        id = 101,
        title = "Allah's Creation and Love",
        segments =

        listOf(


//            LessonSegment.CommonLesson(
//                image = R.drawable.river,
//                description = "Hello, little friends! Today, we're going on a fun adventure to see the beautiful world Allah has made. Let's find out how He created everything and how much He loves us and everything He made!",
//                speech = R.raw.intro
//            ),
//
//            LessonSegment.CommonLesson(
//                image = R.drawable.laila_butterfly,
//                description = "Nana, look at these tiny wonders!, the butterflies are so beautiful, Who made them so beautiful?",
//                speech = R.raw.nana_look_at_these
//            ),
//            LessonSegment.CommonLesson(
//                image = R.drawable.every_creature,
//                description = "Laila, every creature, from the ladybug to the trees, is made by Allah, our Creator.",
//                speech = R.raw.every_creature
//            ),
//            LessonSegment.CommonLesson(
//                image = R.drawable.every_creature,
//                description = "Laila, every creature, from the ladybug to the trees, is made by Allah, our Creator.",
//                speech = R.raw.every_creature
//            ),
//            LessonSegment.CommonLesson(
//                image = R.drawable.laila_butterfly,
//                description = "Allah made all this?",
//                speech = R.raw.allah_made_all_this
//            ),
//            LessonSegment.CommonLesson(
//                image = R.drawable.every_creature,
//                description = "Yes, dear. Imagine a being whose kindness is deeper than the oceans and whose power is higher than the mountains. He created everything you see, the sky above us, and the warmth of the sun on our skin.",
//                speech = R.raw.yes_dear
//            ),
//            LessonSegment.CommonLesson(
//                image = R.drawable.laila_nana_juice,
//                description = "So, everything around us, the flowers, clouds, sunsets, and even the fizzy apple juice we love, are gifts from Allah?",
//                speech = R.raw.gifts_from_allah
//            ),
//            LessonSegment.CommonLesson(
//                image = R.drawable.laila_final,
//                description = "Laila now looks at the world with a new sense of wonder, recognizing Allah's presence in all aspects of nature, including the simple joys of life like fizzy apple juice.",
//                speech = R.raw.laila_final
//            ),
//            LessonSegment.MCQLessonItem(
//                question = "Who created this amazing world, the beautiful sky, the flowing river and the shining stars?",
//                choices = listOf(
//                    McqItem("Humans", false),
//                    McqItem("Allah", true),
//                    McqItem("Someone else", false)
//                ),
//                speech = R.raw.mcq_sample
//            ),
            LessonSegment.DragAndDropExperiment(
                question = "Who created al these amazing things in this world drag and drop them to the right box below",
                dragItems,
                dropItemList = dropItems
            ),
            LessonSegment.CommonLesson(
                image = R.drawable.jaroperningkitchen,
                description = "Nana, I can't open this!",
                speech = R.raw.nana_i_cant_open
            ),
            LessonSegment.CommonLesson(
                image = R.drawable.jaroperningkitchen,
                description = "Let's say 'Bismillah' first, Laila. It means 'In the name of Allah' It helps us remember to start everything with Allah’s name",
                speech = R.raw.lets_say_bismillah
            ),
            LessonSegment.CommonLesson(
                image = R.drawable.jaropenedkitchen,
                description = "Bismillah,  It worked, Nana!",
                speech = R.raw.bismillah_it_worked
            ),
            LessonSegment.CommonLesson(
                image = R.drawable.jaropenedkitchen,
                description = "Yes, dear. Saying \"Bismillah\" is like asking for Allah’s help and blessing.\n",
                speech = R.raw.yes_dear_saying
            ),
            LessonSegment.CommonLesson(
                image = R.drawable.lailatalkingkitchen,
                description = "Is that why you say \"Bismillah\" when cooking?",
                speech = R.raw.is_that_why_you_say
            ),
            LessonSegment.CommonLesson(
                image = R.drawable.nanatalkingkitchen,
                description = "Exactly! And when we finish something or enjoy something good, we say \"Alhamdulillah,\" which means \"All praise is for Allah.\"",
                speech = R.raw.exactly_and_when_we
            ),
            LessonSegment.CommonLesson(
                image = R.drawable.lailatalkingkitchen,
                description = "So, when I eat yummy food, I should say \"Alhamdulillah\"?",
                speech = R.raw.so_when_i_eat_yummy
            ),
            LessonSegment.CommonLesson(
                image = R.drawable.nanatalkingkitchen,
                description = "That's right! It shows we’re thankful for Allah's blessings.",
                speech = R.raw.thats_right_shows_thankful
            ),
            LessonSegment.CommonLesson(
                image = R.drawable.lailatalkingkitchen,
                description = " Like, Alhamdulillah for this delicious breakfast!",
                speech = R.raw.like_alham_dhulillah
            ),
            LessonSegment.CommonLesson(
                image = R.drawable.lailananakitchenfinal,
                description = " Very good! Remember, Laila, \"Bismillah\" before we start, and \"Alhamdulillah\" when we're thankful.",
                speech = R.raw.very_good_remember
            ),
            LessonSegment.CommonLesson(
                image = R.drawable.lailananakitchenfinal,
                description = "  I will, Nana! Bismillah when I start my drawings and Alhamdulillah when I play with my friends!",
                speech = R.raw.i_will_nana_bismi
            ),
            LessonSegment.MCQLessonItem(
                question = "What should you say before starting to eat your food?",
                choices = listOf(
                    McqItem("Alhamdulillah", false),
                    McqItem("Bismillah", true),
                    McqItem("Nothing", false)
                ),
                speech = R.raw.what_should_you_say
            ),
            LessonSegment.MCQLessonItem(
                question = "Laila could not open a jar. What did she say to get help?",
                choices = listOf(
                    McqItem("Bismillah", true),
                    McqItem("Alhamdulillah", false),
                    McqItem("Nothing", false)
                ),
                speech = R.raw.laila_couldnot_open
            ),

            LessonSegment.MCQLessonItem(
                question = "What should you say after eating your food?",
                choices = listOf(
                    McqItem("Bismillah", false),
                    McqItem("Alhamdulillah", true),
                    McqItem("Nothing", false)
                ),
                speech = R.raw.what_should_you_say_after
            ),
            LessonSegment.MCQLessonItem(
                question = "What phrase should you say to thank Allah for a beautiful day?",
                choices = listOf(
                    McqItem("Alhamdulillah", true),
                    McqItem("Bismillah", false),
                    McqItem("Nothing", false)
                ),
                speech = R.raw.what_phrase_to_say
            ),
            )
    ),
    Lesson(
        id = 102,
        title = "Allah's Creation and Love",
        segments = listOf(
            LessonSegment.CommonLesson(
                image = R.drawable.rivers,
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
