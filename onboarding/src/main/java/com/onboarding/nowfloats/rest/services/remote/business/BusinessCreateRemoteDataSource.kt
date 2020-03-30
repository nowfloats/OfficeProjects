package com.onboarding.nowfloats.rest.services.remote.business

import com.onboarding.nowfloats.model.business.BusinessCreateRequest
import com.onboarding.nowfloats.rest.EndPoints
import io.reactivex.Observable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface BusinessCreateRemoteDataSource {
    @PUT(EndPoints.POST_CREATE_BUSINESS_URL)
    fun createBusinessOnboarding(@Query("existingProfileId") profileId: String?, @Body request: BusinessCreateRequest): Observable<Response<String>>
}