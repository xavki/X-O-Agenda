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
        //Texto del perfil
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
        // Configura la acción para ir al perfil
        val profileIcon: ImageView = findViewById(R.id.profileicon)
        profileIcon.setOnClickListener {
            // Cierra el menú lateral
            drawerLayout.closeDrawer(GravityCompat.START)

            // Navegar al ProfileFragment
            supportFragmentManager.beginTransaction()
                .replace(R.id.container_fragment, ProfileFragment())
                .addToBackStack(null)  // Esto es opcional, pero permite volver al fragmento anterior
                .commit()
        }

        val textCalendario: TextView = findViewById(R.id.textCalendario)
        textCalendario.setOnClickListener {
            // Cierra el menú lateral
            drawerLayout.closeDrawer(GravityCompat.START)

            // Navegar al ProfileFragment
            supportFragmentManager.beginTransaction()
                .replace(R.id.container_fragment, FragmentCalendari())
                .addToBackStack(null)  // Esto es opcional, pero permite volver al fragmento anterior
                .commit()
        }

        val calendaricon: ImageView = findViewById(R.id.calendaricon)
        calendaricon.setOnClickListener {
            // Cierra el menú lateral
            drawerLayout.closeDrawer(GravityCompat.START)

            // Navegar al calendarifragment
            supportFragmentManager.beginTransaction()
                .replace(R.id.container_fragment, FragmentCalendari())
                .addToBackStack(null)  // Esto es opcional, pero permite volver al fragmento anterior
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
