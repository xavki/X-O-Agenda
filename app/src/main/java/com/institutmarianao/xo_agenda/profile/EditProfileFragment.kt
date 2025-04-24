package com.institutmarianao.xo_agenda

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.institutmarianao.xo_agenda.login.RecoveryActivity
import com.institutmarianao.xo_agenda.profile.ProfileActivity

class EditProfileFragment : Fragment() {
    private lateinit var nameEdit: EditText
    private lateinit var emailEdit: EditText
    private lateinit var phoneEdit: EditText
    private lateinit var SendResetEmail: Button
    private lateinit var Confirm: Button
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

        nameEdit = view.findViewById(R.id.editname)
        emailEdit = view.findViewById(R.id.editemail)
        phoneEdit = view.findViewById(R.id.editphone)
        SendResetEmail  = view.findViewById(R.id.btnSendResetEmail)
        Confirm = view.findViewById(R.id.btnConfirm)


        loadProfile()

        Confirm.setOnClickListener {
            /* val intent = Intent(requireContext(), ProfileFragment::class.java)
             startActivity(intent)*/
        }


        // Enviar email de restablecimiento
        SendResetEmail.setOnClickListener {
            val email = FirebaseAuth.getInstance().currentUser?.email
            if (email.isNullOrBlank()) {
                Toast.makeText(requireContext(),
                    "No hay sesión iniciada", Toast.LENGTH_SHORT).show()
            } else {
                FirebaseAuth.getInstance()
                    .sendPasswordResetEmail(email)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(),
                            "Email enviado a $email",
                            Toast.LENGTH_LONG).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(),
                            "Error: ${e.message}",
                            Toast.LENGTH_SHORT).show()
                    }
            }
        }
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
                        Log.d("DEBUG_USERS", "Encontrados ${snap.size()} docs en users/")
                        for (doc in snap.documents) {
                            Log.d("DEBUG_USERS", " → ${doc.id}: ${doc.data}")
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("DEBUG_USERS", "Error listando users/", e)
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
                    Log.d("ProfileFragment", "No existe users/$uid")
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
