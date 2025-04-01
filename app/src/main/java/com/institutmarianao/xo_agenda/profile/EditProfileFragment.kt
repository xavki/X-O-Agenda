package com.institutmarianao.xo_agenda

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment

class EditProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_editprofile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Aquí puedes abrir el menú lateral si quieres:
        val btnOpenMenu = view.findViewById<ImageView>(R.id.btnOpenMenu)
        btnOpenMenu?.setOnClickListener {
            (activity as? MenuActivity)?.openDrawer()
        }
    }
}
