package com.eta.assistant.ui.screens.voice

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.eta.assistant.data.local.entity.ProductEntity
import com.eta.assistant.data.preferences.UserPreferencesRepository
import com.eta.assistant.data.repository.CartItem
import com.eta.assistant.data.repository.EtaRepository
import com.eta.assistant.data.repository.SaleResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class VoiceUiState(
    val isListening: Boolean = false,
    val spokenText: String = "",
    val parsedIntent: ParsedIntent? = null,
    val statusMessage: String? = null,
    val isSuccess: Boolean = false,
    val languageCode: String = "en"
)

class VoiceViewModel(
    application: Application,
    private val repository: EtaRepository,
    private val preferencesRepository: UserPreferencesRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(VoiceUiState())
    val uiState: StateFlow<VoiceUiState> = _uiState.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null
    private var hasRetriedOnline = false

    init {
        initSpeechRecognizer()
        viewModelScope.launch {
            preferencesRepository.userPreferencesFlow.collect { prefs ->
                _uiState.value = _uiState.value.copy(languageCode = prefs.languageCode)
            }
        }
    }

    private fun initSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(getApplication())) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplication()).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _uiState.value = _uiState.value.copy(
                            isListening = true,
                            statusMessage = "Listening..."
                        )
                    }

                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {
                        _uiState.value = _uiState.value.copy(isListening = false)
                    }

                    override fun onError(error: Int) {
                        // Error 12 (ERROR_LANGUAGE_NOT_SUPPORTED) or 13 (ERROR_LANGUAGE_UNAVAILABLE)
                        if ((error == 12 || error == 13) && !hasRetriedOnline) {
                            hasRetriedOnline = true
                            startListeningInternal(preferOffline = false)
                            return
                        }
                        _uiState.value = _uiState.value.copy(
                            isListening = false,
                            statusMessage = "Could not hear audio. You can also type commands."
                        )
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull() ?: ""
                        processCommand(text)
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        matches?.firstOrNull()?.let {
                            _uiState.value = _uiState.value.copy(spokenText = it)
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
        }
    }

    fun startListening() {
        hasRetriedOnline = false
        startListeningInternal(preferOffline = true)
    }

    private fun startListeningInternal(preferOffline: Boolean) {
        val speechLocale = when (_uiState.value.languageCode) {
            "hi" -> "hi-IN"
            "ta" -> "ta-IN"
            "bn" -> "bn-IN"
            else -> "en-IN"
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, speechLocale)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, speechLocale)
            if (preferOffline) {
                putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            }
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Say a business command...")
        }
        speechRecognizer?.startListening(intent)
        _uiState.value = _uiState.value.copy(isListening = true, statusMessage = "Listening...")
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        _uiState.value = _uiState.value.copy(isListening = false)
    }

    fun processCommand(commandText: String) {
        val intent = VoiceCommandParser.parse(commandText)
        _uiState.value = _uiState.value.copy(
            isListening = false,
            spokenText = commandText,
            parsedIntent = intent,
            statusMessage = null
        )
    }

    private fun matchProduct(query: String, products: List<ProductEntity>): ProductEntity? {
        val q = query.trim().lowercase()
        if (q.isBlank() || q == "store item" || q == "general item" || q == "item") return null

        // 1. Direct substring match
        val direct = products.find { it.name.lowercase().contains(q) }
        if (direct != null) return direct

        // 2. Bilingual alias map
        val aliases = mapOf(
            "sugar" to listOf("sugar", "chini", "shakkar", "चीनी", "शक्कर", "சர்க்கரை", "চিনি"),
            "rice" to listOf("rice", "chawal", "चावल", "அரிசி", "চাল"),
            "oil" to listOf("oil", "tel", "cooking oil", "तेल", "எண்ணெய்", "তেল"),
            "tea" to listOf("tea", "chai", "patti", "tea powder", "चाय", "தேயிலை", "চা"),
            "biscuit" to listOf("biscuit", "biscuits", "parle", "बिस्कुट", "बिस्किट", "பிஸ்கட்", "বিস্কুট"),
            "flour" to listOf("flour", "atta", "aata", "wheat", "आटा", "கோதுமை மாவு", "আটা"),
            "dal" to listOf("dal", "daal", "lentils", "दाल", "பருப்பு", "ডাল"),
            "soap" to listOf("soap", "sabun", "साबुन", "சோப்பு", "সাবান")
        )

        for ((key, synonyms) in aliases) {
            if (synonyms.any { q.contains(it) || it.contains(q) }) {
                val matched = products.find { prod ->
                    val prodLower = prod.name.lowercase()
                    synonyms.any { prodLower.contains(it) } || prodLower.contains(key)
                }
                if (matched != null) return matched
            }
        }

        // 3. Levenshtein distance <= 2 on base product name (stripping parenthetical units)
        val cleanQuery = q.replace(Regex("""\([^)]*\)"""), "").trim()
        for (prod in products) {
            val baseName = prod.name.lowercase().replace(Regex("""\([^)]*\)"""), "").trim()
            if (levenshteinDistance(cleanQuery, baseName) <= 2) {
                return prod
            }
        }

        return null
    }

    private fun levenshteinDistance(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }
        for (i in 0..s1.length) dp[i][0] = i
        for (j in 0..s2.length) dp[0][j] = j
        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,
                    dp[i][j - 1] + 1,
                    dp[i - 1][j - 1] + cost
                )
            }
        }
        return dp[s1.length][s2.length]
    }

    fun executeParsedAction(
        onNavigateToCustomers: () -> Unit,
        onNavigateToReports: () -> Unit,
        onSaleRecorded: (Long) -> Unit
    ) {
        val intent = _uiState.value.parsedIntent ?: return
        viewModelScope.launch {
            when (intent) {
                is ParsedIntent.RecordSale -> {
                    val products = repository.getAllProducts().first()
                    var targetProduct = matchProduct(intent.productName, products)

                    if (targetProduct == null && intent.amount != null && intent.amount > 0) {
                        val existingGeneral = products.find { it.name.equals("General Item", ignoreCase = true) }
                        targetProduct = if (existingGeneral != null) {
                            existingGeneral.copy(sellingPrice = intent.amount)
                        } else {
                            val newId = repository.addProduct(
                                ProductEntity(
                                    name = "General Item",
                                    category = "General",
                                    unit = "item",
                                    costPrice = intent.amount * 0.8,
                                    sellingPrice = intent.amount,
                                    stockQuantity = 9999.0,
                                    lowStockThreshold = 10.0
                                )
                            )
                            repository.getProductById(newId)
                        }
                    }

                    if (targetProduct != null) {
                        val customers = repository.getAllCustomers().first()
                        val matchedCustomer = customers.find {
                            it.name.contains(intent.customerName, ignoreCase = true)
                        }

                        val totalBill = intent.amount ?: (targetProduct.sellingPrice * intent.quantity)
                        val paidAmount = if (intent.isCredit) 0.0 else totalBill
                        val paymentMethod = if (intent.isCredit) "CREDIT" else "CASH"

                        val result = repository.recordSale(
                            customerId = matchedCustomer?.id,
                            customerName = matchedCustomer?.name ?: intent.customerName,
                            items = listOf(CartItem(targetProduct, intent.quantity)),
                            paidAmount = paidAmount,
                            paymentMethod = paymentMethod
                        )

                        if (result is SaleResult.Success) {
                            _uiState.value = _uiState.value.copy(isSuccess = true)
                            onSaleRecorded(result.saleId)
                        } else {
                            _uiState.value = _uiState.value.copy(statusMessage = "Could not record sale: ${ (result as SaleResult.Error).message }")
                        }
                    } else {
                        _uiState.value = _uiState.value.copy(statusMessage = "No matching product in inventory")
                    }
                }
                is ParsedIntent.AddStock -> {
                    val products = repository.getAllProducts().first()
                    val target = matchProduct(intent.productName, products) ?: products.find { it.name.contains(intent.productName, ignoreCase = true) }
                    if (target != null) {
                        repository.adjustProductStock(target.id, intent.quantity, "VOICE_RESTOCK")
                        _uiState.value = _uiState.value.copy(
                            isSuccess = true,
                            statusMessage = "Added ${intent.quantity.toInt()} to ${target.name} stock successfully!"
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(statusMessage = "Product '${intent.productName}' not found in inventory")
                    }
                }
                is ParsedIntent.ShowDues -> {
                    onNavigateToCustomers()
                }
                is ParsedIntent.ShowTopSelling -> {
                    onNavigateToReports()
                }
                is ParsedIntent.Unknown -> {
                    _uiState.value = _uiState.value.copy(statusMessage = "Command not recognized. Try one of the examples below.")
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognizer?.destroy()
    }
}

class VoiceViewModelFactory(
    private val application: Application,
    private val repository: EtaRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return VoiceViewModel(application, repository, preferencesRepository) as T
    }
}
