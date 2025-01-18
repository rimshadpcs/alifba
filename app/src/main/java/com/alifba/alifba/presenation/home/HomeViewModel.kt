package com.alifba.alifba.presenation.home

import androidx.lifecycle.ViewModel
import com.alifba.alifba.R
import com.alifba.alifba.presenation.home.model.LevelItem

open class HomeViewModel : ViewModel() {
    val levelItemList = listOf(
        LevelItem("Level 1", R.drawable.levelone, 1),
        LevelItem("Level 2",R.drawable.leveltwo,2),
        LevelItem("Level 3", R.drawable.levelthree, 3),
        LevelItem("Level 4",R.drawable.levelfour,4),
        LevelItem("Level 5",R.drawable.levelfive,5),
        LevelItem("Level 6", R.drawable.levelseven, 6),
        LevelItem("Level 7",R.drawable.levelsix,7),
        LevelItem("Level 8",R.drawable.leveleight,8),
        LevelItem("Level 9", R.drawable.levelnine, 9),
        LevelItem("Level 10",R.drawable.levelten,10),
        )
}