package com.alifba.alifba.ui_components.widgets

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable

/**
 * Reusable player for the dotLottie (.lottie) animations shipped as extracted
 * assets/<name>/<name>.json + assets/<name>/images/ (see moonwave for the original one-off
 * version this was factored out of). Wired up via a plain LottieAnimationView through
 * AndroidView rather than the lottie-compose LottieAnimation composable, since image assets
 * require calling setImagesAssetsFolder explicitly (no implicit default) and that setter isn't
 * exposed through the Compose composition APIs in the lottie-compose version this app uses
 * (4.0.0, which also can't open a raw .lottie file directly — dotLottie support wasn't added
 * until 6.1 — hence extracting the JSON + images at build time instead of bundling the .lottie
 * file itself).
 *
 * @param name The extracted animation's folder/file name, e.g. "moonwave" for
 * assets/moonwave/moonwave.json + assets/moonwave/images/.
 * @param viewScale Applied via the View's own scaleX/scaleY (its native draw-pass transform)
 * rather than Modifier.scale() (backed by a Compose graphicsLayer, i.e. an offscreen snapshot
 * layer) — pass this instead of wrapping the call site in Modifier.scale() if the animation
 * needs to render larger than its layout bounds.
 */
@Composable
fun DotLottieView(
    name: String,
    modifier: Modifier = Modifier,
    viewScale: Float = 1f,
    repeatCount: Int = LottieDrawable.INFINITE
) {
    AndroidView(
        factory = { ctx ->
            LottieAnimationView(ctx).apply {
                imageAssetsFolder = "$name/images/"
                scaleX = viewScale
                scaleY = viewScale
                addLottieOnCompositionLoadedListener {
                    // setAnimation(String) loads asynchronously — playAnimation() has to wait
                    // for this listener rather than running immediately after setAnimation(),
                    // or it races a composition/duration that isn't set yet.
                    this.repeatCount = repeatCount
                    playAnimation()
                }
                setAnimation("$name/$name.json")
            }
        },
        modifier = modifier
    )
}
