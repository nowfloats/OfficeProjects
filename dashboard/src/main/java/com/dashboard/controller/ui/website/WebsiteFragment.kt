package com.dashboard.controller.ui.website

import android.view.View
import androidx.core.content.ContextCompat.getColor
import com.dashboard.R
import com.dashboard.base.AppBaseFragment
import com.dashboard.constant.RecyclerViewActionType
import com.dashboard.constant.RecyclerViewItemType
import com.dashboard.controller.getDomainName
import com.dashboard.controller.ui.dashboard.checkIsPremiumUnlock
import com.dashboard.databinding.FragmentWebsiteBinding
import com.dashboard.model.live.websiteItem.WebsiteActionItem
import com.dashboard.model.live.websiteItem.WebsiteDataResponse
import com.dashboard.pref.BASE_IMAGE_URL
import com.dashboard.pref.Key_Preferences
import com.dashboard.pref.UserSessionManager
import com.dashboard.recyclerView.AppBaseRecyclerViewAdapter
import com.dashboard.recyclerView.BaseRecyclerViewItem
import com.dashboard.recyclerView.RecyclerItemClickListener
import com.dashboard.utils.*
import com.dashboard.viewmodel.DashboardViewModel
import com.framework.extensions.observeOnce
import com.framework.glide.util.glideLoad
import com.framework.utils.fromHtml
import java.util.*

class WebsiteFragment : AppBaseFragment<FragmentWebsiteBinding, DashboardViewModel>(), RecyclerItemClickListener {

  private var session: UserSessionManager? = null
  private var adapterWebsite: AppBaseRecyclerViewAdapter<WebsiteActionItem>? = null

  override fun getLayout(): Int {
    return R.layout.fragment_website
  }

  override fun getViewModelClass(): Class<DashboardViewModel> {
    return DashboardViewModel::class.java
  }

  override fun onCreateView() {
    super.onCreateView()
    session = UserSessionManager(baseActivity)
    getWebsiteData()
    setUserData()
    setOnClickListener(binding?.txtDomainName, binding?.btnProfileLogo, binding?.editProfile)
    WebEngageController.trackEvent("Website Page", "pageview", session?.fpTag)
  }

  private fun setUserData() {
    val desc = session?.getFPDetails(Key_Preferences.GET_FP_DETAILS_DESCRIPTION)
    binding?.txtDesc?.text = if (desc.isNullOrEmpty().not()) desc else ""
    binding?.txtBusinessName?.text = session?.getFPDetails(Key_Preferences.GET_FP_DETAILS_BUSINESS_NAME)
    binding?.txtDomainName?.text = fromHtml("<u>${session!!.getDomainName()}</u>")
    var imageUri = session?.getFPDetails(Key_Preferences.GET_FP_DETAILS_IMAGE_URI)
    if (imageUri.isNullOrEmpty().not() && imageUri!!.contains("http").not()) {
      imageUri = BASE_IMAGE_URL + imageUri
    }
    binding?.imgProfileLogo?.let {
      if (imageUri.isNullOrEmpty().not()) {
        baseActivity.glideLoad(mImageView = it, url = imageUri!!,placeholder = R.drawable.gradient_white,isLoadBitmap = true)
      } else it.setImageResource(R.drawable.ic_add_logo_d)
    }
  }

  private fun getWebsiteData() {
    viewModel?.getBoostWebsiteItem(baseActivity)?.observeOnce(viewLifecycleOwner, { it0 ->
      val response = it0 as? WebsiteDataResponse
      if (response?.isSuccess() == true && response.data.isNullOrEmpty().not()) {
        val data = response.data?.firstOrNull { it.type.equals(session?.fP_AppExperienceCode, ignoreCase = true) }
        if (data != null && data.actionItem.isNullOrEmpty().not()) {
          data.actionItem!!.map { it2 -> if (it2.premiumCode.isNullOrEmpty().not() && session.checkIsPremiumUnlock(it2.premiumCode).not()) it2.isLock = true }
          binding?.mainContent?.setBackgroundColor(getColor(baseActivity, if (data.actionItem!!.size % 2 != 0) R.color.bg_grey_light else R.color.white))
          setAdapterCustomer(data.actionItem!!)
        }
      }
    })
  }

  private fun setAdapterCustomer(actionItem: ArrayList<WebsiteActionItem>) {
    actionItem.map { it.recyclerViewItemType = RecyclerViewItemType.BOOST_WEBSITE_ITEM_VIEW.getLayout() }
    binding?.rvEnquiries?.apply {
      if (adapterWebsite == null) {
        adapterWebsite = AppBaseRecyclerViewAdapter(baseActivity, actionItem, this@WebsiteFragment)
        adapter = adapterWebsite
      } else adapterWebsite?.notify(actionItem)
    }
  }

  override fun onItemClick(position: Int, item: BaseRecyclerViewItem?, actionType: Int) {
    when (actionType) {
      RecyclerViewActionType.WEBSITE_ITEM_CLICK.ordinal -> {
        val data = item as? WebsiteActionItem ?: return
        data.type?.let { WebsiteActionItem.IconType.fromName(it) }?.let { clickActionButton(it) }
      }
    }
  }

  private fun clickActionButton(type: WebsiteActionItem.IconType) {
    when (type) {
      WebsiteActionItem.IconType.service_product_catalogue -> baseActivity.startListServiceProduct(session)
      WebsiteActionItem.IconType.latest_update_tips -> session?.let { baseActivity.startUpdateLatestStory(it) }
      WebsiteActionItem.IconType.all_images -> baseActivity.startAllImage(session)
      WebsiteActionItem.IconType.business_profile -> baseActivity.startFragmentsFactory(session, fragmentType = "Business_Profile_Fragment_V2")
      WebsiteActionItem.IconType.testimonials -> baseActivity.startTestimonial(session)
      WebsiteActionItem.IconType.custom_page -> baseActivity.startCustomPage(session)
      WebsiteActionItem.IconType.project_teams -> baseActivity.startListProjectAndTeams(session)
      WebsiteActionItem.IconType.unlimited_digital_brochures -> baseActivity.startAddDigitalBrochure(session)
      WebsiteActionItem.IconType.toppers_institute -> baseActivity.startListToppers(session)
      WebsiteActionItem.IconType.upcoming_batches -> baseActivity.startListBatches(session)
      WebsiteActionItem.IconType.faculty_management -> baseActivity.startFacultyMember(session)
      WebsiteActionItem.IconType.places_look_around -> baseActivity.startNearByView(session)
      WebsiteActionItem.IconType.trip_adviser_ratings -> baseActivity.startListTripAdvisor(session)
      WebsiteActionItem.IconType.seasonal_offers -> baseActivity.startListSeasonalOffer(session)
    }
  }

  override fun onClick(v: View) {
    super.onClick(v)
    when (v) {
      binding?.txtDomainName -> baseActivity.startWebViewPageLoad(session, session!!.getDomainName(false))
      binding?.btnProfileLogo -> baseActivity.startBusinessDescriptionEdit(session)
      binding?.editProfile -> baseActivity.startFragmentsFactory(session, fragmentType = "Business_Profile_Fragment_V2")

    }
  }
}