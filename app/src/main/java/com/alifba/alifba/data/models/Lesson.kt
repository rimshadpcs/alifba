package com.alifba.alifba.data.models

import androidx.annotation.DrawableRes
import com.alifba.alifba.R

data class Lesson(
    val id: Int = 0,
    val title: String = "",
    val segments: List<LessonSegment> = emptyList(),
    val chapterType: String
)


sealed class LessonSegment {

    data class LetterTracing(
        val letterId: String?,
        val speech: String? = null,
        val repeatCount: Int? = null)
        : LessonSegment()

    data class CloudTappingLesson(
        val letterId: String?,
        val speech: String?=null,
        val targetLetter:String?=null,
        val nonTargetLetters:List<String>?=null
    ): LessonSegment()

    data class CommonLesson(
        val image: String = "",
        val description: String ="",
        val speech: String ="",
        val character: String="",

    ) :
        LessonSegment()

    data class TextMcqLesson(val question: String = "", val choices: List<TextMcqItem> = emptyList(), val speech: String = "") :
        LessonSegment()


    data class FlashCardExercise(val title: String = "", val description:String = "", val image: String =""): LessonSegment()
    data class DragAndDropExperiment(
        val question:String = "",
        val dragItemList: List<DragItem> = emptyList(),
        val dropItemList: List<DropItem> = emptyList()
    ): LessonSegment()

    data class FillInTheBlanks(
        val exercise: FillInTheBlanksExercise
    ): LessonSegment()
    data class PictureMcqLesson(val question: String = "", val pictureChoices: List<PictureMcqItem> = emptyList(), val correctAnswer:String="", val speech:String ="") :

        LessonSegment()

}



data class DragItem(val id: Int=0, val answer: String="", @DrawableRes val image: Int=0)

data class DropItem(val id: Int=0, val name: String="")


val dropItems = listOf(
    DropItem(1, "Everyday"),
    DropItem(2,"Occasionally")
)
data class TextMcqItem(
    val choice: String="",
    val answer: Boolean=false
)
data class PictureMcqItem(
    val image:String,
    val choice: String = "",
    val answer: Boolean = false

)

data class FillInTheBlanksExercise(
    val speech: String="",
    val imageResId: String="",
    val sentenceParts: List<String> = emptyList(),
    val options: List<OptionsForFillInTheBlanks> = emptyList(),
    val correctAnswers: List<Int?> = emptyList()
)
data class OptionsForFillInTheBlanks(
    val option: String = "",
    val isSelected: Boolean = false,
    val position: Int = -1
)
