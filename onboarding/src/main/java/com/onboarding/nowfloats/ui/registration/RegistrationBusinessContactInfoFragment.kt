package com.onboarding.nowfloats.ui.registration

import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import com.framework.extensions.isVisible
import com.onboarding.nowfloats.extensions.fadeIn
import com.onboarding.nowfloats.R
import com.onboarding.nowfloats.databinding.FragmentRegistrationBusinessContactInfoBinding

class RegistrationBusinessContactInfoFragment : BaseRegistrationFragment<FragmentRegistrationBusinessContactInfoBinding>() {

    companion object {
        @JvmStatic
        fun newInstance(bundle: Bundle? = null): RegistrationBusinessContactInfoFragment {
            val fragment = RegistrationBusinessContactInfoFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView() {
        super.onCreateView()
        binding?.viewImage?.post {
            (binding?.viewImage?.fadeIn(600L)?.mergeWith(binding?.viewBusiness?.fadeIn()))
                ?.andThen(binding?.title?.fadeIn(150L)?.mergeWith(binding?.subTitle?.fadeIn(150L)))
                ?.andThen(binding?.viewForm?.fadeIn())?.andThen(binding?.next?.fadeIn(150L))
                ?.subscribe()
        }
        binding?.contactInfo = viewModel
        setOnClickListener(binding?.next)
    }

    override fun onClick(v: View) {
        when (v) {
            binding?.next -> if (binding?.textBtn?.isVisible() == true && isValid()) {
                requestFloatsModel?.contactInfo = viewModel
                getDotProgress()?.let {
                    binding?.textBtn?.visibility = GONE
                    binding?.next?.addView(it)
                    it.startAnimation()
                    Handler().postDelayed({
                        it.stopAnimation()
                        it.removeAllViews()
                        binding?.textBtn?.visibility = VISIBLE
                        gotoBusinessWebsite()
                    }, 1000)
                }
            }
        }
    }

    private fun isValid(): Boolean {
        viewModel?.storeName = binding?.storeName?.text?.toString()
        viewModel?.address = binding?.address?.text?.toString()
        viewModel?.email = binding?.email?.text?.toString()
        viewModel?.number = binding?.number?.text?.toString()
        return viewModel?.let {
            return if (it.storeName.isNullOrBlank()) {
                showShortToast(resources.getString(R.string.business_cant_empty))
                false
            } else if (it.address.isNullOrBlank()) {
                showShortToast(resources.getString(R.string.business_address_cant_empty))
                false
            } else if (!it.isEmailValid()) {
                showShortToast(resources.getString(R.string.email_invalid))
                false
            } else if (!it.isNumberValid()) {
                showShortToast(resources.getString(R.string.phone_number_invalid))
                false
            } else true
        } ?: false
    }
}