package com.institutmarianao.xo_agenda

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.institutmarianao.xo_agenda.login.LoginActivity
import java.util.Locale
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SettingFragments : Fragment() {

    private lateinit var txtStyle: TextView
    private lateinit var txtLang: TextView
    private lateinit var txtCloseS: TextView
    private lateinit var txtPairGoogle: TextView
    private lateinit var txtDeleteAcc: TextView

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

        // Botón para abrir el menú lateral
        val btnOpenMenu = view.findViewById<ImageView>(R.id.btnOpenMenu)

        btnOpenMenu.setOnClickListener {
            // Llama al método público de la actividad para abrir el drawer
            (activity as? MenuActivity)?.openDrawer()
        }

        // CAMBIAR TEMA
        txtStyle.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Selecciona un tema")

            // Establecer las opciones de tema
            val themes = arrayOf("Claro", "Oscuro")

            builder.setItems(themes) { _, which ->
                when (which) {
                    0 -> {
                        // Cambiar a tema claro
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                        updateThemeText()
                    }
                    1 -> {
                        // Cambiar a tema oscuro
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                        updateThemeText()
                    }
                }
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
            // Lógica para vincular la cuenta de Google
        }

        // Lógica para eliminar la cuenta
        txtDeleteAcc.setOnClickListener {
            // Primer AlertDialog de confirmación
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("¿Estás seguro de que quieres eliminar tu cuenta?")
                .setMessage("Esta acción no se puede deshacer.")
                .setPositiveButton("Sí") { _, _ ->
                    // Si el usuario confirma, mostrar la segunda confirmación
                    showSecondConfirmationDialog()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss() // Cierra el diálogo si el usuario no quiere eliminar la cuenta
                }
                .show()
        }

        return view
    }
    // Función para actualizar el texto de txtStyle dependiendo del tema
    private fun updateThemeText() {
        val currentMode = AppCompatDelegate.getDefaultNightMode()
        if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) {
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

    // Mostrar el segundo AlertDialog con el nombre del usuario
    private fun showSecondConfirmationDialog() {
        val currentUser = auth.currentUser
        currentUser?.let {
            // Obtener el nombre del usuario (si está disponible)
            val userName = it.displayName ?: "Usuario"

            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Confirmar eliminación")
                .setMessage("¿Seguro que deseas eliminar la cuenta de $userName? Esta acción es irreversible.")
                .setPositiveButton("Eliminar cuenta") { _, _ ->
                    deleteUserAccount()
                }
                .setNegativeButton("Cancelar") { dialog, _ ->
                    dialog.dismiss() // Si el usuario cancela, cierra el diálogo
                }
                .show()
        }
    }

    // Eliminar la cuenta de Firebase y la base de datos (si se utiliza Firestore)
    private fun deleteUserAccount() {
        val currentUser = auth.currentUser
        currentUser?.let {
            // Eliminar datos de la base de datos (por ejemplo, Firestore)
            val userId = it.uid
            firestore.collection("users").document(userId).delete()
                .addOnSuccessListener {
                    // Eliminar la cuenta en Firebase Authentication
                    currentUser.delete()
                        .addOnSuccessListener {
                            // Después de eliminar la cuenta, redirigir al login
                            val intent = Intent(requireContext(), LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            requireActivity().finish()
                        }
                        .addOnFailureListener { exception ->
                            // Si ocurre un error al eliminar la cuenta de Firebase Authentication
                            showErrorDialog("Error al eliminar la cuenta: ${exception.message}")
                        }
                }
                .addOnFailureListener { exception ->
                    // Si ocurre un error al eliminar la cuenta de Firestore
                    showErrorDialog("Error al eliminar los datos de la base de datos: ${exception.message}")
                }
        }
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


