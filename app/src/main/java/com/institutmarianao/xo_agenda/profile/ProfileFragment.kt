package com.institutmarianao.xo_agenda

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private lateinit var nameEdit: EditText
    private lateinit var emailEdit: EditText
    private lateinit var phoneEdit: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Referencias a los EditText del layout
        nameEdit = view.findViewById(R.id.editname)
        emailEdit = view.findViewById(R.id.editemail)
        phoneEdit = view.findViewById(R.id.editphone)

        // Botón para abrir el menú lateral
        view.findViewById<ImageView>(R.id.btnOpenMenu).setOnClickListener {
            (activity as? MenuActivity)?.openDrawer()
        }

        // Botón para navegar a EditProfileFragment
        view.findViewById<ImageView>(R.id.ivEdit).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.container_fragment, EditProfileFragment())
                .addToBackStack(null)
                .commit()
        }

        // Carga de datos de Firestore
        loadProfile()

        return view
    }

    private fun loadProfile() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(
                requireContext(),
                "Usuario no autenticado", Toast.LENGTH_SHORT
            ).show()
            return
        }

        val uid = user.uid
        Log.d("ProfileFragment", "UID actual = $uid")

        FirebaseFirestore.getInstance()
            .collection("usuarios")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                val db = FirebaseFirestore.getInstance()
                db.collection("usuarios")
                    .get()
                    .addOnSuccessListener { snap ->
                        Log.d("DEBUG_USERS", "Encontrados ${snap.size()} docs en usuarios/")
                        for (doc in snap.documents) {
                            Log.d("DEBUG_USERS", " → ${doc.id}: ${doc.data}")
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("DEBUG_USERS", "Error listando usuarios/", e)
                    }

                if (doc != null && doc.exists()) {
                    // 1) Nombre: usa "nom"
                    val nameVal = doc.getString("nom") ?: "Nombre no disponible"
                    // 2) Email: está bien
                    val emailVal = doc.getString("email") ?: "Email no disponible"
                    // 3) Teléfono: conviértelo a String
                    val phoneValRaw = doc.get("phone")
                    val phoneVal = phoneValRaw?.toString() ?: "Teléfono no disponible"

                    // Rellenamos los EditText
                    nameEdit.setText(nameVal)
                    emailEdit.setText(emailVal)
                    phoneEdit.setText(phoneVal)
                } else {
                    Log.d("ProfileFragment", "No existe usuarios/$uid")
                    Toast.makeText(
                        requireContext(),
                        "No se encontraron datos de perfil", Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("ProfileFragment", "Error al leer perfil", e)
                Toast.makeText(
                    requireContext(),
                    "Error al obtener datos: ${e.message}", Toast.LENGTH_SHORT
                ).show()
            }
    }

}
