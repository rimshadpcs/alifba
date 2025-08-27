package com.alifba.alifba.presenation.lessonScreens.lessonSegment.dragAndDropLesson

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
import com.alifba.alifba.data.models.DragItem
import com.alifba.alifba.data.models.DropItem
import com.alifba.alifba.data.models.LessonSegment
import com.alifba.alifba.data.models.dropItems
import com.alifba.alifba.ui_components.dialogs.LottieAnimationDialog
import kotlinx.coroutines.delay

@Composable
fun DragDropLessonScreen(segment: LessonSegment.DragAndDropExperiment, onNextClicked: () -> Unit) {
    val dragItems = remember { mutableStateListOf(*segment.dragItemList.toTypedArray()) }
    val showDialog = remember { mutableStateOf(false) }
    val allItemsCorrect = remember { mutableStateOf(false) }
    //val playAudio = remember { mutableStateOf(false) }

    fun onAllItemsCorrect() {
        showDialog.value = true
       // onNextClicked()
    }
    LaunchedEffect(allItemsCorrect.value) {
        if (allItemsCorrect.value) {
            delay(1000)  // Delay before navigating to the next screen
            onNextClicked()
        }
    }
    // Effect for handling the sequence after showing the dialog
    LaunchedEffect(showDialog.value) {
        if (showDialog.value) {
            delay(500)  // Short delay to show the tick animation
            showDialog.value = false
            //playAudio.value = false
            if (dragItems.isEmpty()) {
                delay(500)  // Ensure there is a small pause after the last item is correctly dropped
                //onNextClicked()  // Call the next screen function here directly after all animations
            }
        }
    }

    fun handleItemDropped(dragItem: DragItem, dropCategory: String) {
        val indexToRemove = dragItems.indexOfFirst { it.id == dragItem.id }
        if (indexToRemove != -1 && dragItem.answer == dropCategory) {
            dragItems.removeAt(indexToRemove)
            showDialog.value = true
            //playAudio.value = true  // Play audio when item is correctly placed
            // Check if all items are correctly dropped to handle additional logic or UI updates
            if (dragItems.isEmpty()) {

                allItemsCorrect.value = true

            }
        }
    }


    if (showDialog.value) {
        LottieAnimationDialog(showDialog = showDialog, lottieFileRes = R.raw.tick)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = segment.question,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
            fontFamily = FontFamily(Font(R.font.vag_round, FontWeight.Bold)),
            fontWeight = FontWeight.Bold,
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
fun BoxScope.DropCardContainer(dropItems: List<DropItem>, onItemDropped: (DragItem, String) -> Unit) {
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
            DropCard(dropItem = dropItem) { dragItem ->
                onItemDropped(dragItem, dropItem.name)
            }
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
                //dragItems,
                //dropItems,
            ),
            onNextClicked = {}
        )
    }

