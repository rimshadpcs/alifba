package com.alifba.alifba.ui_layouts.home.layout

import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.alifba.alifba.R
import com.alifba.alifba.ui_layouts.home.HomeViewModel

@Composable
fun HomeScreen(viewModel: HomeViewModel, navController: NavController) {
    val scrollState = rememberLazyListState()

    LazyRow(
        state = scrollState,
        reverseLayout = true
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