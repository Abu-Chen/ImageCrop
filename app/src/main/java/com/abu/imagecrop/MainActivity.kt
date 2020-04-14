package com.abu.imagecrop

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.abu.imagecrop.view.MainFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        //findViewById<BottomAppBar>(R.id.bottomAppBar).replaceMenu(R.menu.crop_menu)
//        findViewById<BottomAppBar>(R.id.bottomAppBar).setNavigationOnClickListener {
//
//        }


        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, MainFragment.newInstance())
                    .commitNow()
        }
    }

}
