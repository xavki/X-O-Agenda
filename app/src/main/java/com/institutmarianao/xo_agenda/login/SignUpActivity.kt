package com.institutmarianao.xo_agenda.login

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.institutmarianao.xo_agenda.R


class SignUpActivity : AppCompatActivity(){
    private lateinit var txtBackLogin: TextView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        txtBackLogin = findViewById(R.id.txtBackLogin)

        txtBackLogin.setOnClickListener {
            setContentView(R.layout.activity_login)

        }
    }


}