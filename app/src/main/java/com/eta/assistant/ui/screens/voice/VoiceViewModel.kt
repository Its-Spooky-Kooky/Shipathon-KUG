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
import com.eta.assistant.data.preferences.UserPreferencesRepository
import com.eta.assistant.data.repository.CartItem
import com.eta.assistant.data.repository.EtaRepository
import com.eta.assistant.data.repository.SaleResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale

data class VoiceUiState(
    val isListening: Boolean = false,
    val spokenText: String = "",
    val parsedIntent: ParsedIntent? = null,
    val statusMessage: String? = null,
    val isSuccess: Boolean = false
)

class VoiceViewModel(
    application: Application,
    private val repository: EtaRepository,
    private val preferencesRepository: UserPreferencesRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(VoiceUiState())
    val uiState: StateFlow<VoiceUiState> = _uiState.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null

    init {
        initSpeechRecognizer()
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
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
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

    fun executeParsedAction(
        onNavigateToCustomers: () -> Unit,
        onNavigateToReports: () -> Unit,
        onSaleRecorded: (Long) -> Unit
    ) {
        val intent = _uiState.value.parsedIntent ?: return
        viewModelScope.launch {
            when (intent) {
                is ParsedIntent.RecordSale -> {
                    // Find product and customer or create
                    val products = repository.getAllProducts().first()
                    val targetProduct = products.find {
                        it.name.contains(intent.productName, ignoreCase = true)
                    } ?: products.firstOrNull()

                    if (targetProduct != null) {
                        val customers = repository.getAllCustomers().first()
                        val matchedCustomer = customers.find {
                            it.name.contains(intent.customerName, ignoreCase = true)
                        }

                        val result = repository.recordSale(
                            customerId = matchedCustomer?.id,
                            customerName = matchedCustomer?.name ?: intent.customerName,
                            items = listOf(CartItem(targetProduct, intent.quantity)),
                            paidAmount = intent.amount ?: (targetProduct.sellingPrice * intent.quantity),
                            paymentMethod = if (intent.amount == null || intent.amount > 0) "CASH" else "CREDIT"
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
                    val target = products.find { it.name.contains(intent.productName, ignoreCase = true) }
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

