package com.institutmarianao.xo_agenda.login

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.institutmarianao.xo_agenda.R


class LoginActivity : AppCompatActivity() {

    /* private lateinit var auth: FirebaseAuth
    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val data = result.data
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)

        try {
            val account = task.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            Toast.makeText(this, "Error al iniciar sesión", Toast.LENGTH_SHORT).show()
        }
    }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        /* auth = FirebaseAuth.getInstance()

        val googleSignInButton: Button = findViewById(R.id.btnGoogleSignIn)
        googleSignInButton.setOnClickListener { signInWithGoogle() }
    }

    private fun signInWithGoogle() {
        val googleSignInClient = GoogleSignIn.getClient(
            this,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("484185689718-37gjku0om1lg1kbp38oib9jqml1mj6gn.apps.googleusercontent.com")  // ✅ Usa el correcto
                .requestEmail()
                .build()
        )

        googleSignInLauncher.launch(googleSignInClient.signInIntent)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                } else {
                    Toast.makeText(this, "Error en la autenticación", Toast.LENGTH_SHORT).show()
                }
            }
    }*/


    }
}

