package com.boost.marketplace.ui.Compare_Plans

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.boost.dbcenterapi.data.remote.NewApiInterface
import com.boost.dbcenterapi.upgradeDB.local.AppDatabase
import com.boost.dbcenterapi.upgradeDB.model.BundlesModel
import com.boost.dbcenterapi.upgradeDB.model.CartModel
import com.boost.dbcenterapi.upgradeDB.model.FeaturesModel
import com.boost.dbcenterapi.utils.DataLoader
import com.boost.dbcenterapi.utils.Utils
import com.facebook.FacebookSdk.getApplicationContext
import com.framework.models.BaseViewModel
import com.google.gson.Gson
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class ComparePacksViewModel: BaseViewModel() {

    var featureResult: MutableLiveData<List<FeaturesModel>> = MutableLiveData()
    var cartResult: MutableLiveData<List<CartModel>> = MutableLiveData()
    var updatesError: MutableLiveData<String> = MutableLiveData()
    var updatesLoader: MutableLiveData<Boolean> = MutableLiveData()
    var NewApiService = Utils.getRetrofit(true).create(NewApiInterface::class.java)
    var experienceCode: String = "SVC"
    var _fpTag: String = "ABC"
    var allBundleResult: MutableLiveData<List<BundlesModel>> = MutableLiveData()

    fun getSpecificFeature(): LiveData<List<FeaturesModel>> {
        return featureResult
    }

    fun cartResult(): LiveData<List<CartModel>> {
        return cartResult
    }

    fun updatesLoader(): LiveData<Boolean> {
        return updatesLoader
    }

    fun getFeatureValues(list: List<String>) {
        CompositeDisposable().add(
            AppDatabase.getInstance(Application())!!
                .featuresDao()
                .getSpecificFeature(list)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        featureResult.postValue(it)
                        updatesLoader.postValue(false)
                    },
                    {
                        it.printStackTrace()
                        updatesError.postValue(it.message)
                        updatesLoader.postValue(false)
                    }
                )
        )
    }

    fun getCartItems() {
        updatesLoader.postValue(true)
        CompositeDisposable().add(
            AppDatabase.getInstance(Application())!!
                .cartDao()
                .getCartItems()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess {
                    cartResult.postValue(it)
                    updatesLoader.postValue(false)
                }
                .doOnError {
                    updatesError.postValue(it.message)
                    updatesLoader.postValue(false)
                }
                .subscribe()
        )
    }

    fun setCurrentExperienceCode(code: String, fpTag: String) {
        experienceCode = code
    }

    fun getAllBundles(): LiveData<List<BundlesModel>> {
        return allBundleResult
    }

    fun loadPackageUpdates() {
        updatesLoader.postValue(true)
        if (Utils.isConnectedToInternet(getApplicationContext())) {
            CompositeDisposable().add(
                NewApiService.GetAllFeatures()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        {
                            Log.e("GetAllFeatures", it.toString())
                            val bundles = arrayListOf<BundlesModel>()
                            for (item in it.Data[0].bundles) {
                                if (item.exclusive_for_customers != null && item.exclusive_for_customers!!.size > 0) {
                                    var applicableToCurrentFPTag = false
                                    for (code in item.exclusive_for_customers!!) {
                                        if (code.equals(_fpTag, true)) {
                                            applicableToCurrentFPTag = true
                                            break
                                        }
                                    }
                                    if (!applicableToCurrentFPTag)
                                        continue
                                }
                                if (item.exclusive_to_categories != null && item.exclusive_to_categories!!.size > 0) {
                                    var applicableToCurrentExpCode = false
                                    for (code in item.exclusive_to_categories!!) {
                                        if (code.equals(experienceCode, true)) {
                                            applicableToCurrentExpCode = true
                                            break
                                        }
                                    }
                                    if (!applicableToCurrentExpCode)
                                        continue
                                }
                                bundles.add(
                                    BundlesModel(
                                        item._kid,
                                        item.name,
                                        if (item.min_purchase_months != null && item.min_purchase_months!! > 1) item.min_purchase_months!! else 1,
                                        item.overall_discount_percent,
                                        if (item.primary_image != null) item.primary_image!!.url else null,
                                        Gson().toJson(item.included_features),
                                        item.target_business_usecase,
                                        Gson().toJson(item.exclusive_to_categories), item.desc
                                    )
                                )
                            }
                                    Log.i("insertAllBundles", "Successfully")
                                    allBundleResult.postValue(bundles)
                                    updatesLoader.postValue(false)
                        },
                        {
                            Log.e("GetAllFeatures", "error" + it.message)
                            updatesLoader.postValue(false)
                        }
                    )
            )
        }
    }

    fun addItemToCartPackage1(cartItem: CartModel) {
        updatesLoader.postValue(true)
        Completable.fromAction {
            AppDatabase.getInstance(Application())!!.cartDao()
                .insertToCart(cartItem)
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnComplete {
                updatesLoader.postValue(false)
                //add cartitem to firebase
                DataLoader.updateCartItemsToFirestore(Application())
            }
            .doOnError {
                updatesError.postValue(it.message)
                updatesLoader.postValue(false)
            }
            .subscribe()
    }
}