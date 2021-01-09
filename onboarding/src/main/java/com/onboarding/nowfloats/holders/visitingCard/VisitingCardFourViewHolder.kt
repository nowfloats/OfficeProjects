package com.onboarding.nowfloats.holders.visitingCard

import com.framework.extensions.gone
import com.framework.extensions.visible
import com.framework.glide.util.glideLoad
import com.framework.utils.fromHtml
import com.onboarding.nowfloats.databinding.ItemVisitingCardFourBinding
import com.onboarding.nowfloats.model.digitalCard.DigitalCardData
import com.onboarding.nowfloats.recyclerView.AppBaseRecyclerViewHolder
import com.onboarding.nowfloats.recyclerView.BaseRecyclerViewItem

class VisitingCardFourViewHolder(binding: ItemVisitingCardFourBinding) : AppBaseRecyclerViewHolder<ItemVisitingCardFourBinding>(binding) {

  override fun bind(position: Int, item: BaseRecyclerViewItem) {
    super.bind(position, item)
    val data = item as? DigitalCardData ?: return
    binding.businessName.text = data.businessName
    binding.number.text = data.number
    if (data.businessLogo.isNullOrEmpty().not()) {
      binding.profileView.visible()
//      binding.channels.gone()
      binding.channels.visible()
      activity?.glideLoad(binding.imgBusinessLogo, data.businessLogo!!)
    } else {
      binding.profileView.gone()
      binding.channels.visible()
    }
    binding.email.text = fromHtml("<u>${data.email}</u>")
    binding.website.text = fromHtml("<u>${data.website}</u>")
  }
}