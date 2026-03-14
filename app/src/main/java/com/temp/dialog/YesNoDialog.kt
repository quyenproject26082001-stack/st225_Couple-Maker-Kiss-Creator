package com.temp.dialog

import android.animation.ObjectAnimator
import android.app.Activity
import android.graphics.Color
import android.view.animation.LinearInterpolator
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.temp.core.extensions.gone
import com.temp.core.extensions.hideNavigation
import com.temp.core.extensions.tap
import com.temp.R
import com.temp.core.base.BaseDialog
import com.temp.core.extensions.strings
import com.temp.core.extensions.visible
import com.temp.databinding.DialogConfirmBinding

enum class DialogType {
    DELETE_EXIT,
    RESET,
    LOADING,
    INTERNET,
    PERMISSION
}

class YesNoDialog(
    val context: Activity,
    val title: Int,
    val description: Int,
    val isError: Boolean = false,
    val dialogType: DialogType = DialogType.DELETE_EXIT
) : BaseDialog<DialogConfirmBinding>(context, maxWidth = true, maxHeight = true) {
    override val layoutId: Int = R.layout.dialog_confirm
    override val isCancelOnTouchOutside: Boolean = false
    override val isCancelableByBack: Boolean = false

    var onNoClick: (() -> Unit) = {}
    var onYesClick: (() -> Unit) = {}
    var onDismissClick: (() -> Unit) = {}

    private var spinAnim: ObjectAnimator? = null

    override fun initView() {
        initText()
        initBackground()
        if (isError) {
            binding.btnNo.gone()
        }
        context.hideNavigation()
        binding.tvTitle.isSelected = true
    }

    private fun initBackground() {
        val bgRes = when (dialogType) {
            DialogType.DELETE_EXIT -> R.drawable.bg_dialog_delete_exit
            DialogType.RESET -> R.drawable.bg_dialog_reset
            DialogType.LOADING -> R.drawable.bg_dialog_loading
            DialogType.INTERNET -> R.drawable.bg_dialog_internet
            DialogType.PERMISSION -> R.drawable.bg_dialog_loading
        }

        binding.containerDialog.setBackgroundResource(bgRes)

        val textColor = when (dialogType) {
            DialogType.DELETE_EXIT -> Color.parseColor("#FFFFFF")
            DialogType.RESET -> Color.parseColor("#FFFFFF")
            DialogType.LOADING -> Color.parseColor("#FFFFFF")
            DialogType.INTERNET -> Color.parseColor("#FFFFFF")
            DialogType.PERMISSION -> Color.parseColor("#FFFFFF")
        }

        binding.tvDescription.setTextColor(textColor)
        binding.btnNo.setTextColor(textColor)

        when (dialogType) {
            DialogType.LOADING -> {
                binding.btnNo.gone()
                binding.btnYes.setBackgroundResource(R.drawable.bg_btn_internet_yes)
                (binding.btnYes.layoutParams as LinearLayout.LayoutParams).marginStart = 0

                // Show spinner and start rotation
                binding.spin.visible()
                spinAnim = ObjectAnimator.ofFloat(binding.spin, "rotation", 0f, 360f).apply {
                    duration = 1000
                    repeatCount = ObjectAnimator.INFINITE
                    interpolator = LinearInterpolator()
                    start()
                }

                // Anchor tvDescription to bottom
                val cs = ConstraintSet()
                cs.clone(binding.containerDialog as ConstraintLayout)
                cs.clear(R.id.tvDescription, ConstraintSet.TOP)
                cs.connect(
                    R.id.tvDescription, ConstraintSet.BOTTOM,
                    ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM,
                    (8 * context.resources.displayMetrics.density).toInt()
                )
                cs.applyTo(binding.containerDialog as ConstraintLayout)
            }
            DialogType.INTERNET -> {
                binding.btnNo.gone()
                binding.btnYes.setBackgroundResource(R.drawable.bg_btn_internet_yes)
                (binding.btnYes.layoutParams as LinearLayout.LayoutParams).marginStart = 0
            }
            DialogType.PERMISSION -> {
                binding.btnNo.setBackgroundResource(R.drawable.bg_btn_permission_no)
                binding.btnYes.setBackgroundResource(R.drawable.bg_btn_permission_yes)
                binding.btnYes.setTextColor(Color.parseColor("#FFFFFF"))
                val paddingVertical = (9 * context.resources.displayMetrics.density).toInt()
                binding.btnNo.setPadding(0, paddingVertical, 0, paddingVertical)
                binding.btnYes.setPadding(0, paddingVertical, 0, paddingVertical)
            }
            DialogType.RESET -> {
                binding.btnNo.setBackgroundResource(R.drawable.bg_btn_permission_no)
                binding.btnYes.setBackgroundResource(R.drawable.bg_btn_permission_yes)
                binding.btnYes.setTextColor(Color.parseColor("#FFFFFF"))
                val paddingVertical = (9 * context.resources.displayMetrics.density).toInt()
                binding.btnNo.setPadding(0, paddingVertical, 0, paddingVertical)
                binding.btnYes.setPadding(0, paddingVertical, 0, paddingVertical)
            }
            DialogType.DELETE_EXIT -> {
                binding.btnNo.setBackgroundResource(R.drawable.bg_btn_permission_no)
                binding.btnYes.setBackgroundResource(R.drawable.bg_btn_permission_yes)
                binding.btnYes.setTextColor(Color.parseColor("#FFFFFF"))
                val paddingVertical = (9 * context.resources.displayMetrics.density).toInt()
                binding.btnNo.setPadding(0, paddingVertical, 0, paddingVertical)
                binding.btnYes.setPadding(0, paddingVertical, 0, paddingVertical)
            }
        }
    }

    override fun initAction() {
        binding.apply {
            btnNo.tap { onNoClick.invoke() }
            btnYes.tap { onYesClick.invoke() }
            flOutSide.tap { onDismissClick.invoke() }
        }
    }

    override fun onDismissListener() {
        spinAnim?.cancel()
        spinAnim = null
    }

    private fun initText() {
        binding.apply {
            tvTitle.text = context.strings(title)
            tvDescription.text = context.strings(description)
            if (isError) {
                btnYes.text = context.strings(R.string.ok)
            }
        }
    }
}
