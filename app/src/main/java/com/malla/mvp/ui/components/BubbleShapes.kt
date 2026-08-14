package com.malla.mvp.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.malla.mvp.ui.settings.BubbleStyle

object BubbleShapes {
    private val rounded = RoundedCornerShape(16.dp)
    private val square = RoundedCornerShape(2.dp)
    private val round = RoundedCornerShape(percent = 50)

    private val tailedOwn = object : Shape {
        override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
            val path = Path().apply {
                val w = size.width
                val h = size.height
                val d = density.density * 4f
                val tailW = 8f * d
                val tailH = 8f * d
                val corner = 16f * d
                moveTo(corner, 0f)
                lineTo(w - corner, 0f)
                cubicTo(w - 4f * d, 0f, w, 4f * d, w, corner)
                lineTo(w, h - tailH - corner)
                lineTo(w, h - tailH)
                lineTo(w - tailW, h)
                lineTo(w - tailW - 4f * d, h - tailH)
                cubicTo(w - 8f * d, h, 8f * d, h, 0f, h - corner)
                lineTo(0f, corner)
                cubicTo(0f, 4f * d, 4f * d, 0f, corner, 0f)
                close()
            }
            return Outline.Generic(path)
        }
    }

    private val tailedOther = object : Shape {
        override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
            val path = Path().apply {
                val w = size.width
                val h = size.height
                val d = density.density * 4f
                val tailW = 8f * d
                val tailH = 8f * d
                val corner = 16f * d
                moveTo(w - corner, 0f)
                lineTo(corner, 0f)
                cubicTo(4f * d, 0f, 0f, 4f * d, 0f, corner)
                lineTo(0f, h - tailH - corner)
                lineTo(0f, h - tailH)
                lineTo(tailW, h)
                lineTo(tailW + 4f * d, h - tailH)
                cubicTo(8f * d, h, w - 8f * d, h, w, h - corner)
                lineTo(w, corner)
                cubicTo(w, 4f * d, w - 4f * d, 0f, w - corner, 0f)
                close()
            }
            return Outline.Generic(path)
        }
    }

    fun getShape(style: BubbleStyle, isOwn: Boolean): Shape = when (style) {
        BubbleStyle.ROUNDED -> rounded
        BubbleStyle.SQUARE -> square
        BubbleStyle.ROUND -> round
        BubbleStyle.TAILED -> if (isOwn) tailedOwn else tailedOther
    }
}
