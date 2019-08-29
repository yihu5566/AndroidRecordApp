package com.recordapp.record

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

import java.util.ArrayList

class MyRecyclerAdapter(private val context: Context, private val arrayList: ArrayList<String>?) :
    RecyclerView.Adapter<MyRecyclerAdapter.MyViewHolder>() {

    internal var listener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflate = LayoutInflater.from(context).inflate(R.layout.item_path, null)
        return MyViewHolder(inflate)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.tvPath.text = arrayList!![position]
        holder.tvPath.setOnClickListener { listeners ->
            if (listener != null) {
                listener!!.onItemClick(arrayList[position])
            }
        }

    }

    override fun getItemCount(): Int {
        return arrayList?.size ?: 0
    }

    internal inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvPath: TextView

        init {
            tvPath = itemView.findViewById(R.id.tv_path)
        }
    }

    fun setListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    internal interface OnItemClickListener {
        fun onItemClick(path: String)
    }
}
