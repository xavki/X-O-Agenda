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
            val name = nombre.text.toString().trim()
            val emailText = email.text.toString().trim()
            val passwordText = password.text.toString().trim()
            val confirmPasswordText = confirmpassword.text.toString().trim()

            var isValid = true
            nombre.error = null
            email.error = null
            password.error = null
           // confirm.error = null


            if (name.isEmpty()) {
                nombre.error = "Introduce tu nombre"
                nombre.requestFocus()
                isValid = false
            }

            if (emailText.isEmpty()) {
                email.error = "Introduce tu email"
                email.requestFocus()
                isValid = false
            }
            if (passwordText.isEmpty()) {
                password.error = "Introduce tu contraseña"
                password.requestFocus()
                isValid = false
            }
            /*if (confirmPasswordText.isEmpty()) {
                confirm.error = "Introduce tu nombre"
                confirm.requestFocus()
                isValid = false
            }*/



            if (passwordText != confirmPasswordText) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (passwordText.length < 6) {
                Toast.makeText(
                    this,
                    "La contraseña debe tener al menos 6 caracteres",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            if (!isValid) return@setOnClickListener

            auth.createUserWithEmailAndPassword(emailText, passwordText)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        val uid = user?.uid ?: return@addOnCompleteListener

                        val userData = hashMapOf(
                            "nom" to name,
                            "email" to emailText
                        )

                        db.collection("usuarios")
                            .document(uid)
                            .set(userData)
                            .addOnSuccessListener {
                                val intent = Intent(this, LoginActivity::class.java)
                                intent.flags =
                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    this,
                                    "Error al guardar los datos: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                    } else {
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
