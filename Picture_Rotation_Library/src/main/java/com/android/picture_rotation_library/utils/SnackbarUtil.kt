@file:Suppress("SameParameterValue")

package com.kredily.web.utils

import android.app.Activity
import android.graphics.Color
import android.view.View
import android.widget.FrameLayout
import com.google.android.material.snackbar.Snackbar

object SnackbarUtil {
    
    private const val SIDE_MARGIN = 16
    private const val BOTTOM_MARGIN = 100
    private const val SUCCESS_BACKGROUND = "#F2F3F9"
    private const val SUCCESS_TEXT = "#4A5BC0"
    private const val SNACK_LENGTH = 8000
    
    @JvmStatic
    fun showSnackbar(activity: Activity?, message: String?) {
        if(null != activity && null != message) {
            showSnackbar(activity.findViewById(android.R.id.content) as View, message, SNACK_LENGTH)
        }
    }
    
    @JvmStatic
    private fun showSnackbar(view : View, message : String, length : Int) {
        val snack = Snackbar.make(view, message, length)
        val snackbarView = snack.view
        val params = snackbarView.layoutParams as FrameLayout.LayoutParams
        params.setMargins(params.leftMargin + SIDE_MARGIN,
            params.topMargin,
            params.rightMargin + SIDE_MARGIN,
            params.bottomMargin + BOTTOM_MARGIN)
        snackbarView.setBackgroundColor(Color.parseColor(SUCCESS_BACKGROUND))
        snack.setActionTextColor(Color.parseColor(SUCCESS_TEXT))
        snack.show()
    }
    
    @JvmStatic
    fun showSnackbarWithSingleAction(activity : Activity?, message : String?, length : Int, action : String, actionTextColor : Int, callback : SnackbarCallback?) {
        if(null != activity && null != message) {
            showSnackbarWithSingleAction(activity.findViewById(android.R.id.content) as View, message, length, action, actionTextColor, callback)
        }
    }
    
    @JvmStatic
    private fun showSnackbarWithSingleAction(view : View, message : String, length : Int, action : String, actionTextColor : Int, callback : SnackbarCallback?) {
        val snack = Snackbar.make(view, message, length)
        snack.setAction(action) {
            snack.dismiss()
            callback?.onDismissClicked()
        }
        snack.setActionTextColor(actionTextColor)
        val snackbarView = snack.view
        val params = snackbarView.layoutParams as FrameLayout.LayoutParams
        params.setMargins(params.leftMargin + SIDE_MARGIN,
            params.topMargin,
            params.rightMargin + SIDE_MARGIN,
            params.bottomMargin + BOTTOM_MARGIN)
        snackbarView.setBackgroundColor(Color.parseColor(SUCCESS_BACKGROUND))
        snack.setActionTextColor(Color.parseColor(SUCCESS_TEXT))
        snack.show()
    }
}