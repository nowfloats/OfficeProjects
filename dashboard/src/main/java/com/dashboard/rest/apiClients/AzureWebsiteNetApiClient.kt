package com.dashboard.rest.apiClients

import com.framework.rest.BaseApiClient

class AzureWebsiteNetApiClient : BaseApiClient(true) {

  companion object {
    val shared = AzureWebsiteNetApiClient()
  }
}