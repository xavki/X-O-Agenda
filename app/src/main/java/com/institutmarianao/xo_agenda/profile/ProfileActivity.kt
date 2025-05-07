package com.institutmarianao.xo_agenda.profile

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.institutmarianao.xo_agenda.R

class ProfileActivity : AppCompatActivity() {

    // Declaración de las vistas donde se mostrarán los datos del usuario
    private lateinit var nameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var phoneTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Establece el layout de la actividad
        setContentView(R.layout.fragment_profile)

        // Inicializa las referencias a las vistas
        nameTextView = findViewById(R.id.editname)
        emailTextView = findViewById(R.id.editemail)
        phoneTextView = findViewById(R.id.editphone)

        // 1. Obtén la instancia de FirebaseAuth y el usuario actual
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            // 2. Obtén el UID del usuario actual
            val uid = currentUser.uid

            // 3. Obtén la referencia al documento del usuario en la colección "users" de Firestore
            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("users").document(uid)

            // 4. Realiza la consulta para obtener los datos del perfil
            userRef.get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        // El documento existe, extrae los datos
                        val name = document.getString("name") ?: "Nombre no disponible"
                        val email = document.getString("email") ?: "Email no disponible"
                        val phone = document.getString("phone") ?: "Telefono no disponible"

                        // 5. Actualiza la UI con los datos obtenidos
                        nameTextView.text = name
                        emailTextView.text = email
                        phoneTextView.text = phone
                    } else {
                        // El documento no existe: notifica al usuario
                        Toast.makeText(this, "No se encontraron datos de perfil", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    // Maneja el error al obtener el documento
                    Toast.makeText(this, "Error al obtener datos: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            // No hay usuario autenticado: notifica y redirige según tu flujo
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            // Aquí podrías redirigir a la pantalla de inicio de sesión o cerrar la actividad
        }
    }
}
