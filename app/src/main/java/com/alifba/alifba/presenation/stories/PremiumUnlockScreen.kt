package com.alifba.alifba.presenation.stories

import androidx.compose.runtime.Composable

@Composable
fun PremiumUnlockScreen(
    onCloseClick: () -> Unit,
    onSubscribeClick: (String) -> Unit,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier
) {
    RevenueCatPaywall(
        onClose = onCloseClick,
        source = "premium_unlock",
        requireParentGate = false
    )
}
