package com.catlogservice.viewmodel

import androidx.lifecycle.LiveData
import com.catlogservice.rest.repository.ServiceCreateRepository
import com.framework.base.BaseResponse
import com.framework.models.BaseViewModel
import com.framework.models.toLiveData

class ServiceViewModel : BaseViewModel() {

  fun getSellerOrders(request: Any): LiveData<BaseResponse> {
    return ServiceCreateRepository.createService(request).toLiveData()
  }
}