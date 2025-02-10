package com.alifba.alifba.data.db
import android.util.Log
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
@Dao
interface LessonCacheDao {
    @Query("""
        SELECT * FROM lesson_cache 
        WHERE chapterId = :chapterId 
        AND levelId = :levelId
    """)
    suspend fun getLessonCache(
        chapterId: Int,
        levelId: String
    ): LessonCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(lessonCache: LessonCacheEntity) {
        println("Inserting entity: $lessonCache") // Use println as fallback
        try {
            _actualInsert(lessonCache)
            println("Insert successful")
        } catch (e: Exception) {
            System.err.println("Insert failed: ${e.message}")
            e.printStackTrace()
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun _actualInsert(lessonCache: LessonCacheEntity)
}