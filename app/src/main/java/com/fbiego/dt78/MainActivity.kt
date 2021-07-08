package com.fbiego.dt78

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import com.fbiego.dt78.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import timber.log.Timber


class MainActivity : AppCompatActivity() {

    private lateinit var navListener: BottomNavigationView.OnNavigationItemSelectedListener
    private lateinit var menu: Menu
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var mBinding: ActivityMainBinding
    private lateinit var navController: NavController
    override fun onNightModeChanged(mode: Int) {
        super.onNightModeChanged(mode)
        Timber.w("Night mode changed to $mode")

    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        navController = findNavController(R.id.nav_host_fragment)
        setListeners()
    }



    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        this.menu = menu
        return super.onCreateOptionsMenu(menu)
    }

    private fun setListeners() {
        mBinding.navView.setOnNavigationItemSelectedListener(bottomNavigationListener)

    }

    //**************************************************************//

    var bottomNavigationListener: BottomNavigationView.OnNavigationItemSelectedListener =
            BottomNavigationView.OnNavigationItemSelectedListener { item ->
                when(item.itemId){
                    R.id.navigation_home ->{
                        if (navController.currentDestination!!.id != R.id.navigation_home) {
                            val navBuilder = NavOptions.Builder()
                            navBuilder.setEnterAnim(R.anim.slide_in_left).setPopExitAnim(R.anim.slide_out_left).setExitAnim(R.anim.slide_out_right)
                            navController.navigate(item.itemId, bundleOf("data" to ""), navBuilder.build())
                        }
                    }

                    R.id.navigation_explore -> {
                        if (navController.currentDestination!!.getId() != R.id.navigation_explore) {
                            val navBuilder = NavOptions.Builder()
                            if (navController.currentDestination!!.id == R.id.navigation_home) {
                                navBuilder.setEnterAnim(R.anim.slide_in_right).setPopExitAnim(R.anim.slide_out_right).setPopEnterAnim(R.anim.slide_in_left).setExitAnim(R.anim.slide_out_left)
                            } else {
                                navBuilder.setEnterAnim(R.anim.slide_in_left).setExitAnim(R.anim.slide_out_right).setPopExitAnim(R.anim.slide_out_left).setPopEnterAnim(R.anim.slide_in_right)
                            }
//                            navBuilder.setLaunchSingleTop(true)
                            navController.navigate(R.id.navigation_explore, bundleOf("data" to ""), navBuilder.build())
                        }
                    }

                    R.id.navigation_user -> {
                        if (navController.currentDestination!!.getId() != R.id.navigation_user) {
                            val navBuilder = NavOptions.Builder()
                            navBuilder.setEnterAnim(R.anim.slide_in_right).setExitAnim(R.anim.slide_out_left)
//                            navBuilder.setLaunchSingleTop(true)
                            navController.navigate(R.id.navigation_user, bundleOf("data" to ""), navBuilder.build())
                        }
                    }
                    else -> {

                    }
                }

                true




            }





}
