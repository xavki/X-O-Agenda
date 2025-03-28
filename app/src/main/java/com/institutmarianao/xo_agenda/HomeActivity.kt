package com.institutmarianao.xo_agenda

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.institutmarianao.xo_agenda.adapters.MenuAdapter


// Pantalla principal con calendario y tareas
/*class HomeActivity : AppCompatActivity() {

    //private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        //auth = FirebaseAuth.getInstance()


    }
}*/
class HomeActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var recyclerView: RecyclerView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawer_menu)
        recyclerView = findViewById(R.id.recycler_view_menu)

        // Cargar el fragment del menú
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container_fragment, MenuFragment())
                .commit()
        }

        // Configurar el RecyclerView con el adaptador
        val menuItems = listOf("Opción 1", "Opción 2", "Opción 3")
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = MenuAdapter(menuItems)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Aquí puedes manejar las opciones del menú
        return super.onOptionsItemSelected(item)
    }
}

