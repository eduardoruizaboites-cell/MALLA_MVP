package com.malla.mvp.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

object BubbleShapes {
    // ── Clásico (IRC) ───────────────────────────────
    val ClassicOwn = RoundedCornerShape(12.dp)
    val ClassicOther = RoundedCornerShape(12.dp)

    // ── Tarjeta MSN (rectangular con bordes sutiles) ──
    val MsnCardOwn = RoundedCornerShape(4.dp)
    val MsnCardOther = RoundedCornerShape(4.dp)

    // ── Cómic AIM (cola pronunciada) ────────────────
    val AimComicOwn = object : Shape {
        override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
            val path = Path().apply {
                val w = size.width
                val h = size.height
                val d = density.density * 4f
                val tailW = 16f * d
                val tailH = 12f * d
                moveTo(0f, h * 0.5f)
                lineTo(0f, 16f * d)
                cubicTo(0f, 4f * d, 16f * d, 0f, 28f * d, 4f * d)
                lineTo(w - tailW, 0f)
                // Cola inferior derecha
                lineTo(w - tailW, h - tailH)
                lineTo(w, h)
                lineTo(w - tailW / 2, h - tailH)
                cubicTo(w - 8f * d, h, 8f * d, h, 0f, h - 8f * d)
                lineTo(0f, 8f * d)
                cubicTo(0f, 4f * d, 4f * d, 0f, 8f * d, 0f)
                close()
            }
            return Outline.Generic(path)
        }
    }
    val AimComicOther = object : Shape {
        override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
            val path = Path().apply {
                val w = size.width
                val h = size.height
                val d = density.density * 4f
                val tailW = 16f * d
                val tailH = 12f * d
                moveTo(w, h * 0.5f)
                lineTo(w, 16f * d)
                cubicTo(w, 4f * d, w - 16f * d, 0f, w - 28f * d, 4f * d)
                lineTo(tailW, 0f)
                lineTo(tailW, h - tailH)
                lineTo(0f, h)
                lineTo(tailW / 2, h - tailH)
                cubicTo(8f * d, h, w - 8f * d, h, w, h - 8f * d)
                lineTo(w, 8f * d)
                cubicTo(w, 4f * d, w - 4f * d, 0f, w - 8f * d, 0f)
                close()
            }
            return Outline.Generic(path)
        }
    }

    // ── Skype Full Width ─────────────────────────────
    val SkypeFullOwn = RoundedCornerShape(2.dp)
    val SkypeFullOther = RoundedCornerShape(2.dp)

    // ── WhatsApp (con cola) ──────────────────────────
    val WhatsAppOwn = object : Shape {
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
    val WhatsAppOther = object : Shape {
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

    // ── Instagram (píldora tailless) ────────────────
    val InstagramOwn = RoundedCornerShape(24.dp)
    val InstagramOther = RoundedCornerShape(24.dp)

    // ── Glass (misma forma que Instagram) ────────────
    val GlassOwn = RoundedCornerShape(24.dp)
    val GlassOther = RoundedCornerShape(24.dp)

    // ── Custom (placeholder) ─────────────────────────
    val CustomOwn = RoundedCornerShape(16.dp)
    val CustomOther = RoundedCornerShape(16.dp)

    
    val MaterialYouOwn = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 4.dp)
    val MaterialYouOther = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomEnd = 20.dp, bottomStart = 4.dp)

    val GradientOwn = RoundedCornerShape(20.dp)
    val GradientOther = RoundedCornerShape(20.dp)

    val RetroOwn = object : Shape {
        override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
            val path = Path().apply {
                val w = size.width
                val h = size.height
                val d = density.density * 4f
                moveTo(0f, h * 0.1f)
                lineTo(0f, h * 0.9f)
                cubicTo(0f, h, w * 0.1f, h, w * 0.9f, h * 0.9f)
                lineTo(w, h * 0.1f)
                cubicTo(w, 0f, w * 0.9f, 0f, w * 0.1f, 0f)
                close()
            }
            return Outline.Generic(path)
        }
    }
    val RetroOther = RetroOwn

    val ChatHeadOwn = RoundedCornerShape(24.dp)
    val ChatHeadOther = RoundedCornerShape(24.dp)

    val EphemeralOwn = RoundedCornerShape(4.dp)
    val EphemeralOther = RoundedCornerShape(4.dp)

    val PixelOwn = object : Shape {
        override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
            return Outline.Rectangle(Rect(0f, 0f, size.width, size.height))
        }
    }
    val PixelOther = PixelOwn

    fun getShape(style: com.malla.mvp.ui.settings.BubbleStyle, isOwn: Boolean): Shape {
        return when (style) {
            com.malla.mvp.ui.settings.BubbleStyle.CLASSIC -> if (isOwn) ClassicOwn else ClassicOther
            com.malla.mvp.ui.settings.BubbleStyle.MSN_CARD -> if (isOwn) MsnCardOwn else MsnCardOther
            com.malla.mvp.ui.settings.BubbleStyle.AIM_COMIC -> if (isOwn) AimComicOwn else AimComicOther
            com.malla.mvp.ui.settings.BubbleStyle.SKYPE_FULL -> if (isOwn) SkypeFullOwn else SkypeFullOther
            com.malla.mvp.ui.settings.BubbleStyle.WHATSAPP -> if (isOwn) WhatsAppOwn else WhatsAppOther
            com.malla.mvp.ui.settings.BubbleStyle.INSTAGRAM -> if (isOwn) InstagramOwn else InstagramOther
            com.malla.mvp.ui.settings.BubbleStyle.GLASS -> if (isOwn) GlassOwn else GlassOther
            com.malla.mvp.ui.settings.BubbleStyle.CUSTOM -> if (isOwn) CustomOwn else CustomOther
            com.malla.mvp.ui.settings.BubbleStyle.MATERIAL_YOU -> if (isOwn) MaterialYouOwn else MaterialYouOther
            com.malla.mvp.ui.settings.BubbleStyle.GRADIENT -> if (isOwn) GradientOwn else GradientOther
            com.malla.mvp.ui.settings.BubbleStyle.RETRO -> if (isOwn) RetroOwn else RetroOther
            com.malla.mvp.ui.settings.BubbleStyle.CHAT_HEAD -> if (isOwn) ChatHeadOwn else ChatHeadOther
            com.malla.mvp.ui.settings.BubbleStyle.EPHEMERAL -> if (isOwn) EphemeralOwn else EphemeralOther
            com.malla.mvp.ui.settings.BubbleStyle.PIXEL -> if (isOwn) PixelOwn else PixelOther
        }
    }
}
