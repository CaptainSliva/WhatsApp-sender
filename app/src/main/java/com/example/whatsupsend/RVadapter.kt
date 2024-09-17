package com.example.whatsupsend

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.core.content.ContextCompat.getString
import androidx.recyclerview.widget.RecyclerView
import java.security.AccessController.getContext


class RVadapter(
    private val context: Context,
    private val recyclerDataArrayList: MutableList<RecyclerData>
) :
    RecyclerView.Adapter<RVadapter.RecyclerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        // Inflate Layout
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.text_card, parent, false)
        return RecyclerViewHolder(view, context, MyCustomEditTextListener())
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        // Set the data to textview and imageview.
        val item = recyclerDataArrayList[position]
        holder.myCustomEditTextListener.updatePosition(position)
        holder.text.setText(item.getText())
        holder.container.setBackgroundColor(holder.view.context.getColor(item.getColor()!!))

        Log.d("print","${item.getText()}")


    }

    override fun getItemCount(): Int {
        // this method returns the size of recyclerview
        return recyclerDataArrayList.size
    }

    // View Holder Class to handle Recycler View.
    inner class RecyclerViewHolder(itemView: View, context: Context, myCustomEditTextListener: MyCustomEditTextListener) : RecyclerView.ViewHolder(itemView), OnClickListener{
        internal val text = itemView.findViewById<EditText>(R.id.text)
        internal val container = itemView.findViewById<RelativeLayout>(R.id.container)
        internal val view = itemView.findViewById<View>(R.id.card)
        internal val myCustomEditTextListener = myCustomEditTextListener
        internal val popup = itemView.findViewById<ImageView>(R.id.action)

        init {

            this.text.addTextChangedListener(myCustomEditTextListener)
            itemView.alpha = 0.5F

            popup.setOnClickListener {
                val popupMenu = PopupMenu(context, popup)
                popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)
                popupMenu.javaClass.getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                    .invoke(popupMenu,true)

                popupMenu.setOnMenuItemClickListener { menuItem ->
                    if (menuItem.title == getString(context, R.string.choose)) {
                        chooseItem(position)
                        adapter.notifyDataSetChanged()
                    }
                    if (menuItem.title == getString(context, R.string.delete)) {
                        changeColor(position)
                        data.removeAt(position)
                        adapter.notifyDataSetChanged()
                    }
                    true
                }
                popupMenu.show()
            }
        }
        override fun onClick(p0: View?) {
            Log.d("Ck", p0.toString())
        }

    }

    class MyCustomEditTextListener : TextWatcher {
        private var position = 0

        fun updatePosition(position: Int) {
            this.position = position
        }

        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i2: Int, i3: Int) {
            // no op
        }

        override fun onTextChanged(charSequence: CharSequence, i: Int, i2: Int, i3: Int) {
            data[position].setText(charSequence.toString())
        }

        override fun afterTextChanged(editable: Editable) {
            // no op
        }
    }


}