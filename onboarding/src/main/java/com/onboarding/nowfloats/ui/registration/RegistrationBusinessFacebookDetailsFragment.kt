package com.onboarding.nowfloats.ui.registration

import android.os.Bundle
import android.view.View
import com.facebook.CallbackManager
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.framework.utils.PreferencesUtils
import com.nowfloats.facebook.FacebookLoginHelper
import com.nowfloats.facebook.constants.FacebookGraphRequestType
import com.nowfloats.facebook.constants.FacebookGraphRequestType.*
import com.nowfloats.facebook.constants.FacebookPermissions
import com.nowfloats.facebook.graph.FacebookGraphManager
import com.nowfloats.facebook.models.FacebookGraphMeAccountResponse
import com.onboarding.nowfloats.constant.RecyclerViewItemType
import com.onboarding.nowfloats.extensions.fadeIn
import com.onboarding.nowfloats.extensions.setGridRecyclerViewAdapter
import com.onboarding.nowfloats.model.channel.ChannelModel
import com.onboarding.nowfloats.model.channel.haveTwitterChannels
import com.onboarding.nowfloats.model.channel.haveWhatsAppChannels
import com.onboarding.nowfloats.model.channel.isFacebookChannel
import com.onboarding.nowfloats.databinding.FragmentRegistrationBusinessFacebookDetailsBinding
import com.onboarding.nowfloats.recyclerView.AppBaseRecyclerViewAdapter

class RegistrationBusinessFacebookDetailsFragment : BaseRegistrationFragment<FragmentRegistrationBusinessFacebookDetailsBinding>(),
    FacebookLoginHelper, FacebookGraphManager.GraphRequestUserAccountCallback {

    private val callbackManager = CallbackManager.Factory.create()
    private var facebookChannelsAdapter: AppBaseRecyclerViewAdapter<ChannelModel>? = null

    companion object {
        @JvmStatic
        fun newInstance(bundle: Bundle? = null): RegistrationBusinessFacebookDetailsFragment {
            val fragment = RegistrationBusinessFacebookDetailsFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView() {
        super.onCreateView()
        registerFacebookLoginCallback(this, callbackManager)
        binding?.facebookChannels?.post {
            (binding?.facebookChannels?.fadeIn()?.mergeWith(binding?.viewBusiness?.fadeIn(1000L)))
                ?.andThen(binding?.title?.fadeIn(200L))?.andThen(binding?.subTitle?.fadeIn(200L))
                ?.andThen(binding?.linkFacebook?.fadeIn(200L))
                ?.andThen(binding?.next?.fadeIn(100L))?.subscribe()
        }
        setOnClickListener(binding?.next, binding?.linkFacebook)
        setSetSelectedFacebookChannels(channels)
    }

    private fun setSetSelectedFacebookChannels(list: ArrayList<ChannelModel>) {
        val selectedItems = list.filter { it.isFacebookChannel() }.map { it.recyclerViewType = RecyclerViewItemType.SELECTED_CHANNEL_ITEM.getLayout(); it }
        facebookChannelsAdapter = binding?.facebookChannels?.setGridRecyclerViewAdapter(baseActivity, selectedItems.size, selectedItems)
        facebookChannelsAdapter?.notifyDataSetChanged()
    }

    override fun onClick(v: View) {
        super.onClick(v)
        when (v) {
            binding?.next -> {
                when {
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
            }
            binding?.linkFacebook -> loginWithFacebook(this, listOf(FacebookPermissions.pages_show_list))
        }
    }

    override fun onFacebookLoginSuccess(result: LoginResult?) {
        showShortToast(result?.toString())
        val accessToken = result?.accessToken ?: return
        PreferencesUtils.instance.saveFacebookUserToken(accessToken.token)
        PreferencesUtils.instance.saveFacebookUserId(accessToken.userId)
        FacebookGraphManager.requestUserAccount(accessToken, this)
    }

    override fun onFacebookLoginCancel() {
        showShortToast("cancel")
    }

    override fun onFacebookLoginError(error: FacebookException?) {
        showShortToast(error?.localizedMessage)
    }

    override fun onCompleted(type: FacebookGraphRequestType, facebookGraphMeAccountResponse: FacebookGraphMeAccountResponse?) {
        if (type != USER_ACCOUNT) return
        val pageName = facebookGraphMeAccountResponse?.data?.first()?.category_list?.first()?.name
        showShortToast(pageName)
    }
}