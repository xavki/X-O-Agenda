package com.institutmarianao.xo_agenda.login

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.institutmarianao.xo_agenda.R
import javax.microedition.khronos.egl.EGLDisplay


class SignUpActivity : AppCompatActivity() {
    private lateinit var txtBackLogin: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var nombre: EditText
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var confirmpassword: EditText
    private lateinit var iniciarsession: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        auth = FirebaseAuth.getInstance()


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
            val email    = email.text.toString().trim()
            val password = password.text.toString().trim()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            } else {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Registro OK → vamos al menú
                            val intent = Intent(this, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
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


}