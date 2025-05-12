package com.institutmarianao.xo_agenda.login

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.Button
import android.widget.CheckBox
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
    private lateinit var termino: CheckBox

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
        termino = findViewById(R.id.terminos)

        // Crear un SpannableString para que los términos sean clickeables
        val termsText = "I agree to the Terms of Service and Privacy Policy."
        val spannableString = SpannableString(termsText)

        // Establecer los enlaces en los textos de Términos y Política de privacidad
        val termsStart = termsText.indexOf("Terms of Service")
        val termsEnd = termsText.indexOf("Privacy Policy") + "Privacy Policy".length
        val privacyStart = termsText.indexOf("Privacy Policy")
        val privacyEnd = termsText.length
        // Crear el enlace para los Términos de Servicio
                spannableString.setSpan(object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://www.tusitio.com/terminos")
                        )
                        startActivity(intent)
                    }
                }, termsStart, termsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        // Crear el enlace para la Política de Privacidad
        spannableString.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent =
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://www.tusitio.com/privacidad"))
                startActivity(intent)
            }
        }, privacyStart, privacyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        // Asegurarse de que el texto sea clickeable
        termino.text = spannableString
        termino.movementMethod = LinkMovementMethod.getInstance()


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
            confirmpassword.error = null


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
            } else if (passwordText.length < 6) {
                password.error = "La contraseña debe tener al menos 6 caracteres,"
                password.requestFocus()
                isValid = false
            }

            if (confirmPasswordText.isEmpty()) {
                confirmpassword.error = "Introduce tu contraseña"
                confirmpassword.requestFocus()
                isValid = false
            } else if (confirmPasswordText != passwordText) {
                confirmpassword.error = "Las contraseñas tienen que ser iguales"
                confirmpassword.requestFocus()
                password.error = "Las contraseñas tienen que ser iguales"
                password.requestFocus()
                isValid = false
            }


            if (!isValid) return@setOnClickListener

            auth.createUserWithEmailAndPassword(emailText, passwordText)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        val uid = user?.uid ?: return@addOnCompleteListener

                        // Enviar correo de verificación
                        user.sendEmailVerification()
                            .addOnCompleteListener { verifyTask ->
                                if (verifyTask.isSuccessful) {
                                    Toast.makeText(
                                        this,
                                        "Registro exitoso. Revisa tu correo para verificar tu cuenta.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        this,
                                        "Error al enviar el correo de verificación.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                        // Guardar datos en Firestore
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
