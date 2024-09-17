package com.example.whatsupsend

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import kotlinx.coroutines.NonDisposableHandle.parent

class RecyclerData(text: String, colorRes: Int) {


    private var text: String = text
    private var color: Int = colorRes

    fun getText(): String {
        return text
    }

    fun getColor(): Int {
        return color
    }

    fun setText(text: String?) {
        this.text = text.toString()
    }

    fun setColor(colorRes: Int) {
        this.color = colorRes
    }

    fun RecyclerData(text: String, color: Int) {
        this.text = text
        this.color = color
    }
}