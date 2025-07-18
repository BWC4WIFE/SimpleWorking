package com.bwctrans

// --- IMPORTS ---
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.SeekBar
import com.bwctrans.databinding.DialogSettingsBinding

// --- CLASS & INTERFACE ---
class SettingsDialog(
    private val listener: DevSettingsListener,
    context: Context,
    private val prefs: SharedPreferences,
    private val models: List<String>
) : Dialog(context) {

    interface DevSettingsListener {
        fun onForceConnect()
    }

    // --- COMPANION OBJECT ---
    companion object {
        private const val TAG = "SettingsDialog"
    }

    // --- PROPERTIES ---
    private lateinit var binding: DialogSettingsBinding
    private var apiVersionsList: List<ApiVersion> = emptyList()
    private var apiKeysList: List<ApiKeyInfo> = emptyList()
    private var selectedApiVersion: ApiVersion? = null
    private var selectedApiKeyInfo: ApiKeyInfo? = null
    private var selectedModel: String = models.firstOrNull() ?: ""

    // --- DIALOG LIFECYCLE ---
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCancelable(true) // Allow dismissing by clicking outside

        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        loadApiVersionsFromResources()
        loadApiKeysFromResources()

        selectedModel = prefs.getString("selected_model", models.firstOrNull()) ?: models.firstOrNull() ?: ""

        setupViews()
    }

    // --- INITIALIZATION & DATA LOADING ---
    private fun loadApiVersionsFromResources() {
        val rawApiVersions = context.resources.getStringArray(R.array.api_versions)
        val parsedList = mutableListOf<ApiVersion>()

        for (itemString in rawApiVersions) {
            val parts = itemString.split("|", limit = 2)
            if (parts.size == 2) {
                parsedList.add(ApiVersion(parts[0].trim(), parts[1].trim()))
            } else {
                Log.w(TAG, "API version item in resources: '$itemString' does not contain '|'. Using as DisplayName|Value.")
                parsedList.add(ApiVersion(itemString.trim(), itemString.trim()))
            }
        }
        apiVersionsList = parsedList

        val currentApiVersionValue = prefs.getString("api_version", null)
        selectedApiVersion = parsedList.firstOrNull { it.value == currentApiVersionValue } ?: parsedList.firstOrNull()
    }

    private fun loadApiKeysFromResources() {
        val rawApiKeys = context.resources.getStringArray(R.array.api_keys)
        val parsedList = mutableListOf<ApiKeyInfo>()

        for (itemString in rawApiKeys) {
            val parts = itemString.split(":", limit = 2)
            if (parts.size == 2) {
                val displayName = parts[0].trim()
                val value = parts[1].trim()
                parsedList.add(ApiKeyInfo(displayName, value))
            } else {
                Log.e(TAG, "Malformed API key item in resources: '$itemString'. Expected 'DisplayName:Value' format.")
            }
        }
        apiKeysList = parsedList
        val currentApiKeyValue = prefs.getString("api_key", null)
        selectedApiKeyInfo = parsedList.firstOrNull { it.value == currentApiKeyValue } ?: parsedList.firstOrNull()
    }

    // --- UI SETUP & EVENT HANDLERS ---
    private fun setupViews() {
        // VAD Sensitivity SeekBar
        val currentVad = prefs.getInt("vad_sensitivity_ms", 800)
        binding.vadSensitivity.progress = currentVad
        binding.vadValue.text = "$currentVad ms"

        binding.vadSensitivity.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.vadValue.text = "$progress ms"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Model RadioGroup
        models.forEach { model ->
            val radioButton = RadioButton(context).apply {
                text = model
                tag = model
                isChecked = model == selectedModel
            }
            binding.modelRadioGroup.addView(radioButton)
        }

        binding.modelRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            val radioButton = group.findViewById<RadioButton>(checkedId)
            selectedModel = radioButton.tag.toString()
            binding.modelManualEditText.setText(selectedModel)
        }

        // API Version RadioGroup
        apiVersionsList.forEach { apiVersion ->
            val radioButton = RadioButton(context).apply {
                text = apiVersion.displayName
                tag = apiVersion
                isChecked = apiVersion == selectedApiVersion
            }
            binding.apiVersionRadioGroup.addView(radioButton)
        }

        binding.apiVersionRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            val radioButton = group.findViewById<RadioButton>(checkedId)
            selectedApiVersion = radioButton.tag as ApiVersion
        }


        binding.debugOverlaySwitch.isChecked = prefs.getBoolean("show_debug_overlay", false)

        // Save Button Listener
        binding.saveSettingsBtn.setOnClickListener {
            prefs.edit().apply {
                putInt("vad_sensitivity_ms", binding.vadSensitivity.progress)
                putString("api_key", binding.apiKeyManualEditText.text.toString())
                putString("selected_model", binding.modelManualEditText.text.toString())
                putBoolean("show_debug_overlay", binding.debugOverlaySwitch.isChecked)
                selectedApiVersion?.let { putString("api_version", it.value) }
                apply()
            }
            dismiss()
        }

        // Force Connect Button Listener
        binding.forceConnectBtn.setOnClickListener {
            listener.onForceConnect()
            dismiss() // Close the dialog
        }
    }
}
