package com.institutmarianao.xo_agenda

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope

import com.google.firebase.auth.EmailAuthProvider
import com.institutmarianao.xo_agenda.login.LoginActivity
import java.util.Locale
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.lifecycle.lifecycleScope
import com.google.firebase.functions.ktx.functions
import kotlinx.coroutines.tasks.await

class SettingFragments : Fragment() {

    private lateinit var txtStyle: TextView
    private lateinit var txtLang: TextView
    private lateinit var txtCloseS: TextView
    private lateinit var txtPairGoogle: TextView
    private lateinit var txtDeleteAcc: TextView
    private lateinit var txtGenerarImp: TextView
    private var pendingPassword: String? = null


    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_config, container, false)

        // Inicializar las vistas
        txtStyle = view.findViewById(R.id.txtStyle)
        txtLang = view.findViewById(R.id.txtLang)
        txtCloseS = view.findViewById(R.id.txtCloseS)
        txtPairGoogle = view.findViewById(R.id.txtPairGoogle)
        txtDeleteAcc = view.findViewById(R.id.txtDeleteAcc)
        txtGenerarImp = view.findViewById(R.id.txtGenerarImportador)

        // Botón para abrir el menú lateral
        val btnOpenMenu = view.findViewById<ImageView>(R.id.btnOpenMenu)

        btnOpenMenu.setOnClickListener {
            // Llama al método público de la actividad para abrir el drawer
            (activity as? MenuActivity)?.openDrawer()
        }

        // CAMBIAR TEMA
        // CAMBIAR TEMA
        txtStyle.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Selecciona un tema")

            // Opciones de temas
            val themes = arrayOf("Claro", "Oscuro")

            // Detectar el modo actual
            val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

            val defaultSelection = when (currentNightMode) {
                Configuration.UI_MODE_NIGHT_YES -> 1 // Oscuro
                Configuration.UI_MODE_NIGHT_NO -> 0  // Claro
                else -> 0
            }

            builder.setSingleChoiceItems(themes, defaultSelection) { dialog, which ->
                when (which) {
                    0 -> {
                        // Cambiar a tema claro
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    }
                    1 -> {
                        // Cambiar a tema oscuro
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    }
                }
                // Después de cambiar el modo, actualizar el texto
                updateThemeText()
                dialog.dismiss()
            }

            // Mostrar el AlertDialog
            builder.show()
        }

        // Actualizar el texto del TextView de acuerdo al tema actual
        updateThemeText()

        // CAMBIAR IDIOMA
        txtLang.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Selecciona un idioma")

            // Establecer las opciones de idioma
            val languages = arrayOf("Español", "Inglés")

            builder.setItems(languages) { _, which ->
                when (which) {
                    0 -> {
                        // Cambiar a Español
                        setLanguage("es")
                        updateLanguageText()
                    }
                    1 -> {
                        // Cambiar a Inglés
                        setLanguage("en")
                        updateLanguageText()
                    }
                }
            }
            // Mostrar el AlertDialog
            builder.show()
        }

        // Actualizar el texto del TextView de acuerdo al idioma actual
        updateLanguageText()

        // CERRAR SESIÓN
        txtCloseS.setOnClickListener {
            // Crear el AlertDialog de confirmación
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Cerrar sesión")
                .setMessage("¿Estás seguro de que deseas cerrar sesión?")
                .setPositiveButton("Sí") { _, _ ->
                    // Si el usuario confirma, cierra la sesión
                    auth.signOut() // Cerrar sesión en Firebase
                    // Eliminar cualquier información de sesión guardada si es necesario

                    // Redirigir al login
                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    requireActivity().finish()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss() // Si el usuario no quiere cerrar sesión, simplemente cierra el diálogo
                }
                .show()
        }

        // VINCULAR CUENTA DE GOOGLE
        txtPairGoogle.setOnClickListener {
            Log.d("SettingFragments", "txtPairGoogle clicked") // Log para verificar el clic
            // Llama al método de la Activity para iniciar el flujo de Google Calendar
            (activity as? MenuActivity)?.startGoogleCalendarSyncFlow()
                ?: Log.e("SettingFragments", "Hosting Activity is not MenuActivity or is null") // Log de error si no es MenuActivity
        }

        txtGenerarImp.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser

            if (user != null) {
                val userId = user.uid
                val userEmail = user.email

                val data = hashMapOf(
                    "userId" to userId,
                    "userEmail" to userEmail
                )

                val functions = FirebaseFunctions.getInstance()

                lifecycleScope.launch {
                    try {
                        val result = withContext(Dispatchers.IO) {
                            functions
                                .getHttpsCallable("generateAndEmailCalendar")
                                .call(data)
                                .await()
                        }

                        val response = result.data as? Map<String, Any>
                        val status = response?.get("status") as? String
                        val message = response?.get("message") as? String

                        if (status == "success") {
                            Toast.makeText(
                                requireContext(), // ✅ CONTEXTO CORRECTO EN UN FRAGMENT
                                message ?: "Archivo generado y correo enviado.",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            Toast.makeText(
                                requireContext(),
                                message ?: "Error al generar el archivo o enviar el correo.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(
                            requireContext(),
                            "Error: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }



        // Eliminar cuenta: primer diálogo
        txtDeleteAcc.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("¿Estás seguro de que quieres eliminar tu cuenta?")
                .setMessage("Esta acción no se puede deshacer.")
                .setPositiveButton("Sí") { _, _ ->
                    promptPasswordAndConfirmDelete()
                }
                .setNegativeButton("No", null)
                .show()
        }

        return view
    }
    // Función para actualizar el texto de txtStyle dependiendo del tema
// Función para actualizar el texto de txtStyle dependiendo del tema
    private fun updateThemeText() {
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            txtStyle.text = "Tema: Oscuro"
        } else {
            txtStyle.text = "Tema: Claro"
        }
    }

    // Función para actualizar el texto de txtLang dependiendo del idioma actual
    private fun updateLanguageText() {
        val language = getCurrentLanguage()
        if (language == "es") {
            txtLang.text = "Idioma: Español"
        } else {
            txtLang.text = "Idioma: English"
        }
    }

    // Función para cambiar el idioma
    private fun setLanguage(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        requireContext().resources.updateConfiguration(config, requireContext().resources.displayMetrics)

        // Opcional: Guardar la preferencia del idioma, para persistir el cambio después de reiniciar la aplicación
        val sharedPreferences = requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("language", languageCode).apply()
    }

    // Función para obtener el idioma actual
    private fun getCurrentLanguage(): String {
        val sharedPreferences = requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("language", "es") ?: "es"  // Por defecto, Español
    }
    private fun promptPasswordAndConfirmDelete() {
        val currentEmail = auth.currentUser?.email ?: return
        val input = EditText(requireContext()).apply {
            hint = "Contraseña"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Vuelve a introducir tu contraseña")
            .setView(input)
            .setPositiveButton("Continuar") { _, _ ->
                val pwd = input.text.toString().trim()
                if (pwd.isEmpty()) {
                    Toast.makeText(requireContext(), "La contraseña no puede estar vacía", Toast.LENGTH_SHORT).show()
                } else {
                    showSecondConfirmationDialog(currentEmail, pwd)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }


    // Mostrar el segundo AlertDialog con el nombre del usuario
    private fun showSecondConfirmationDialog(email: String, password: String) {
        val userName = auth.currentUser?.displayName ?: "Usuario"
        AlertDialog.Builder(requireContext())
            .setTitle("Confirmar eliminación")
            .setMessage("¿Seguro que deseas eliminar la cuenta de $userName? Esta acción es irreversible.")
            .setPositiveButton("Eliminar cuenta") { _, _ ->
                deleteUserAccount(email, password)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }


    // Eliminar la cuenta de Firebase y la base de datos (si se utiliza Firestore)
    private fun deleteUserAccount(email: String, password: String) {
        val user = auth.currentUser ?: return
        val credential = EmailAuthProvider.getCredential(email, password)
        user.reauthenticate(credential)
            .addOnSuccessListener {
                val userId = user.uid
                firestore.collection("usuarios").document(userId)
                    .delete()
                    .addOnCompleteListener {
                        user.delete()
                            .addOnSuccessListener {
                                val intent = Intent(requireContext(), LoginActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                requireActivity().finish()
                            }
                            .addOnFailureListener { e -> showErrorDialog("Error al eliminar la cuenta: ${e.message}") }
                    }
                    .addOnFailureListener { e -> showErrorDialog("Error al eliminar datos en Firestore: ${e.message}") }
            }
            .addOnFailureListener { e -> showErrorDialog("Reautenticación fallida: ${e.message}") }
    }



    // Mostrar un mensaje de error si la eliminación falla
    private fun showErrorDialog(message: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Error")
            .setMessage(message)
            .setPositiveButton("Aceptar", null)
            .show()
    }
}


