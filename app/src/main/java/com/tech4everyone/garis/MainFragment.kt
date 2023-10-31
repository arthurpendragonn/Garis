package com.tech4everyone.garis

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.auth
import com.google.firebase.Firebase
import com.tech4everyone.garis.listfragments.MyMonthlyFragment
import com.tech4everyone.garis.databinding.FragmentMainBinding
import com.tech4everyone.garis.listfragments.MyDailyFragment
import com.tech4everyone.garis.listfragments.YearlyFragment

class MainFragment : Fragment(), MenuProvider {
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private lateinit var pagerAdapter: FragmentStateAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentMainBinding.inflate(inflater, container, false)

        activity?.actionBar?.title = "Garis"

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // MenuProvider
        val menuHost: MenuHost = requireActivity() as MenuHost
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        // Create the adapter that will return a fragment for each section
        pagerAdapter = object : FragmentStateAdapter(childFragmentManager, viewLifecycleOwner.lifecycle) {
            private val fragments = arrayOf<Fragment>(
                MyDailyFragment(),
                MyMonthlyFragment(),
                YearlyFragment()
            )

            override fun createFragment(position: Int) = fragments[position]

            override fun getItemCount() = fragments.size
        }

        // Set up the ViewPager with the sections adapter.
        with(binding) {
            container.adapter = pagerAdapter
            TabLayoutMediator(tabs, container) { tab, position ->
                tab.text = when (position) {
                    0 -> "Harian"
                    1 -> "Bulanan"
                    else -> "Tahunan"
                }
            }.attach()
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_main, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return if (menuItem.itemId == R.id.action_logout) {
            Firebase.auth.signOut()
            val intent = Intent(requireContext(), StartActivity::class.java)
            startActivity(intent)
            true
        } else {
            false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
