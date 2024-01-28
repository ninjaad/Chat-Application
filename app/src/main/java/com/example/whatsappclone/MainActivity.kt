package com.example.whatsappclone

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar= findViewById<MaterialToolbar>(R.id.toolbar)
        val viewPager= findViewById<ViewPager2>(R.id.viewPager)
        val tabs = findViewById<TabLayout>(R.id.tabs)
        setSupportActionBar(toolbar)
        viewPager.adapter = ScreenSliderAdapter(this)
        TabLayoutMediator(tabs,viewPager,
        TabLayoutMediator.TabConfigurationStrategy { tab: TabLayout.Tab,pos:Int->
            when(pos){
                 0->tab.text="CHATS"
                1->tab.text="PEOPLE"
            }
        }).attach()
    }
}