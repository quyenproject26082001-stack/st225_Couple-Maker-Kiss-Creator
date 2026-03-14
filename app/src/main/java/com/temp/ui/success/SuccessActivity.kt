package com.temp.ui.success

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.lvt.ads.util.Admob
import com.temp.R
import com.temp.core.base.BaseActivity
import com.temp.core.extensions.checkPermissions
import com.temp.core.extensions.goToSettings
import com.temp.core.extensions.gone
import com.temp.core.extensions.handleBackLeftToRight
import com.temp.core.extensions.invisible
import com.temp.core.extensions.loadImage
import com.temp.core.extensions.loadNativeCollabAds
import com.temp.core.extensions.requestPermission
import com.temp.core.extensions.select
import com.temp.core.extensions.setImageActionBar
import com.temp.core.extensions.setTextActionBar
import com.temp.core.extensions.showInterAll
import com.temp.core.extensions.startIntentRightToLeft
import com.temp.core.extensions.startIntentWithClearTop
import com.temp.core.extensions.strings
import com.temp.core.extensions.tap
import com.temp.core.extensions.visible
import com.temp.core.helper.UnitHelper
import com.temp.core.utils.key.IntentKey
import com.temp.core.utils.key.RequestKey
import com.temp.core.utils.key.ValueKey
import com.temp.core.utils.state.HandleState
import com.temp.databinding.ActivitySuccessBinding
import com.temp.ui.home.HomeActivity
import com.temp.ui.my_creation.MyCreationActivity
import com.temp.ui.permission.PermissionViewModel
import kotlinx.coroutines.launch

class SuccessActivity : BaseActivity<ActivitySuccessBinding>() {
    private val viewModel: SuccessViewModel by viewModels()
    private val permissionViewModel: PermissionViewModel by viewModels()

    override fun setViewBinding(): ActivitySuccessBinding {
        return ActivitySuccessBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        viewModel.setPath(intent.getStringExtra(IntentKey.INTENT_KEY) ?: "")
        setButtonBackgrounds()
    }

    private fun setButtonBackgrounds() {
        binding.includeLayoutBottom.apply {
            
            tvDownload.select()
            tvShare.select()

        }
    }

    override fun dataObservable() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.pathInternal.collect { path ->
                        if (path.isNotEmpty()) {
                            loadImage(this@SuccessActivity, path, binding.imvImage)
                        }
                    }
                }
            }
        }
    }

    private fun handleBack() {
        handleBackLeftToRight()
    }
    override fun viewListener() {
        binding.apply {
            actionBar.apply {
                btnActionBarNextRight.tap {
                    showInterAll {
                        startIntentWithClearTop(HomeActivity::class.java)
                    }
                }
                btnActionBarLeftText.tap {  handleBack()  }

                btnActionBarRight.tap(2000){
                    viewModel.shareFiles(this@SuccessActivity)
                }
            }

            // My Album button
            includeLayoutBottom.btnWhatsapp.tap(2590) {
                showInterAll {
                    startIntentRightToLeft(MyCreationActivity::class.java, IntentKey.TAB_KEY, ValueKey.MY_DESIGN_TYPE)
                }
            }

            // Download button
            includeLayoutBottom.btnTelegram.tap(2000) {
                checkStoragePermission()
            }

        }
    }

    override fun initActionBar() {
        binding.actionBar.apply {

            btnActionBarLeftText.visible()
            btnActionBarRight.visible()
            btnActionBarRight.setBackgroundResource(R.drawable.ic_share)
            tvCenter.invisible()
            imgCenter.gone()
                setImageActionBar(btnActionBarNextRight, R.drawable.ic_home)
            btnActionBarNextRight.visible()

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
            viewModel.downloadFiles(this@SuccessActivity).collect { state ->
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
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

//    override fun initAds() {
//        initNativeCollab()
//    }
//
//    fun initNativeCollab() {
//
//        loadNativeCollabAds(R.string.native_cl_success, binding.flNativeCollab)
//
//
//    }

    @android.annotation.SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        handleBackLeftToRight()
    }
}
