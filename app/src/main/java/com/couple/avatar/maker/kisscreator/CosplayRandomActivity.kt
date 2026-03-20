package com.couple.avatar.maker.kisscreator

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.core.graphics.createBitmap
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.couple.avatar.maker.kisscreator.core.base.BaseActivity
import com.couple.avatar.maker.kisscreator.core.extensions.gone
import com.couple.avatar.maker.kisscreator.core.extensions.handleBackLeftToRight
import com.couple.avatar.maker.kisscreator.core.extensions.hideNavigation
import com.couple.avatar.maker.kisscreator.core.extensions.invisible
import com.couple.avatar.maker.kisscreator.core.extensions.select
import com.couple.avatar.maker.kisscreator.core.extensions.showInterAll
import com.couple.avatar.maker.kisscreator.core.extensions.startIntentRightToLeft
import com.couple.avatar.maker.kisscreator.core.extensions.tap
import com.couple.avatar.maker.kisscreator.core.extensions.visible
import com.couple.avatar.maker.kisscreator.core.helper.InternetHelper
import com.couple.avatar.maker.kisscreator.core.helper.MediaHelper
import com.couple.avatar.maker.kisscreator.core.utils.key.ValueKey
import com.couple.avatar.maker.kisscreator.core.utils.state.SaveState
import com.couple.avatar.maker.kisscreator.data.model.custom.SuggestionModel
import com.couple.avatar.maker.kisscreator.databinding.ActivityCosplayRandomBinding
import com.couple.avatar.maker.kisscreator.ui.customize.CustomizeCharacterViewModel
import com.couple.avatar.maker.kisscreator.ui.home.DataViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CosplayRandomActivity : BaseActivity<ActivityCosplayRandomBinding>() {

    private val dataViewModel: DataViewModel by viewModels()
    private val customizeViewModel: CustomizeCharacterViewModel by viewModels()
    private var currentSuggestion: SuggestionModel? = null

    override fun setViewBinding(): ActivityCosplayRandomBinding {
        return ActivityCosplayRandomBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        lifecycleScope.launch { showLoading() }
        dataViewModel.ensureData(this)
        binding.titleGuide.select()
        setCosPlayButtonEnabled(false)
    }

    override fun dataObservable() {
        lifecycleScope.launch {
            dataViewModel.allData.collect { data ->
                if (data.isNotEmpty()) {
                    dismissLoading()
                }
            }
        }
    }

    override fun viewListener() {
        binding.apply {
            btnGenerate.tap(800) { handleGenerate() }
            btnCosPlay.tap(800) { handlePlay() }
            actionBar.btnActionBarLeftText.tap { handleBackLeftToRight() }
            actionBar.btnActionBarRight.tap {
                vOverlayGuide.visible()
                containerGuide.visible()
            }
            btnCloseGuide.tap {
                containerGuide.gone()
                vOverlayGuide.gone()
            }

        }
    }

    override fun initActionBar() {
        binding.actionBar.apply {
            btnActionBarLeftText.visible()
            btnActionBarRight.setImageResource(R.drawable.ic_guide)
            btnActionBarRight.visible()
        }
    }

    private fun handleGenerate() {
        val allData = dataViewModel.allData.value
        if (allData.isEmpty()) return
        val hasInternet = InternetHelper.isInternetAvailable(this)
        val filteredData = if (hasInternet) allData else allData.filter { !it.isFromAPI }
        if (filteredData.isEmpty()) return
        val randomData = filteredData.random()
        generateRandomSuggestion(randomData)
    }

    private fun generateRandomSuggestion(data: com.couple.avatar.maker.kisscreator.data.model.custom.CustomizeModel) {
        val exHandler = CoroutineExceptionHandler { _, _ -> }
        CoroutineScope(SupervisorJob() + Dispatchers.IO + exHandler).launch {
            withContext(Dispatchers.Main) { showLoading() }

            customizeViewModel.positionSelected = dataViewModel.allData.value.indexOf(data)
            customizeViewModel.setDataCustomize(data)
            customizeViewModel.updateAvatarPath(data.avatar)
            customizeViewModel.resetDataList()
            customizeViewModel.addValueToItemNavList()
            customizeViewModel.setItemColorDefault()
            val allNavList = data.layerList.mapIndexed { index, layer ->
                com.couple.avatar.maker.kisscreator.data.model.custom.NavigationModel(
                    imageNavigation = layer.imageNavigation,
                    layerIndex = index
                )
            }.toCollection(ArrayList())
            allNavList.firstOrNull()?.isSelected = true
            customizeViewModel.setBottomNavigationList(allNavList)
            customizeViewModel.setClickRandomFullLayer()

            val suggestion = customizeViewModel.getSuggestionList()
            currentSuggestion = suggestion

            val paths = suggestion.pathSelectedList.filter { it.isNotEmpty() }

            if (paths.isEmpty()) {
                withContext(Dispatchers.Main) { dismissLoading() }
                return@launch
            }

            val firstBitmap = Glide.with(this@CosplayRandomActivity)
                .asBitmap().load(paths.first()).submit().get()
            val w = firstBitmap.width / 2
            val h = firstBitmap.height / 2

            val bitmaps = ArrayList<Bitmap>()
            paths.forEach { path ->
                bitmaps.add(
                    Glide.with(this@CosplayRandomActivity)
                        .asBitmap().load(path).submit(w, h).get()
                )
            }

            val combined = createBitmap(w, h)
            val canvas = Canvas(combined)
            bitmaps.forEach { bmp ->
                canvas.drawBitmap(bmp, (w - bmp.width) / 2f, (h - bmp.height) / 2f, null)
            }

            MediaHelper.saveBitmapToInternalStorage(
                this@CosplayRandomActivity, ValueKey.RANDOM_TEMP_ALBUM, combined
            ).collect { state ->
                if (state is SaveState.Success) {
                    suggestion.pathInternalRandom = state.path
                    withContext(Dispatchers.Main) {
                        binding.guidRandom.gone()
                        binding.imvImage.visibility = View.VISIBLE
                        Glide.with(this@CosplayRandomActivity)
                            .load(state.path).into(binding.imvImage)
                        setCosPlayButtonEnabled(true)
                        dismissLoading()
                    }
                }
            }
        }
    }

    private fun setCosPlayButtonEnabled(enabled: Boolean) {
        binding.btnCosPlay.isEnabled = enabled
        binding.btnCosPlay.alpha = if (enabled) 1f else 0.4f
    }

    private fun handlePlay() {
        val suggestion = currentSuggestion ?: return
        lifecycleScope.launch {
            showLoading()
            withContext(Dispatchers.IO) {
                MediaHelper.writeModelToFile(
                    this@CosplayRandomActivity,
                    ValueKey.SUGGESTION_FILE_INTERNAL,
                    suggestion
                )
            }
            dismissLoading()
            showInterAll {
                startIntentRightToLeft(
                    CosplayCustomizeActivity::class.java,
                    customizeViewModel.positionSelected
                )
            }
        }
    }


    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            applyUiCustomize()
            hideNavigation(false)

            window.decorView.removeCallbacks(reHideRunnable)
            window.decorView.postDelayed(reHideRunnable, 1500)
        } else {
            window.decorView.removeCallbacks(reHideRunnable)
        }
    }

    private val reHideRunnable = Runnable {
        applyUiCustomize()
        hideNavigation(false)
    }
    @Suppress("DEPRECATION")
    private fun applyUiCustomize() {
        // Cho phép app tự vẽ màu system bar
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        // Transparent status bar
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        // Flags
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        // nếu muốn icon status bar đen thì thêm:
        // or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    }



}
