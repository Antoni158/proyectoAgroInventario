package com.example.inventario.ui.branding

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.inventario.R

@Composable
private fun BrandLogoImage(
    modifier: Modifier,
    contentScale: ContentScale = ContentScale.Fit
) {
    Image(
        painter = painterResource(R.drawable.logo_inventario_transparent),
        contentDescription = "Inventario Agrícola",
        modifier = modifier,
        contentScale = contentScale
    )
}

@Composable
fun BrandLogo(
    modifier: Modifier = Modifier,
    size: Dp = 160.dp,
    showTitle: Boolean = false,
    titleColor: Color = Color.White,
    animate: Boolean = false,
    animationProgress: Float = 1f
) {
    val context = LocalContext.current
    val aspect = remember(context) { LogoBitmapUtil.logoAspectRatio(context) }

    val scale by animateFloatAsState(
        targetValue = if (animate) animationProgress else 1f,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "logoScale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (animate) animationProgress else 1f,
        animationSpec = tween(700),
        label = "logoAlpha"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BrandLogoImage(
            modifier = Modifier
                .height(size)
                .width(size * aspect)
                .scale(scale)
                .alpha(alpha),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
fun BrandLogoCompact(
    modifier: Modifier = Modifier,
    height: Dp = 44.dp
) {
    val context = LocalContext.current
    val aspect = remember(context) { LogoBitmapUtil.logoAspectRatio(context) }

    BrandLogoImage(
        modifier = modifier
            .height(height)
            .width(height * aspect),
        contentScale = ContentScale.Fit
    )
}
