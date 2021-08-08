package sk.vyzyvacky.utilities

import android.content.Context
import android.widget.Toast
import com.android.volley.NetworkError
import com.android.volley.ServerError
import com.android.volley.TimeoutError
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject
import sk.vyzyvacky.R

enum class RequestType {
    LOGIN, ENTRY, PARTICIPANT
}

class HttpRequestManager {
    companion object {

        fun sendObjectRequest(
            context: Context,
            jsonObj: JSONObject?,
            type: RequestType,
            method: Int,
            handlerFunction: (response: JSONObject, success: Boolean) -> Unit,
            urlExtra: String = ""
        ) {
            val que = Volley.newRequestQueue(context)
            val url = getUrlFromType(type) + urlExtra

            val jsonObjectRequest = object : JsonObjectRequest(
                method, url, jsonObj,
                { response ->
                    handlerFunction(response, true)
                }, { error ->
                    if (error.networkResponse != null) {
                        if (error.networkResponse.statusCode == 400) {
                            handlerFunction(JSONObject(), false)
                        } else if (noConnectionErrorPresent(context, error))
                            handlerFunction(JSONObject(String(error.networkResponse.data)), false)
                    } else
                        handlerFunction(JSONObject(), false)
                }) {
                override fun getHeaders(): MutableMap<String, String> {
                    return defaultHeaders(context)
                }
            }
            que.add(jsonObjectRequest)
        }

        fun sendArrayRequest(
            context: Context,
            jsonArr: JSONArray?,
            type: RequestType,
            method: Int,
            handlerFunction: (response: JSONArray, success: Boolean) -> Unit,
            urlExtra: String = ""
        ) {
            val que = Volley.newRequestQueue(context)
            val url = getUrlFromType(type) + urlExtra

            val jsonArrayRequest = object : JsonArrayRequest(method, url, jsonArr,
                { response ->
                    handlerFunction(response, true)
                }, { error ->
                    if (noConnectionErrorPresent(context, error))
                        handlerFunction(JSONArray(String(error.networkResponse.data)), false)
                    else
                        handlerFunction(JSONArray(), false)
                }) {
                override fun getHeaders(): MutableMap<String, String> {
                    return defaultHeaders(context)
                }
            }
            que.add(jsonArrayRequest)
        }

        fun defaultHeaders(context: Context): MutableMap<String, String> {
            val params: MutableMap<String, String> = HashMap()
            params["Content-Type"] = "application/json"
            params["Authorization"] = DataHandler(context).getToken()
            return params
        }

        private fun getUrlFromType(type: RequestType): String {
            return when (type) {
                RequestType.LOGIN -> Urls.LOGIN
                RequestType.ENTRY -> Urls.ENTRY
                RequestType.PARTICIPANT -> Urls.PARTICIPANT
            }
        }

        private fun noConnectionErrorPresent(context: Context, error: VolleyError): Boolean {
            when (error) {
                is NetworkError -> Toast.makeText(
                    context,
                    context.getString(R.string.error_connection),
                    Toast.LENGTH_LONG
                ).show()
                is ServerError -> Toast.makeText(
                    context,
                    context.getString(R.string.error_server),
                    Toast.LENGTH_LONG
                ).show()
                is TimeoutError -> Toast.makeText(
                    context,
                    context.getString(R.string.error_timeout),
                    Toast.LENGTH_LONG
                ).show()
                else -> return true
            }
            return false
        }
    }
}