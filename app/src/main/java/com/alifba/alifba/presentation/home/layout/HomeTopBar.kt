package com.alifba.alifba.presentation.home.layout

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.alifba.alifba.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar() {
    Row(
        modifier = Modifier
            .background(Color.Transparent)
            .fillMaxWidth()
            .padding(8.dp), // Add padding if necessary
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TopBarIcons(image = R.drawable.profile, contentDescription = "Profile")
        TopBarIcons(image = R.drawable.achievments, contentDescription = "Achievements")
        TopBarIcons(image = R.drawable.`fun`, contentDescription = "Fun")
        TopBarIcons(image = R.drawable.settings, contentDescription = "Settings")
    }
}

@Composable
fun TopBarIcons(image: Int, contentDescription: String) {
    // Using Box as a clickable area instead of IconButton
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(64.dp) // Set the desired size
            .clip(CircleShape)
            .background(Color.White)
            .padding(8.dp)
            .clickable { /* Handle click */ }

    ) {
        Image(

            painter = painterResource(id = image),
            contentDescription = contentDescription,
            modifier = Modifier
                .size(64.dp)
        )
    }
}
