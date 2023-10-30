package com.tech4everyone.garis

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.navigation.findNavController
import com.tech4everyone.garis.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        val toolbar = binding.toolbar
//        setSupportActionBar(toolbar)

        val fab = binding.fab
        val navController = findNavController(R.id.nav_host_fragment)
        navController.setGraph(R.navigation.nav_graph_kotlin)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.MainFragment) {
                fab.isVisible = true
                fab.setOnClickListener {
                    navController.navigate(R.id.action_MainFragment_to_NewPostFragment)
                }
            } else {
                fab.isGone = true
            }
        }

    }
}