package com.couple.avatar.maker.kisscreator.ui

import android.view.LayoutInflater
import com.couple.avatar.maker.kisscreator.R
import com.couple.avatar.maker.kisscreator.core.base.BaseActivity
import com.couple.avatar.maker.kisscreator.core.extensions.gone
import com.couple.avatar.maker.kisscreator.core.extensions.handleBackLeftToRight
import com.couple.avatar.maker.kisscreator.core.extensions.policy
import com.couple.avatar.maker.kisscreator.core.extensions.select
import com.couple.avatar.maker.kisscreator.core.extensions.setImageActionBar
import com.couple.avatar.maker.kisscreator.core.extensions.setTextActionBar
import com.couple.avatar.maker.kisscreator.core.extensions.shareApp
import com.couple.avatar.maker.kisscreator.core.extensions.startIntentRightToLeft
import com.couple.avatar.maker.kisscreator.core.extensions.visible
import com.couple.avatar.maker.kisscreator.core.utils.key.IntentKey
import com.couple.avatar.maker.kisscreator.core.utils.state.RateState
import com.couple.avatar.maker.kisscreator.databinding.ActivitySettingsBinding
import com.couple.avatar.maker.kisscreator.ui.language.LanguageActivity
import com.couple.avatar.maker.kisscreator.core.extensions.tap
import com.couple.avatar.maker.kisscreator.core.helper.MusicHelper
import com.couple.avatar.maker.kisscreator.core.helper.RateHelper
import kotlin.jvm.java

class SettingsActivity : BaseActivity<ActivitySettingsBinding>() {
    override fun setViewBinding(): ActivitySettingsBinding {
        return ActivitySettingsBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
       // binding.tvMusic.select()
        initRate()
        initMusic()
        binding.tvSetting.select()
    }

    private fun initMusic() {
        updateMusicUI(sharePreference.isMusicEnabled())
    }

    private fun updateMusicUI(isEnabled: Boolean) {
      //  binding.btnMusic.setImageResource(
//            if (isEnabled) R.drawable.ic_sw_on else R.drawable.ic_sw_off_ms
//        )
    }

    private fun toggleMusic() {
        val isEnabled = !sharePreference.isMusicEnabled()
        sharePreference.setMusicEnabled(isEnabled)
        updateMusicUI(isEnabled)
        if (isEnabled) {
            MusicHelper.play()
        } else {
            MusicHelper.pause()
        }
    }

    override fun viewListener() {
        binding.apply {
           btnActionBarLeft.tap { handleBackLeftToRight() }
        //    layoutMusic.tap { toggleMusic() }
            btnLang.tap { startIntentRightToLeft(LanguageActivity::class.java, IntentKey.INTENT_KEY) }
            btnShareApp.tap(1500) { shareApp() }
            btnRate.tap {
                RateHelper.showRateDialog(this@SettingsActivity, sharePreference){ state ->
                    if (state != RateState.CANCEL){
                        btnRate.gone()
                        showToast(R.string.have_rated)
                    }
                }
            }
            btnPolicy.tap(1500) { policy() }
        }
    }

    override fun initText() {
        //binding.actionBar.tvCenter.select()
    }

    override fun initActionBar() {
//        binding.actionBar.apply {
//            setImageActionBar(btnActionBarLeft, R.drawable.ic_back)
//            setTextActionBar(tvCenter, getString(R.string.settings))
//        }
    }

    private fun initRate() {
        if (sharePreference.getIsRate(this)) {
            binding.btnRate.gone()
        } else {
            binding.btnRate.visible()
        }
    }
}