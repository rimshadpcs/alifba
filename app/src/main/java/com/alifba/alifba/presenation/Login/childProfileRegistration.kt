package com.alifba.alifba.presenation.Login

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.alifba.alifba.R
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.util.lerp
import com.alifba.alifba.ui_components.theme.navyBlue
import com.alifba.alifba.ui_components.theme.white
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.alifba.alifba.ui_components.theme.black
import com.alifba.alifba.ui_components.theme.darkPink
import com.alifba.alifba.ui_components.theme.lightPink
import com.alifba.alifba.ui_components.widgets.buttons.CommonButton
import com.alifba.alifba.ui_components.widgets.textFields.CustomInputField

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProfileRegistration(
    navController: NavController
) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val halfScreenHeight = screenHeight / 2
    val alifbaFont: FontFamily = FontFamily(
        Font(R.font.more_sugar_regular, FontWeight.SemiBold)
    )
    val authViewModel: AuthViewModel = hiltViewModel()
    val context = LocalContext.current
    var childName by remember { mutableStateOf("") }
    var parentName by remember { mutableStateOf("") }
    var selectedAge by remember { mutableStateOf<Int?>(null) }
    var selectedAvatar by remember { mutableStateOf(0) }
    var selectedAvatarName by remember { mutableStateOf("") }

    val profileCreationState by authViewModel.profileCreationState.collectAsState()


    LaunchedEffect(profileCreationState) {
        when (profileCreationState) {
            is ProfileCreationState.Success -> {
                // Navigate to home screen
                navController.navigate("home") {
                    popUpTo("createProfile") { inclusive = true }
                }
                Toast.makeText(context, "Registration Successful!", Toast.LENGTH_SHORT).show()
            }

            is ProfileCreationState.Error -> {
                val errorMessage = (profileCreationState as ProfileCreationState.Error).message
                Toast.makeText(context, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
            }

            else -> {
                // Do nothing for Idle state
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(white)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(halfScreenHeight)
        ) {
            // Background Image
            Image(
                painter = painterResource(id = R.drawable.ufo_background),
                contentDescription = "Background",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Avatar Carousel
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
            ) {


                AvatarCarousel { avatarName ->
                    selectedAvatarName = avatarName
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Choose Avatar",
            style = MaterialTheme.typography.h6,
            fontFamily = alifbaFont,
            color = navyBlue,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
        )
        // Remaining Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .background(white),
            //verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Name Input Field
            NameInputField(parentName = parentName, onParentNameChange = { parentName = it },
                childName = childName, onChildNameChange = { childName = it })

            // Age Buttons
            AgeSelectionButtons(onAgeSelected = { selectedAge = it })
            Spacer(modifier = Modifier.height(2.dp))
            CommonButton(
                onClick = {
//                    sendProfileDataToFireStore(
//                        parentName,
//                        childName,
//                        selectedAge,
//                        selectedAvatarName,
//                        da
//                        navController,
//                        context
//                    )
                    authViewModel.sendProfileDataToFireStore(parentName, childName, selectedAge, selectedAvatarName)
                    Log.d("ProfileRegistration", "Selected Avatar Name: $selectedAvatarName")
                },
                buttonText = "Submit",
                shadowColor = darkPink,
                mainColor = lightPink,
                textColor = white
            )
        }
    }
}






data class Avatar(val id: Int, val name: String)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AvatarCarousel(
    onAvatarSelected: (String) -> Unit
) {
    val avatars = listOf(
        Avatar(R.drawable.deenasaur, "Deenasaur"),
        Avatar(R.drawable.duallama, "Duallama"),
        Avatar(R.drawable.firdawsaur, "Firdawsaur"),
        Avatar(R.drawable.ihsaninguin, "Ihsaninguin"),
        Avatar(R.drawable.imamoth, "Imamoth"),
        Avatar(R.drawable.khilafox, "Khilafox"),
        Avatar(R.drawable.shukraf, "Shukraf"),
        Avatar(R.drawable.jannahbee, "Jannah Bee"),
        Avatar(R.drawable.qadragon, "Qadragon"),
        Avatar(R.drawable.sabracorn, "Sabracorn"),
        Avatar(R.drawable.sadiqling, "Sadiqling"),
        Avatar(R.drawable.sidqhog, "Sidqhog")
    )

    val alifbaFont: FontFamily = FontFamily(
        Font(R.font.more_sugar_regular, FontWeight.SemiBold)
    )

    val repeatedCount = 1000
    val infiniteAvatars = List(avatars.size * repeatedCount) { index -> avatars[index % avatars.size] }

    // Set initial page to the middle position where Deenasaur is the visible item
    val middlePosition = (infiniteAvatars.size / 2) - (infiniteAvatars.size / 2 % avatars.size)
    val pagerState = rememberPagerState(initialPage = middlePosition, pageCount = { infiniteAvatars.size })

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(pagerState.currentPage) {
        // Update selected avatar name on page change
        onAvatarSelected(infiniteAvatars[pagerState.currentPage % avatars.size].name)

        // If at the start or end, jump to the middle of the list to simulate infinite scrolling
        if (pagerState.currentPage == 0 || pagerState.currentPage == infiniteAvatars.size - 1) {
            val newPage = middlePosition + (pagerState.currentPage % avatars.size)
            coroutineScope.launch {
                pagerState.scrollToPage(newPage)  // Use scrollToPage for a seamless jump
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        contentAlignment = Alignment.Center
    ) {
        // Display the avatars in the HorizontalPager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentPadding = PaddingValues(horizontal = 88.dp),
            pageSpacing = (-30).dp,
        ) { page ->
            val actualPage = page % avatars.size
            val scale = lerp(
                start = 0.8f,
                stop = 1f,
                fraction = 1f - pagerState.currentPageOffsetFraction.absoluteValue.coerceIn(0f, 1f)
            )

            Box(
                modifier = Modifier.graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = avatars[actualPage].id),
                    contentDescription = "Avatar $actualPage",
                    modifier = Modifier.size(250.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = avatars[actualPage].name,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = (-30).dp),
                    fontFamily = alifbaFont,
                    color = black,
                    fontSize = 30.sp,

                    )

            }
        }

        // Bottom Row for navigation buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Arrow Button
            Button(
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                    }
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
                elevation = ButtonDefaults.elevation(0.dp),
                modifier = Modifier.size(64.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.leftarrow),
                    contentDescription = "Scroll Left",
                    modifier = Modifier.size(64.dp)
                )
            }

            // Right Arrow Button
            Button(
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
                elevation = ButtonDefaults.elevation(0.dp),
                modifier = Modifier.size(64.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.rightarrow),
                    contentDescription = "Scroll Right",
                    modifier = Modifier.size(64.dp)
                )
            }
        }
    }
}

@Composable
fun NameInputField(parentName: String, onParentNameChange: (String) -> Unit,
                   childName: String, onChildNameChange: (String) -> Unit) {

    CustomInputField(value = parentName, onValueChange = onParentNameChange, labelText = "Enter parent's name")
    CustomInputField(value = childName, onValueChange = onChildNameChange, labelText = "Enter Child's Name")
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun AgeSelectionButtons(onAgeSelected: (Int) -> Unit) {
    val ageRange = (4..13).toList()
    var selectedAge by remember { mutableStateOf<Int?>(null) }
    val alifbaFont = FontFamily(
        Font(R.font.more_sugar_regular, FontWeight.SemiBold)
    )
    Text(
        text = "Select child's age",
        style = MaterialTheme.typography.h6,
        fontFamily = alifbaFont,
        color = navyBlue,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    )
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(
            space = 8.dp,
            alignment = Alignment.CenterHorizontally
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ageRange.forEach { age ->
            Button(
                onClick = { selectedAge = age
                    onAgeSelected(age) },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = if (selectedAge == age) Color.White else navyBlue,
                    contentColor = if (selectedAge == age) navyBlue else Color.White
                ),
                modifier = Modifier.width(60.dp)
            ) {
                Text(
                    text = "$age",
                    fontFamily = alifbaFont
                )

            }
        }
    }
}


