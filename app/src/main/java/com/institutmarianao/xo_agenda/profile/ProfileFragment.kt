package com.institutmarianao.xo_agenda

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private lateinit var nameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var phoneTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Referencias a las vistas del layout
        nameTextView = view.findViewById(R.id.editname)
        emailTextView = view.findViewById(R.id.editemail)
        phoneTextView = view.findViewById(R.id.editphone)

        // Botón para abrir el menú lateral
        val btnOpenMenu = view.findViewById<ImageView>(R.id.btnOpenMenu)
        btnOpenMenu.setOnClickListener {
            (activity as? MenuActivity)?.openDrawer()
        }

        // Botón para editar el perfil (navega a otro fragmento, por ejemplo, EditProfileFragment)
        val btnEdit = view.findViewById<ImageView>(R.id.ivEdit)
        btnEdit.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.container_fragment, EditProfileFragment())
                .addToBackStack(null)
                .commit()
        }

        // Lógica para consultar datos del usuario en Firestore
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val uid = currentUser.uid
            Log.d("ProfileFragment", "UID del usuario autenticado: $uid")
            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("users").document(uid)

            userRef.get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        Log.d("ProfileFragment", "Datos obtenidos: ${document.data}")
                        // Ajusta los nombres de campo según lo que tengas en Firestore:
                        val name = document.getString("name") ?: "Nombre no disponible"
                        val email = document.getString("email") ?: "Email no disponible"
                        // Cambia "phone" por "address" si es el nombre del campo que usas en Firestore
                        val phone = document.getString("phone") ?: "Teléfono no disponible"

                        nameTextView.text = name
                        emailTextView.text = email
                        phoneTextView.text = phone
                    } else {
                        Log.d("ProfileFragment", "No se encontró documento para el UID: $uid")
                        Toast.makeText(context, "No se encontraron datos de perfil", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("ProfileFragment", "Error al obtener datos: ", e)
                    Toast.makeText(context, "Error al obtener datos: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(context, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
        }

        return view
    }
}
