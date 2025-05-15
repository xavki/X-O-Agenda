package com.institutmarianao.xo_agenda.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.institutmarianao.xo_agenda.MenuActivity
import com.institutmarianao.xo_agenda.R

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var emailEditText: EditText
    private lateinit var googleSignInClient: GoogleSignInClient

    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

    // private lateinit var passwordEditText: EditText
    private lateinit var btnSingIn: Button
    private lateinit var btnSingUp: Button
    private lateinit var btnGoogle: Button
    private lateinit var cbRemember: CheckBox
    private lateinit var txtNoPass: TextView
    private lateinit var imgEye: ImageView
    private var isEyeOpen = false
    private lateinit var editTextPassword: EditText
    private lateinit var firestore: FirebaseFirestore // <--- Declarada aquí, ¡muy bien!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inicializar Firebase Auth
        auth = FirebaseAuth.getInstance()
        FirebaseApp.initializeApp(this);


        val auth = FirebaseAuth.getInstance()
// Obtén el usuario actualmente autenticado
        val currentUser: FirebaseUser? = auth.currentUser
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

        firestore = FirebaseFirestore.getInstance() // <--- ¡Añadimos esta línea!


        btnSingUp.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)


        }

        btnGoogle.setOnClickListener {
            /* FALTA POR IMPLEMENTAR*/
        }

        txtNoPass.setOnClickListener {
            val intent = Intent(this, RecoveryActivity::class.java)
            startActivity(intent)
        }
        cbRemember.setOnCheckedChangeListener { _, isChecked ->
            val prefs = getSharedPreferences("MY_APP_PREFS", MODE_PRIVATE)
            // Guardamos el valor booleano "REMEMBER_ME" para indicar si el usuario quiere que se recuerde su sesión
            prefs.edit().putBoolean("REMEMBER_ME", isChecked).apply()
        }

        imgEye.setOnClickListener {
            if (isEyeOpen) {
                imgEye.setImageResource(R.drawable.ic_eye)
                editTextPassword.transformationMethod =
                    PasswordTransformationMethod.getInstance()  // Ocultar contraseña
            } else {
                imgEye.setImageResource(R.drawable.ic_crossed_eye)
                editTextPassword.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()  // Mostrar contraseña
            }
            isEyeOpen = !isEyeOpen

            editTextPassword.setSelection(editTextPassword.text.length)

        }


        btnSingIn.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = editTextPassword.text.toString().trim()

            var isValid = true
            emailEditText.error = null
            editTextPassword.error = null

            // Validar email
            if (email.isEmpty()) {
                emailEditText.error = "Introduce tu email"
                if (isValid) {
                    emailEditText.requestFocus()
                }
                isValid = false
            }

            if (password.isEmpty()) {
                editTextPassword.error = "Introduce tu contraseña"
                if (isValid) {
                    editTextPassword.requestFocus()
                }
                isValid = false
            }

            if (isValid) {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener { authResult ->
                        val user = authResult.user
                        if (user != null && user.isEmailVerified) {
                            // Email verificado: continuar
                            Toast.makeText(this, "¡Inicio de sesión exitoso!", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, MenuActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            // Email NO verificado: reenviar email de verificación
                            user?.sendEmailVerification()
                                ?.addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(
                                            this,
                                            "Tu email no está verificado. Te hemos enviado un nuevo correo de verificación.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    } else {
                                        Toast.makeText(
                                            this,
                                            "Error al enviar el correo de verificación.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                    // Después de enviar el correo, cerramos sesión
                                    FirebaseAuth.getInstance().signOut()
                                }
                        }

                    }
                    .addOnFailureListener { e ->
                        // Errores de autenticación
                        when (e) {
                            is FirebaseAuthInvalidUserException -> {
                                Toast.makeText(this, "El usuario no existe.", Toast.LENGTH_SHORT)
                                    .show()
                            }

                            is FirebaseAuthInvalidCredentialsException -> {
                                Toast.makeText(this, "Contraseña incorrecta.", Toast.LENGTH_SHORT)
                                    .show()
                            }

                            is FirebaseAuthException -> {
                                Toast.makeText(
                                    this,
                                    "Error de autenticación: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            else -> {
                                Toast.makeText(
                                    this,
                                    "Error inesperado: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
            }
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail() // Puedes pedir el email si lo necesitas
            .requestProfile() // Puedes pedir el perfil (nombre, foto, etc.)
            .build()

        // Crea el cliente de inicio de sesión de Google
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Inicializa el Activity Result Launcher para el inicio de sesión de Google
        googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data
                val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
                try {
                    // La tarea fue exitosa, obtiene la cuenta de Google
                    val account = task.getResult(ApiException::class.java)
                    if (account != null) {
                        // Ahora autentica en Firebase con la cuenta de Google
                        firebaseAuthWithGoogle(account.idToken)
                    } else {
                        Toast.makeText(this, "Error: Cuenta de Google nula", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: ApiException) {
                    // Error en el inicio de sesión de Google
                    Toast.makeText(this, "Google Sign-In falló: ${e.statusCode}", Toast.LENGTH_SHORT).show()
                    // Log.e("GoogleSignIn", "Google sign in failed", e) // Opcional: loguear el error detallado
                }
            } else {
                // El usuario canceló el inicio de sesión de Google
                Toast.makeText(this, "Inicio de sesión de Google cancelado", Toast.LENGTH_SHORT).show()
            }
        }


        // Encuentra el botón y establece el OnClickListener
        val btnGoogle = findViewById<Button>(R.id.btnGoogle)
        btnGoogle.setOnClickListener {
            // Inicia el flujo de inicio de sesión de Google
            signInWithGoogle()
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    // Función para autenticar en Firebase con la credencial de Google
    private fun firebaseAuthWithGoogle(idToken: String?) {
        if (idToken == null) {
            Toast.makeText(this, "ID Token de Google nulo", Toast.LENGTH_SHORT).show()
            return
        }

        val credential = GoogleAuthProvider.getCredential(idToken, null)

        // Inicia sesión en Firebase con la credencial de Google
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Inicio de sesión en Firebase exitoso
                    val user = auth.currentUser
                    val intent = Intent(this, MenuActivity::class.java)
                    intent.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                    if (user != null) {
                        Toast.makeText(this, "¡Autenticación con Google exitosa!", Toast.LENGTH_SHORT).show()
                        // Ahora, guarda o actualiza los datos del usuario en Firestore
                        saveUserDataToFirestore(user)
                        // Puedes navegar a la siguiente pantalla aquí si los datos no son críticos para la navegación
                        // navigateToMainScreen()
                    }
                } else {
                    // Si falla el inicio de sesión en Firebase
                    Toast.makeText(this, "Autenticación con Firebase fallida.", Toast.LENGTH_SHORT).show()
                    // Puedes examinar task.exception para ver el error específico
                }
            }
    }

    // Función para guardar o actualizar los datos del usuario en Firestore
    private fun saveUserDataToFirestore(user: com.google.firebase.auth.FirebaseUser) {
        // Crea un Map con los datos que quieres guardar
        val userData = hashMapOf(
            "uid" to user.uid, // Siempre es bueno guardar el UID también en el documento
            "name" to user.displayName,
            "email" to user.email,
            "photoUrl" to user.photoUrl.toString(), // Convierte la URI a String
            "lastSignIn" to System.currentTimeMillis() // Marca de tiempo del último inicio de sesión
            // Puedes añadir otros campos por defecto aquí si es un nuevo usuario
        )

        // Guarda los datos en la colección 'users', usando el UID de Firebase Auth como ID del documento
        // set(userData) creará el documento si no existe, o lo sobrescribirá si ya existe
        firestore.collection("users").document(user.uid)
            .set(userData)
            .addOnSuccessListener {
                // Datos guardados/actualizados en Firestore con éxito
                Toast.makeText(this, "Datos del usuario guardados en Firestore.", Toast.LENGTH_SHORT).show()
                // Ahora sí puedes navegar a la siguiente pantalla
                // navigateToMainScreen()
            }
            .addOnFailureListener { e ->
                // Error al guardar datos en Firestore
                Toast.makeText(this, "Error al guardar datos en Firestore.", Toast.LENGTH_SHORT).show()
                // Log.e("Firestore", "Error writing document", e) // Opcional: loguear el error
                // Decide qué hacer si falla la escritura en la base de datos (ej: permitir la navegación de todas formas?)
                // navigateToMainScreen() // Podrías navegar igual si no es crítico
            }
    }


    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Login exitoso
                    val intent = Intent(this, MenuActivity::class.java)
                    intent.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {  // Verificar si existe una excepción en la tarea (por ejemplo, contraseña o email incorrectos)
                    val errorMessage = task.exception?.message
                    val errorToShow = when {
                        errorMessage.isNullOrEmpty() -> "Error desconocido. Intenta de nuevo."
                        errorMessage.contains(
                            "password",
                            ignoreCase = true
                        ) -> "La contraseña es incorrecta."

                        errorMessage.contains(
                            "email",
                            ignoreCase = true
                        ) -> "El correo electrónico no está registrado."

                        else -> "Error al iniciar sesión: $errorMessage"
                    }

                    // Muestra el mensaje de error adecuado
                    Toast.makeText(this, errorToShow, Toast.LENGTH_LONG).show()
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
        if (currentUser != null && currentUser.isEmailVerified) {
            val intent = Intent(this, MenuActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

    }

}

