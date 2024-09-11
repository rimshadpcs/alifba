package com.alifba.alifba.presenation.home

import androidx.lifecycle.ViewModel
import com.alifba.alifba.R
import com.alifba.alifba.presenation.home.model.LessonMenuItem

open class HomeViewModel : ViewModel() {
    val lessonMenuItemList = listOf(
        LessonMenuItem("Level 1", R.drawable.levelone, 1),
        LessonMenuItem("Level 2",R.drawable.leveltwo,2),
        LessonMenuItem("Level 3", R.drawable.levelthree, 3),
        LessonMenuItem("Level 4",R.drawable.levelfour,4),
        LessonMenuItem("Level 5",R.drawable.levelfive,5),
        LessonMenuItem("Level 6", R.drawable.levelseven, 6),
        LessonMenuItem("Level 7",R.drawable.levelsix,7),
        LessonMenuItem("Level 8",R.drawable.leveleight,8),
        LessonMenuItem("Level 9", R.drawable.levelnine, 9),
        LessonMenuItem("Level 10",R.drawable.levelten,10),
        )
}