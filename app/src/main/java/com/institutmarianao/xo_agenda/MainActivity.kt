package com.institutmarianao.xo_agenda

import android.annotation.SuppressLint
import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.text.InputType
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.institutmarianao.xo_agenda.login.LoginActivity


class MainActivity : AppCompatActivity() {

    private lateinit var btnSingIn: Button
    private lateinit var btnSingUp: Button
    private lateinit var btnGoogle: Button
    private lateinit var cbRemember: CheckBox
    private lateinit var txtNoPass: TextView
    private lateinit var imgEye: ImageView
    private var isEyeOpen = false
    private lateinit var editTextPassword: EditText

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_profile)

        // val db = Firebase.firestore
        //val db = FirebaseFirestore.getInstance()

       /* // CONFIGURACIÓN BOTONES:
        btnSingIn = findViewById(R.id.btnSingIn)
        btnSingUp = findViewById(R.id.btnSingUp)
        btnGoogle = findViewById(R.id.btnGoogle)
        cbRemember = findViewById(R.id.cbRemember)
        txtNoPass = findViewById(R.id.txtNoPass)

        imgEye = findViewById(R.id.imgEye)
        imgEye.setImageResource(R.drawable.ic_eye)
        editTextPassword = findViewById(R.id.loginpassword)


        btnSingIn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // opcional, para cerrar la pantalla de login
        }

        btnSingUp.setOnClickListener {
            setContentView(R.layout.activity_sign_up)
        }

        btnGoogle.setOnClickListener {
            /* FALTA POR IMPLEMENTAR*/
        }

        txtNoPass.setOnClickListener {
            setContentView(R.layout.activity_recover_pass)
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

        }*/
    }


}