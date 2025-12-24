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

fun Modifier.neumorphicShadow(
    cornerRadius: Dp = 0.dp,
    elevation: Dp,
    lightShadowColor: Color,
    darkShadowColor: Color,
    isPressed: Boolean = false
): Modifier {
    if (isPressed || elevation <= 0.dp) {
        return this
    }

    return this.drawBehind { 
        val shadowOffset = elevation.toPx() / 2.5f
        val blurRadius = elevation.toPx()

        val paint = Paint()
        val frameworkPaint = paint.asFrameworkPaint()

        if (blurRadius > 0) {
            frameworkPaint.maskFilter = BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.NORMAL)
        }

        drawIntoCanvas { canvas ->
            val left = 0f
            val top = 0f
            val right = this.size.width
            val bottom = this.size.height
            val radius = cornerRadius.toPx()

            // Dark shadow (bottom-right)
            frameworkPaint.color = darkShadowColor.toArgb()
            canvas.save()
            canvas.translate(shadowOffset, shadowOffset)
            canvas.drawRoundRect(left, top, right, bottom, radius, radius, paint)
            canvas.restore()

            // Light shadow (top-left)
            frameworkPaint.color = lightShadowColor.toArgb()
            canvas.save()
            canvas.translate(-shadowOffset, -shadowOffset)
            canvas.drawRoundRect(left, top, right, bottom, radius, radius, paint)
            canvas.restore()
        }
    }
}
