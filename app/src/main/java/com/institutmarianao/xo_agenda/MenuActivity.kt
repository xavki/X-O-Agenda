package com.institutmarianao.xo_agenda

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout

class MenuActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Cargar el layout sin Toolbar
        setContentView(R.layout.activity_main)

        // Configura solo el DrawerLayout
        drawerLayout = findViewById(R.id.drawer_layout)

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
        //Pestañas de perfil
        val textPerfil: TextView = findViewById(R.id.textPerfil)
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
}
