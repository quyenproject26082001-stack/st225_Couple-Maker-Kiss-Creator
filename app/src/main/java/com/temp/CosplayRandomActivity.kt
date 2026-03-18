package com.temp

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.LayoutInflater
import android.view.View
import androidx.activity.viewModels
import androidx.core.graphics.createBitmap
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.temp.core.base.BaseActivity
import com.temp.core.extensions.gone
import com.temp.core.extensions.handleBackLeftToRight
import com.temp.core.extensions.invisible
import com.temp.core.extensions.select
import com.temp.core.extensions.showInterAll
import com.temp.core.extensions.startIntentRightToLeft
import com.temp.core.extensions.tap
import com.temp.core.extensions.visible
import com.temp.core.helper.InternetHelper
import com.temp.core.helper.MediaHelper
import com.temp.core.utils.key.ValueKey
import com.temp.core.utils.state.SaveState
import com.temp.data.model.custom.SuggestionModel
import com.temp.databinding.ActivityCosplayRandomBinding
import com.temp.ui.customize.CustomizeCharacterViewModel
import com.temp.ui.home.DataViewModel
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
            actionBar.btnActionBarRight.tap { containerGuide.visible() }
            btnCloseGuide.tap { containerGuide.gone() }

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

    private fun generateRandomSuggestion(data: com.temp.data.model.custom.CustomizeModel) {
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
                com.temp.data.model.custom.NavigationModel(
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
                        dismissLoading()
                    }
                }
            }
        }
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
}
