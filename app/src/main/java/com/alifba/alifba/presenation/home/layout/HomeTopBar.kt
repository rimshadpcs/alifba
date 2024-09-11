package com.alifba.alifba.presenation.home.layout

import android.util.Log
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.alifba.alifba.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(navController: NavController) {
    Row(
        modifier = Modifier
            .background(Color.Transparent)
            .fillMaxWidth()
            .padding(8.dp), // Add padding if necessary
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TopBarIcons(image = R.drawable.profile, contentDescription = "Profile", onClick = {
            Log.d("profile-icon-click","clicking")
            navController.navigate("profile")
        })
        TopBarIcons(image = R.drawable.achievments, contentDescription = "Achievements", onClick = {})
        TopBarIcons(image = R.drawable.`fun`, contentDescription = "Fun", onClick = {})
        TopBarIcons(image = R.drawable.settings, contentDescription = "Settings", onClick = {})
    }
}
@Composable
fun TopBarIcons(image: Int, contentDescription: String, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(Color.White)
            .padding(8.dp)
            .clickable { onClick() }

    ) {
        Image(

            painter = painterResource(id = image),
            contentDescription = contentDescription,
            modifier = Modifier
                .size(64.dp)
        )
    }
}
