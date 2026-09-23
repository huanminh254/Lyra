package com.devpro.sound

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.devpro.sound.databinding.ActivityMainBinding
import com.devpro.sound.ui.discover.DiscoverFragment
import com.devpro.sound.ui.downloads.DownloadsFragment
import com.devpro.sound.ui.downloads.UploadSongFragment
import com.devpro.sound.ui.favorites.FavoritesFragment
import com.devpro.sound.ui.account.AccountFragment
import com.devpro.sound.ui.search.SearchFragment
import com.devpro.sound.ui.auth.LoginFragment
import com.devpro.sound.ui.nowplaying.NowPlayingFragment
import dagger.hilt.android.AndroidEntryPoint
import android.view.animation.OvershootInterpolator
import com.devpro.sound.ui.nowplaying.NowPlayingViewModel
import javax.inject.Inject
import kotlin.math.hypot
import androidx.activity.viewModels
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import com.devpro.sound.ui.components.MiniPlayerBinder
import com.google.firebase.auth.FirebaseAuth


@AndroidEntryPoint
class MainActivity () : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    @Inject
    lateinit var firebaseAuth: FirebaseAuth
    private var hoveredButton: View? = null
    private var currentTab = NavigationTab.DISCOVER
    private lateinit var radialItems: MutableList<RadialItem>
    private val viewModel: NowPlayingViewModel by viewModels()
    private lateinit var miniPlayer: MiniPlayerBinder
    private var miniPlayerMarginAnimator: ValueAnimator? = null
    private var loadedAuthUserId: String? = null
    private enum class NavigationTab(val iconRes: Int) {
        DISCOVER(R.drawable.ic_music_note),
        FAVORITES(R.drawable.ic_favorite),
        DOWNLOADS(R.drawable.ic_download),
        ACCOUNT(R.drawable.account)
    }
    private data class RadialItem(
        val button: ImageButton,
        var tab: NavigationTab
    )

    private data class BubblePosition(
        val button: View,
        val x: Float,
        val y: Float
    )

    private val showMenuRunnable = Runnable {
        expandRadialMenu()
    }

    private val authStateListener = FirebaseAuth.AuthStateListener { auth ->
        if (auth.currentUser == null) {
            loadedAuthUserId = null
            showRoot(LoginFragment())
        } else {
            val userId = auth.currentUser?.uid
            if (userId != null && userId != loadedAuthUserId) {
                loadedAuthUserId = userId
                viewModel.refreshSongs()
            }
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
            if (currentFragment is LoginFragment) {
                showRoot(DiscoverFragment())
            } else {
                updateNavigationVisibility()
            }
        }
    }

    @Suppress("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val fragmentManager = supportFragmentManager
                    if (fragmentManager.backStackEntryCount > 0) {
                        fragmentManager.popBackStack()
                        return
                    }

                    val currentFragment = fragmentManager.findFragmentById(R.id.fragment_container)
                    if (isAuthenticated() && currentFragment !is DiscoverFragment) {
                        navigateToDiscover()
                        return
                    }

                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        )

        radialItems = mutableListOf(
            RadialItem(binding.menuToggle1, NavigationTab.FAVORITES),
            RadialItem(binding.menuToggle2, NavigationTab.DOWNLOADS),
            RadialItem(binding.menuToggle3, NavigationTab.ACCOUNT)
        )
        radialItems.forEach { item ->
            item.button.setOnClickListener {
                swapTabAndNavigate(item.button)
                resetHoveredButton()
                collapseRadialMenu()
            }
        }

        binding.menuToggleMain.setOnTouchListener { view, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    if (!isAuthenticated()) {
                        showRoot(LoginFragment())
                    } else {
                        view.postDelayed(showMenuRunnable, 200L)
                    }
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    if (isRadialMenuVisible()) {
                        updateHoveredButton(event.rawX, event.rawY)
                    }
                    true
                }

                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> {
                    view.removeCallbacks(showMenuRunnable)
                    if (event.actionMasked == MotionEvent.ACTION_UP) {
                        view.performClick()
                    }
                    if (event.actionMasked == MotionEvent.ACTION_UP && isRadialMenuVisible()) {
                        hoveredButton?.let { swapTabAndNavigate(it) }
                    }
                    resetHoveredButton()
                    collapseRadialMenu()
                    true
                }

                else -> true
            }
        }

        supportFragmentManager.addOnBackStackChangedListener { updateNavigationVisibility() }

        if (savedInstanceState == null) {
            showRoot(
                if (isAuthenticated()) DiscoverFragment() else LoginFragment()
            )
        }
        miniPlayer = MiniPlayerBinder(
            root = binding.mainMiniPlayer.root,
            onOpen = {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, NowPlayingFragment())
                    .addToBackStack("now_playing")
                    .commit()
            },
            onPlayPause = viewModel::onPlayPauseClick,
            onFavorite = viewModel::toggleFavorite
        )
        viewModel.uiState.observe(this){state ->
            miniPlayer.render(
                song = state.song,
                isPlaying = state.isPlaying,
                isFavorite = state.song?.let { song ->
                    state.favoriteSongs.any { favorite -> favorite.id == song.id }
                } == true,
                progress = state.progress
            )
            updateMiniPlayerVisibility()
        }
    }

    override fun onStart() {
        super.onStart()
        firebaseAuth.addAuthStateListener(authStateListener)
    }

    override fun onStop() {
        firebaseAuth.removeAuthStateListener(authStateListener)
        super.onStop()
    }

    fun navigateToDiscover() {
        navigateToTab(NavigationTab.DISCOVER)
    }

    private fun showRoot(fragment: Fragment): Boolean {
        if (!isAuthenticated() && fragment !is LoginFragment) {
            return showRoot(LoginFragment())
        }

        if (isAuthenticated() && fragment is LoginFragment) {
            return showRoot(DiscoverFragment())
        }

        supportFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
        binding.bottomNavigation.visibility = if (
            fragment is LoginFragment ||
                fragment is SearchFragment ||
                fragment is NowPlayingFragment ||
                fragment is UploadSongFragment
        ) View.GONE else View.VISIBLE
        updateMiniPlayerVisibility(fragment)
        return true
    }

    private fun expandRadialMenu() {
        animateMiniPlayerMargin(expanded = true)
        bubblePositions().forEach { item ->
            item.button.animate().cancel()
            item.button.visibility = View.VISIBLE
            item.button.alpha = 0f
            item.button.scaleX = 0.75f
            item.button.scaleY = 0.75f
            item.button.translationX = 0f
            item.button.translationY = 0f
            item.button.animate()
                .translationX(dp(item.x))
                .translationY(dp(item.y))
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(220L)
                .setInterpolator(OvershootInterpolator(1.1f))
                .start()
        }
    }

    private fun collapseRadialMenu() {
        animateMiniPlayerMargin(expanded = false)
        bubblePositions().forEach { item ->
            item.button.animate().cancel()
            item.button.animate()
                .translationX(0f)
                .translationY(0f)
                .alpha(0f)
                .scaleX(0.75f)
                .scaleY(0.75f)
                .setDuration(120L)
                .withEndAction {
                    item.button.visibility = View.GONE
                    item.button.alpha = 1f
                    item.button.scaleX = 1f
                    item.button.scaleY = 1f
                    item.button.translationX = dp(item.x)
                    item.button.translationY = dp(item.y)
                }
                .start()
        }
    }

    private fun isRadialMenuVisible(): Boolean {
        return binding.menuToggle1.isVisible
    }

    private fun swapTabAndNavigate(selectedButton: View) {
        if (!isAuthenticated()) {
            showRoot(LoginFragment())
            return
        }

        val selectedItem = radialItems.firstOrNull { it.button === selectedButton }
            ?: return

        navigateToTab(selectedItem.tab)
    }

    private fun navigateToTab(tab: NavigationTab) {
        if (!isAuthenticated()) {
            showRoot(LoginFragment())
            return
        }

        if (currentTab != tab) {
            radialItems.firstOrNull { it.tab == tab }?.let { selectedItem ->
                selectedItem.tab = currentTab
            }
            currentTab = tab
        }

        binding.menuToggleMain.setImageResource(currentTab.iconRes)
        radialItems.forEach { item ->
            item.button.setImageResource(item.tab.iconRes)
        }
        showRoot(fragmentFor(currentTab))
    }

    private fun fragmentFor(tab: NavigationTab): Fragment {
        return when (tab) {
            NavigationTab.DISCOVER -> DiscoverFragment()
            NavigationTab.FAVORITES -> FavoritesFragment()
            NavigationTab.DOWNLOADS -> DownloadsFragment()
            NavigationTab.ACCOUNT -> AccountFragment()
        }
    }

    private fun bubblePositions(): List<BubblePosition> {
        return listOf(
            BubblePosition(binding.menuToggle1, -76f, 0f),
            BubblePosition(binding.menuToggle2, -76f, -76f),
            BubblePosition(binding.menuToggle3, 0f, -76f)
        )
    }

    private fun updateHoveredButton(rawX: Float, rawY: Float) {
        val newHoveredButton = bubblePositions()
            .firstOrNull { item -> isPointInsideBase(item, rawX, rawY) }
            ?.button

        if (newHoveredButton === hoveredButton) return

        val oldHoveredButton = hoveredButton
        val oldHoveredPosition = bubblePositions().firstOrNull { it.button === oldHoveredButton }

        oldHoveredButton?.animate()
            ?.scaleX(1f)
            ?.scaleY(1f)
            ?.translationX(oldHoveredPosition?.let { dp(it.x) } ?: 0f)
            ?.translationY(oldHoveredPosition?.let { dp(it.y) } ?: 0f)
            ?.setDuration(100L)
            ?.start()

        newHoveredButton?.animate()
            ?.scaleX(1.2f)
            ?.scaleY(1.2f)
            ?.setDuration(100L)
            ?.start()

        hoveredButton = newHoveredButton
    }

    private fun resetHoveredButton() {
        val oldHoveredButton = hoveredButton
        val oldHoveredPosition = bubblePositions().firstOrNull { it.button === oldHoveredButton }

        oldHoveredButton?.animate()
            ?.scaleX(1f)
            ?.scaleY(1f)
            ?.translationX(oldHoveredPosition?.let { dp(it.x) } ?: 0f)
            ?.translationY(oldHoveredPosition?.let { dp(it.y) } ?: 0f)
            ?.setDuration(100L)
            ?.start()
        hoveredButton = null
    }

    private fun isPointInsideBase(item: BubblePosition, rawX: Float, rawY: Float): Boolean {
        val location = IntArray(2)
        binding.menuToggleMain.getLocationOnScreen(location)

        val centerX = location[0] + binding.menuToggleMain.width / 2f + dp(item.x)
        val centerY = location[1] + binding.menuToggleMain.height / 2f + dp(item.y)
        val hitRadius = dp(34f)
        val distanceX = rawX - centerX
        val distanceY = rawY - centerY

        return hypot(distanceX.toDouble(), distanceY.toDouble()) <= hitRadius
    }

    private fun dp(value: Float): Float {
        return value * resources.displayMetrics.density
    }

    private fun animateMiniPlayerMargin(expanded: Boolean) {
        val miniPlayer = binding.mainMiniPlayer.root
        val layoutParams = miniPlayer.layoutParams as? ViewGroup.MarginLayoutParams
            ?: return
        val targetMarginEnd = dp(if (expanded) 80f else 5f).toInt()
        miniPlayerMarginAnimator?.cancel()
        miniPlayerMarginAnimator = ValueAnimator.ofInt(
            layoutParams.marginEnd,
            targetMarginEnd
        ).apply {
            duration = if (expanded) 220L else 120L
            addUpdateListener { animator ->
                val updatedParams = miniPlayer.layoutParams as ViewGroup.MarginLayoutParams
                updatedParams.marginEnd = animator.animatedValue as Int
                miniPlayer.layoutParams = updatedParams
            }
            start()
        }
    }

    private fun updateNavigationVisibility() {
        if (!isAuthenticated()) {
            binding.bottomNavigation.visibility = View.GONE
            updateMiniPlayerVisibility()
            return
        }

        val current = supportFragmentManager.findFragmentById(R.id.fragment_container)
        binding.bottomNavigation.visibility = if (
            current is SearchFragment ||
                current is NowPlayingFragment ||
                current is UploadSongFragment
        ) View.GONE else View.VISIBLE
        updateMiniPlayerVisibility()
    }

    private fun updateMiniPlayerVisibility(fragment: Fragment? = null) {
        if (!::miniPlayer.isInitialized) return

        val current = fragment
            ?: supportFragmentManager.findFragmentById(R.id.fragment_container)
        val canShow = isAuthenticated() && (
            current is DiscoverFragment ||
                current is FavoritesFragment ||
                current is DownloadsFragment ||
                current is SearchFragment
            )
        val hasSong = viewModel.uiState.value?.song != null

        binding.mainMiniPlayer.root.visibility =
            if (canShow && hasSong) View.VISIBLE else View.GONE
    }

    private fun isAuthenticated(): Boolean {
        return firebaseAuth.currentUser != null
    }

}
