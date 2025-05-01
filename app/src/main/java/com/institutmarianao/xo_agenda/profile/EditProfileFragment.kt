package com.institutmarianao.xo_agenda

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
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
        SendResetEmail = view.findViewById(R.id.btnSendResetEmail)
        Confirm = view.findViewById(R.id.btnConfirm)


        loadProfile()

        Confirm.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser
            user?.providerData?.forEach { profile ->
                Log.d("AUTH_PROVIDER", "→ ${profile.providerId}")
            }
            if (user == null) {
                Toast.makeText(requireContext(), "Usuario no autenticado", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            // Capturamos los datos introducidos por el usuario
            val uid = user.uid
            val name = nameEdit.text.toString().trim()
            val newEmail = emailEdit.text.toString().trim()
            val phone = phoneEdit.text.toString().trim()

            // Datos actuales desde FirebaseAuth
            val currentEmail = user.email ?: ""

            if (name.isEmpty() || newEmail.isEmpty() || phone.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Por favor completa todos los campos.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val updates = hashMapOf<String, Any>(
                "nom" to name,
                "phone" to phone
            )

            // Si el email cambió, primero reautenticamos
            if (newEmail != currentEmail) {
                // Mostramos un diálogo para pedir la contraseña
               // cambioemail(user, currentEmail, newEmail, updates, uid)
            } else {
                // Solo actualiza nombre/teléfono
                FirebaseFirestore.getInstance().collection("usuarios").document(uid)
                    .update(updates)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Perfil actualizado", Toast.LENGTH_SHORT)
                            .show()
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.container_fragment, ProfileFragment())
                            .commit()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            requireContext(),
                            "Error al actualizar: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }


        // Enviar email de restablecimiento
        SendResetEmail.setOnClickListener {
            val email = FirebaseAuth.getInstance().currentUser?.email
            if (email.isNullOrBlank()) {
                Toast.makeText(
                    requireContext(),
                    "No hay sesión iniciada", Toast.LENGTH_SHORT
                ).show()
            } else {
                FirebaseAuth.getInstance()
                    .sendPasswordResetEmail(email)
                    .addOnSuccessListener {
                        Toast.makeText(
                            requireContext(),
                            "Email enviado a $email",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            requireContext(),
                            "Error: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
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

   /* private fun cambioemail(
        user: FirebaseUser,
        currentEmail: String,
        newEmail: String,
        updates: HashMap<String, Any>,
        uid: String
    ) {
        val passwordInput = EditText(requireContext())
        passwordInput.inputType =
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

        // Diálogo para pedir la contraseña
        AlertDialog.Builder(requireContext())
            .setTitle("Reautenticación requerida")
            .setMessage("Introduce tu contraseña para cambiar el email")
            .setView(passwordInput)
            .setPositiveButton("Confirmar") { _, _ ->
                val password = passwordInput.text.toString()

                // Creamos credenciales para reautenticación
                val credential = EmailAuthProvider.getCredential(currentEmail, password)

                user.reauthenticate(credential)
                    .addOnSuccessListener {
                        // Aquí verificamos si la reautenticación fue exitosa
                        Log.d("EmailChange", "Reauthentication successful.")

                        // Enviar el correo de verificación al nuevo email
                        FirebaseAuth.getInstance().fetchSignInMethodsForEmail(newEmail)
                            .addOnSuccessListener {
                                // Enviamos verificación al nuevo correo
                                FirebaseAuth.getInstance().currentUser?.sendEmailVerification()
                                    ?.addOnSuccessListener {
                                        Toast.makeText(
                                            requireContext(),
                                            "Verificación enviada a $newEmail. Verifica antes de continuar.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        Log.d("EmailChange", "Email de validacion enviado")


                                        // Aquí podrías redirigir al usuario a otra pantalla de "verificación pendiente"
                                        // y guardar el newEmail temporalmente si lo necesitas luego.
                                    }
                                    ?.addOnFailureListener { e ->
                                        Toast.makeText(
                                            requireContext(),
                                            "Error al enviar verificación: ${e.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    requireContext(),
                                    "No se puede enviar verificación: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            .addOnFailureListener { exception ->
                                val message = exception.message ?: "Error desconocido"
                                Log.e("EmailChange", "Fallo al actualizar email", exception)
                                Toast.makeText(
                                    requireContext(),
                                    "Error al cambiar email: $message",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    }
                    .addOnFailureListener { exception ->
                        // Aquí registramos si la reautenticación falló
                        Log.e("EmailChange", "Reauthentication failed", exception)
                        Toast.makeText(
                            requireContext(),
                            "Contraseña incorrecta",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

            }
            .setNegativeButton("Cancelar", null)
            .show()
    }*/


}
