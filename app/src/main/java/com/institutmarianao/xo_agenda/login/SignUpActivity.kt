package com.institutmarianao.xo_agenda.login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.institutmarianao.xo_agenda.R

class SignUpActivity : AppCompatActivity() {
    private lateinit var txtBackLogin: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var nombre: EditText
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var confirmpassword: EditText
    private lateinit var iniciarsession: Button
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        txtBackLogin = findViewById(R.id.txtBackLogin)
        nombre = findViewById(R.id.anadirnombre)
        email = findViewById(R.id.anadirmail)
        password = findViewById(R.id.anadirpassword)
        confirmpassword = findViewById(R.id.anadirpaswordconfim)
        iniciarsession = findViewById(R.id.btnSingIn)

        txtBackLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        iniciarsession.setOnClickListener {
            val email = email.text.toString().trim()
            val password = password.text.toString().trim()
            val name = nombre.text.toString().trim()  // Obtener el nombre

            if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            } else {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Registro OK → vamos a la base de datos Firestore
                            val user = auth.currentUser
                            val uid = user?.uid ?: return@addOnCompleteListener

                            // Crear un mapa con los datos del usuario
                            val userData = hashMapOf(
                                "nom" to name,
                                "email" to email
                            )

                            // Guardar los datos en Firestore
                            db.collection("usuarios")
                                .document(uid)
                                .set(userData)
                                .addOnSuccessListener {
                                    // Registro y datos guardados correctamente
                                    val intent = Intent(this, LoginActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    // Manejo de error al guardar los datos en Firestore
                                    Toast.makeText(this, "Error al guardar los datos: ${e.message}", Toast.LENGTH_LONG).show()
                                }

                        } else {
                            // Error al registrar usuario
                            Toast.makeText(
                                this,
                                "Error al registrarse: ${task.exception?.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
            }
        }
    }
}
