package com.institutmarianao.xo_agenda

import android.os.Bundle
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
