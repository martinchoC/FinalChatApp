package com.martin.finalapp

import android.os.Bundle
import android.support.v4.view.ViewPager
import com.martin.finalapp.adapters.PagerAdapter
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.martin.finalapp.R
import com.martin.finalapp.fragments.ChatFragment
import com.martin.finalapp.fragments.InfoFragment
import com.martin.finalapp.fragments.RatesFragment
import com.martin.mylibrary.ToolbarActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : ToolbarActivity() {

    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private lateinit var adapter: PagerAdapter
    private var prevBottomSelected: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbarToLoad(toolbarView as Toolbar)
        setUpViewPager(getPagerAdapter())
        setUpBottomNavigationBar()
    }

    private fun getPagerAdapter(): PagerAdapter {
        adapter = PagerAdapter(supportFragmentManager)
        adapter.addFragment(ChatFragment())
        adapter.addFragment(InfoFragment())
        adapter.addFragment(RatesFragment())
        return adapter
    }

    private fun setUpViewPager(adapter: PagerAdapter) {
        viewPager.adapter = adapter
        viewPager.addOnPageChangeListener(object: ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                if(prevBottomSelected == null) {
                    bottomNavigation.menu.getItem(0).isChecked = false
                }
                else {
                    prevBottomSelected!!.isChecked = false
                }
                bottomNavigation.menu.getItem(position).isChecked = true
                prevBottomSelected = bottomNavigation.menu.getItem(position)
            }

        })
    }

    private fun setUpBottomNavigationBar() {
        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_nav_info -> {
                    viewPager.currentItem = 0
                    true
                }
                R.id.bottom_nav_rates -> {
                    viewPager.currentItem = 1
                    true
                }
                R.id.bottom_nav_chat -> {
                    viewPager.currentItem = 2
                    true
                }
                else -> false
            }
        }
    }
}
