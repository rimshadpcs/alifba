package com.alifba.alifba.ui_layouts.home.layout

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.alifba.alifba.R
import com.alifba.alifba.ui_layouts.home.HomeViewModel
import com.alifba.alifba.utils.PlayAudio
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(viewModel: HomeViewModel, navController: NavController) {
    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val alifbaFont = FontFamily(Font(R.font.more_sugar_regular, FontWeight.Normal))


    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Assalamu Alaikkum, \n Laila",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = alifbaFont ,
            lineHeight = 28.sp,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 8.dp, bottom = 8.dp)
        )
        Spacer(modifier = Modifier.weight(.3f))  // This will push the LazyRow towards center if needed

        LazyRow(
            reverseLayout = true, // List is reversed
            state = scrollState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(viewModel.lessonMenuItemList.size) { index ->
                val item = viewModel.lessonMenuItemList[index]
                LessonMenuItems(
                    image = item.image,
                    name = item.name,
                    onClick = {
                        navController.navigate("lessonPathScreen")
                    }
                )
            }
        }

        // Navigation arrows at the bottom
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left arrow for scrolling to show more items on the left
            if (scrollState.firstVisibleItemIndex + scrollState.layoutInfo.visibleItemsInfo.size < viewModel.lessonMenuItemList.size) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val itemIndex = (scrollState.firstVisibleItemIndex + 1).coerceAtMost(viewModel.lessonMenuItemList.size - 1)
                            scrollState.animateScrollToItem(itemIndex)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.leftarrow),
                        contentDescription = "Show More Left",
                        modifier = Modifier.size(64.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(64.dp))  // Placeholder to keep alignment
            }

            // Right arrow for going back to the right
            if (scrollState.firstVisibleItemIndex > 0) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val itemIndex = (scrollState.firstVisibleItemIndex - 1).coerceAtLeast(0)
                            scrollState.animateScrollToItem(itemIndex)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.rightarrow),
                        contentDescription = "Go Right",
                        modifier = Modifier.size(64.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(64.dp))  // Placeholder to keep alignment
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    // Creating a mock NavController
    val navController = rememberNavController()

    // Creating a mock ViewModel with sample data
    val mockViewModel = viewModel<HomeViewModel>(factory = MockViewModelFactory())

    // Invoking the HomeScreen with the mock data and NavController
    HomeScreen(viewModel = mockViewModel, navController = navController)
}

// This will be your ViewModel class modified to fit preview needs
class MockViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MockHomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MockHomeViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class MockHomeViewModel : HomeViewModel() {
    init {
        // Populate the lessonMenuItemList with sample data
        val lessonMenuItemList = listOf(
            LessonMenuItem(image = R.drawable.levelone, name = "Lesson 1"),
            LessonMenuItem(image = R.drawable.leveltwo, name = "Lesson 2")
            // Add more items as needed
        )
    }
}

// Assuming you have a simple data model
data class LessonMenuItem(val image: Int, val name: String)

// This should match the real ViewModel used in your actual composable if it's different
open class HomeViewModel : ViewModel() {
    // Replace with actual type of list and data handling
    var lessonMenuItemList = listOf<LessonMenuItem>()
}