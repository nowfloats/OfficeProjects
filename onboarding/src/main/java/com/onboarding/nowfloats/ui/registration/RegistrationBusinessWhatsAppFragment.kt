package com.onboarding.nowfloats.ui.registration

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import com.framework.extensions.getDrawable
import com.onboarding.nowfloats.R
import com.onboarding.nowfloats.constant.RecyclerViewItemType
import com.onboarding.nowfloats.databinding.FragmentRegistrationBusinessWhatsappBinding
import com.onboarding.nowfloats.extensions.afterTextChanged
import com.onboarding.nowfloats.extensions.drawableEnd
import com.onboarding.nowfloats.extensions.fadeIn
import com.onboarding.nowfloats.extensions.setGridRecyclerViewAdapter
import com.onboarding.nowfloats.model.channel.ChannelModel
import com.onboarding.nowfloats.model.channel.isWhatsAppChannel
import com.onboarding.nowfloats.model.channel.request.ChannelActionData
import com.onboarding.nowfloats.model.channel.request.isLinked
import com.onboarding.nowfloats.recyclerView.AppBaseRecyclerViewAdapter
import com.thedevelopercat.sonic.utils.ValidationUtils

class RegistrationBusinessWhatsAppFragment : BaseRegistrationFragment<FragmentRegistrationBusinessWhatsappBinding>() {

    private var whatsAppData: ChannelActionData = ChannelActionData()
    private var whatsAppAdapter: AppBaseRecyclerViewAdapter<ChannelModel>? = null

    companion object {
        @JvmStatic
        fun newInstance(bundle: Bundle? = null): RegistrationBusinessWhatsAppFragment {
            val fragment = RegistrationBusinessWhatsAppFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView() {
        super.onCreateView()
        setSavedData()
        baseActivity.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        var confirmButtonAlpha = 0.3f
        if (ValidationUtils.isMobileNumberValid(binding?.number?.text?.toString() ?: "")){
            confirmButtonAlpha = 1f
        }

        binding?.whatsappChannels?.post {
            (binding?.whatsappChannels?.fadeIn(1000L)?.mergeWith(binding?.viewBusiness?.fadeIn()))
                ?.andThen(binding?.title?.fadeIn(200L))?.andThen(binding?.subTitle?.fadeIn(200L))
                ?.andThen(
                    binding?.edtView?.fadeIn()?.mergeWith(binding?.confirmBtn?.fadeIn(200L, confirmButtonAlpha))
                )
                ?.andThen(binding?.skip?.fadeIn(100L))?.subscribe()
        }
        setOnClickListener(binding?.confirmBtn, binding?.skip)
        setSetSelectedWhatsAppChannel(channels)
        binding?.number?.afterTextChanged { checkValidNumber(it) }
    }

    override fun setSavedData() {
        val whatsAppData = requestFloatsModel?.
        channelActionDatas?.firstOrNull()?: return

        requestFloatsModel?.channelActionDatas?.remove(whatsAppData)
        this.whatsAppData = whatsAppData

        binding?.number?.setText(whatsAppData.active_whatsapp_number)
        binding?.confirmBtn?.post {
            onNumberValid(ValidationUtils.isMobileNumberValid(binding?.number?.text?.toString() ?: ""))
        }
    }

    private fun checkValidNumber(phoneNumber: String?) {
        val number = phoneNumber ?: return
        val isNumberValid = ValidationUtils.isMobileNumberValid(number)

        onNumberValid(isNumberValid)
    }

    private fun onNumberValid(isNumberValid: Boolean) {
        if (isNumberValid) {
            binding?.number?.drawableEnd = resources.getDrawable(baseActivity, R.drawable.ic_valid)
            binding?.confirmBtn?.alpha = 1f
            binding?.skip?.text = resources.getString(R.string.skip)
            whatsAppData.active_whatsapp_number = binding?.number?.text?.toString()
        } else {
            binding?.number?.drawableEnd = null
            binding?.confirmBtn?.alpha = 0.3f
            binding?.skip?.text = resources.getString(R.string.i_don_t_have_one_will_do_later)
        }
    }

    private fun setSetSelectedWhatsAppChannel(list: ArrayList<ChannelModel>) {
        val selectedItems = ArrayList(list.filter { it.isWhatsAppChannel() }.map {
            it.recyclerViewType = RecyclerViewItemType.SELECTED_CHANNEL_ITEM.getLayout(); it
        })

        whatsAppAdapter = binding?.whatsappChannels?.setGridRecyclerViewAdapter(
            baseActivity,
            selectedItems.size,
            selectedItems
        )
        whatsAppAdapter?.notifyDataSetChanged()
    }

    override fun onClick(v: View) {
        when (v) {
            binding?.confirmBtn -> {
                if (binding?.number?.length() == 10) gotoBusinessApiCallDetails()
            }
            binding?.skip -> gotoBusinessApiCallDetails()
        }
    }

    override fun gotoBusinessApiCallDetails() {
        if (whatsAppData.isLinked()) {
            requestFloatsModel?.channelActionDatas?.add(whatsAppData)
        }
        super.gotoBusinessApiCallDetails()
    }

    override fun clearInfo() {
        super.clearInfo()
        requestFloatsModel?.channelActionDatas?.clear()
    }
}