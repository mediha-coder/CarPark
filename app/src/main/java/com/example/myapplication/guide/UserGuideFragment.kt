package com.example.myapplication.guide

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.myapplication.R

class UserGuideFragment : Fragment() {

    private lateinit var contentOutdoor: TextView
    private lateinit var contentIndoor: TextView
    private lateinit var contentUnderground: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_guide, container, false)

        val titleOutdoor = view.findViewById<TextView>(R.id.titleOutdoor)
        val titleIndoor = view.findViewById<TextView>(R.id.titleIndoor)
        val titleUnderground = view.findViewById<TextView>(R.id.titleUnderground)

        contentOutdoor = view.findViewById(R.id.contentOutdoor)
        contentIndoor = view.findViewById(R.id.contentIndoor)
        contentUnderground = view.findViewById(R.id.contentUnderground)

        // Fonction pour toggle la visibilité et fermer les autres
        fun toggleSection(selectedContent: TextView) {
            val sections = listOf(contentOutdoor, contentIndoor, contentUnderground)
            sections.forEach {
                if (it == selectedContent) {
                    it.visibility = if (it.visibility == View.GONE) View.VISIBLE else View.GONE
                } else {
                    it.visibility = View.GONE
                }
            }
        }

        titleOutdoor.setOnClickListener { toggleSection(contentOutdoor) }
        titleIndoor.setOnClickListener { toggleSection(contentIndoor) }
        titleUnderground.setOnClickListener { toggleSection(contentUnderground) }

        return view
    }
}

