package com.bwctrans

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bwctrans.databinding.ItemTranslationBinding

class TranslationAdapter : RecyclerView.Adapter<TranslationAdapter.TranslationViewHolder>() {

    // Pair<Text, isUser>
    private val translations = mutableListOf<Pair<String, Boolean>>()
    private var lastSpeakerIsUser: Boolean? = null

    class TranslationViewHolder(val binding: ItemTranslationBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TranslationViewHolder {
        val binding = ItemTranslationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TranslationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TranslationViewHolder, position: Int) {
        // --- MODIFICATION: Access translations in reverse to match layout ---
        val (text, isUser) = translations[translations.size - 1 - position]
        holder.binding.translationText.text = text
        holder.binding.speakerLabel.text = if (isUser) "You said:" else "Translation:"
        holder.binding.translationText.gravity = if (isUser) android.view.Gravity.END else android.view.Gravity.START
        holder.binding.speakerLabel.gravity = if (isUser) android.view.Gravity.END else android.view.Gravity.START
    }

    override fun getItemCount() = translations.size

    fun addOrUpdateTranslation(text: String, isUser: Boolean) {
        if (lastSpeakerIsUser == isUser && translations.isNotEmpty()) {
            // Update the last message in the list
            translations[translations.size - 1] = text to isUser
            notifyItemChanged(0) // Because layout is reversed, the last item is at position 0
        } else {
            // Add a new message to the end of the list
            translations.add(text to isUser)
            notifyItemInserted(0) // Notify insertion at the "top" of the reversed view
        }
        lastSpeakerIsUser = isUser
    }
}
