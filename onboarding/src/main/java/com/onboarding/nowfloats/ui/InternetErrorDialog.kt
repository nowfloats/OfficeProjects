package com.onboarding.nowfloats.ui

import com.framework.base.BaseDialogFragment
import com.framework.models.BaseViewModel
import com.onboarding.nowfloats.R
import com.onboarding.nowfloats.databinding.DialogInternetErrorBinding

class InternetErrorDialog : BaseDialogFragment<DialogInternetErrorBinding, BaseViewModel>() {

    override fun getLayout(): Int {
        return R.layout.dialog_internet_error
    }

    override fun getViewModelClass(): Class<BaseViewModel> {
        return BaseViewModel::class.java
    }

    override fun getTheme(): Int {
        return R.style.MaterialDialogThemeFull
    }

    override fun onViewCreated() {
        isCancelable = false
        binding?.retryBtn?.setOnClickListener { dismiss() }
    }
}