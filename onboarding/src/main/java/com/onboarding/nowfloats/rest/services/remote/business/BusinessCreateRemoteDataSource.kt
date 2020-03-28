package com.onboarding.nowfloats.rest.services.remote.business

import com.onboarding.nowfloats.model.business.BusinessCreateRequest
import com.onboarding.nowfloats.model.business.BusinessCreateResponse
import com.onboarding.nowfloats.rest.EndPoints
import io.reactivex.Observable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface BusinessCreateRemoteDataSource {
    @POST(EndPoints.POST_CREATE_BUSINESS_URL)
    fun createBusinessOnboarding(@Url url: String = EndPoints.BASE_WITH_FLOATS,
                                 @Body request: BusinessCreateRequest): Observable<Response<BusinessCreateResponse>>
}