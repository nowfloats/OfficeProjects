package com.appservice.ui.staffs.ui.details

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.appservice.R
import com.appservice.base.AppBaseFragment
import com.appservice.constant.FragmentType
import com.appservice.databinding.FragmentStaffDetailsBinding
import com.appservice.ui.catlogService.widgets.ClickType
import com.appservice.ui.catlogService.widgets.ImagePickerBottomSheet
import com.appservice.ui.staffs.ui.home.startStaffFragmentActivity
import com.framework.imagepicker.ImagePicker
import com.framework.models.BaseViewModel
import kotlinx.android.synthetic.main.fragment_staff_details.*

class StaffDetailsFragment : AppBaseFragment<FragmentStaffDetailsBinding, BaseViewModel>() {
    companion object {
        fun newInstance(): StaffDetailsFragment {
            return StaffDetailsFragment()
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_staff_details
    }

    override fun getViewModelClass(): Class<BaseViewModel> {
        return BaseViewModel::class.java
    }

    override fun onCreateView() {
        setOnClickListener(binding?.flAddStaffImg)
        setOnClickListener(binding?.rlStaffTiming)
        setOnClickListener(binding?.rlServiceProvided)
        setOnClickListener(binding?.rlScheduledBreaks)
        setOnClickListener(binding!!.toggleYesNo)
        setOnClickListener(binding!!.csExperience)
        setOnClickListener(binding!!.csGender)
        setOnClickListener(binding!!.flSavePublish)
    }

    override fun onClick(v: View) {
        super.onClick(v)
        val bundle: Bundle = Bundle.EMPTY
        when (v) {
            binding?.flAddStaffImg -> {
                openImagePicker()
            }
            binding?.rlStaffTiming -> {
                startStaffFragmentActivity(requireActivity(), FragmentType.STAFF_TIMING_FRAGMENT, bundle, clearTop = false, isResult = true)
            }
            binding?.rlServiceProvided -> {
                startStaffFragmentActivity(requireActivity(), FragmentType.STAFF_SELECT_SERVICES_FRAGMENT, bundle, clearTop = false, isResult = true)
            }
            binding?.rlScheduledBreaks -> {
                startStaffFragmentActivity(requireActivity(), FragmentType.STAFF_SCHEDULED_BREAK_FRAGMENT, bundle, clearTop = false, isResult = true)
            }
            binding?.toggleYesNo -> {
            }
            binding?.csExperience -> {
            }
            binding?.csGender -> {
            }
            binding?.flSavePublish -> {

            }
        }
    }

    private fun openImagePicker() {
        val filterSheet = ImagePickerBottomSheet()
        filterSheet.isHidePdf(true)
        filterSheet.onClicked = { openImagePicker(it) }
        filterSheet.show(this@StaffDetailsFragment.parentFragmentManager, ImagePickerBottomSheet::class.java.name)
    }

    private fun openImagePicker(it: ClickType) {
        val type = if (it == ClickType.CAMERA) ImagePicker.Mode.CAMERA else ImagePicker.Mode.GALLERY
        ImagePicker.Builder(baseActivity)
                .mode(type)
                .compressLevel(ImagePicker.ComperesLevel.SOFT).directory(ImagePicker.Directory.DEFAULT)
                .extension(ImagePicker.Extension.PNG).allowMultipleImages(false)
                .enableDebuggingMode(true).build()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ImagePicker.IMAGE_PICKER_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK) {
            val mPaths = data?.getSerializableExtra(ImagePicker.EXTRA_IMAGE_PATH) as List<String>
            setImage(mPaths)

        }


    }

    private fun setImage(mPaths: List<String>) {
        binding?.civStaffImg?.setImageURI(Uri.parse(mPaths[0]))
        binding?.ctvImgChange?.text = getString(R.string.change_picture)
        binding?.ctvImgChange?.setTextColor(getColor(R.color.black_4a4a4a))
        binding?.ctvImgChange?.setBackgroundColor(Color.WHITE)
        binding?.flAddStaffImg?.setPadding(2, 2, 2, 2);
        binding?.flAddStaffImg?.backgroundTintList = ColorStateList.valueOf(getColor(R.color.gray_light_4))
    }
}