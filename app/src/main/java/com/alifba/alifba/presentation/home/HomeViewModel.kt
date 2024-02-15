package com.alifba.alifba.presentation.home

import androidx.lifecycle.ViewModel
import com.alifba.alifba.R
import com.alifba.alifba.presentation.home.model.LessonMenuItem

class HomeViewModel : ViewModel() {
    val lessonMenuItemList = listOf(
        LessonMenuItem("Level 1", R.drawable.levelone, 1),
        LessonMenuItem("Level 2",R.drawable.leveltwo,2),
        LessonMenuItem("Level 3", R.drawable.levelthree, 3),
        LessonMenuItem("Level 4",R.drawable.levelfive,4),
        LessonMenuItem("Level 5",R.drawable.levelfour,5),
        LessonMenuItem("Level 6", R.drawable.levelseven, 6),
        LessonMenuItem("Level 7",R.drawable.levelsix,7),
        )
}