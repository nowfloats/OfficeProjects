package com.festive.poster.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.festive.poster.R
import com.festive.poster.databinding.BsheetCustomizePosterBinding
import com.festive.poster.models.PosterCustomizationModel
import com.festive.poster.utils.WebEngageController
import com.festive.poster.viewmodels.FestivePosterSharedViewModel
import com.framework.base.BaseBottomSheetDialog
import com.framework.extensions.gone
import com.framework.extensions.visible
import com.framework.models.BaseViewModel
import com.framework.webengageconstant.FESTIVAL_POSTER_UPDATE_INFO
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.util.FileUtil
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class CustomizePosterSheet: BaseBottomSheetDialog<BsheetCustomizePosterBinding, BaseViewModel>() {

    private val TAG = "CustomizePosterSheet"
    private var path: String?=null
    private val RC_IMAGE_PCIKER=422
    private var sharedViewModel:FestivePosterSharedViewModel?=null
    private var packTag:String?=null
    private var isAlreadyPurchased:Boolean=false
    private var creatorName:String?=null

    companion object{
        val BK_TAG="BK_TITLE"
        val BK_IS_PURCHASED="BK_IS_PURCHASED"
        val BK_CREATOR_NAME="BK_CREATOR_NAME"

        @JvmStatic
        fun newInstance(tag: String, isAlreadyPurchased: Boolean, creatorName: String?=null): CustomizePosterSheet {
            val bundle = Bundle().apply {
                putString(BK_TAG,tag)
                putBoolean(BK_IS_PURCHASED,isAlreadyPurchased)
                putString(BK_CREATOR_NAME,creatorName)
            }
            val fragment =CustomizePosterSheet()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun getLayout(): Int {
        return R.layout.bsheet_customize_poster
    }

    override fun getViewModelClass(): Class<BaseViewModel> {
        return BaseViewModel::class.java
    }

    override fun onCreateView() {
        packTag = arguments?.getString(BK_TAG)
        isAlreadyPurchased = arguments?.getBoolean(BK_IS_PURCHASED) == true
        creatorName = arguments?.getString(BK_CREATOR_NAME)
        sharedViewModel = ViewModelProvider(requireActivity()).get(FestivePosterSharedViewModel::class.java)

        setOnClickListener(binding?.ivCancel,binding?.uploadSelfie,binding?.tvUpdateInfo,binding?.imgEdit)
    }

    override fun onClick(v: View) {
        super.onClick(v)
        when(v){
            binding?.ivCancel->{
                dismiss()
            }
            binding?.uploadSelfie->{

                ImagePicker.with(this).start(RC_IMAGE_PCIKER)
            }
            binding?.tvUpdateInfo->{
                Log.i(TAG, "path: $path")
                if (validation()){
                    WebEngageController.trackEvent(FESTIVAL_POSTER_UPDATE_INFO,event_value = HashMap())
                    submitDetails()
                }

            }
            binding?.imgEdit->{
                ImagePicker.with(this).start(RC_IMAGE_PCIKER)

            }

        }
    }

    private fun submitDetails() {
        sharedViewModel?.customizationDetails?.value  = PosterCustomizationModel(
            packTag!!,
            binding?.etName?.text.toString(),
            binding?.etEmail?.text.toString(),
            binding?.etWhatsapp?.text.toString(),
            binding?.etDesc?.text.toString(),
            binding?.etWebsite?.text.toString(),
            path
        )

        if (!isAlreadyPurchased){
            PosterPaymentSheet().show(parentFragmentManager,PosterPaymentSheet::class.java.name)
        }else if (creatorName==null) {
            addFragmentReplace(R.id.container,PosterListFragment.newInstance(packTag!!),true)
        }
        dismiss()
    }

    private fun validation(): Boolean {
        if (path==null){
            showLongToast("Please Upload Image")
            return false
        }
        if (binding?.etName?.text.toString().isEmpty()){
            showLongToast("Please Enter Name")
            return false
        }
        if (binding?.etEmail?.text.toString().isEmpty()){
            showLongToast("Please Enter Email")
            return false
        }
        if (binding?.etWhatsapp?.text.toString().isEmpty()){
            showLongToast("Please Enter Whatsapp")
            return false
        }
        if (binding?.etDesc?.text.toString().isEmpty()){
            showLongToast("Please Enter Greeting Message")
            return false
        }

        if (binding?.etWebsite?.text.toString().isEmpty()){
            showLongToast("Please Enter Website Address")
            return false
        }
        if (binding?.etWhatsapp?.text.toString().isEmpty()){
            showLongToast("Please Enter Whatsapp")
            return false
        }

        return true

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode== RESULT_OK&&requestCode==RC_IMAGE_PCIKER){
            openCropFragment(data)
        }
    }

    private fun openCropFragment(data: Intent?) {
        val uri = data?.data!!
        path = getTempFile(requireContext(),uri)?.path
        binding?.ivUserImg?.setImageURI(uri)
        showUserImage()

    }

    private fun showUserImage() {
        binding?.layoutImage?.visible()
        binding?.layoutNoImage?.gone()

    }

    fun getTempFile(context: Context, uri: Uri): File? {
        try {
            val destination = File(context.cacheDir, "image_picker.jpg")

            val parcelFileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")
            val fileDescriptor = parcelFileDescriptor?.fileDescriptor ?: return null

            val src = FileInputStream(fileDescriptor).channel
            val dst = FileOutputStream(destination).channel
            dst.transferFrom(src, 0, src.size())
            src.close()
            dst.close()

            return destination
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
        return null
    }
}