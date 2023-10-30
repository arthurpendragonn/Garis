package com.tech4everyone.garis.start

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.tech4everyone.garis.databinding.FragmentViewPagerBinding


class ViewPagerFragment : Fragment() {

    private var _binding: FragmentViewPagerBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentViewPagerBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val fragmentList = arrayListOf<Fragment>(
            FirstOnBoardingFragment(),
            SecondOnBoardingFragment(),
            ThirdOnBoardingFragment()
        )

        val adapter = ViewPagerAdapter(
            fragmentList,
            requireActivity().supportFragmentManager,
            lifecycle
        )

        binding.viewPager.adapter = adapter

        return root
    }

}