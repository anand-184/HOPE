package com.anand.hope

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.anand.hope.databinding.FragmentBotBinding
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException

class BotFragment : Fragment() {

    private var botBinding: FragmentBotBinding? = null
    private val client = OkHttpClient()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        botBinding = FragmentBotBinding.inflate(inflater, container, false)
        return botBinding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        botBinding?.predictButton?.setOnClickListener {
            val inputText = botBinding?.userInput?.text.toString()
            if (inputText.isNotEmpty()) {
                predictDisaster(inputText) { result ->
                    activity?.runOnUiThread {
                        botBinding?.predictionResult?.text = "Prediction: $result"
                    }
                }
            } else {
                botBinding?.predictionResult?.text = "Please enter a description."
            }
        }
    }

    private fun predictDisaster(userText: String, callback: (String) -> Unit) {
        val url = "https://api-inference.huggingface.co/models/facebook/bart-large-mnli"
        val apiKey = "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"  // Replace with your actual API key

        val requestBody = RequestBody.create(
            "application/json".toMediaTypeOrNull(),
            "{\"inputs\": \"$userText\", \"parameters\": {\"candidate_labels\": [\"Flood\", \"Earthquake\", \"No Disaster\"]}}"
        )

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")  // Corrected API Key usage
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback("Error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()

                if (!responseData.isNullOrEmpty()) {
                    try {
                        val json = JSONObject(responseData)
                        if (json.has("labels")) {
                            val prediction = json.getJSONArray("labels").getString(0)
                            callback(prediction)
                        } else {
                            callback("Error: Unexpected response format")
                        }
                    } catch (e: Exception) {
                        callback("Error parsing response: ${e.message}")
                    }
                } else {
                    callback("Error: Empty response from API")
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        botBinding = null
    }
}
