package com.lidesheng.hyperlyric.root.mediacard.notification.background

import android.app.WallpaperColors
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.HardwareRenderer
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.RenderEffect
import android.graphics.RenderNode
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.hardware.HardwareBuffer
import android.media.ImageReader
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import com.lidesheng.hyperlyric.common.color.ColorExtractor
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

internal data class NotificationMediaColorConfig(
    val textPrimary: Int,
    val textSecondary: Int,
    val backgroundStart: Int,
    val backgroundEnd: Int
)

internal data class RenderedNotificationMediaBackground(
    val bitmap: Bitmap,
    val colors: NotificationMediaColorConfig
)

internal class NotificationMediaBackgroundRenderer(
    private val classLoader: ClassLoader
) {
    private val monet = MonetApi.create(classLoader)

    fun render(
        context: Context,
        artworkIcon: Icon?,
        packageName: String,
        style: Int,
        blurAmount: Int,
        autoInvert: Boolean,
        width: Int,
        height: Int
    ): RenderedNotificationMediaBackground? {
        if (width <= 0 || height <= 0) return null
        val artwork = runCatching {
            artworkIcon?.loadDrawable(context)
                ?: context.packageManager.getApplicationIcon(packageName)
        }.getOrNull() ?: return null
        val source = artwork.toBitmapSafe() ?: return null
        val extracted = ColorExtractor.extractThemePalette(source, 3).rawColors
        if (extracted.isEmpty()) {
            source.recycle()
            return null
        }
        val wallpaperColors = WallpaperColors(
            Color.valueOf(extracted[0]),
            extracted.getOrNull(1)?.let { Color.valueOf(it) },
            extracted.getOrNull(2)?.let { Color.valueOf(it) }
        )
        val palette = monet.palette(wallpaperColors) ?: run {
            source.recycle()
            return null
        }
        val colors = colorConfig(style, source, palette, autoInvert)
        val result = when (style) {
            1 -> renderCoverArt(source, colors, context, width, height)
            2 -> renderBlurredCover(source, colors, blurAmount, width, height)
            3 -> renderRadialGradient(source, colors, width, height)
            4 -> renderLinearGradient(source, colors, width, height)
            else -> null
        }
        source.recycle()
        return result?.let { RenderedNotificationMediaBackground(it, colors) }
    }

    private fun colorConfig(
        style: Int,
        artwork: Bitmap,
        palette: MonetPalette,
        autoInvert: Boolean
    ): NotificationMediaColorConfig {
        return when (style) {
            1 -> NotificationMediaColorConfig(
                palette.accent1[2], palette.accent1[2],
                palette.accent1[8], palette.accent1[8]
            )
            2, 3 -> NotificationMediaColorConfig(
                palette.neutral1[1], palette.neutral2[3],
                palette.accent2[9], palette.accent1[9]
            )
            4 -> {
                val reverse = autoInvert && artwork.brightness() >= 192f
                val text = palette.accent1[if (reverse) 8 else 2]
                val background = palette.accent1[if (reverse) 3 else 8]
                NotificationMediaColorConfig(text, text, background, background)
            }
            else -> NotificationMediaColorConfig(Color.WHITE, Color.WHITE, Color.BLACK, Color.BLACK)
        }
    }

    private fun renderCoverArt(
        artwork: Bitmap,
        colors: NotificationMediaColorConfig,
        context: Context,
        width: Int,
        height: Int
    ): Bitmap {
        val tile = artwork.scale(132, 132)
        val smallTile = tile.scale(66, 66)
        val mosaic = createBitmap(264, 264)
        val canvas = Canvas(mosaic)
        val positions = arrayOf(0f to 0f, 132f to 0f, 0f to 132f, 132f to 132f, 99f to 99f)
        positions.forEachIndexed { index, position ->
            val source = if (index < 4) tile else smallTile
            val matrix = Matrix().apply {
                postRotate(Random.nextInt(4) * 90f, source.width / 2f, source.height / 2f)
                postScale(
                    if (Random.nextBoolean()) -1f else 1f,
                    if (Random.nextBoolean()) -1f else 1f,
                    source.width / 2f,
                    source.height / 2f
                )
            }
            val transformed = Bitmap.createBitmap(
                source, 0, 0, source.width, source.height, matrix, true
            )
            canvas.drawBitmap(transformed, position.first, position.second, null)
            if (transformed !== source) transformed.recycle()
        }
        tile.recycle()
        smallTile.recycle()

        val correction = when (mosaic.brightness()) {
            in 0f..<50f -> 40f
            in 50f..<100f -> 20f
            in 100f..<200f -> -20f
            else -> -40f
        }
        val matrix = ColorMatrix(floatArrayOf(
            1f, 0f, 0f, 0f, correction,
            0f, 1f, 0f, 0f, correction,
            0f, 0f, 1f, 0f, correction,
            0f, 0f, 0f, 1f, 0f
        ))
        canvas.drawBitmap(mosaic, 0f, 0f, Paint().apply {
            colorFilter = ColorMatrixColorFilter(matrix)
        })
        canvas.drawColor(colors.backgroundStart and 0x6fffffff)
        val darkMode = context.resources.configuration.uiMode and 0x30 == 0x20
        val overlay = if (darkMode) 0 else 248
        canvas.drawColor(Color.argb(20, overlay, overlay, overlay))

        val blurred = mosaic.hardwareBlur(40f)
        mosaic.recycle()
        val result = blurred.centerCrop(width, height)
        blurred.recycle()
        return result
    }

    private fun renderBlurredCover(
        artwork: Bitmap,
        colors: NotificationMediaColorConfig,
        blurAmount: Int,
        width: Int,
        height: Int
    ): Bitmap {
        val base = artwork.centerCrop(max(width, height), max(width, height))
        val canvas = Canvas(base)
        canvas.drawCircleGradient(colors.backgroundStart, colors.backgroundEnd)
        val blurred = base.hardwareBlur(height * blurAmount.coerceIn(1, 20) / 100f)
        base.recycle()
        val result = blurred.centerCrop(width, height)
        blurred.recycle()
        return result
    }

    private fun renderRadialGradient(
        artwork: Bitmap,
        colors: NotificationMediaColorConfig,
        width: Int,
        height: Int
    ): Bitmap {
        val result = artwork.centerCrop(width, height)
        Canvas(result).drawCircleGradient(colors.backgroundStart, colors.backgroundEnd)
        return result
    }

    private fun renderLinearGradient(
        artwork: Bitmap,
        colors: NotificationMediaColorConfig,
        width: Int,
        height: Int
    ): Bitmap {
        val result = createBitmap(width, height)
        val canvas = Canvas(result)
        canvas.drawColor(colors.backgroundStart)
        val side = min(width, height)
        val cover = artwork.centerCrop(side, side)
        canvas.drawBitmap(cover, (width - side).toFloat(), 0f, null)
        cover.recycle()
        val shader = LinearGradient(
            (width - side).toFloat(), 0f, width.toFloat(), 0f,
            colors.backgroundStart,
            colors.backgroundStart and 0x00ffffff or (51 shl 24),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect((width - side).toFloat(), 0f, width.toFloat(), height.toFloat(), Paint().apply {
            this.shader = shader
        })
        return result
    }

    private fun Canvas.drawCircleGradient(start: Int, end: Int) {
        val shader = RadialGradient(
            0f, height / 2f, max(width, height).toFloat(),
            intArrayOf(
                start and 0x00ffffff or (64 shl 24),
                end or (255 shl 24)
            ), null, Shader.TileMode.CLAMP
        )
        drawRect(0f, 0f, width.toFloat(), height.toFloat(), Paint().apply { this.shader = shader })
    }

    private fun Drawable.toBitmapSafe(): Bitmap? {
        val width = intrinsicWidth.takeIf { it > 0 } ?: return null
        val height = intrinsicHeight.takeIf { it > 0 } ?: return null
        return runCatching {
            val bitmap = createBitmap(width, height)
            val originalBounds = Rect(bounds)
            try {
                setBounds(0, 0, width, height)
                draw(Canvas(bitmap))
            } finally {
                setBounds(originalBounds)
            }
            bitmap
        }.getOrNull()
    }

    private fun Bitmap.centerCrop(targetWidth: Int, targetHeight: Int): Bitmap {
        val scale = max(targetWidth / width.toFloat(), targetHeight / height.toFloat())
        val scaledWidth = (width * scale).toInt().coerceAtLeast(targetWidth)
        val scaledHeight = (height * scale).toInt().coerceAtLeast(targetHeight)
        val scaled = scale(scaledWidth, scaledHeight)
        val result = Bitmap.createBitmap(
            scaled,
            (scaled.width - targetWidth) / 2,
            (scaled.height - targetHeight) / 2,
            targetWidth,
            targetHeight
        ).copy(Bitmap.Config.ARGB_8888, true)
        if (scaled !== this) scaled.recycle()
        return result
    }

    private fun Bitmap.brightness(): Float {
        val step = 5
        var total = 0f
        var count = 0
        for (x in 0 until width step step) {
            for (y in 0 until height step step) {
                val pixel = getPixel(x, y)
                total += Color.red(pixel) * 0.299f +
                    Color.green(pixel) * 0.587f + Color.blue(pixel) * 0.114f
                count++
            }
        }
        return if (count == 0) 0f else total / count
    }

    private fun Bitmap.hardwareBlur(radius: Float): Bitmap {
        val reader = ImageReader.newInstance(
            width, height, PixelFormat.RGBA_8888, 1,
            HardwareBuffer.USAGE_GPU_SAMPLED_IMAGE or HardwareBuffer.USAGE_GPU_COLOR_OUTPUT
        )
        val node = RenderNode("HyperLyricMediaBlur")
        val renderer = HardwareRenderer()
        try {
            renderer.setSurface(reader.surface)
            renderer.setContentRoot(node)
            node.setPosition(0, 0, width, height)
            node.setRenderEffect(RenderEffect.createBlurEffect(radius, radius, Shader.TileMode.MIRROR))
            node.beginRecording().also { canvas ->
                canvas.drawBitmap(this, 0f, 0f, null)
                node.endRecording()
            }
            renderer.createRenderRequest().setWaitForPresent(true).syncAndDraw()
            reader.acquireNextImage().use { image ->
                val buffer = image?.hardwareBuffer ?: error("No blur output buffer")
                buffer.use {
                    return Bitmap.wrapHardwareBuffer(buffer, null)
                        ?.copy(Bitmap.Config.ARGB_8888, false)
                        ?: error("Unable to copy blur output")
                }
            }
        } finally {
            node.discardDisplayList()
            renderer.destroy()
            reader.close()
        }
    }

    private data class MonetPalette(
        val neutral1: List<Int>,
        val neutral2: List<Int>,
        val accent1: List<Int>,
        val accent2: List<Int>
    )

    private class MonetApi private constructor(
        private val constructor: java.lang.reflect.Constructor<*>,
        private val styleContent: Any,
        private val allShadesField: java.lang.reflect.Field,
        private val neutral1Field: java.lang.reflect.Field,
        private val neutral2Field: java.lang.reflect.Field,
        private val accent1Field: java.lang.reflect.Field,
        private val accent2Field: java.lang.reflect.Field
    ) {
        @Suppress("UNCHECKED_CAST")
        fun palette(colors: WallpaperColors): MonetPalette? = runCatching {
            val scheme = if (constructor.parameterCount == 3) {
                constructor.newInstance(colors, true, styleContent)
            } else {
                constructor.newInstance(colors, styleContent)
            }
            MonetPalette(
                allShadesField.get(neutral1Field.get(scheme)) as List<Int>,
                allShadesField.get(neutral2Field.get(scheme)) as List<Int>,
                allShadesField.get(accent1Field.get(scheme)) as List<Int>,
                allShadesField.get(accent2Field.get(scheme)) as List<Int>
            )
        }.getOrNull()

        companion object {
            fun create(classLoader: ClassLoader): MonetApi {
                val schemeClass = classLoader.loadClass("com.android.systemui.monet.ColorScheme")
                val paletteClass = classLoader.loadClass("com.android.systemui.monet.TonalPalette")
                val styleClass = classLoader.loadClass("com.android.systemui.monet.Style")
                val constructor = schemeClass.declaredConstructors.singleOrNull { it.parameterCount == 3 }
                    ?: schemeClass.declaredConstructors.single { it.parameterCount == 2 }
                constructor.isAccessible = true
                val valueOf = styleClass.getDeclaredMethod("valueOf", String::class.java).apply {
                    isAccessible = true
                }
                return MonetApi(
                    constructor,
                    valueOf.invoke(null, "CONTENT") ?: error("Monet CONTENT style is unavailable"),
                    paletteClass.requiredField("allShades"),
                    schemeClass.requiredField("mNeutral1", "neutral1"),
                    schemeClass.requiredField("mNeutral2", "neutral2"),
                    schemeClass.requiredField("mAccent1", "accent1"),
                    schemeClass.requiredField("mAccent2", "accent2")
                )
            }

            private fun Class<*>.requiredField(vararg names: String): java.lang.reflect.Field {
                names.forEach { name ->
                    runCatching { getDeclaredField(name) }.getOrNull()?.let {
                        it.isAccessible = true
                        return it
                    }
                }
                error("Missing field ${names.joinToString()} in $name")
            }
        }
    }
}
