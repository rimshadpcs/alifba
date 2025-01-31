package com.alifba.alifba.presenation.home

import androidx.lifecycle.ViewModel
import com.alifba.alifba.R
import com.alifba.alifba.presenation.home.model.LevelItem

open class HomeViewModel : ViewModel() {
    val levelItemList = listOf(
        LevelItem("Level 1", R.drawable.levelone, "level1"),
        LevelItem("Level 2",R.drawable.leveltwo,"level2"),
        LevelItem("Level 3", R.drawable.levelthree, "level3"),
        LevelItem("Level 4",R.drawable.levelfour,"level4"),
        LevelItem("Level 5",R.drawable.levelfive,"level5"),
        LevelItem("Level 6", R.drawable.levelseven, "level6"),
        LevelItem("Level 7",R.drawable.levelsix,"level7"),
        LevelItem("Level 8",R.drawable.leveleight,"level8"),
        LevelItem("Level 9", R.drawable.levelnine, "level9"),
        LevelItem("Level 10",R.drawable.levelten,"level10"),
        )
}