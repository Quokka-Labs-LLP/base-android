package com.example.baseandroid.features.setting.ui.screens

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.chuckerteam.chucker.api.Chucker
import com.example.baseandroid.databinding.ActivitySettingBinding
import com.example.baseandroid.features.logviewer.ui.LogViewerActivity

class SettingActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnApiLogger.setOnClickListener {
            startActivity(Chucker.getLaunchIntent(this))
        }
        binding.btnLogViewer.setOnClickListener {
            startActivity(Intent(this@SettingActivity, LogViewerActivity::class.java))
        }
    }
}
