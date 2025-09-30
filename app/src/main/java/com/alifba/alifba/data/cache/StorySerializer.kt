package com.alifba.alifba.data.cache

import com.alifba.alifba.data.models.Story
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

@Serializable
data class SerializableStory(
    val id: Int = 0,
    val documentId: String = "",
    val name: String = "",
    val background: String = "",
    val thumbnail: String = "",
    val audio: String = "",
    val backgroundImage: String = "",
    val isLocked: Boolean = false,
    val isBedtime: Boolean = false,
    val duration: Long = 0L,
    val category: String = "",
    val status: String = ""
)

object StorySerializer {
    private val json = Json { ignoreUnknownKeys = true }
    
    fun serializeStory(story: Story): String {
        val serializable = SerializableStory(
            id = story.id,
            documentId = story.documentId,
            name = story.name,
            background = story.background,
            thumbnail = story.thumbnail,
            audio = story.audio,
            backgroundImage = story.backgroundImage,
            isLocked = story.isLocked,
            isBedtime = story.isBedtime,
            duration = story.duration,
            category = story.category,
            status = story.status
        )
        return json.encodeToString(serializable)
    }
    
    fun deserializeStory(jsonString: String): Story {
        val serializable = json.decodeFromString<SerializableStory>(jsonString)
        return Story(
            id = serializable.id,
            documentId = serializable.documentId,
            name = serializable.name,
            background = serializable.background,
            thumbnail = serializable.thumbnail,
            audio = serializable.audio,
            backgroundImage = serializable.backgroundImage,
            isLocked = serializable.isLocked,
            isBedtime = serializable.isBedtime,
            duration = serializable.duration,
            category = serializable.category,
            status = serializable.status
        )
    }
    
    fun serializeStories(stories: List<Story>): List<String> {
        return stories.map { serializeStory(it) }
    }
    
    fun deserializeStories(jsonStrings: List<String>): List<Story> {
        return jsonStrings.map { deserializeStory(it) }
    }
}