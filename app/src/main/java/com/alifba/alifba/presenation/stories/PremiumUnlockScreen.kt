package com.alifba.alifba.presenation.stories

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alifba.alifba.R
import com.alifba.alifba.ui_components.theme.*
import com.alifba.alifba.ui_components.widgets.buttons.SoundEffectManager

@Composable
fun PremiumUnlockScreen(
    onCloseClick: () -> Unit,
    onSubscribeClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val alifbaFont = FontFamily(
        Font(R.font.vag_round, FontWeight.Normal),
        Font(R.font.vag_round_boldd, FontWeight.Bold)
    )
    
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Background image
        Image(
            painter = painterResource(id = R.drawable.storiesbackground),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // Dark overlay for readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
        )
        
        // Close button
        IconButton(
            onClick = {
                SoundEffectManager.playClickSound()
                onCloseClick()
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .size(40.dp)
                .background(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        
        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Title
            Text(
                text = "Unlock All Stories",
                fontFamily = alifbaFont,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // Subtitle
            Text(
                text = "🌟 Launch Offer - Limited Time! 🌟",
                fontFamily = alifbaFont,
                fontWeight = FontWeight.Normal,
                fontSize = 18.sp,
                color = lightPurple,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            
            // Features list
            Column(
                modifier = Modifier.padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FeatureItem(
                    text = "Access to ALL lessons and chapters",
                    alifbaFont = alifbaFont
                )
                FeatureItem(
                    text = "Hours and hours of bedtime stories",
                    alifbaFont = alifbaFont
                )
                FeatureItem(
                    text = "Unlimited access to mini-games",
                    alifbaFont = alifbaFont
                )
                FeatureItem(
                    text = "New content added weekly",
                    alifbaFont = alifbaFont
                )
                FeatureItem(
                    text = "Ad-free learning experience",
                    alifbaFont = alifbaFont
                )
            }
            
            // Pricing display (non-interactive)
            Column(
                modifier = Modifier.padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Yearly plan (recommended)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = lightPurple.copy(alpha = 0.3f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(2.dp, lightPurple)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        // Recommended badge - positioned correctly
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "Yearly Plan",
                                fontFamily = alifbaFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.White
                            )
                            
                            Box(
                                modifier = Modifier
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(lightPurple, darkPurple)
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "RECOMMENDED",
                                    fontFamily = alifbaFont,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = Color.White
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    text = "£34.99/year",
                                    fontFamily = alifbaFont,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "Just £2.91/month",
                                    fontFamily = alifbaFont,
                                    fontSize = 14.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                            
                            Text(
                                text = "Save 27%",
                                fontFamily = alifbaFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = lightPurple
                            )
                        }
                    }
                }
                
                // Monthly plan
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.1f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Monthly Plan",
                            fontFamily = alifbaFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = "£3.99/month",
                                fontFamily = alifbaFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                color = Color.White
                            )
                            
                            Text(
                                text = "Cancel anytime",
                                fontFamily = alifbaFont,
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
            
            // Go to subscription button
            Button(
                onClick = {
                    SoundEffectManager.playClickSound()
                    onSubscribeClick("subscription")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(lightPurple, darkPurple)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Go to Subscription",
                        fontFamily = alifbaFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun FeatureItem(
    text: String,
    alifbaFont: FontFamily
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Check icon
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(
                    color = lightPurple,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Included",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
        
        Text(
            text = text,
            fontFamily = alifbaFont,
            fontSize = 16.sp,
            color = Color.White,
            modifier = Modifier.weight(1f)
        )
    }
}

