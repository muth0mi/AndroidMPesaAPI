/*
 * Copyright 2021 Juma Allan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.androidstudy.daraja.callback

import com.androidstudy.daraja.model.ErrorResponse
import com.androidstudy.daraja.model.PaymentResult
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class DarajaPaymentCallback(
    private val listener: DarajaPaymentListener
) : Callback<PaymentResult> {

    override fun onResponse(call: Call<PaymentResult>, response: Response<PaymentResult>) {
        if (response.isSuccessful) {
            val result: PaymentResult? = response.body()
            if (result != null) {
                if (result.ResponseCode == "0") {
                    listener.onPaymentRequestComplete(result)
                } else {
                    val error = "${result.ResponseCode} : ${result.ResponseDescription}"
                    listener.onPaymentFailure(DarajaException(error))
                }
                return
            }
        } else {
            try {
                val gson = GsonBuilder().create()
                val error = gson.fromJson(response.errorBody()?.string(), ErrorResponse::class.java)
                listener.onPaymentFailure(DarajaException(error))
            } catch (e: IOException) {
                e.printStackTrace()
                listener.onPaymentFailure(DarajaException("${response.code()}"))
            }
        }
    }

    override fun onFailure(call: Call<PaymentResult>, t: Throwable) {
        listener.onNetworkFailure(DarajaException(t.localizedMessage))
    }
}