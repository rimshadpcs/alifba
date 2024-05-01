package com.alifba.alifba.ui_layouts.lessonScreens.lessonSegment.DragAndDropLesson

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alifba.alifba.R
import com.alifba.alifba.models.DragItem
import com.alifba.alifba.models.DropItem


@Composable
fun DropCard(dropItem: DropItem, onItemDropped: (DragItem) -> Unit) {
    val dragItems = remember { mutableStateMapOf<Int, DragItem>() }
    val alifbaFont = FontFamily(Font(R.font.more_sugar_regular, FontWeight.SemiBold))

    DropTarget<DragItem>(
        modifier = Modifier
            .padding(6.dp)
            .size(width = 120.dp, height = 100.dp)
    ) { isInBound, dragItem ->
        if (isInBound && dragItem != null) {
            dragItems[dragItem.id] = dragItem
            onItemDropped(dragItem)
        }

        val bgColor = if (isInBound) Color.Green else Color(0xFFe57cbc)

        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(8.dp))
                .background(bgColor),
            contentAlignment = Alignment.TopCenter,
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(bottom = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFFF8AD1)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = dropItem.name,
                    fontFamily = alifbaFont,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}



@Composable
fun DragItemCard(dragItem: DragItem) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFCCC2DC)),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.padding(8.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .height(120.dp)
        ) {
            DragTarget(modifier = Modifier.size(100.dp), dataToDrop = dragItem) {
                Image(
                    painter = painterResource(id = dragItem.image),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
            }
        }
    }
}


