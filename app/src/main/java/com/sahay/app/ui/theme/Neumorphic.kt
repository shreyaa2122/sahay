package com.sahay.app.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sahay.app.ui.util.neumorphicShadow

@Composable
fun NeumorphicCard(
    modifier: Modifier = Modifier,
    elevation: Dp = 8.dp,
    cornerRadius: Dp = 20.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    content: @Composable () -> Unit
) {
    val isDark = isDarkTheme()

    Box(
        modifier = modifier
            .neumorphicShadow(
                elevation = elevation,
                cornerRadius = cornerRadius,
                lightShadowColor = if (isDark) NeumorphicDarkShadowLight else NeumorphicLightShadowLight,
                darkShadowColor = if (isDark) NeumorphicDarkShadowDark else NeumorphicLightShadowDark
            )
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(cornerRadius)
            )
            .clip(RoundedCornerShape(cornerRadius)),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun NeumorphicButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    elevation: Dp = 8.dp,
    cornerRadius: Dp = 20.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    padding: PaddingValues = PaddingValues(horizontal = 32.dp, vertical = 16.dp),
    content: @Composable () -> Unit
) {
    val isDark = isDarkTheme()

    Box(
        modifier = modifier
            .clickable(onClick = onClick)
            .neumorphicShadow(
                elevation = elevation,
                cornerRadius = cornerRadius,
                lightShadowColor = if (isDark) NeumorphicDarkShadowLight else NeumorphicLightShadowLight,
                darkShadowColor = if (isDark) NeumorphicDarkShadowDark else NeumorphicLightShadowDark
            )
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(cornerRadius)
            )
            .clip(RoundedCornerShape(cornerRadius))
            .padding(padding),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun NeumorphicCircleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    elevation: Dp = 12.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    isPressed: Boolean = false,
    content: @Composable () -> Unit
) {
    val isDark = isDarkTheme()

    val pressedElevation = if (isPressed) 2.dp else elevation

    Box(
        modifier = modifier
            .size(size)
            .clickable(onClick = onClick)
            .neumorphicShadow(
                elevation = pressedElevation,
                cornerRadius = size / 2, // Circle
                lightShadowColor = if (isDark) NeumorphicDarkShadowLight else NeumorphicLightShadowLight,
                darkShadowColor = if (isDark) NeumorphicDarkShadowDark else NeumorphicLightShadowDark,
                isPressed = isPressed
            )
            .background(
                color = backgroundColor,
                shape = CircleShape
            )
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun NeumorphicGradientText(
    text: String,
    modifier: Modifier = Modifier
) {
    val isDark = isDarkTheme()

    val gradientColors = if (isDark) {
        listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.tertiary
        )
    } else {
        listOf(
            Color(0xFF6A88E3),
            Color(0xFF4B69C1)
        )
    }

    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.displaySmall.copy(
            fontWeight = FontWeight.Bold,
            brush = Brush.linearGradient(
                colors = gradientColors,
                start = Offset.Zero,
                end = Offset.Infinite
            )
        )
    )
}

@Composable
fun isDarkTheme(): Boolean {
    return isSystemInDarkTheme()
}
