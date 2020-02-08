package com.example.wheresthepothole

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.view.get
import androidx.recyclerview.widget.RecyclerView

class ProfileAdapter(private val myDataset: MutableList<String>) :
    RecyclerView.Adapter<ProfileAdapter.MyViewHolder>() {



     inner class MyViewHolder(cardView: CardView) : RecyclerView.ViewHolder(cardView){

        var textView : TextView = cardView.findViewById(R.id.my_text_view)

     }


    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): MyViewHolder {
        val cardView = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_report_details, parent, false) as CardView



        return MyViewHolder(cardView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        holder.textView.text = myDataset[position]
    }

    override fun getItemCount() = myDataset.size
}