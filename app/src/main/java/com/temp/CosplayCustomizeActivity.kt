package com.temp

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.temp.core.base.BaseActivity
import com.temp.core.extensions.gone
import com.temp.core.extensions.handleBackLeftToRight
import com.temp.core.extensions.hideNavigation
import com.temp.core.extensions.invisible
import com.temp.core.extensions.setImageActionBar
import com.temp.core.extensions.showInterAll
import com.temp.core.extensions.tap
import com.temp.core.extensions.visible
import com.temp.core.helper.BitmapHelper
import com.temp.core.helper.MediaHelper
import com.temp.core.utils.key.IntentKey
import com.temp.core.utils.key.ValueKey
import com.temp.core.utils.state.SaveState
import com.temp.data.model.custom.SuggestionModel
import com.temp.databinding.ActivityCosplayCustomizeBinding
import com.temp.ui.customize.BottomNavigationCustomizeAdapter
import com.temp.ui.customize.ColorLayerCustomizeAdapter
import com.temp.ui.customize.CustomizeCharacterViewModel
import com.temp.ui.customize.LayerCustomizeAdapter
import com.temp.ui.home.DataViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CosplayCustomizeActivity : BaseActivity<ActivityCosplayCustomizeBinding>() {

    private val viewModel: CustomizeCharacterViewModel by viewModels()
    private val dataViewModel: DataViewModel by viewModels()

    val layerCustomizeAdapter by lazy { LayerCustomizeAdapter(this) }
    val colorLayerCustomizeAdapter by lazy { ColorLayerCustomizeAdapter(this) }
    val bottomNavigationCustomizeAdapter by lazy { BottomNavigationCustomizeAdapter(this) }

    private var suggestionModel = SuggestionModel()
    private var countdownJob: Job? = null
    private var currentProgress = 0

    override fun setViewBinding(): ActivityCosplayCustomizeBinding {
        return ActivityCosplayCustomizeBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        initRcv()
        binding.tvCountDown.text = "01:00"
        lifecycleScope.launch { showLoading() }
        dataViewModel.ensureData(this)
    }

    override fun dataObservable() {
        lifecycleScope.launch {
            launch {
                dataViewModel.allData.collect { list ->
                    if (list.isNotEmpty()) {
                        viewModel.positionSelected = intent.getIntExtra(IntentKey.INTENT_KEY, 0)
                        val safePos = viewModel.positionSelected.coerceIn(0, list.size - 1)
                        viewModel.setDataCustomize(list[safePos])
                        viewModel.setIsDataAPI(list[safePos].isFromAPI)
                        initData()
                    }
                }
            }
            launch {
                viewModel.bottomNavigationList.collect { navList ->
                    if (navList.isNotEmpty()) {
                        bottomNavigationCustomizeAdapter.submitList(navList)
                        layerCustomizeAdapter.submitList(viewModel.itemNavList[viewModel.positionNavSelected])
                        colorLayerCustomizeAdapter.submitList(viewModel.colorItemNavList[viewModel.positionNavSelected])
                    }
                }
            }
        }
    }

    override fun viewListener() {
        binding.apply {
            btnSamplePhoto.tap(300) { toggleOverlay() }
            btnCloseImage.tap(300) { hideOverlay() }
            btnMan.tap(300) { handleGenderSwitch(1) }
            btnWooman.tap(300) { handleGenderSwitch(2) }
            actionBar.apply {
                btnActionBarLeftText.tap { confirmExit() }
                btnActionBarRight.tap(800) { navigateToSuccess() }
            }
        }
        handleAdapters()
    }

    override fun initActionBar() {
        binding.actionBar.apply {
            btnActionBarLeftText.visible()
            setImageActionBar(btnActionBarRight, R.drawable.ic_close_image)
            tvCenter.gone()
            btnActionBarCenter.invisible()
            btnActionBarRightText.gone()
        }
    }

    private fun initRcv() {
        binding.apply {
            rcvLayer.apply { adapter = layerCustomizeAdapter; itemAnimator = null }
            rcvColor.apply { adapter = colorLayerCustomizeAdapter; itemAnimator = null }
            rcvNavigation.apply { adapter = bottomNavigationCustomizeAdapter; itemAnimator = null }
        }
    }

    private fun handleAdapters() {
        layerCustomizeAdapter.onItemClick = { item, position ->
            lifecycleScope.launch(Dispatchers.IO) {
                val path = viewModel.setClickFillLayer(item, position)
                withContext(Dispatchers.Main) {
                    Glide.with(this@CosplayCustomizeActivity).load(path)
                        .into(viewModel.imageViewList[viewModel.positionCustom])
                    layerCustomizeAdapter.submitList(viewModel.itemNavList[viewModel.positionNavSelected])
                    updateProgress()
                }
            }
        }
        layerCustomizeAdapter.onNoneClick = { position ->
            lifecycleScope.launch(Dispatchers.IO) {
                viewModel.setIsSelectedItem(viewModel.positionCustom)
                viewModel.setPathSelected(viewModel.positionCustom, "")
                viewModel.setKeySelected(viewModel.positionNavSelected, "")
                viewModel.setItemNavList(viewModel.positionNavSelected, position)
                withContext(Dispatchers.Main) {
                    Glide.with(this@CosplayCustomizeActivity)
                        .clear(viewModel.imageViewList[viewModel.positionCustom])
                    layerCustomizeAdapter.submitList(viewModel.itemNavList[viewModel.positionNavSelected])
                    updateProgress()
                }
            }
        }
        layerCustomizeAdapter.onRandomClick = {
            lifecycleScope.launch(Dispatchers.IO) {
                val (path, isMoreColors) = viewModel.setClickRandomLayer()
                withContext(Dispatchers.Main) {
                    Glide.with(this@CosplayCustomizeActivity).load(path)
                        .into(viewModel.imageViewList[viewModel.positionCustom])
                    layerCustomizeAdapter.submitList(viewModel.itemNavList[viewModel.positionNavSelected])
                    if (isMoreColors) {
                        colorLayerCustomizeAdapter.submitList(viewModel.colorItemNavList[viewModel.positionNavSelected])
                    }
                    updateProgress()
                }
            }
        }
        colorLayerCustomizeAdapter.onItemClick = { position ->
            lifecycleScope.launch(Dispatchers.IO) {
                val pathColor = viewModel.setClickChangeColor(position)
                viewModel.updateAllItemsColor(position)
                withContext(Dispatchers.Main) {
                    if (pathColor.isNotEmpty()) {
                        Glide.with(this@CosplayCustomizeActivity).load(pathColor)
                            .into(viewModel.imageViewList[viewModel.positionCustom])
                    }
                    colorLayerCustomizeAdapter.submitList(viewModel.colorItemNavList[viewModel.positionNavSelected])
                    layerCustomizeAdapter.submitList(viewModel.itemNavList[viewModel.positionNavSelected].toList())
                    checkStatusColor()
                    updateProgress()
                }
            }
        }
        bottomNavigationCustomizeAdapter.onItemClick = { pos ->
            val layerIndex = viewModel.bottomNavigationList.value.getOrNull(pos)?.layerIndex ?: pos
            if (layerIndex != viewModel.positionNavSelected) {
                lifecycleScope.launch(Dispatchers.IO) {
                    viewModel.setPositionNavSelected(layerIndex)
                    viewModel.setPositionCustom(viewModel.dataCustomize.value!!.layerList[layerIndex].positionCustom)
                    viewModel.setClickBottomNavigation(pos)
                    withContext(Dispatchers.Main) {
                        layerCustomizeAdapter.submitList(viewModel.itemNavList[viewModel.positionNavSelected])
                        colorLayerCustomizeAdapter.submitList(viewModel.colorItemNavList[viewModel.positionNavSelected])
                        checkStatusColor()
                    }
                }
            }
        }
    }

    private fun initData() {
        val exHandler = CoroutineExceptionHandler { _, _ -> }
        CoroutineScope(SupervisorJob() + Dispatchers.IO + exHandler).launch {
            var pathDefault = ""
            val d1 = async {
                viewModel.updateAvatarPath(viewModel.dataCustomize.value!!.avatar)
                // Load the target suggestion (for comparison / sample photo display)
                val model = MediaHelper.readModelFromFile<SuggestionModel>(
                    this@CosplayCustomizeActivity, ValueKey.SUGGESTION_FILE_INTERNAL
                )
                if (model != null) {
                    suggestionModel = model
                }
                // Always start fresh so the user builds the character from scratch
                viewModel.resetDataList()
                viewModel.addValueToItemNavList()
                viewModel.setItemColorDefault()
                viewModel.setBottomNavigationListDefault()
                val firstLayerIndex = viewModel.bottomNavigationList.value.first().layerIndex
                viewModel.setPositionNavSelected(firstLayerIndex)
                viewModel.setPositionCustom(viewModel.dataCustomize.value!!.layerList[firstLayerIndex].positionCustom)
                pathDefault = viewModel.dataCustomize.value!!.layerList[firstLayerIndex].layer.firstOrNull()?.image ?: ""
                viewModel.setIsSelectedItem(viewModel.positionCustom)
                viewModel.setPathSelected(viewModel.positionCustom, pathDefault)
                viewModel.setKeySelected(viewModel.positionNavSelected, pathDefault)
                return@async true
            }
            val d2 = async(Dispatchers.Main) {
                if (d1.await()) viewModel.setImageViewList(binding.layoutCustomLayer)
                return@async true
            }
            withContext(Dispatchers.Main) {
                if (d1.await() && d2.await()) {
                    // Load the starting default layer image
                    Glide.with(this@CosplayCustomizeActivity).load(pathDefault)
                        .into(viewModel.imageViewList[viewModel.positionCustom])

                    binding.btnMan.isSelected = true
                    binding.btnWooman.isSelected = false
                    layerCustomizeAdapter.submitList(viewModel.itemNavList[viewModel.positionNavSelected])
                    colorLayerCustomizeAdapter.submitList(viewModel.colorItemNavList[viewModel.positionNavSelected])
                    checkStatusColor()

                    // Show target in sample photo button
                    if (suggestionModel.pathInternalRandom.isNotEmpty()) {
                        Glide.with(this@CosplayCustomizeActivity)
                            .load(suggestionModel.pathInternalRandom)
                            .into(binding.btnSamplePhoto)
                        Glide.with(this@CosplayCustomizeActivity)
                            .load(suggestionModel.pathInternalRandom)
                            .into(binding.btnSamplePhotoShow)
                    }

                    viewModel.setIsCreated(true)
                    updateProgress()
                    startCountdown()
                    dismissLoading()
                    hideNavigation(false)
                }
            }
        }
    }

    // ── Color visibility ─────────────────────────────────────────────────────

    private fun handleGenderSwitch(gender: Int) {
        if (viewModel.selectedGender.value == gender) return
        binding.btnMan.isSelected = gender == 1
        binding.btnWooman.isSelected = gender == 2
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.setGender(gender)
            viewModel.setBottomNavigationListDefault()
            val firstLayerIndex = viewModel.bottomNavigationList.value.first().layerIndex
            viewModel.setPositionNavSelected(firstLayerIndex)
            viewModel.setPositionCustom(viewModel.dataCustomize.value!!.layerList[firstLayerIndex].positionCustom)
            withContext(Dispatchers.Main) {
                layerCustomizeAdapter.submitList(viewModel.itemNavList[viewModel.positionNavSelected])
                colorLayerCustomizeAdapter.submitList(viewModel.colorItemNavList[viewModel.positionNavSelected])
                checkStatusColor()
            }
        }
    }

    private fun checkStatusColor() {
        val pos = viewModel.positionNavSelected
        if (pos >= 0 && viewModel.colorItemNavList.size > pos &&
            viewModel.colorItemNavList[pos].isNotEmpty()
        ) {
            binding.color.visible()
            binding.btnColor.gone()
            binding.flColor.visible()
        } else {
            binding.color.invisible()
            binding.flColor.invisible()
        }
    }

    // ── Overlay toggle ──────────────────────────────────────────────────────

    private fun toggleOverlay() {
        if (binding.btnOverLay.visibility == View.VISIBLE) {
            hideOverlay()
        } else {
            binding.btnOverLay.visible()
            binding.containerBtnSamplePhotoShow.visible()
        }
    }

    private fun hideOverlay() {
        binding.btnOverLay.gone()
    }

    // ── Progress ─────────────────────────────────────────────────────────────

    private fun calculateProgress(): Int {
        val targetPaths = suggestionModel.pathSelectedList
        val currentPaths = viewModel.pathSelectedList
        if (targetPaths.isEmpty()) return 0
        val totalNonEmpty = targetPaths.count { it.isNotEmpty() }
        if (totalNonEmpty == 0) return 0
        var matches = 0
        for (i in currentPaths.indices) {
            if (i < targetPaths.size && currentPaths[i].isNotEmpty() && currentPaths[i] == targetPaths[i]) {
                matches++
            }
        }
        return (matches * 100 / totalNonEmpty).coerceIn(0, 100)
    }

    private fun updateProgress() {
        val progress = calculateProgress()
        currentProgress = progress
        binding.progressBar.pivotX = 0f
        binding.progressBar.scaleX = progress / 100f
        binding.progressContainer.post {
            val containerW = binding.progressContainer.width
            val thumbW = binding.tvThumbProgress.width
            val maxX = (containerW - thumbW).toFloat().coerceAtLeast(0f)
            binding.tvThumbProgress.translationX = (progress / 100f) * maxX
            binding.tvThumbProgress.text = "$progress%"
        }
    }

    // ── Countdown ────────────────────────────────────────────────────────────

    private fun startCountdown() {
        countdownJob?.cancel()
        countdownJob = lifecycleScope.launch {
            var secondsLeft = 60
            while (secondsLeft >= 0) {
                val m = secondsLeft / 60
                val s = secondsLeft % 60
                binding.tvCountDown.text = String.format("%02d:%02d", m, s)
                if (secondsLeft == 0) {
                    navigateToSuccess()
                    break
                }
                delay(1000)
                secondsLeft--
            }
        }
    }

    // ── Navigation ───────────────────────────────────────────────────────────

    private fun navigateToSuccess() {
        countdownJob?.cancel()
        lifecycleScope.launch {
            showLoading()
            val savedPath = withContext(Dispatchers.IO) {
                try {
                    val bitmap = BitmapHelper.createBimapFromView(binding.layoutCustomLayer)
                    var path = ""
                    MediaHelper.saveBitmapToInternalStorage(
                        this@CosplayCustomizeActivity, ValueKey.RANDOM_TEMP_ALBUM, bitmap
                    ).collect { state ->
                        if (state is SaveState.Success) path = state.path
                    }
                    path
                } catch (e: Exception) {
                    ""
                }
            }
            dismissLoading()
            val intent = Intent(this@CosplayCustomizeActivity, CosplaySuccessfulActivity::class.java).apply {
                putExtra(IntentKey.INTENT_KEY, savedPath)
                putExtra(IntentKey.STATUS_KEY, currentProgress)
                putExtra(IntentKey.PATH_KEY, suggestionModel.pathInternalRandom)
            }
            val opt = ActivityOptions.makeCustomAnimation(
                this@CosplayCustomizeActivity, R.anim.slide_in_right, R.anim.slide_out_left
            )
            startActivity(intent, opt.toBundle())
            finish()
        }
    }

    private fun confirmExit() {
        showInterAll { handleBackLeftToRight() }
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    override fun onDestroy() {
        super.onDestroy()
        countdownJob?.cancel()
        viewModel.setIsCreated(false)
    }

    @SuppressLint("GestureBackNavigation", "MissingSuperCall")
    override fun onBackPressed() {
        confirmExit()
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
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    }
}
