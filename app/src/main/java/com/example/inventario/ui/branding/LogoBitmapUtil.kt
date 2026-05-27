package com.example.inventario.ui.branding

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import com.example.inventario.R

object LogoBitmapUtil {

    fun decodeLogoRaw(context: Context): Bitmap? =
        BitmapFactory.decodeResource(context.resources, R.drawable.logo_inventario_agricola)

    fun decodeLogoTransparent(context: Context): Bitmap? {
        BitmapFactory.decodeResource(context.resources, R.drawable.logo_inventario_transparent)?.let {
            return it
        }
        return stripBlackBackground(decodeLogoRaw(context))
    }

    private fun stripBlackBackground(src: Bitmap?): Bitmap? {
        if (src == null) return null
        val w = src.width
        val h = src.height
        val pixels = IntArray(w * h)
        src.getPixels(pixels, 0, w, 0, 0, w, h)

        fun isBackground(px: Int): Boolean {
            val r = Color.red(px)
            val g = Color.green(px)
            val b = Color.blue(px)
            val a = Color.alpha(px)
            return a > 180 && r < 35 && g < 35 && b < 35
        }

        val visited = BooleanArray(w * h)
        val queue = ArrayDeque<Int>()

        fun enqueue(x: Int, y: Int) {
            if (x !in 0 until w || y !in 0 until h) return
            val idx = y * w + x
            if (visited[idx] || !isBackground(pixels[idx])) return
            visited[idx] = true
            queue.add(idx)
        }

        for (x in 0 until w) {
            enqueue(x, 0)
            enqueue(x, h - 1)
        }
        for (y in 0 until h) {
            enqueue(0, y)
            enqueue(w - 1, y)
        }

        while (queue.isNotEmpty()) {
            val idx = queue.removeFirst()
            val x = idx % w
            val y = idx / w
            enqueue(x - 1, y)
            enqueue(x + 1, y)
            enqueue(x, y - 1)
            enqueue(x, y + 1)
        }

        val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        for (i in pixels.indices) {
            val x = i % w
            val y = i / w
            out.setPixel(x, y, if (visited[i]) Color.TRANSPARENT else pixels[i])
        }
        if (src != out) src.recycle()
        return out
    }

    fun decodeLogoForExport(context: Context, targetWidthPx: Int): Bitmap? {
        val src = decodeLogoTransparent(context) ?: return null
        if (targetWidthPx <= 0) return src
        val aspect = src.height.toFloat() / src.width.coerceAtLeast(1)
        val w = targetWidthPx
        val h = (w * aspect).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(src, w, h, true)
    }

    fun logoAspectRatio(context: Context): Float {
        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeResource(context.resources, R.drawable.logo_inventario_transparent, opts)
        if (opts.outWidth > 0 && opts.outHeight > 0) {
            return opts.outWidth.toFloat() / opts.outHeight
        }
        val src = decodeLogoRaw(context) ?: return 1f
        return src.width.toFloat() / src.height.coerceAtLeast(1)
    }
}
