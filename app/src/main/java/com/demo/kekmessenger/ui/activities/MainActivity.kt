package com.demo.kekmessenger.ui.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.demo.kekmessenger.R
import com.demo.kekmessenger.app.KekMessengerApp
import com.demo.kekmessenger.databinding.ActivityMainBinding
import com.demo.kekmessenger.ui.activities.di.MainActivityComponent
import com.demo.kekmessenger.ui.fragments.ChatsFragmentDirections
import com.demo.kekmessenger.viewModels.MainViewModel
import javax.inject.Inject


class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by viewModels<MainViewModel> { viewModelFactory }
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    val mainActivityComponent: MainActivityComponent by lazy {
        initializeMainActivityComponent()
    }

    private fun initializeMainActivityComponent(): MainActivityComponent =
        (application as KekMessengerApp).applicationComponent.mainActivityComponentFactory()
            .create(this)

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            if (destination.id == R.id.chatFragment) {
                menu?.clear()
            } else if (destination.id == R.id.chatsFragment) {
                menuInflater.inflate(R.menu.options_menu, menu)
            }
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.change_name -> {
                val action = ChatsFragmentDirections.actionChatsFragmentToChangeNameFragment()
                navController.navigate(action)
            }

            R.id.clear_all -> viewModel.clearAll()
            R.id.clear_cached_images -> viewModel.clearCachedImages()
            R.id.clear_messages_db -> viewModel.clearCachedMessages()
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        mainActivityComponent.inject(this)
        setContentView(binding.root)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        navController =
            (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController
    }

}