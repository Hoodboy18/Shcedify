package com.example.shcedify

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.isVisible
import com.example.shcedify.core.FragmentCommunicator
import com.example.shcedify.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), FragmentCommunicator {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun manageLoader(isVisible: Boolean) {
        binding.loaderView.isVisible = isVisible
    }
}