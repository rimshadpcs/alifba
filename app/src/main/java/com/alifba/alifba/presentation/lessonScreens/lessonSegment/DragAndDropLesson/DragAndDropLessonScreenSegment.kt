package com.alifba.alifba.presentation.lessonScreens.lessonSegment.DragAndDropLesson

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alifba.alifba.R
import com.alifba.alifba.models.DragItem
import com.alifba.alifba.models.DropItem
import com.alifba.alifba.models.LessonSegment
import com.alifba.alifba.models.dragItems
import com.alifba.alifba.models.dropItems
import com.alifba.alifba.presentation.dialogs.LottieAnimationDialog
import kotlinx.coroutines.delay

@Composable
fun DragDropLessonScreen(segment: LessonSegment.DragAndDropExperiment) {
    val dragItems = remember { mutableStateListOf(*segment.dragItemList.toTypedArray()) }
    val showDialog = remember { mutableStateOf(false) }
    val animationFinished = remember { mutableStateOf(false) }


    fun onAllItemsRemoved() {
        showDialog.value = true
    }

    if (showDialog.value) {
        LottieAnimationDialog(showDialog = showDialog, lottieFileRes = R.raw.celebration)
        LaunchedEffect(showDialog.value) {
            delay(2000)  // Assuming 2000 milliseconds animation duration
            showDialog.value = false
            animationFinished.value = true  // Set the animation finish state to true
        }
    }
    fun handleItemDropped(dragItem: DragItem) {
        val indexToRemove = dragItems.indexOfFirst { it.id == dragItem.id }
        if (indexToRemove != -1) {
            dragItems.removeAt(indexToRemove)
            // Update the IDs of the remaining items
            for (i in indexToRemove until dragItems.size) {
                dragItems[i] = dragItems[i].copy(id = i + 1)
            }
            if (dragItems.isEmpty()) {
                onAllItemsRemoved()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = segment.question,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
            fontFamily = FontFamily(Font(R.font.more_sugar_regular, FontWeight.Normal)),
            modifier = Modifier.padding(12.dp)
        )
        TouchPressDraggable(modifier = Modifier.fillMaxSize()) {
            LazyVerticalGrid(
                GridCells.Fixed(2),
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 10.dp)
            ) {
                items(items = dragItems) { dragItem ->
                    DragItemCard(dragItem = dragItem)
                }
            }
            DropCardContainer(dropItems = segment.dropItemList, onItemDropped = ::handleItemDropped)
        }
    }
}


@Composable
fun BoxScope.DropCardContainer(dropItems: List<DropItem>, onItemDropped: (DragItem) -> Unit) {
    LazyRow(
        modifier = Modifier
            .fillMaxHeight(0.3f)
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .align(Alignment.BottomCenter),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        items(items = dropItems) { dropItem ->
            DropCard(dropItem = dropItem, onItemDropped = onItemDropped)
        }
    }
}



@Preview(showBackground = true, backgroundColor = 0xFFFFFF, widthDp = 320, heightDp = 640)
    @Composable
    fun DragDropLessonScreenPreview() {

        DragDropLessonScreen(
            LessonSegment.DragAndDropExperiment(
                question = "Who created al these amazing things in this world drag and drop them to the right box below",
                dragItems,
                dropItems
            )
        )
    }

