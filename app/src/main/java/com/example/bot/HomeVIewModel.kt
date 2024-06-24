package com.example.bot

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeVIewModel :ViewModel() {

    private val _uiState : MutableStateFlow<HomeUiState> = MutableStateFlow(HomeUiState.Initial)
    val uiState = _uiState.asStateFlow()

    private lateinit var generativeModel : GenerativeModel

    init {

        val config = generationConfig {
            temperature = 0.70f
        }
         generativeModel = GenerativeModel(
            modelName = "gemini-1.0-pro-vision-latest",
            apiKey = com.example.bot.BuildConfig.apiKey,
            generationConfig = config
        )
    }
    fun question(userInput : String , selectedImages : List<Bitmap>){
        _uiState.value = HomeUiState.Loading
        val prompt = "Image or Question:$userInput"

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val content = content {
                    for (bitmap in selectedImages){
                        image(bitmap)
                    }
                    text(prompt)
                }
                var output = ""
                generativeModel.generateContentStream(content).collect{
                    output+=it.text
                    _uiState.value = HomeUiState.Success(output)
                }
            }catch (e:Exception){
                _uiState.value = HomeUiState.Error(e.localizedMessage ?: "Something gone wrong!!")

            }
        }
    }
}

sealed interface HomeUiState{
    object Initial : HomeUiState
    object Loading : HomeUiState
    data class Error(
        val error: String
    ) : HomeUiState
    data class Success(
        val outputText:String
    ) : HomeUiState
}

