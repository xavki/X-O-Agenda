package com.institutmarianao.xo_agenda.login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.institutmarianao.xo_agenda.R
import com.institutmarianao.xo_agenda.login.LoginActivity

class RecoveryActivity : AppCompatActivity() {

    private lateinit var emailEt: EditText
    private lateinit var sendBtn: Button
    private lateinit var infoTxt: TextView
    private lateinit var signUp: TextView

    private val PREFS_NAME    = "recovery_prefs"
    private val KEY_LAST_SEND = "last_send_timestamp"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recover_pass)

        // 1) Vincular vistas
        emailEt = findViewById(R.id.editrecoverymail)
        sendBtn = findViewById(R.id.btnRecoverymail)
        infoTxt = findViewById(R.id.txtNoPass)
        signUp  = findViewById(R.id.txtSingUp)

        // 2) Listener “volver a login”
        signUp.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // 3) Listener enviar email
        sendBtn.setOnClickListener { trySendRecoveryEmail() }
    }

    private fun trySendRecoveryEmail() {
        val email = emailEt.text.toString().trim()
        if (email.isEmpty()) {
            Toast.makeText(this, "Introduce un email válido", Toast.LENGTH_SHORT).show()
            return
        }

        val prefs   = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val last    = prefs.getLong(KEY_LAST_SEND, 0L)
        val now     = System.currentTimeMillis()
        val fiveMin = 5 * 60 * 1000

        if (now - last < fiveMin) {
            Toast.makeText(this,
                "Debes esperar 5 minutos antes de reenviar.",
                Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseAuth.getInstance()
            .sendPasswordResetEmail(email)
            .addOnSuccessListener {
                prefs.edit().putLong(KEY_LAST_SEND, now).apply()
                Toast.makeText(this,
                    "Email de recuperación enviado a $email",
                    Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT).show()
            }
    }
}
