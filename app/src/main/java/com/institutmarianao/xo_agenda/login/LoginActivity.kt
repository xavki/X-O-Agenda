package com.institutmarianao.xo_agenda.login

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.institutmarianao.xo_agenda.MainActivity
import com.institutmarianao.xo_agenda.MenuActivity
import com.institutmarianao.xo_agenda.R
import com.institutmarianao.xo_agenda.profile.ProfileActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var emailEditText: EditText
   // private lateinit var passwordEditText: EditText
    private lateinit var btnSingIn: Button
    private lateinit var btnSingUp: Button
    private lateinit var btnGoogle: Button
    private lateinit var cbRemember: CheckBox
    private lateinit var txtNoPass: TextView
    private lateinit var imgEye: ImageView
    private var isEyeOpen = false
    private lateinit var editTextPassword: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inicializar Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Referencias a la UI


        // CONFIGURACIÓN BOTONES:
        btnSingIn = findViewById(R.id.btnSingIn)
        btnSingUp = findViewById(R.id.btnSingUp)
        btnGoogle = findViewById(R.id.btnGoogle)
        cbRemember = findViewById(R.id.cbRemember)
        txtNoPass = findViewById(R.id.txtNoPass)
        imgEye = findViewById(R.id.imgEye)
        emailEditText = findViewById(R.id.loginemail)  // Inicialización
        //passwordEditText = findViewById(R.id.loginpassword)  // Inicializació
        editTextPassword = findViewById(R.id.loginpassword)  // Inicializació



        btnSingUp.setOnClickListener {
            setContentView(R.layout.activity_sign_up)
        }

        btnGoogle.setOnClickListener {
            /* FALTA POR IMPLEMENTAR*/
        }

        txtNoPass.setOnClickListener {
            setContentView(R.layout.activity_recover_pass)
        }
        cbRemember.setOnCheckedChangeListener { _, isChecked ->
            val prefs = getSharedPreferences("MY_APP_PREFS", MODE_PRIVATE)
            // Guardamos el valor booleano "REMEMBER_ME" para indicar si el usuario quiere que se recuerde su sesión
            prefs.edit().putBoolean("REMEMBER_ME", isChecked).apply()
        }

        imgEye.setOnClickListener {
            if (isEyeOpen) {
                imgEye.setImageResource(R.drawable.ic_eye)
                editTextPassword.transformationMethod = PasswordTransformationMethod.getInstance()  // Ocultar contraseña
            } else {
                imgEye.setImageResource(R.drawable.ic_crossed_eye)
                editTextPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()  // Mostrar contraseña
            }
            isEyeOpen = !isEyeOpen

            editTextPassword.setSelection(editTextPassword.text.length)

        }


        btnSingIn.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = editTextPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            } else {
                loginUser(email, password)
            }
        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Login exitoso
                    val intent = Intent(this, MenuActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    // Aquí muestra el error que obtienes
                    Toast.makeText(this, "Error al iniciar sesión: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }

    }

   override fun onStart() {
        super.onStart()
       // Accedemos a las mismas SharedPreferences para leer la configuración guardada
       val prefs = getSharedPreferences("MY_APP_PREFS", MODE_PRIVATE)
       // Obtenemos el valor booleano de "REMEMBER_ME"; si no existe, se toma 'false' como valor predeterminado
       val rememberMe = prefs.getBoolean("REMEMBER_ME", false)

       // Si el usuario no ha marcado "Recordarme", cerramos sesión.
       if (!rememberMe) {
           auth.signOut()
       }

       // Si ya hay un usuario autenticado (y se recuerda la sesión), redirige a la actividad deseada
       val currentUser = auth.currentUser
       if (currentUser != null) {
           val intent = Intent(this, MenuActivity::class.java)
           intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
           startActivity(intent)
           finish()
       }
    }
}
