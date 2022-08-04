package com.festive.poster.ui.promoUpdates

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.festive.poster.R
import com.festive.poster.base.AppBaseActivity
import com.festive.poster.base.AppBaseFragment
import com.festive.poster.constant.RecyclerViewActionType
import com.festive.poster.databinding.FragmentFavouriteListBinding
import com.festive.poster.models.*
import com.festive.poster.models.response.TemplateSaveActionBody
import com.festive.poster.recyclerView.AppBaseRecyclerViewAdapter
import com.festive.poster.recyclerView.BaseRecyclerViewItem
import com.festive.poster.recyclerView.RecyclerItemClickListener
import com.festive.poster.ui.TemplateDiffUtil
import com.festive.poster.utils.WebEngageController
import com.festive.poster.utils.posterPostClicked
import com.festive.poster.utils.posterWhatsappShareClicked
import com.festive.poster.utils.setUpUsingDiffUtil
import com.festive.poster.viewmodels.PostUpdatesViewModel
import com.festive.poster.viewmodels.PromoUpdatesViewModel
import com.framework.base.BaseActivity
import com.framework.pref.UserSessionManager
import com.framework.utils.toArrayList
import com.framework.webengageconstant.Promotional_Update_Category_Click


class FavouriteListFragment : AppBaseFragment<FragmentFavouriteListBinding, PostUpdatesViewModel>(),
    RecyclerItemClickListener {


    private val DEFAULT_SELECTED_POS=0
    private var promoUpdatesViewModel: PromoUpdatesViewModel? = null
    private var session: UserSessionManager? = null
    private var selectedPos: Int = 0
    private var posterRvAdapter: AppBaseRecyclerViewAdapter<BrowseAllTemplate>? = null
    private var categoryAdapter: AppBaseRecyclerViewAdapter<BrowseAllCategory>? = null
    var categoryList = ArrayList<BrowseAllCategory>()


    override fun getLayout(): Int {
        return R.layout.fragment_favourite_list
    }

    override fun getViewModelClass(): Class<PostUpdatesViewModel> {
        return PostUpdatesViewModel::class.java
    }

    companion object {

        val BK_POSTER_PACK_LIST = "POSTER_PACK_LIST"
        val BK_SELECTED_POS = "BK_SELECTED_POS"

        fun newInstance(): FavouriteListFragment {
            val bundle: Bundle = Bundle()
            val fragment = FavouriteListFragment()
            fragment.arguments = bundle
            return fragment
        }


    }


    override fun onCreateView() {
        super.onCreateView()
        session = UserSessionManager(requireActivity())
        promoUpdatesViewModel =
            ViewModelProvider(requireActivity()).get(PromoUpdatesViewModel::class.java)
       initUi()

        getFavTemp()
    }

    private fun initUi() {
        binding.rvCat.layoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvPosters.layoutManager = LinearLayoutManager(requireActivity())
        categoryAdapter = AppBaseRecyclerViewAdapter(
            requireActivity() as BaseActivity<*, *>,
            categoryList,
            this
        )
        binding.rvCat.adapter = categoryAdapter

        posterRvAdapter = AppBaseRecyclerViewAdapter(
            requireActivity() as BaseActivity<*, *>,
            ArrayList(), this
        )
        binding.rvPosters.adapter = posterRvAdapter
        binding.rvPosters.layoutManager = LinearLayoutManager(requireActivity())
    }

    private fun getFavTemp() {

        showProgress()
        promoUpdatesViewModel?.favData?.observe(viewLifecycleOwner) {
            hideProgress()

            if (it.isEmpty()) {
                zerothCase(true)
                return@observe
            } else {
                zerothCase(false)
            }

            if (categoryList.isEmpty()){
                setCatgories(it)
                switchToSelectedItem(DEFAULT_SELECTED_POS,
                    it[DEFAULT_SELECTED_POS].getParentTemplates()
                        ?.asBrowseAllModels())
            }else{
                switchToSelectedItem(selectedPos,
                    it[selectedPos].getParentTemplates()
                        ?.asBrowseAllModels())
                setCatgories(it)
            }

        }

    }

    private fun setCatgories(it: java.util.ArrayList<CategoryUi>) {
        categoryList.clear()
        categoryList.addAll(it.asBrowseAllModels().toArrayList())
        categoryAdapter?.notifyDataSetChanged()
    }


    private fun zerothCase(b: Boolean) {
        binding.layoutZeroth.isVisible = b
    }



    override fun onItemClick(position: Int, item: BaseRecyclerViewItem?, actionType: Int) {
        when (actionType) {
            RecyclerViewActionType.BROWSE_ALL_POSTER_CAT_CLICKED.ordinal -> {
                WebEngageController.trackEvent(Promotional_Update_Category_Click)

                switchToSelectedItem(position,categoryList[position].templates)
            }
            RecyclerViewActionType.WHATSAPP_SHARE_CLICKED.ordinal -> {
                posterWhatsappShareClicked(
                    item as TemplateUi,
                    requireActivity() as BaseActivity<*, *>
                )
            }
            RecyclerViewActionType.POST_CLICKED.ordinal -> {
                posterPostClicked(item as TemplateUi, requireActivity() as AppBaseActivity<*, *>)
            }

            RecyclerViewActionType.POSTER_LOVE_CLICKED.ordinal->{
                callFavApi(item as TemplateUi)
            }
        }

    }

    private fun callFavApi(templateUi: TemplateUi) {
        showProgress()
        viewModel?.templateSaveAction(
            TemplateSaveActionBody.ActionType.FAVOURITE,
            templateUi.isFavourite.not(),templateUi.id)?.observe(viewLifecycleOwner){
            if (it.isSuccess()){
              promoUpdatesViewModel?.getTemplatesUi()
            }
            hideProgress()
        }
    }

    private fun switchToSelectedItem(positon:Int,newList: List<BrowseAllTemplate>?) {
        if (newList==null){
            return
        }
        selectedPos=positon
        selectCategory()
        posterRvAdapter?.setUpUsingDiffUtil(newList)
    }

    private fun selectCategory() {
        val selectedItem = categoryList[selectedPos]
        categoryList.forEach { it.isSelected = false }
        selectedItem.isSelected = true
        categoryAdapter?.notifyDataSetChanged()
        binding.tvCatTitle.text = selectedItem.name
        binding.tvCatSize.text = selectedItem.templates?.size.toString()
    }
}

