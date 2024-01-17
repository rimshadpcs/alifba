package com.alifba.alifba.presentation.home

import androidx.lifecycle.ViewModel
import com.alifba.alifba.R
import com.alifba.alifba.presentation.home.model.LessonMenuItem

class HomeViewModel : ViewModel() {
    val lessonMenuItemList = listOf(
        LessonMenuItem("Level 1", R.drawable.levelone, 0),
        LessonMenuItem("Level 2",R.drawable.leveltwo,1),
        LessonMenuItem("Level 3", R.drawable.levelthree, 2),
        LessonMenuItem("Stories",R.drawable.stories,3)

    )
}