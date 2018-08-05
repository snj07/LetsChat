package com.snj.letschat.utils

import android.graphics.*
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool


class ImageUtils {

    protected fun transform(pool: BitmapPool, toTransform: Bitmap?, outWidth: Int, outHeight: Int): Bitmap? {
        if (toTransform == null) return null

        val size = Math.min(toTransform.width, toTransform.height)
        val x = (toTransform.width - size) / 2
        val y = (toTransform.height - size) / 2

        // TODO this could be acquired from the pool too
        val squared = Bitmap.createBitmap(toTransform, x, y, size, size)

        var result = pool.get(size, size, Bitmap.Config.ARGB_8888)
        if (result == null) {
            result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        }

        val canvas = Canvas(result)
        val paint = Paint()
        paint.shader = BitmapShader(squared, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        paint.isAntiAlias = true
        val r = size / 2f
        canvas.drawCircle(r, r, r, paint)
        return result
    }
}