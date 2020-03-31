package com.onboarding.nowfloats.ui.registration

import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.View
import androidx.lifecycle.Observer
import com.framework.base.BaseResponse
import com.framework.exceptions.NoNetworkException
import com.framework.extensions.getDrawable
import com.framework.extensions.observeOnce
import com.onboarding.nowfloats.R
import com.onboarding.nowfloats.constant.RecyclerViewItemType
import com.onboarding.nowfloats.databinding.FragmentRegistrationBusinessWebsiteBinding
import com.onboarding.nowfloats.extensions.afterTextChanged
import com.onboarding.nowfloats.extensions.drawableEnd
import com.onboarding.nowfloats.extensions.fadeIn
import com.onboarding.nowfloats.extensions.setGridRecyclerViewAdapter
import com.onboarding.nowfloats.model.channel.*
import com.onboarding.nowfloats.model.channel.ChannelType
import com.onboarding.nowfloats.model.domain.BusinessDomainRequest
import com.onboarding.nowfloats.recyclerView.AppBaseRecyclerViewAdapter
import com.onboarding.nowfloats.ui.InternetErrorDialog

class RegistrationBusinessWebsiteFragment : BaseRegistrationFragment<FragmentRegistrationBusinessWebsiteBinding>() {

    private var googleChannelsAdapter: AppBaseRecyclerViewAdapter<ChannelModel>? = null
    private var isDomain: Boolean = false
    private var domainValue: String? = null

    companion object {
        @JvmStatic
        fun newInstance(bundle: Bundle? = null): RegistrationBusinessWebsiteFragment {
            val fragment = RegistrationBusinessWebsiteFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView() {
        super.onCreateView()
        binding?.googleChannels?.post {
            (binding?.googleChannels?.fadeIn(1000L)?.mergeWith(binding?.viewBusiness?.fadeIn()))
                    ?.andThen(binding?.title?.fadeIn(200L))?.andThen(binding?.subTitle?.fadeIn(200L))
                    ?.andThen(binding?.subdomain?.fadeIn()?.mergeWith(binding?.inputType?.fadeIn(200L)))
                    ?.andThen(binding?.next?.fadeIn(100L))?.subscribe()
        }
        setOnClickListener(binding?.next)
        setSetSelectedGoogleChannels(channels)
        val contactInfo = requestFloatsModel?.contactInfo

        val subdomain = contactInfo?.domainName ?: contactInfo?.businessName ?: ""
        setSubDomain(subdomain, isInitial = true)
        apiCheckDomain(subdomain)

        binding?.subdomain?.afterTextChanged { setSubDomain(it) }
    }


    private fun setSubDomain(storeName: String?, isInitial: Boolean = false) {
        val subDomain = storeName?.filter { it.isLetterOrDigit() } ?: return
        val lengthDifference = storeName.length - subDomain.length
        if (subDomain != storeName || isInitial) {
            val selection = binding?.subdomain?.selectionEnd?.minus(lengthDifference) ?: return
            binding?.subdomain?.setText(subDomain.toLowerCase())
            if (selection > 1) binding?.subdomain?.setSelection(selection)
        }
        apiCheckDomain(subDomain.toLowerCase())
    }

    private fun apiCheckDomain(subDomain: String, onSuccess: ()->Unit = {}) {
        if (!TextUtils.isEmpty(subDomain)) {
            val data = BusinessDomainRequest(clientId, subDomain, requestFloatsModel?.contactInfo?.businessName)
            viewModel?.postCheckBusinessDomain(data)?.observeOnce(viewLifecycleOwner, Observer {
                onPostBusinessDomainCheckResponse(it, onSuccess)
            })
        } else binding?.subdomain?.drawableEnd = null
    }

    private fun onPostBusinessDomainCheckResponse(response: BaseResponse, onSuccess: () -> Unit) {
        if (response.error is NoNetworkException){
            InternetErrorDialog().show(parentFragmentManager, InternetErrorDialog::class.java.name)
            return
        }

        if (response.status == 200 || response.status == 201 || response.status == 202) {
            isDomain = true
            domainValue = if (!response.stringResponse.isNullOrEmpty()) response.stringResponse else binding?.subdomain?.text.toString().toUpperCase()
            binding?.subdomain?.drawableEnd = resources.getDrawable(baseActivity, R.drawable.ic_valid)
            onSuccess()
        } else {
            isDomain = false
            domainValue = ""
            binding?.subdomain?.drawableEnd = resources.getDrawable(baseActivity, R.drawable.ic_error)
        }
    }

    private fun setSetSelectedGoogleChannels(list: ArrayList<ChannelModel>) {
        val selectedItems = ArrayList(list.filter { it.isGoogleChannel() }.map {
            it.recyclerViewType = RecyclerViewItemType.SELECTED_CHANNEL_ITEM.getLayout(); it
        })
        val googleBusiness = ChannelModel(type = ChannelType.G_BUSINESS.name)
        googleBusiness.recyclerViewType = RecyclerViewItemType.SELECTED_CHANNEL_ITEM.getLayout()
        selectedItems.add(googleBusiness)
        googleChannelsAdapter = binding?.googleChannels?.setGridRecyclerViewAdapter(
                baseActivity,
                selectedItems.size,
                selectedItems
        )
        googleChannelsAdapter?.notifyDataSetChanged()
    }

    override fun onClick(v: View) {
        when (v) {
            binding?.next ->
                if (domainValue.isNullOrEmpty().not() && isDomain) {
                    requestFloatsModel?.contactInfo?.domainName = domainValue
                    if ((binding?.textBtn?.visibility == View.VISIBLE)) {
                        getDotProgress()?.let {
                            binding?.textBtn?.visibility = View.GONE
                            binding?.next?.addView(it)
                            it.startAnimation()
                            Handler().postDelayed({
                                it.stopAnimation()
                                it.removeAllViews()
                                binding?.textBtn?.visibility = View.VISIBLE
                                when {
                                    channels.haveFacebookPage() -> {
                                        gotoFacebookPage()
                                    }
                                    channels.haveFacebookShop() -> {
                                        gotoFacebookShop()
                                    }
                                    channels.haveTwitterChannels() -> {
                                        gotoTwitterDetails()
                                    }
                                    channels.haveWhatsAppChannels() -> {
                                        gotoWhatsAppCallDetails()
                                    }
                                    else -> {
                                        gotoBusinessApiCallDetails()
                                    }
                                }
                            }, 1000)
                        }
                    }
                }
                else{
                    apiCheckDomain(binding?.subdomain?.text?.toString() ?: "")
                    {binding?.next?.performClick()}
                }
        }

    }

    override fun clearInfo() {
        requestFloatsModel?.contactInfo?.domainName = null
        super.clearInfo()
    }
}