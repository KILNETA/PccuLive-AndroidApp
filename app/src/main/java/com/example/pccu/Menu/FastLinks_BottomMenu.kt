package com.example.pccu.Menu
import com.example.pccu.R
import android.os.Bundle

import com.google.android.material.bottomsheet.BottomSheetDialogFragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.content.Intent
import android.net.Uri
import android.widget.LinearLayout

class BottomSheetFragment : BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fast_links, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val url:List<String> = listOf(
            "https://icas.pccu.edu.tw/cfp/#my",
            "https://ecampus.pccu.edu.tw/ecampus/default.aspx?usertype=student",
            "https://www.pccu.edu.tw/",
            "https://mycourse.pccu.edu.tw/",
            "https://www.pccu.edu.tw/fever/fever.html"
        )
        var uri: Uri

        view.findViewById<LinearLayout>(R.id.FastLink_Course_assistance_system)
            .setOnClickListener{
                uri = Uri.parse(url[0])
                startActivity(Intent(Intent.ACTION_VIEW, uri!!))
            }
        view.findViewById<LinearLayout>(R.id.FastLink_Student_area)
            .setOnClickListener{
                uri = Uri.parse(url[1])
                startActivity(Intent(Intent.ACTION_VIEW, uri!!))
            }
        view.findViewById<LinearLayout>(R.id.FastLink_Pccu_page)
            .setOnClickListener{
                uri = Uri.parse(url[2])
                startActivity(Intent(Intent.ACTION_VIEW, uri!!))
            }
        view.findViewById<LinearLayout>(R.id.FastLink_Course_selection)
            .setOnClickListener{
                uri = Uri.parse(url[3])
                startActivity(Intent(Intent.ACTION_VIEW, uri!!))
            }
        view.findViewById<LinearLayout>(R.id.FastLink_Epidemic_Prevention_Zone)
            .setOnClickListener{
                uri = Uri.parse(url[4])
                startActivity(Intent(Intent.ACTION_VIEW, uri!!))
            }
    }
}