package com.institutmarianao.xo_agenda

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Botón para abrir el menú lateral
        val btnOpenMenu = view.findViewById<ImageView>(R.id.btnOpenMenu)

        btnOpenMenu.setOnClickListener {
            // Llama al método público de la actividad para abrir el drawer
            (activity as? MenuActivity)?.openDrawer()
        }
        val btnEdit = view.findViewById<ImageView>(R.id.ivEdit)

        btnEdit.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.container_fragment, EditProfileFragment())
                .addToBackStack(null)
                .commit()
        }


        return view
    }
}
