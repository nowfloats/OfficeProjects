package com.appservice.staffs.ui.home

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.AbsListView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appservice.R
import com.appservice.base.AppBaseFragment
import com.appservice.constant.FragmentType
import com.appservice.databinding.FragmentStaffListingBinding
import com.appservice.recyclerView.AppBaseRecyclerViewAdapter
import com.appservice.recyclerView.BaseRecyclerViewItem
import com.appservice.recyclerView.PaginationScrollListener
import com.appservice.recyclerView.PaginationScrollListener.Companion.PAGE_START
import com.appservice.recyclerView.RecyclerItemClickListener
import com.appservice.staffs.model.DataItem
import com.appservice.staffs.model.FilterBy
import com.appservice.staffs.model.GetStaffListingRequest
import com.appservice.staffs.model.GetStaffListingResponse
import com.appservice.staffs.ui.UserSession
import com.appservice.staffs.ui.startStaffFragmentActivity
import com.appservice.staffs.ui.viewmodel.StaffViewModel
import kotlinx.android.synthetic.main.fragment_staff_profile.view.*
import java.util.*
import kotlin.collections.ArrayList

class StaffProfileListingFragment : AppBaseFragment<FragmentStaffListingBinding, StaffViewModel>(), RecyclerItemClickListener, SearchView.OnQueryTextListener {

    private var layoutManager: LinearLayoutManager? = null
    private val list: ArrayList<DataItem> = ArrayList()
    private val copyList: ArrayList<DataItem> = ArrayList()
    private lateinit var adapter: AppBaseRecyclerViewAdapter<DataItem>
    private var filter = FilterBy("", 10, 0)

    /* Paging */
    private var isLastPageD = false
    private var pageNumber = 0

    override fun getLayout(): Int {
        return R.layout.fragment_staff_listing
    }

    override fun getViewModelClass(): Class<StaffViewModel> {
        return StaffViewModel::class.java
    }

    private fun showMenuItem() {
        appBaseActivity?.getToolbar()?.menu?.findItem(R.id.app_bar_search)?.isVisible = true
    }

    private fun hideMenuItem() {
        appBaseActivity?.getToolbar()?.menu?.findItem(R.id.app_bar_search)?.isVisible = false
    }

    companion object {
        fun newInstance(): StaffProfileListingFragment {
            return StaffProfileListingFragment()
        }
    }

    override fun onCreateView() {
        super.onCreateView()
        setHasOptionsMenu(true)
        hideMenuItem()
        setOnClickListener(binding?.fragmentStaffAdd?.flAddStaff)

        layoutManager = LinearLayoutManager(baseActivity)

        fetchStaffListing()
        setupOnScrollListener()
    }

    private fun fetchStaffListing() {

        if (copyList.size == 0) showProgress("Loading")

        viewModel?.getStaffList(GetStaffListingRequest(filter, UserSession.fpId, ""))?.observe(this.requireActivity(), {

            hideProgress()
            if (this::adapter.isInitialized) {
                adapter.removeLoadingFooter()
            }

            when (it.status) {

                200 -> {
                    val getStaffListingResponse = it as GetStaffListingResponse
                    val data = getStaffListingResponse.result?.data

                    if (data?.isNotEmpty() == true) {
                        binding?.layoutStaffListing!!.root.visibility = View.VISIBLE
                        binding?.fragmentStaffAdd!!.root.visibility = View.GONE
                        showMenuItem()

                        data as ArrayList<DataItem>
                        if (pageNumber == 0) list.clear()
                        list.addAll(data)
                        copyList.clear()
                        copyList.addAll(data)

                        if (this::adapter.isInitialized) {
                            adapter.notifyDataSetChanged()
                        } else {
                            adapter = AppBaseRecyclerViewAdapter(activity = baseActivity, list = list, itemClickListener = this@StaffProfileListingFragment)
                            binding?.layoutStaffListing?.rvStaffList?.adapter = adapter
                        }

                        isLastPageD = data.size < 10
                    } else {
                        if (list.size == 0) {
                            binding?.layoutStaffListing?.root?.visibility = View.GONE
                            binding?.fragmentStaffAdd!!.root.visibility = View.VISIBLE
                        }

                        isLastPageD = data?.size!! < 10
                    }
                    Log.e("list_size", list.size.toString())
                }



                else -> {
                    isLastPageD = true
                }
            }

        })
    }

    private fun setupOnScrollListener() {
        binding?.layoutStaffListing?.rvStaffList?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                var LayoutM = binding?.layoutStaffListing?.rvStaffList?.layoutManager as LinearLayoutManager

                if (LayoutM.findLastCompletelyVisibleItemPosition() == copyList.size - 1) {

                    if (!isLastPageD) {
                        pageNumber++
                        adapter?.addLoadingFooter(DataItem())
                        filter.offset = filter.offset?.plus(10)
                        fetchStaffListing()
                        Log.e("scroller", "---> called")
                    }
                }
            }
        })
    }

    override fun onItemClick(position: Int, item: BaseRecyclerViewItem?, actionType: Int) {
        val staff = item as DataItem
        val bundle = Bundle()
        bundle.putSerializable("STAFF_DETAILS", staff)
        startStaffFragmentActivity(requireActivity(), FragmentType.STAFF_PROFILE_DETAILS_FRAGMENT, bundle, clearTop = false, isResult = false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_stafflisting, menu)
        val searchItem = menu.findItem(R.id.app_bar_search)
        searchItem.isVisible = list.isNullOrEmpty().not()
        val searchView: SearchView = searchItem.actionView as SearchView
        searchView.queryHint = "Search Staff"
        searchView.setOnQueryTextListener(this)
        searchView.clearFocus()
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.menu_add_staff -> {
                startStaffFragmentActivity(requireActivity(), FragmentType.STAFF_DETAILS_FRAGMENT, clearTop = false, isResult = false)
                true
            }
            R.id.app_bar_search -> {
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onClick(v: View) {
        when (v) {
            binding!!.fragmentStaffAdd.flAddStaff -> {
                startStaffFragmentActivity(requireActivity(), FragmentType.STAFF_DETAILS_FRAGMENT, clearTop = false, isResult = false)
            }
        }
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return true
    }


    override fun onQueryTextChange(newText: String?): Boolean {
        if (newText != null) {
            filter(newText)
        }
        return true
    }

    fun filter(queryText: String) {
        list.clear()
        if (queryText.isEmpty() && queryText.isBlank()) {
            list.addAll(copyList)
        } else {
            for (dataItem in copyList) {
                if (dataItem.name?.toLowerCase()?.contains(queryText.toLowerCase())!!) {
                    list.add(dataItem)
                }
            }
        }
        adapter.notifyDataSetChanged()
    }
}