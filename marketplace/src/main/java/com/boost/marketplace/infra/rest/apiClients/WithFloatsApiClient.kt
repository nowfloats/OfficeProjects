package com.boost.marketplace.infra.rest.apiClients

import com.framework.rest.BaseApiClient

class WithFloatsApiClient : BaseApiClient() {

  companion object {
    val shared = WithFloatsApiClient()
  }
}