package com.example.pccu.Page

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pccu.R
import kotlinx.android.synthetic.main.announcement_item.view.*
import kotlinx.android.synthetic.main.announcement_page.*

class  Announcement_Page : Fragment(R.layout.announcement_page){

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        announcement_list.layoutManager = LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false)
        announcement_list.adapter = Adapter()
    }
    inner class  Adapter:RecyclerView.Adapter<MyViewHolder>(){
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val view =
                LayoutInflater.from(context).inflate(R.layout.announcement_item,parent,false)
            return MyViewHolder(view)
        }

        override fun getItemCount(): Int {
            return 20
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            holder.itemView.item_announcement_unit.text = "學務處"
            holder.itemView.item_announcement_title.text = "1102學年-弱勢助學金資訊"
        }

    }

    inner class MyViewHolder(view: View): RecyclerView.ViewHolder(view){

    }
}