package com.dragon.tribe.fire.oc.maker.core.helper

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.graphics.createBitmap

object UnitHelper {
    fun spToPx(spVal: Float): Int {
        val r = Resources.getSystem()
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spVal, r.displayMetrics).toInt()
    }

    fun Context.showKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager?
        inputMethodManager!!.showSoftInput(
            view,
            InputMethodManager.SHOW_IMPLICIT
        )
    }
    fun viewToBitmap(view: View): Bitmap {
        val bitmap = createBitmap(view.width, view.height)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }
    fun pxToDpInt(context: Context, px: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, px.toFloat(), context.resources.displayMetrics).toInt()
    }

    fun pxToDpFloat(context: Context, px: Int): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, px.toFloat(), context.resources.displayMetrics).toFloat()
    }

    fun dpToPx(context: Context, dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }
    fun dpToPx(context: Context, dp: Float): Float {
        return (dp * context.resources.displayMetrics.density).toFloat()
    }
    @SuppressLint("DefaultLocale")
    fun formatFileSize(sizeInBytes: Long): String {
        return when {
            sizeInBytes < 1024 -> "$sizeInBytes Bytes"
            sizeInBytes < 1024 * 1024 -> String.format("%.1f KB", sizeInBytes / 1024.0)
            sizeInBytes < 1024 * 1024 * 1024 -> String.format("%.1f MB", sizeInBytes / (1024.0 * 1024))
            else -> String.format("%.1f GB", sizeInBytes / (1024.0 * 1024 * 1024))
        }
    }

}

