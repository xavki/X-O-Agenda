package com.institutmarianao.xo_agenda

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


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