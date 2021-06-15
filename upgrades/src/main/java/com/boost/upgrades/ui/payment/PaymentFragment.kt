package com.boost.upgrades.ui.payment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.biz2.nowfloats.boost.updates.base_class.BaseFragment
import com.boost.upgrades.R
import com.boost.upgrades.UpgradeActivity
import com.boost.upgrades.adapter.CardPaymentAdapter
import com.boost.upgrades.adapter.StateListAdapter
import com.boost.upgrades.adapter.UPIAdapter
import com.boost.upgrades.adapter.WalletAdapter
import com.boost.upgrades.data.api_model.PaymentThroughEmail.PaymentPriorityEmailRequestBody
import com.boost.upgrades.data.api_model.customerId.customerInfo.AddressDetails
import com.boost.upgrades.data.api_model.customerId.customerInfo.BusinessDetails
import com.boost.upgrades.data.api_model.customerId.customerInfo.CreateCustomerInfoRequest
import com.boost.upgrades.data.api_model.customerId.customerInfo.TaxDetails
import com.boost.upgrades.data.api_model.customerId.get.Result
import com.boost.upgrades.datamodule.SingleNetBankData
import com.boost.upgrades.interfaces.PaymentListener
import com.boost.upgrades.ui.checkoutkyc.BusinessDetailsFragment
import com.boost.upgrades.ui.confirmation.OrderConfirmationFragment
import com.boost.upgrades.ui.popup.*
import com.boost.upgrades.ui.razorpay.RazorPayWebView
import com.boost.upgrades.utils.Constants
import com.boost.upgrades.utils.Constants.Companion.ADD_CARD_POPUP_FRAGMENT
import com.boost.upgrades.utils.Constants.Companion.BUSINESS_DETAILS_FRAGMENT
import com.boost.upgrades.utils.Constants.Companion.EXTERNAL_EMAIL_POPUP_FRAGMENT
import com.boost.upgrades.utils.Constants.Companion.NETBANKING_POPUP_FRAGMENT
import com.boost.upgrades.utils.Constants.Companion.RAZORPAY_WEBVIEW_POPUP_FRAGMENT
import com.boost.upgrades.utils.Constants.Companion.STATE_LIST_FRAGMENT
import com.boost.upgrades.utils.Constants.Companion.UPI_POPUP_FRAGMENT
import com.boost.upgrades.utils.SharedPrefs
import com.boost.upgrades.utils.WebEngageController
import com.bumptech.glide.Glide
import com.framework.pref.Key_Preferences
import com.framework.pref.UserSessionManager
import com.framework.webengageconstant.*
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.razorpay.Razorpay
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.checkoutkyc_fragment.*
import kotlinx.android.synthetic.main.payment_fragment.*
import org.json.JSONObject
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class PaymentFragment : BaseFragment(), PaymentListener {

    lateinit var root: View
    private lateinit var viewModel: PaymentViewModel

    lateinit var razorpay: Razorpay

    lateinit var cardPaymentAdapter: CardPaymentAdapter
    lateinit var upiAdapter: UPIAdapter
    lateinit var walletAdapter: WalletAdapter

    val addCardPopUpFragement = AddCardPopUpFragement()
    val netBankingPopUpFragement = NetBankingPopUpFragement()
    val upiPopUpFragement = UPIPopUpFragement()
    val externalEmailPopUpFragement = ExternalEmailPopUpFragement()
    val razorPayWebView = RazorPayWebView()

    var cartCheckoutData = JSONObject()

    var paymentData = JSONObject()

    var netbankingList = arrayListOf<SingleNetBankData>()

    var totalAmount = 0.0

    var createCustomerInfoRequest: Result? = null
    var customerInfoState = false
    var paymentProceedFlag = true
    private var session: UserSessionManager? = null
    val businessDetailsFragment = BusinessDetailsFragment()
    val stateFragment = StateListPopFragment()

    companion object {
        fun newInstance() = PaymentFragment()
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.payment_fragment, container, false)

        totalAmount = requireArguments().getDouble("amount")
        session = UserSessionManager(requireActivity())
        cartCheckoutData.put("customerId", requireArguments().getString("customerId"))
        cartCheckoutData.put("amount", Math.round(totalAmount * 100).toInt())
        cartCheckoutData.put("order_id", requireArguments().getString("order_id"))
        //subscription testing
//        cartCheckoutData.put("amount", 50000)
//        cartCheckoutData.put("subscription_id", "sub_Fj7nfvetEC7C0W")
        cartCheckoutData.put("transaction_id", requireArguments().getString("transaction_id"))
        cartCheckoutData.put("email", requireArguments().getString("email"))
        cartCheckoutData.put("currency", requireArguments().getString("currency"));
        cartCheckoutData.put("contact", requireArguments().getString("contact"))

//        //this is a offer created from admin dashboard.
//        cartCheckoutData.put("offer_id", arguments!!.getString("offer_F5hUaalR9tpSzn"))

        razorpay = (activity as UpgradeActivity).getRazorpayObject()

        netbankingList = ArrayList<SingleNetBankData>()
        netbankingList.add(SingleNetBankData("UTIB", "Axis", razorpay.getBankLogoUrl("UTIB")))
        netbankingList.add(SingleNetBankData("ICIC", "ICICI", razorpay.getBankLogoUrl("ICIC")))
        netbankingList.add(SingleNetBankData("HDFC", "HDFC", razorpay.getBankLogoUrl("HDFC")))
        netbankingList.add(SingleNetBankData("CIUB", "City Union", razorpay.getBankLogoUrl("CIUB")))
        netbankingList.add(SingleNetBankData("SBIN", "SBI", razorpay.getBankLogoUrl("SBIN")))

        cardPaymentAdapter = CardPaymentAdapter(requireActivity(), ArrayList())
        upiAdapter = UPIAdapter(ArrayList())
        walletAdapter = WalletAdapter(razorpay, ArrayList(), this)

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

//        requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
//        requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        viewModel = ViewModelProviders.of(requireActivity()).get(PaymentViewModel::class.java)

        loadData()
        loadCustomerInfo()
        initMvvm()

        initializeCardRecycler()
        initializeNetBankingSelector()
        initializeUPIRecycler()
        initializeWalletRecycler()
        updateSubscriptionDetails()

        WebEngageController.trackEvent(EVENT_NAME_ADDONS_MARKETPLACE_PAYMENT_LOAD, PAGE_VIEW, ADDONS_MARKETPLACE_PAYMENT_SCREEN)

        var firebaseAnalytics = Firebase.analytics
        val revenue = cartCheckoutData.getDouble("amount")
        val bundle = Bundle()
        bundle.putDouble(FirebaseAnalytics.Param.VALUE, revenue / 100)
        bundle.putString(FirebaseAnalytics.Param.CURRENCY, "INR")
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.BEGIN_CHECKOUT, bundle)

        back_button.setOnClickListener {
            WebEngageController.trackEvent(ADDONS_MARKETPLACE_CLICKED_BACK_BUTTON_PAYMENTSCREEN, ADDONS_MARKETPLACE, NO_EVENT_VALUE)
            (activity as UpgradeActivity).popFragmentFromBackStack()
        }

        payment_submit.setOnClickListener {
            if (paymentData.length() > 0) {
                payThroughRazorPay()
            }
        }

        add_new_card.setOnClickListener {
            if(paymentProceedFlag){
                WebEngageController.trackEvent(ADDONS_MARKETPLACE_ADD_NEW_CARD_CLICK , ADDONS_MARKETPLACE_ADD_NEW_CARD, NO_EVENT_VALUE)
                val args = Bundle()
                args.putString("customerId", cartCheckoutData.getString("customerId"))
                addCardPopUpFragement.arguments = args
                addCardPopUpFragement.show((activity as UpgradeActivity).supportFragmentManager, ADD_CARD_POPUP_FRAGMENT)
            }else{
                payment_business_details_layout.setBackgroundResource(R.drawable.all_side_curve_bg_payment)
            }
        }

        show_more_bank.setOnClickListener {
            WebEngageController.trackEvent(ADDONS_MARKETPLACE_SHOW_MORE_BANK_CLICK , ADDONS_MARKETPLACE_SHOW_MORE_BANK, NO_EVENT_VALUE)
            netBankingPopUpFragement.show(
                    (activity as UpgradeActivity).supportFragmentManager,
                    NETBANKING_POPUP_FRAGMENT
            )
        }

        add_upi_layout.setOnClickListener {
            if(paymentProceedFlag){
                WebEngageController.trackEvent(ADDONS_MARKETPLACE_UPI_CLICK, ADDONS_MARKETPLACE_UPI, NO_EVENT_VALUE)
                upiPopUpFragement.show(
                    (activity as UpgradeActivity).supportFragmentManager,
                    UPI_POPUP_FRAGMENT
                )
            }else{
                payment_business_details_layout.setBackgroundResource(R.drawable.all_side_curve_bg_payment)
            }
        }

        add_external_email.setOnClickListener {
            if(paymentProceedFlag){
                WebEngageController.trackEvent(ADDONS_MARKETPLACE_PAYMENT_LINK_CLICK, ADDONS_MARKETPLACE_PAYMENT_LINK , NO_EVENT_VALUE)
                externalEmailPopUpFragement.show(
                    (activity as UpgradeActivity).supportFragmentManager,
                    EXTERNAL_EMAIL_POPUP_FRAGMENT
                )
            }else{
                payment_business_details_layout.setBackgroundResource(R.drawable.all_side_curve_bg_payment)
            }
        }

        payment_view_details.setOnClickListener {
            payment_main_layout.post {
                payment_main_layout.fullScroll(View.FOCUS_DOWN)
            }
        }

        coupon_discount_value.setOnClickListener {

        }
        edit_business_details.setOnClickListener {
            businessDetailsFragment.show(
                (activity as UpgradeActivity).supportFragmentManager,
                BUSINESS_DETAILS_FRAGMENT
            )
        }

        all_business_button.setOnClickListener{
            businessDetailsFragment.show(
                (activity as UpgradeActivity).supportFragmentManager,
                BUSINESS_DETAILS_FRAGMENT
            )

        }
        supply_place_button.setOnClickListener{
            stateFragment.show(
                (activity as UpgradeActivity).supportFragmentManager,
                STATE_LIST_FRAGMENT
            )
        }

        WebEngageController.trackEvent( ADDONS_MARKETPLACE_PAYMENT_SCREEN_LOADED, PAYMENT_SCREEN, NO_EVENT_VALUE)
    }

    fun loadData() {
        viewModel.loadpaymentMethods(razorpay)
//        viewModel.getRazorPayToken(cartCheckoutData.getString("customerId"))
    }

    @SuppressLint("FragmentLiveDataObserve")
    fun initMvvm() {
        viewModel.cardData().observe(this, Observer {
            Log.i("cardObserver >>>>>", it.toString())
            paymentData = it
            payThroughRazorPay()
        })

        viewModel.netBankingData().observe(this, Observer {
            Log.i("netBankingObserver >", it.toString())
            paymentData = it
            payThroughRazorPay()
        })

        viewModel.upiPaymentData().observe(this, Observer {
            Log.i("upiPaymentObserver >", it.toString())
            paymentData = it
            payThroughRazorPay()
        })
        viewModel.externalEmailPaymentData().observe(this, Observer {
            Log.i("emailPaymentObserver >", it.toString())
            paymentData = it
            payViaPaymentLink()
        })
        viewModel.walletPaymentData().observe(this, Observer {
            Log.i("walletPaymentObserver >", it.toString())
            loadWallet(it)
        })
        viewModel.getPamentUsingExternalLink().observe(this, Observer {
//            if (it != null && it.equals("SUCCESSFULLY ADDED TO QUEUE")) {
            if (it != null && it.equals("OK")) {
                val orderConfirmationFragment = OrderConfirmationFragment.newInstance()
                val args = Bundle()
                args.putString("payment_type", "External_Link")
                orderConfirmationFragment.arguments = args
                (activity as UpgradeActivity).replaceFragment(
                        orderConfirmationFragment,
                        Constants.ORDER_CONFIRMATION_FRAGMENT
                )
            } else {
                Toasty.error(requireContext(), "Unable To Send Link To Email. Try Later...", Toast.LENGTH_SHORT, true).show();
            }
        })

        viewModel.getCustomerInfoResult().observe(this, Observer {
            createCustomerInfoRequest = it.Result
            if (createCustomerInfoRequest != null) {
                if (createCustomerInfoRequest!!.BusinessDetails != null) {
                    business_mobile_value.setText(createCustomerInfoRequest!!.BusinessDetails!!.PhoneNumber)
                    business_email_value.setText(createCustomerInfoRequest!!.BusinessDetails!!.Email)
                    if(createCustomerInfoRequest!!.BusinessDetails!!.Email != null){
                        business_email_value.setText(createCustomerInfoRequest!!.BusinessDetails!!.Email)
                    }else if((session?.getFPDetails(Key_Preferences.PRIMARY_EMAIL)) != null){
                            business_email_value.setText(session?.getFPDetails(Key_Preferences.PRIMARY_EMAIL))
                    }

                    if(createCustomerInfoRequest!!.BusinessDetails!!.PhoneNumber != null){
                        business_mobile_value.setText(createCustomerInfoRequest!!.BusinessDetails!!.PhoneNumber)
                    }else if((session?.getFPDetails(Key_Preferences.PRIMARY_NUMBER)) != null){
                        business_mobile_value.setText(session?.getFPDetails(Key_Preferences.PRIMARY_NUMBER))
                    }
                }
                if (createCustomerInfoRequest!!.AddressDetails != null) {
                    business_supply_place_value.setText(createCustomerInfoRequest!!.AddressDetails!!.City)
                }

                if(createCustomerInfoRequest!!.BusinessDetails!!.PhoneNumber == null){
                    business_mobile_missing.visibility = View.VISIBLE
                    paymentProceedFlag = false
                }else{
                    if(session?.fPPrimaryContactNumber == null){
                        business_mobile_missing.visibility = View.VISIBLE
                        paymentProceedFlag = false
                    }else{
                        business_mobile_missing.visibility = View.GONE
                    }
//                    business_mobile_missing.visibility = View.GONE
                }
                if(createCustomerInfoRequest!!.BusinessDetails!!.Email == null){
                    business_email_missing.visibility = View.VISIBLE
                    paymentProceedFlag = false
                }else{
                    if(session?.fPEmail == null){
                        business_email_missing.visibility = View.VISIBLE
                        paymentProceedFlag = false
                    }else{
                        business_email_missing.visibility = View.GONE
                    }

                }
                if(createCustomerInfoRequest!!.AddressDetails!!.City == null){
                    business_supply_place_missing.visibility = View.VISIBLE
                    paymentProceedFlag = false
                }else{
                    business_supply_place_missing.visibility = View.GONE
                }

                if(createCustomerInfoRequest!!.BusinessDetails!!.PhoneNumber != null &&
                    createCustomerInfoRequest!!.BusinessDetails!!.Email != null  &&
                    createCustomerInfoRequest!!.AddressDetails!!.City  != null   ){
                    paymentProceedFlag = true
                    business_button_layout.visibility = View.GONE
                    business_button_separator.visibility = View.GONE
                    edit_business_details.visibility = View.VISIBLE
                }

            }
        })
        viewModel.getCustomerInfoStateResult().observe(this, Observer {
            customerInfoState = it
            if(!customerInfoState){

                if(session?.fPPrimaryContactNumber == null || session?.fPPrimaryContactNumber.equals("")){
                    business_mobile_missing.visibility = View.VISIBLE
                    business_mobile_value.visibility = View.INVISIBLE
                    paymentProceedFlag = false
                }else{
                    business_mobile_value.visibility = View.VISIBLE
                    business_mobile_value.text = session?.fPPrimaryContactNumber
                }

                if(session?.fPEmail == null || session?.fPEmail.equals("") ){
                    business_email_missing.visibility = View.VISIBLE
                    business_email_value.visibility = View.INVISIBLE
                    paymentProceedFlag = false
                }else{
                    business_email_value.visibility = View.VISIBLE
                    business_email_value.text = session?.fPEmail
                }



                business_supply_place_missing.visibility = View.VISIBLE
                business_supply_place_value.visibility = View.INVISIBLE
                business_button_layout.visibility = View.VISIBLE
//                all_business_button.visibility = View.VISIBLE
                supply_place_button.visibility = View.VISIBLE
                paymentProceedFlag = false
                if(session?.fPPrimaryContactNumber.equals("") && session?.fPEmail.equals("")){
                    supply_place_button.visibility = View.GONE
                    all_business_button.visibility = View.VISIBLE
                }
            }
        })

        viewModel.getUpdatedCustomerResult().observe(this, Observer {
            if (it.Result != null) {
                Toasty.success(requireContext(), "Successfully Updated Profile.", Toast.LENGTH_LONG).show()
                loadCustomerInfo()
                (activity as UpgradeActivity).prefs.storeInitialLoadMarketPlace(false)
            } else {
                Toasty.error(requireContext(), "Something went wrong. Try Later!!", Toast.LENGTH_LONG).show()
                (activity as UpgradeActivity).prefs.storeInitialLoadMarketPlace(true)
            }
        })
        viewModel.cityResult().observe(this, androidx.lifecycle.Observer {
            if(it != null){
                val adapter = ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_dropdown_item, it)
                val adapter1 = ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_dropdown_item, it)
//                business_city_name.setAdapter(adapter)
            }

        })
        viewModel.getSelectedStateResult().observe(this, androidx.lifecycle.Observer {
            if(it != null){
                Log.v("getSelectedStateResult", " "+ it)
                if(!session?.fPPrimaryContactNumber.equals("") && !session?.fPEmail.equals("")){
                    viewModel.createCustomerInfo(
                        CreateCustomerInfoRequest(
                        AddressDetails(
                            it,
                            "india",
                            null,
                            null,
                            null,
                            null
                        ),
                        BusinessDetails(
                            "+91",
                            session?.fPEmail,
                            session?.fPPrimaryContactNumber
                        ),
                        (activity as UpgradeActivity).clientid,
                        "+91",
                        "ANDROID",
                        "",
                        (activity as UpgradeActivity).fpid!!,
                            session?.fPPrimaryContactNumber,
                        null,
                        TaxDetails(
                            null,
                            null,
                            null,
                            null
                        )

                    )
                    )
                    supply_place_button.visibility = View.GONE
                    all_business_button.visibility = View.GONE
                    edit_business_details.visibility = View.VISIBLE
                    business_supply_place_value.text = it
                }
            }

        })
    }

    fun payViaPaymentLink() {

        try {
            val paymentLink = "https://www.getboost360.com/subscriptions/" + cartCheckoutData.get("transaction_id") + "/pay-now"
            val emailBody = "You can securely pay for your Boost360 subscription (Order #" + cartCheckoutData.get("transaction_id") + ") using the link below." +
                    "<br/>The subscription will be activated against the account of " + (activity as UpgradeActivity).fpName + ".<br/><br/>Payment Link: " + paymentLink

            var prefs = SharedPrefs(activity as UpgradeActivity)
            val emailArrayList = ArrayList<String>()
            emailArrayList.add(paymentData.get("userEmail").toString())
            emailArrayList.add(prefs.getFPEmail())

            /*viewModel.loadPamentUsingExternalLink((activity as UpgradeActivity).clientid,
                    PaymentThroughEmailRequestBody((activity as UpgradeActivity).clientid,
                            emailBody,
                            "alerts@nowfloats.com",
                            "\uD83D\uDD50 Payment link for your Boost360 Subscription [Order #" + cartCheckoutData.get("transaction_id") + "]",
                            emailArrayList,
                            0
                    ))*/
            viewModel.loadPaymentLinkPriority((activity as UpgradeActivity).clientid,
                    PaymentPriorityEmailRequestBody((activity as UpgradeActivity).clientid,
                            emailBody,
                            "\uD83D\uDD50 Payment link for your Boost360 Subscription [Order #" + cartCheckoutData.get("transaction_id") + "]",
                            emailArrayList,
                    ))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun payThroughRazorPay() {
        try {
            for (key in cartCheckoutData.keys()) {
                if (key != "customerId" && key != "transaction_id") {
                    paymentData.put(key, cartCheckoutData.get(key))
                }
            }
            var firebaseAnalytics = Firebase.analytics
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.ADD_PAYMENT_INFO, null)

            val args = Bundle()
            args.putString("data", paymentData.toString())
            razorPayWebView.arguments = args

            //RazorPay web
            razorPayWebView.show((activity as UpgradeActivity).supportFragmentManager, RAZORPAY_WEBVIEW_POPUP_FRAGMENT)

            paymentData = JSONObject()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun initializeCardRecycler() {
        val gridLayoutManager = GridLayoutManager(requireContext(), 1)
        gridLayoutManager.orientation = LinearLayoutManager.VERTICAL
        card_recycler.apply {
            layoutManager = gridLayoutManager
            card_recycler.adapter = cardPaymentAdapter

        }
    }

    fun initializeNetBankingSelector() {

        Glide.with(requireContext()).load(netbankingList.get(0).bankImage).into(axis_bank_image)
        axis_bank_layout.setOnClickListener {
            Log.v("axis_bank_layout"," "+ paymentProceedFlag )
            if(paymentProceedFlag)
                netbankingSelected(netbankingList.get(0).bankCode)
            else
                payment_business_details_layout.setBackgroundResource(R.drawable.all_side_curve_bg_payment)
        }

        Glide.with(requireContext()).load(netbankingList.get(1).bankImage).into(icici_bank_image)
        icici_bank_layout.setOnClickListener {
            if(paymentProceedFlag)
                netbankingSelected(netbankingList.get(1).bankCode)
            else
                payment_business_details_layout.setBackgroundResource(R.drawable.all_side_curve_bg_payment)
        }

        Glide.with(requireContext()).load(netbankingList.get(2).bankImage).into(hdfc_bank_image)
        hdfc_bank_layout.setOnClickListener {
            if(paymentProceedFlag)
                netbankingSelected(netbankingList.get(2).bankCode)
            else
                payment_business_details_layout.setBackgroundResource(R.drawable.all_side_curve_bg_payment)
        }

        Glide.with(requireContext()).load(netbankingList.get(3).bankImage).into(citi_bank_image)
        citi_bank_layout.setOnClickListener {
            if(paymentProceedFlag)
                netbankingSelected(netbankingList.get(3).bankCode)
            else
                payment_business_details_layout.setBackgroundResource(R.drawable.all_side_curve_bg_payment)
        }

        Glide.with(requireContext()).load(netbankingList.get(4).bankImage).into(sbi_bank_image)
        sbi_bank_layout.setOnClickListener {
            if(paymentProceedFlag)
                netbankingSelected(netbankingList.get(4).bankCode)
            else
                payment_business_details_layout.setBackgroundResource(R.drawable.all_side_curve_bg_payment)
        }


    }

    fun initializeUPIRecycler() {
        val gridLayoutManager = GridLayoutManager(requireContext(), 1)
        gridLayoutManager.orientation = LinearLayoutManager.VERTICAL
        upi_recycler.apply {
            layoutManager = gridLayoutManager
            upi_recycler.adapter = upiAdapter
        }
    }

    fun initializeWalletRecycler() {
        val gridLayoutManager = GridLayoutManager(requireContext(), 1)
        gridLayoutManager.orientation = LinearLayoutManager.VERTICAL
        wallet_recycler.apply {
            layoutManager = gridLayoutManager
            wallet_recycler.adapter = walletAdapter
        }
    }

    fun netbankingSelected(bankCode: String) {
        Log.i("netbankingSelected", bankCode)
        val item = JSONObject()
        item.put("method", "netbanking");
        item.put("bank", bankCode)
        paymentData = item

        WebEngageController.trackEvent(ADDONS_MARKETPLACE_NET_BANKING_SELECTED, bankCode, NO_EVENT_VALUE)
        payThroughRazorPay()
    }

    override fun walletSelected(data: String) {
        Log.i("walletSelected", data)
        if(paymentProceedFlag){
            WebEngageController.trackEvent(ADDONS_MARKETPLACE_WALLET_SELECTED, data, NO_EVENT_VALUE)
            val item = JSONObject()
            item.put("method", "wallet");
            item.put("wallet", data);
            paymentData = item
            payThroughRazorPay()
        }else{
            payment_business_details_layout.setBackgroundResource(R.drawable.all_side_curve_bg_payment)
        }
    }

    private fun loadWallet(data: JSONObject) {
        val paymentMethods = data.get("wallet") as JSONObject
        val retMap: Map<String, Boolean> = Gson().fromJson(
                paymentMethods.toString(), object : TypeToken<HashMap<String, Boolean>>() {}.type
        )
        val list = ArrayList<String>()
        retMap.map {
            if (it.value) {
                list.add(it.key)
            }
        }
        walletAdapter.addupdates(list)
    }


    override fun onDestroy() {
        super.onDestroy()
//        requireActivity().viewModelStore.clear()
    }

    fun updateSubscriptionDetails() {
        var prefs = SharedPrefs(activity as UpgradeActivity)

        //cartOriginalPrice
        val cartOriginalPrice = prefs.getCartOriginalAmount()
        payment_amount_value.setText("₹" + NumberFormat.getNumberInstance(Locale.ENGLISH).format(cartOriginalPrice))

        //coupon discount percentage
        val couponDiscountPercentage = prefs.getCouponDiscountPercentage()
        coupon_discount_title.setText("Coupon discount(" + couponDiscountPercentage.toString() + "%)")

        //coupon discount amount
        val couponDiscountAmount = cartOriginalPrice * couponDiscountPercentage / 100
        coupon_discount_value.setText("-₹" + NumberFormat.getNumberInstance(Locale.ENGLISH).format(couponDiscountAmount))

        //igsttin value
        val temp = ((cartOriginalPrice - couponDiscountAmount) * 18) / 100
        val taxValue = Math.round(temp * 100) / 100.0
        igst_value.setText("+₹" + NumberFormat.getNumberInstance(Locale.ENGLISH).format(taxValue))

        order_total_value.setText("₹" + NumberFormat.getNumberInstance(Locale.ENGLISH).format(totalAmount))
        payment_total_value.setText("₹" + NumberFormat.getNumberInstance(Locale.ENGLISH).format(totalAmount))
        items_cost.setText("₹" + NumberFormat.getNumberInstance(Locale.ENGLISH).format(totalAmount))
    }

    private fun loadCustomerInfo() {
        viewModel.getCustomerInfo((activity as UpgradeActivity).fpid!!, (activity as UpgradeActivity).clientid)
    }

}
