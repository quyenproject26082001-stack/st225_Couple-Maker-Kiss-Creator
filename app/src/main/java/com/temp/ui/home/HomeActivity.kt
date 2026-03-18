package com.temp.ui.home

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.animation.AnimationUtils
import androidx.lifecycle.lifecycleScope
import com.lvt.ads.util.Admob
import com.temp.R
import com.temp.core.base.BaseActivity
import com.temp.core.extensions.hideNavigation
import com.temp.core.extensions.loadNativeCollabAds
import com.temp.core.extensions.rateApp
import com.temp.core.extensions.select
import com.temp.core.extensions.setImageActionBar
import com.temp.core.extensions.showInterAll
import com.temp.core.extensions.startIntentRightToLeft
import com.temp.core.helper.LanguageHelper
import com.temp.core.helper.MediaHelper
import com.temp.core.utils.key.ValueKey
import com.temp.core.utils.state.RateState
import com.temp.databinding.ActivityHomeBinding
import com.temp.ui.SettingsActivity
import com.temp.ui.my_creation.MyCreationActivity
import com.temp.ui.choose_character.ChooseCharacterActivity
import com.temp.core.extensions.tap
import com.temp.core.extensions.strings
import com.temp.CosplayRandomActivity
import com.temp.ui.trending.TrendingActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.system.exitProcess

class HomeActivity : BaseActivity<ActivityHomeBinding>() {

    override fun setViewBinding(): ActivityHomeBinding {
        return ActivityHomeBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        sharePreference.setCountBack(sharePreference.getCountBack() + 1)
        deleteTempFolder()
        binding.tv1.isSelected = true
      //  binding.tv3.isSelected = true
        binding.tv2.isSelected = true
        binding.tv3.isSelected = true
        binding.btnSettings.setImageResource(R.drawable.ic_setting)

        // Apply elastic bounce animation to app name
        val elasticBounce = AnimationUtils.loadAnimation(this, R.anim.elastic_bounce)
       // binding.imvAppName.startAnimation(elasticBounce)
    }

    override fun viewListener() {
        binding.apply {
            btnSettings.tap(800) { startIntentRightToLeft(SettingsActivity::class.java) }
            btnRandom.tap(800) { startIntentRightToLeft(TrendingActivity::class.java) }
            btnMyCreation.tap(800) { showInterAll { startIntentRightToLeft(MyCreationActivity::class.java) } }
            btnCosPlay.tap(800) { showInterAll { startIntentRightToLeft(CosplayRandomActivity::class.java) } }
            btnPlay.tap(800) { startIntentRightToLeft(com.temp.ui.choose_character.ChooseCharacterActivity::class.java) }

        }
    }

    override fun initText() {
        super.initText()
        //binding.actionBar.tvCenter.select()
    }

    override fun initActionBar() {
//        binding.actionBar.apply {
//            setImageActionBar(btnActionBarRight, R.drawable.ic_settings)
//        }
    }

    // Enable background music for HomeActivity
    override fun shouldPlayBackgroundMusic(): Boolean = false

    @SuppressLint("MissingSuperCall", "GestureBackNavigation")
    override fun onBackPressed() {
        if (!sharePreference.getIsRate(this) && sharePreference.getCountBack() % 2 == 0) {
            rateApp(sharePreference) { state ->
                if (state != RateState.CANCEL) {
                    showToast(R.string.have_rated)
                }
                lifecycleScope.launch {
                    withContext(Dispatchers.Main) {
                        delay(1000)
                        exitProcess(0)
                    }
                }
            }
        } else {
            exitProcess(0)
        }
    }

    private fun deleteTempFolder() {
        lifecycleScope.launch(Dispatchers.IO) {
            val dataTemp = MediaHelper.getImageInternal(this@HomeActivity, ValueKey.RANDOM_TEMP_ALBUM)
            if (dataTemp.isNotEmpty()) {
                dataTemp.forEach {
                    val file = File(it)
                    file.delete()
                }
            }
        }
    }

    private fun updateText() {
        binding.apply {
            tv1.text = strings(R.string.play)
            tv2.text = strings(R.string.tv_cosplay)
            tv3.text = strings(R.string.random)
            tv4.text = strings(R.string.creation)
        }
    }

    override fun onRestart() {
        super.onRestart()
        deleteTempFolder()
        LanguageHelper.setLocale(this)
        updateText()
        //initNativeCollab()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
        startStaggeredAnimations()

        }
    }

    private fun startStaggeredAnimations() {
        // Card 1: Slide from right (no delay)
        val slideFromRight1 = AnimationUtils.loadAnimation(this, R.anim.slide_in_right_home)
        binding.btnCosPlay.startAnimation(slideFromRight1)
        binding.tv1.startAnimation(slideFromRight1)


        // Card 2: Slide from left (200ms delay)
        val slideFromLeft = AnimationUtils.loadAnimation(this, R.anim.slide_in_left_home)
        binding.btnRandom.postDelayed({
            binding.btnRandom.startAnimation(slideFromLeft)
            binding.tv2.startAnimation(slideFromLeft)
        }, 200)

        // Card 3: Slide from right (400ms delay)
        val slideFromRight2 = AnimationUtils.loadAnimation(this, R.anim.slide_in_right_home)
        binding.btnMyCreation.postDelayed({
            binding.btnMyCreation.startAnimation(slideFromRight2)
            binding.tv3.startAnimation(slideFromRight2)
        }, 400)
    }

    fun initNativeCollab() {
        Admob.getInstance().loadNativeCollapNotBanner(this,getString(R.string.native_cl_home), binding.flNativeCollab)
    }

    override fun initAds() {
        initNativeCollab()
        Admob.getInstance().loadInterAll(this, getString(R.string.inter_all))
        Admob.getInstance().loadNativeAll(this, getString(R.string.native_all))
    }
}