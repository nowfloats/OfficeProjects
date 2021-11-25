package com.boost.presignin.ui.newOnboarding

import android.os.Bundle
import android.text.SpannableString
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.recyclerview.widget.LinearLayoutManager
import com.boost.presignin.R
import com.boost.presignin.base.AppBaseFragment
import com.boost.presignin.constant.IntentConstant
import com.boost.presignin.constant.RecyclerViewActionType
import com.boost.presignin.databinding.LayoutBusinessCategoryPreviewBinding
import com.boost.presignin.databinding.LayoutSetUpMyWebsiteStep1Binding
import com.boost.presignin.helper.WebEngageController
import com.boost.presignin.model.CategorySuggestionUiModel
import com.boost.presignin.model.category.CategoryDataModel
import com.boost.presignin.model.category.CategoryDataModelOv2
import com.boost.presignin.recyclerView.AppBaseRecyclerViewAdapter
import com.boost.presignin.recyclerView.BaseRecyclerViewItem
import com.boost.presignin.recyclerView.RecyclerItemClickListener
import com.boost.presignin.rest.response.ResponseDataCategoryOv2
import com.boost.presignin.viewmodel.CategoryVideoModel
import com.bumptech.glide.Glide
import com.framework.extensions.observeOnce
import com.framework.utils.convertJsonToObj
import com.framework.webengageconstant.*

class BusinessCategoryPreviewFragment : AppBaseFragment<LayoutBusinessCategoryPreviewBinding, CategoryVideoModel>(),
    RecyclerItemClickListener {

    private val TAG = "BusinessCategoryPreview"
    companion object {
        @JvmStatic
        fun newInstance(bundle: Bundle? = null): BusinessCategoryPreviewFragment {
            val fragment = BusinessCategoryPreviewFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    private lateinit var baseAdapter: AppBaseRecyclerViewAdapter<CategoryDataModelOv2>
    private var categoryList = ArrayList<CategoryDataModelOv2>()
    private val phoneNumber by lazy {
        arguments?.getString(IntentConstant.EXTRA_PHONE_NUMBER.name)
    }

    private val mobilePreview by lazy {
        arguments?.getString(IntentConstant.MOBILE_PREVIEW.name)
    }

    private val desktopPreview by lazy {
        arguments?.getString(IntentConstant.DESKTOP_PREVIEW.name)
    }
    private val categoryModel by lazy {
        convertJsonToObj<CategoryDataModelOv2>(arguments?.getString(IntentConstant.CATEGORY_DATA.name))

    }
    private val categorySuggUiModel by lazy {
        convertJsonToObj<CategorySuggestionUiModel>(arguments?.getString(IntentConstant.CATEGORY_SUGG_UI.name))
    }


    override fun getLayout(): Int {
      return R.layout.layout_business_category_preview
    }

    override fun getViewModelClass(): Class<CategoryVideoModel> {
        return CategoryVideoModel::class.java
    }

    override fun onCreateView() {
        super.onCreateView()
        setOnClickListeners()
        setupUi()


    }

    private fun setupUi() {
        val spannableString = SpannableString(categorySuggUiModel.category+" in "+categorySuggUiModel.subCategory)
        binding?.autocompleteSearchCategory?.setText(spannableString)
        Log.i(TAG, "setupUi: $mobilePreview")
        Glide.with(this).load(mobilePreview).into(binding?.ivPreview!!)
    }

    private fun setOnClickListeners() {
        binding?.tvNextStep?.setOnClickListener {
            addFragment(R.id.inner_container,SetupMyWebsiteStep2Fragment.newInstance(Bundle()
                .apply
                {
                    putString(IntentConstant.EXTRA_PHONE_NUMBER.name,phoneNumber)
                }),true)
        }
        binding?.autocompleteSearchCategory?.setOnClickListener {
            requireActivity().onBackPressed()
        }

    }

    override fun onItemClick(position: Int, item: BaseRecyclerViewItem?, actionType: Int) {
        when (actionType) {

        }

    }
}