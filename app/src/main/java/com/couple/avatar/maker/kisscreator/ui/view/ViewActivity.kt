package com.couple.avatar.maker.kisscreator.ui.view

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.couple.avatar.maker.kisscreator.R
import com.couple.avatar.maker.kisscreator.core.base.BaseActivity
import com.couple.avatar.maker.kisscreator.core.extensions.checkPermissions
import com.couple.avatar.maker.kisscreator.core.extensions.goToSettings
import com.couple.avatar.maker.kisscreator.core.extensions.gone
import com.couple.avatar.maker.kisscreator.core.extensions.handleBackLeftToRight
import com.couple.avatar.maker.kisscreator.core.extensions.hideNavigation
import com.couple.avatar.maker.kisscreator.core.extensions.invisible
import com.couple.avatar.maker.kisscreator.core.extensions.loadImage
import com.couple.avatar.maker.kisscreator.core.extensions.loadImageFromFile
import com.couple.avatar.maker.kisscreator.core.extensions.requestPermission
import com.couple.avatar.maker.kisscreator.core.extensions.select
import com.couple.avatar.maker.kisscreator.core.extensions.setImageActionBar
import com.couple.avatar.maker.kisscreator.core.extensions.showInterAll
import com.couple.avatar.maker.kisscreator.core.extensions.tap
import com.couple.avatar.maker.kisscreator.core.extensions.visible
import com.couple.avatar.maker.kisscreator.core.helper.LanguageHelper
import com.couple.avatar.maker.kisscreator.core.utils.key.IntentKey
import com.couple.avatar.maker.kisscreator.core.utils.key.RequestKey
import com.couple.avatar.maker.kisscreator.core.utils.key.ValueKey
import com.couple.avatar.maker.kisscreator.core.utils.key.ValueKey.AVATAR_TYPE
import com.couple.avatar.maker.kisscreator.core.utils.state.HandleState
import com.couple.avatar.maker.kisscreator.databinding.ActivityViewBinding
import com.couple.avatar.maker.kisscreator.dialog.YesNoDialog
import com.couple.avatar.maker.kisscreator.ui.customize.CustomizeCharacterActivity
import com.couple.avatar.maker.kisscreator.ui.home.DataViewModel
import com.couple.avatar.maker.kisscreator.ui.my_creation.view_model.MyAvatarViewModel
import com.couple.avatar.maker.kisscreator.ui.permission.PermissionViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ViewActivity : BaseActivity<ActivityViewBinding>() {
    private val viewModel: ViewViewModel by viewModels()
    private val myAvatarViewModel: MyAvatarViewModel by viewModels()
    private val dataViewModel: DataViewModel by viewModels()
    private val permissionViewModel: PermissionViewModel by viewModels()

    override fun setViewBinding(): ActivityViewBinding {
        return ActivityViewBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        dataViewModel.ensureData(this)
        val path = intent.getStringExtra(IntentKey.INTENT_KEY)!!
        val statusFrom = intent.getIntExtra(IntentKey.STATUS_KEY, ValueKey.AVATAR_TYPE)
        android.util.Log.d("ViewActivity", "initView path=$path statusFrom=$statusFrom")
        android.util.Log.d("ViewActivity", "file exists=${java.io.File(path).exists()} size=${java.io.File(path).length()}")
        viewModel.setPath(path)
        viewModel.updateStatusFrom(statusFrom)
        binding.includeLayoutBottom.tvShare.select()
        binding.includeLayoutBottom.tvDownload.select()

        setButtonBackgrounds()
        setupUI()
    }

    private fun setButtonBackgrounds() {

    }

    private fun setupUI() {
        binding.apply {
            actionBar.apply {
                setImageActionBar(btnActionBarNextRight, R.drawable.ic_download_actionbar)
                setImageActionBar(btnActionBarNextRight1, R.drawable.ic_share_actionbar)
                setImageActionBar(btnActionBarRight, R.drawable.ic_delete_view)
            }
            includeLayoutBottom.apply {
                if (viewModel.statusFrom == ValueKey.AVATAR_TYPE) {
                    btnTelegram.gone()
                    tvShare.setText(R.string.edit)
                    btnWhatsapp.apply {
                        visible()
                        imgBgBtnWhatsapp.setImageResource(R.drawable.bg_btn_edit)
                        layoutParams = layoutParams.apply {
                            width = ViewGroup.LayoutParams.MATCH_PARENT
                        }
                    }
                } else {
                    btnTelegram.visible()
                    btnWhatsapp.visible()
                }
            }

            // Set scaleType based on content type
            if (viewModel.statusFrom == ValueKey.AVATAR_TYPE) {
                imvImage.scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
            } else {
                imvImage.scaleType = android.widget.ImageView.ScaleType.CENTER
            }
        }
    }

    private val editLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val newPath =
                    result.data?.getStringExtra("NEW_PATH") ?: return@registerForActivityResult
                viewModel.setPath(newPath)
                binding.imvImage.loadImageFromFile(newPath)
            }
        }

    override fun dataObservable() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.pathInternal.collect { path ->
                    android.util.Log.d("ViewActivity", "collect path=$path")
                    android.util.Log.d("ViewActivity", "file exists=${java.io.File(path).exists()} size=${java.io.File(path).length()}")
                    loadImage(this@ViewActivity, path, binding.imvImage)
                }
            }
        }
    }

    override fun viewListener() {
        binding.apply {
            actionBar.apply {
                btnActionBarLeftText.tap { handleBack() }
                btnActionBarRight.tap {
                    handleDelete()
                }
                btnActionBarNextRight.tap {
                    handleDownload()
                }
                btnActionBarNextRight1.tap {
                    viewModel.shareFiles(this@ViewActivity)

                }
            }


            includeLayoutBottom.btnWhatsapp.tap(2000) {
                if (viewModel.statusFrom == AVATAR_TYPE) handleEditClick(viewModel.pathInternal.value)
                else viewModel.shareFiles(this@ViewActivity)
            }
            includeLayoutBottom.btnTelegram.tap(2000) {
                checkStoragePermission()
            }
        }
    }

    override fun initActionBar() {
        binding.actionBar.apply {
            btnActionBarLeftText.visible()
            btnActionBarRightText.gone()
            btnActionBarRight.visible()
            if (viewModel.statusFrom == ValueKey.MY_DESIGN_TYPE) {
                btnActionBarNextRight.gone()
                btnActionBarNextRight1.gone()
            } else {
                btnActionBarNextRight.visible()
                btnActionBarNextRight1.visible()
            }
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
            viewModel.downloadFiles(this@ViewActivity).collect { state ->
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

    private fun handleDelete() {
        val dialog =
            YesNoDialog(this, R.string.delete, R.string.are_you_sure_want_to_delete_this_item)
        LanguageHelper.setLocale(this)
        dialog.show()
        dialog.onNoClick = {
            dialog.dismiss()
            hideNavigation()
        }
        dialog.onYesClick = {
            dialog.dismiss()
            lifecycleScope.launch {
                viewModel.deleteFile(this@ViewActivity, viewModel.pathInternal.value)
                    .collect { state ->
                        when (state) {
                            HandleState.LOADING -> showLoading()
                            HandleState.SUCCESS -> {
                                dismissLoading()
                                setResult(Activity.RESULT_OK, Intent().apply {
                                    putExtra("DELETED_PATH", viewModel.pathInternal.value)
                                })
                                finish()
                            }

                            else -> {
                                dismissLoading()
                                showToast(R.string.delete_failed_please_try_again)
                            }
                        }
                    }
            }
        }
    }

    private fun handleBack() {
        handleBackLeftToRight()
    }

    private fun handleEditClick(pathInternal: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            showLoading()
            myAvatarViewModel.editItem(this@ViewActivity, pathInternal, dataViewModel.allData.value)

            withContext(Dispatchers.Main) {
                delay(300)
                dismissLoading()

                myAvatarViewModel.checkDataInternet(this@ViewActivity) {
                    val intent =
                        Intent(this@ViewActivity, CustomizeCharacterActivity::class.java).apply {
                            putExtra(IntentKey.INTENT_KEY, myAvatarViewModel.positionCharacter)
                            putExtra(IntentKey.STATUS_FROM_KEY, ValueKey.EDIT)
                        }

                    showInterAll { editLauncher.launch(intent) }
                    overridePendingTransition(R.anim.slide_out_left, R.anim.slide_in_right)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
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

//    override fun initAds() {
//        initNativeCollab()
//    }
//
//    fun initNativeCollab() {
//
//        loadNativeCollabAds(R.string.native_cl_detail, binding.flNativeCollab)
//
//
//    }

    @android.annotation.SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        handleBack()
    }
}
