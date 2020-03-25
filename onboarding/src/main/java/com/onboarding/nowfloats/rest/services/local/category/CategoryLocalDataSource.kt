package com.onboarding.nowfloats.rest.services.local.category

import android.content.Context
import com.framework.base.BaseResponse
import com.onboarding.nowfloats.base.rest.AppBaseLocalService
import com.onboarding.nowfloats.R
import com.onboarding.nowfloats.rest.response.category.CategoryListResponse
import io.reactivex.Observable


object CategoryLocalDataSource : AppBaseLocalService() {

  fun getCategory(context: Context): Observable<BaseResponse> {
    return fromJsonRes(context, R.raw.categories, CategoryListResponse::class.java)
  }
}