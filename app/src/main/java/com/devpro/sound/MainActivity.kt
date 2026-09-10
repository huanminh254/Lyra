package com.devpro.sound

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.devpro.sound.databinding.ActivityMainBinding
import com.devpro.sound.ui.discover.DiscoverFragment
import com.devpro.sound.ui.downloads.DownloadsFragment
import com.devpro.sound.ui.favorites.FavoritesFragment
import com.devpro.sound.ui.search.SearchFragment
import com.devpro.sound.ui.settings.SettingsFragment
import com.devpro.sound.ui.nowplaying.NowPlayingFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_discover -> showRoot(DiscoverFragment(), R.id.nav_discover)
                R.id.nav_favorites -> showRoot(FavoritesFragment(), R.id.nav_favorites)
                R.id.nav_downloads -> showRoot(DownloadsFragment(), R.id.nav_downloads)
                R.id.nav_settings -> showRoot(SettingsFragment(), R.id.nav_settings)
                else -> false
            }
        }
        supportFragmentManager.addOnBackStackChangedListener { updateNavigationVisibility() }
        if (savedInstanceState == null) {
            binding.bottomNavigation.selectedItemId = R.id.nav_discover
        }
    }

    fun openSearch() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, SearchFragment())
            .addToBackStack("search")
            .commit()
        binding.bottomNavigation.visibility = View.GONE
    }

    fun openNowPlaying() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, NowPlayingFragment())
            .addToBackStack("now_playing")
            .commit()
        binding.bottomNavigation.visibility = View.GONE
    }

    private fun showRoot(fragment: Fragment, selectedItemId: Int): Boolean {
        supportFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
        binding.bottomNavigation.selectedItemId = selectedItemId
        binding.bottomNavigation.visibility = View.VISIBLE
        return true
    }

    private fun updateNavigationVisibility() {
        val current = supportFragmentManager.findFragmentById(R.id.fragment_container)
        binding.bottomNavigation.visibility = if (current is SearchFragment || current is NowPlayingFragment) View.GONE else View.VISIBLE
    }
}
