package com.institutmarianao.xo_agenda

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MenuActivity : AppCompatActivity() {
    private val REQ_NOTIF = 123

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Cargar el layout sin Toolbar
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawer_layout)
        tvName = findViewById(R.id.tvUserName)
        tvEmail = findViewById(R.id.tvUserEmail)


        // Cargar fragmento inicial
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container_fragment, ProfileFragment())
                .commit()
        }

        val ivClose: ImageView = findViewById(R.id.ivClose)
        // Agrega un OnClickListener al botón de cerrar el menú
        ivClose.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START) // Cierra el menú lateral
        }

        // Obtén el usuario actual de FirebaseAuth
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            tvEmail.text = user.email

            // 🔁 Escuchar en tiempo real los cambios de nombre
            // 2. Creamos un listener en tiempo real sobre el documento del usuario en Firestore
            FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(user.uid)
                .addSnapshotListener { snapshot, error ->

                    // 3. Si hubo un error, lo mostramos por consola
                    if (error != null) {
                        Log.w("FirestoreListener", "Error escuchando documento:", error)
                        return@addSnapshotListener
                    }

                    // 4. Si el documento existe (es decir, el usuario tiene datos guardados)
                    if (snapshot != null && snapshot.exists()) {
                        // 5. Obtenemos el campo "nom" del documento y lo ponemos en el TextView
                        val newName = snapshot.getString("nom")
                        tvName.text = newName ?: ""
                    }
                }
        }


            // Define un mapa de View IDs a lambdas que devuelven el Fragment correspondiente
            val menuMap = mapOf(
                R.id.textPerfil to ::ProfileFragment,
                R.id.profileicon to ::ProfileFragment,
                R.id.textCalendario to ::CalendariFragment,
                R.id.calendaricon to ::CalendariFragment,
                R.id.textAlertas to ::AlertFragment,
                R.id.alertasicon to ::AlertFragment,
                R.id.settingsicon to ::SettingFragments
            )

            // En onCreate(), en lugar de cada listener por separado:
            menuMap.forEach { (viewId, fragmentCtor) ->
                findViewById<View>(viewId).setOnClickListener {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.container_fragment, fragmentCtor())
                        .addToBackStack(null)
                        .commit()
                }
            }
            //Pestañas de perfil
            /* val textPerfil: TextView = findViewById(R.id.textPerfil)
             textPerfil.setOnClickListener {
                 // Cierra el menú lateral
                 drawerLayout.closeDrawer(GravityCompat.START)
                 // Navegar al ProfileFragment
                 supportFragmentManager.beginTransaction()
                     .replace(R.id.container_fragment, ProfileFragment())
                     .addToBackStack(null)  // Esto es opcional, pero permite volver al fragmento anterior
                     .commit()
             }

             val profileIcon: ImageView = findViewById(R.id.profileicon)
             profileIcon.setOnClickListener {
                 drawerLayout.closeDrawer(GravityCompat.START)
                 supportFragmentManager.beginTransaction()
                     .replace(R.id.container_fragment, ProfileFragment())
                     .addToBackStack(null)
                     .commit()
             }


             //Pestañas de calendario
             val textCalendario: TextView = findViewById(R.id.textCalendario)
             textCalendario.setOnClickListener {
                 drawerLayout.closeDrawer(GravityCompat.START)
                 supportFragmentManager.beginTransaction()
                     .replace(R.id.container_fragment, CalendariFragment())
                     .addToBackStack(null)
                     .commit()
             }


             val calendaricon: ImageView = findViewById(R.id.calendaricon)
             calendaricon.setOnClickListener {
                 drawerLayout.closeDrawer(GravityCompat.START)
                 supportFragmentManager.beginTransaction()
                     .replace(R.id.container_fragment, CalendariFragment())
                     .addToBackStack(null)
                     .commit()
             }


             //Pestaña de alertas
             val alertasicon: ImageView = findViewById(R.id.alertasicon)
             alertasicon.setOnClickListener {
                 drawerLayout.closeDrawer(GravityCompat.START)
                 supportFragmentManager.beginTransaction()
                     .replace(R.id.container_fragment, AlertFragment())
                     .addToBackStack(null)
                     .commit()
             }


             val textAlertas: TextView = findViewById(R.id.textAlertas)
             textAlertas.setOnClickListener {
                 drawerLayout.closeDrawer(GravityCompat.START)
                 supportFragmentManager.beginTransaction()
                     .replace(R.id.container_fragment, AlertFragment())
                     .addToBackStack(null)
                     .commit()
             }


             //Pestaña de ajustes
             val settingsicon: ImageView = findViewById(R.id.settingsicon)
             settingsicon.setOnClickListener {
                 drawerLayout.closeDrawer(GravityCompat.START)
                 supportFragmentManager.beginTransaction()
                     .replace(R.id.container_fragment, SettingFragments())
                     .addToBackStack(null)
                     .commit()
             }*/

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQ_NOTIF
                )
            }
        }

        }

        // Método público para abrir el menú desde cualquier fragmento
        fun openDrawer() {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // (Opcional) cerrar drawer si está abierto
        fun closeDrawerIfOpen() {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            }
        }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ_NOTIF) {
            if (grantResults.firstOrNull() != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(
                    this,
                    "Sin permiso de notificaciones, los recordatorios no se mostrarán",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}

