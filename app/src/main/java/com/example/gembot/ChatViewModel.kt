package com.example.gembot

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.launch

class ChatViewModel :ViewModel() {
    private val generativeModel:GenerativeModel = GenerativeModel(modelName = "gemini-1.5-flash", apiKey = Constant.apikey)
    val messageList by lazy { mutableStateListOf<MessageModel>() }
    fun sendMessage(question:String,applicationContext: Context){

        if (!isInternetAvailable(applicationContext)) {
            messageList.add(MessageModel("Error: No internet connection", "model", System.currentTimeMillis()))
            return
        }
        try {
            viewModelScope.launch {
                val currentTime = System.currentTimeMillis()
                messageList.add(MessageModel(question, "user", currentTime))
                messageList.add(MessageModel("Typing...", "model", currentTime))
                val chat = generativeModel.startChat(
                    history = messageList.map {
                        content(it.role){ text(it.message) }
                    }.toList()
                )
                val response=chat.sendMessage(question)
                messageList.removeLast()
                messageList.add(MessageModel(response.text.toString(), "model", System.currentTimeMillis()))
                //Log.d("Response",response.text.toString())
            }
        }catch (e:Exception){
            messageList.removeLast()
            messageList.add(MessageModel("Error: ${e.message}", "model", System.currentTimeMillis()))

        }
    }
    fun clearChat() {
        messageList.clear()
    }
    private fun isInternetAvailable(applicationContext: Context): Boolean {
        val connectivityManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.activeNetwork?.let { network ->
            connectivityManager.getNetworkCapabilities(network)
        }
        return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }
}