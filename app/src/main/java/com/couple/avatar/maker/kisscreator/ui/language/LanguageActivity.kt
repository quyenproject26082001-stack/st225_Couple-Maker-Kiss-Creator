package com.couple.avatar.maker.kisscreator.ui.language

import android.annotation.SuppressLint
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.lvt.ads.util.Admob
import com.couple.avatar.maker.kisscreator.R
import com.couple.avatar.maker.kisscreator.core.base.BaseActivity
import com.couple.avatar.maker.kisscreator.core.extensions.handleBackLeftToRight
import com.couple.avatar.maker.kisscreator.core.extensions.invisible
import com.couple.avatar.maker.kisscreator.core.extensions.select
import com.couple.avatar.maker.kisscreator.core.extensions.startIntentRightToLeft
import com.couple.avatar.maker.kisscreator.core.extensions.startIntentWithClearTop
import com.couple.avatar.maker.kisscreator.core.extensions.visible
import com.couple.avatar.maker.kisscreator.core.utils.key.IntentKey
import com.couple.avatar.maker.kisscreator.databinding.ActivityLanguageBinding
import com.couple.avatar.maker.kisscreator.ui.home.HomeActivity
import com.couple.avatar.maker.kisscreator.ui.intro.IntroActivity
import com.couple.avatar.maker.kisscreator.core.extensions.tap
import com.couple.avatar.maker.kisscreator.core.extensions.strings
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

class LanguageActivity : BaseActivity<ActivityLanguageBinding>() {
    private val viewModel: LanguageViewModel by viewModels()

    private val languageAdapter by lazy { LanguageAdapter(this) }

    override fun setViewBinding(): ActivityLanguageBinding {
        return ActivityLanguageBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        initRcv()
        val intentValue = intent.getStringExtra(IntentKey.INTENT_KEY)
        val currentLang = sharePreference.getPreLanguage()
        viewModel.setFirstLanguage(intentValue == null)
        viewModel.loadLanguages(currentLang)
        binding.tvLang.select()

    }

    override fun dataObservable() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.isFirstLanguage.collect { isFirst ->
                        languageAdapter.isFirstLanguage = isFirst
                        if (isFirst) {
                            binding.btnDone.invisible()
                        } else {
                            binding.btnDone.invisible()
                            binding.btnBackLangSetting.visible()
                            binding.btnDoneLangSetting.visible()
                        }
                    }
                }
                launch {
                    viewModel.languageList.collect { list ->
                        languageAdapter.submitList(list)
                    }
                }
                launch {
                    viewModel.codeLang.collect { code ->
                        if (code.isNotEmpty() && viewModel.isFirstLanguage.value) {
                            binding.btnDone.visible()
                        }
                    }
                }
            }
        }
    }

    override fun viewListener() {
        binding.apply {
            btnBackLangSetting.tap { handleBackLeftToRight() }
            btnDone.tap { handleDone() }
            btnDoneLangSetting.tap { handleDone() }
        }
        handleRcv()
    }

    override fun initText() {
        // binding.actionBar.tvCenter.select()
        //binding.actionBar.tvStart.select()
    }

    override fun initActionBar() {
//        binding.actionBar.apply {
//            btnActionBarLeft.setImageResource(R.drawable.ic_back)
//            btnDone.setImageResource(R.drawable.ic_done)
//            val text = R.string.language
//            tvCenter.text = strings(text)
//            tvStart.text = strings(text)
//        }
    }

    private fun initRcv() {
        binding.rcv.apply {
            adapter = languageAdapter
            itemAnimator = null
        }
    }

    private fun handleRcv() {
        binding.apply {
            languageAdapter.onItemClick = { code ->
                //  binding.actionBar.btnDone.visible()
                viewModel.selectLanguage(code)
            }
        }
    }

    private fun handleDone() {
        val code = viewModel.codeLang.value
        if (code.isEmpty()) {
            showToast(R.string.not_select_lang)
            return
        }
        sharePreference.setPreLanguage(code)

        if (viewModel.isFirstLanguage.value) {
            sharePreference.setIsFirstLang(false)
            startIntentRightToLeft(IntroActivity::class.java)
            finishAffinity()
        } else {
            startIntentWithClearTop(HomeActivity::class.java)
        }
    }

    @SuppressLint("MissingSuperCall", "GestureBackNavigation")
    override fun onBackPressed() {
        if (!viewModel.isFirstLanguage.value) {
            handleBackLeftToRight()
        } else {
            exitProcess(0)
        }
    }

    // Chỉ tắt nhạc khi là màn language đầu tiên
    override fun shouldPlayBackgroundMusic(): Boolean = false

//    override fun initAds() {
//        Admob.getInstance().loadNativeAd(this@LanguageActivity, getString(R.string.native_language), binding.nativeAds, R.layout.ads_native_big_btn_bottom)
//    }

}