package sk.vyzyvacky.utilities.internet

import android.content.Context
import com.android.volley.Request
import org.json.JSONObject

class LoginRequest {
    companion object {
        fun send(
            context: Context,
            gamecode: String,
            password: String,
            responseFunction: (response: JSONObject, success: Boolean) -> Unit
        ) {
            //make json object
            val jsonObj = JSONObject()
            jsonObj.put("code", gamecode)
            jsonObj.put("password", password)

            //send
            HttpRequestManager.sendObjectRequest(
                context, jsonObj,
                RequestType.LOGIN, Request.Method.POST, responseFunction
            )
        }
    }
}