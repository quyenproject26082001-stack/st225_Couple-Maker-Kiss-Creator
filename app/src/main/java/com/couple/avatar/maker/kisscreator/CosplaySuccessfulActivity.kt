package com.couple.avatar.maker.kisscreator

import android.content.pm.PackageManager
import android.os.Build
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.couple.avatar.maker.kisscreator.core.base.BaseActivity
import com.couple.avatar.maker.kisscreator.core.extensions.checkPermissions
import com.couple.avatar.maker.kisscreator.core.extensions.goToSettings
import com.couple.avatar.maker.kisscreator.core.extensions.gone
import com.couple.avatar.maker.kisscreator.core.extensions.handleBackLeftToRight
import com.couple.avatar.maker.kisscreator.core.extensions.invisible
import com.couple.avatar.maker.kisscreator.core.extensions.loadImage
import com.couple.avatar.maker.kisscreator.core.extensions.requestPermission
import com.couple.avatar.maker.kisscreator.core.extensions.showInterAll
import com.couple.avatar.maker.kisscreator.core.extensions.startIntentWithClearTop
import com.couple.avatar.maker.kisscreator.core.extensions.tap
import com.couple.avatar.maker.kisscreator.core.extensions.visible
import com.couple.avatar.maker.kisscreator.core.utils.key.IntentKey
import com.couple.avatar.maker.kisscreator.core.utils.key.RequestKey
import com.couple.avatar.maker.kisscreator.core.utils.state.HandleState
import com.couple.avatar.maker.kisscreator.databinding.ActivityCosplaySuccessfulBinding
import com.couple.avatar.maker.kisscreator.ui.home.HomeActivity
import com.couple.avatar.maker.kisscreator.ui.permission.PermissionViewModel
import com.couple.avatar.maker.kisscreator.ui.success.SuccessViewModel
import kotlinx.coroutines.launch

class CosplaySuccessfulActivity : BaseActivity<ActivityCosplaySuccessfulBinding>() {

    private val viewModel: SuccessViewModel by viewModels()
    private val permissionViewModel: PermissionViewModel by viewModels()

    private var finalProgress = 0
    private var targetImagePath = ""

    override fun setViewBinding(): ActivityCosplaySuccessfulBinding {
        return ActivityCosplaySuccessfulBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        val path = intent.getStringExtra(IntentKey.INTENT_KEY) ?: ""
        finalProgress = intent.getIntExtra(IntentKey.STATUS_KEY, 0)
        targetImagePath = intent.getStringExtra(IntentKey.PATH_KEY) ?: ""
        viewModel.setPath(path)
        updateProgressDisplay()
        if (targetImagePath.isNotEmpty()) {
            loadImage(this, targetImagePath, binding.btnSamplePhoto)
        }
    }

    override fun dataObservable() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.pathInternal.collect { path ->
                    if (path.isNotEmpty()) {
                        loadImage(this@CosplaySuccessfulActivity, path, binding.imvImage)
                    }
                }
            }
        }
    }

    override fun viewListener() {
        binding.apply {
            actionBar.btnActionBarRight.tap {
                showInterAll {
                    startIntentWithClearTop(HomeActivity::class.java)
                }
            }
            includeLayoutBottom.btnWhatsapp.tap(800) {
                showInterAll {
                    startIntentWithClearTop(CosplayRandomActivity::class.java)
                }
            }



            includeLayoutBottom.btnTelegram.tap(2000) {
                viewModel.saveToAvatar(this@CosplaySuccessfulActivity) {
                    showToast(R.string.image_has_been_saved_successfully)
                }
            }
        }
    }

    override fun initActionBar() {
        binding.actionBar.apply {
            btnActionBarRight.visible()
            btnActionBarRight.setImageResource(R.drawable.ic_home)
            tvCenter.gone()
        }
    }

    private fun updateProgressDisplay() {
        binding.progressBar.pivotX = 0f
        binding.progressBar.scaleX = finalProgress / 100f
        binding.progressContainer.post {
            val containerW = binding.progressContainer.width
            val thumbW = binding.tvProgress.width
            val maxX = (containerW - thumbW).toFloat().coerceAtLeast(0f)
            binding.tvProgress.translationX = (finalProgress / 100f) * maxX
            binding.tvProgress.text = "$finalProgress%"
        }
    }

    private fun checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            handleDownload()
        } else {
            val perms = permissionViewModel.getStoragePermissions()
            if (checkPermissions(perms)) {
                handleDownload()
            } else if (permissionViewModel.needGoToSettings(sharePreference, true)) {
                goToSettings()
            } else {
                requestPermission(perms, RequestKey.STORAGE_PERMISSION_CODE)
            }
        }
    }

    private fun handleDownload() {
        lifecycleScope.launch {
            viewModel.downloadFiles(this@CosplaySuccessfulActivity).collect { state ->
                when (state) {
                    HandleState.LOADING -> showLoading()
                    HandleState.SUCCESS -> {
                        dismissLoading()
                        showToast(R.string.download_success)
                    }
                    else -> {
                        dismissLoading()
                        showToast(R.string.download_failed_please_try_again_later)
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RequestKey.STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                permissionViewModel.updateStorageGranted(sharePreference, true)
                handleDownload()
            } else {
                permissionViewModel.updateStorageGranted(sharePreference, false)
            }
        }
    }

    @android.annotation.SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        handleBackLeftToRight()
    }
}
