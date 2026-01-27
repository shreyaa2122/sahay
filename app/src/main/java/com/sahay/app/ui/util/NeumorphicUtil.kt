package com.sahay.app.ui.util

import android.graphics.BlurMaskFilter
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// A more stable implementation of the neumorphic shadow modifier.
fun Modifier.neumorphicShadow(
    elevation: Dp,
    cornerRadius: Dp,
    lightShadowColor: Color,
    darkShadowColor: Color,
    isPressed: Boolean = false
): Modifier = if (elevation <= 0.dp) this else drawBehind {

    if (!isPressed) {
        // The logic for drawing shadows works by drawing two blurred shapes,
        // one offset to the bottom-right (dark) and one to the top-left (light).
        drawIntoCanvas { canvas ->

            val paint = Paint()
            val frameworkPaint = paint.asFrameworkPaint()

            // 1. Draw the dark shadow
            frameworkPaint.color = darkShadowColor.toArgb()
            frameworkPaint.maskFilter = (BlurMaskFilter(elevation.toPx(), BlurMaskFilter.Blur.NORMAL))
            canvas.drawRoundRect(
                left = 0f,
                top = 0f,
                right = size.width,
                bottom = size.height,
                radiusX = cornerRadius.toPx(),
                radiusY = cornerRadius.toPx(),
                paint = paint
            )

            // 2. Draw the light shadow
            frameworkPaint.color = lightShadowColor.toArgb()
            frameworkPaint.maskFilter = (BlurMaskFilter(elevation.toPx(), BlurMaskFilter.Blur.NORMAL))
            canvas.drawRoundRect(
                left = 0f,
                top = 0f,
                right = size.width,
                bottom = size.height,
                radiusX = cornerRadius.toPx(),
                radiusY = cornerRadius.toPx(),
                paint = paint
            )
        }
    }
}
