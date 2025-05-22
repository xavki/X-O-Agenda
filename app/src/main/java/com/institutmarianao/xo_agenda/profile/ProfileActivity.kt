package com.institutmarianao.xo_agenda.profile

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.institutmarianao.xo_agenda.R // Asegúrate de que esta importación sea correcta para tus recursos

class ProfileActivity : AppCompatActivity() {

    // Etiquetas para los logs, útiles para filtrar en Logcat
    private val TAG = "ProfileActivity"

    // Declaración de las vistas donde se mostrarán los datos del usuario
    private lateinit var nameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var phoneTextView: TextView
    // Vista para la foto de perfil
    private lateinit var profileImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Establece el layout de la actividad
        setContentView(R.layout.fragment_profile) // Asegúrate de que este es el layout correcto

        // Inicializa las referencias a las vistas
        nameTextView = findViewById(R.id.editname) // Asegúrate de que estos IDs coinciden con tu XML
        emailTextView = findViewById(R.id.editemail) // Asegúrate de que estos IDs coinciden con tu XML
        phoneTextView = findViewById(R.id.editphone) // Asegúrate de que estos IDs coinciden con tu XML
        profileImageView = findViewById(R.id.profileicon) // Asegúrate de que este ID coincide con tu XML

        // --- COMIENZO: PRUEBA TEMPORAL PARA CARGAR IMAGEN LOCAL CON GLIDE ---
        // Descomenta este bloque para probar si Glide puede cargar una imagen local
        // en tu ImageView. Si esto funciona, el problema está en la URL remota.
        /*
        Log.d(TAG, "--- Ejecutando prueba temporal de carga local con Glide ---")
        try {
            Glide.with(this)
                .load(R.drawable.ic_profile) // Carga tu imagen por defecto local
                .circleCrop() // Opcional: para mostrarla redonda si quieres
                .placeholder(R.drawable.ic_launcher_foreground) // Imagen mientras carga
                .error(R.drawable.ic_profile) // Imagen si hay un error
                .into(profileImageView)

             // Si quieres detener la ejecución aquí para ver solo la prueba local, descomenta la siguiente línea
             // return
             Log.d(TAG, "--- Prueba temporal de carga local con Glide finalizada ---")

        } catch (e: Exception) {
             Log.e(TAG, "Error during temporary Glide local load test", e)
        }
        // --- FIN: PRUEBA TEMPORAL PARA CARGAR IMAGEN LOCAL CON GLIDE ---
        */


        // 1. Obtén la instancia de FirebaseAuth y el usuario actual
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            Log.d(TAG, "Usuario autenticado encontrado: ${currentUser.uid}")

            // 2. Obtén el UID del usuario actual (si lo necesitas para Firestore)
            val uid = currentUser.uid

            // 3. Obtén la referencia al documento del usuario en la colección "usuarios" de Firestore
            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("usuarios").document(uid) // Usa "usuarios" según tu estructura

            // 4. Realiza la consulta para obtener los datos del perfil desde Firestore
            userRef.get()
                .addOnSuccessListener { document ->
                    Log.d(TAG, "Resultado de la consulta a Firestore. ¿Documento existe?: ${document != null && document.exists()}")
                    if (document != null && document.exists()) {
                        // El documento existe, extrae los datos
                        val name = document.getString("nom") ?: currentUser.displayName ?: "Nombre no disponible"
                        val emailFromFirestore = document.getString("email") ?: currentUser.email ?: "Email no disponible"
                        val phone = document.getString("phone") ?: "Telefono no disponible"

                        // Obtén la photoUrl desde Firestore si existe
                        val photoUrlFromFirestoreString = document.getString("photoUrl")

                        Log.d(TAG, "Datos de Firestore obtenidos - Nombre: $name, Email: $emailFromFirestore, Telefono: $phone, photoUrl (Firestore): $photoUrlFromFirestoreString")

                        // 5. Actualiza la UI con los datos obtenidos
                        nameTextView.text = name
                        emailTextView.text = emailFromFirestore
                        phoneTextView.text = phone

                        // Lógica para determinar qué URL de imagen cargar
                        val imageUrlToLoad: String? = if (!photoUrlFromFirestoreString.isNullOrEmpty()) {
                            // Si la URL de Firestore existe y no está vacía, úsala
                            photoUrlFromFirestoreString
                        } else {
                            // Si no, intenta usar la URL de Firebase Authentication como fallback
                            currentUser.photoUrl?.toString() // Convertir Uri a String para Glide
                        }

                        Log.d(TAG, "URL de imagen final a intentar cargar con Glide: $imageUrlToLoad")

                        // Carga la imagen usando Glide si hay una URL válida
                        if (!imageUrlToLoad.isNullOrEmpty()) {
                            Log.d(TAG, "Iniciando carga de imagen con Glide...")
                            Glide.with(this) // 'this' se refiere a la Activity
                                .load(imageUrlToLoad) // Carga la URL de la imagen
                                .circleCrop() // Opcional: para mostrarla redonda
                                .placeholder(R.drawable.ic_launcher_foreground) // Imagen mientras carga
                                .error(R.drawable.ic_profile) // Imagen si hay un error
                                .into(profileImageView) // El ImageView donde se mostrará
                            Log.d(TAG, "Llamada a Glide.load() completada.")
                        } else {
                            // Si ni en Firestore ni en Auth hay URL, muestra la imagen por defecto
                            Log.d(TAG, "No hay URL de imagen válida. Mostrando imagen por defecto.")
                            profileImageView.setImageResource(R.drawable.ic_profile)
                        }

                    } else {
                        // El documento del usuario no existe en Firestore
                        Log.d(TAG, "Documento de perfil en Firestore no encontrado para UID: $uid")
                        Toast.makeText(this, "No se encontraron datos adicionales de perfil en Firestore", Toast.LENGTH_SHORT).show()
                        // Carga datos básicos de Firebase Auth
                        nameTextView.text = currentUser.displayName ?: "Nombre no disponible (Auth)"
                        emailTextView.text = currentUser.email ?: "Email no disponible (Auth)"
                        phoneTextView.text = "Telefono no disponible" // Si el teléfono solo está en Firestore
                        // También muestra la imagen por defecto
                        profileImageView.setImageResource(R.drawable.ic_profile)
                        Log.d(TAG, "Mostrando datos de Auth y imagen por defecto.")
                    }
                }
                .addOnFailureListener { e ->
                    // Error al obtener el documento de Firestore
                    Log.e(TAG, "Error al obtener datos adicionales de Firestore", e)
                    Toast.makeText(this, "Error al obtener datos adicionales de Firestore: ${e.message}", Toast.LENGTH_SHORT).show()
                    // Carga datos básicos de Firebase Auth en caso de error
                    nameTextView.text = currentUser.displayName ?: "Nombre no disponible (Auth)"
                    emailTextView.text = currentUser.email ?: "Email no disponible (Auth)"
                    phoneTextView.text = "Telefono no disponible" // Si el teléfono solo está en Firestore
                    // También muestra la imagen por defecto en caso de error
                    profileImageView.setImageResource(R.drawable.ic_profile)
                    Log.d(TAG, "Mostrando datos de Auth y imagen por defecto debido a un error en Firestore.")
                }
        } else {
            // No hay usuario autenticado
            Log.w(TAG, "Usuario no autenticado. Redirigiendo o finalizando actividad.")
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            // Aquí podrías redirigir a la pantalla de inicio de sesión o cerrar la actividad
            finish() // O redirige a la pantalla de login (requeriría un Intent)
        }
    }
}
