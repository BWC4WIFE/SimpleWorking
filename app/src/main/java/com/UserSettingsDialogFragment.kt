package com.bwctrans

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.app.Dialog
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.bwctrans.databinding.DialogUserSettingsBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import android.Manifest
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager

class UserSettingsDialogFragment : BottomSheetDialogFragment() {

    // --- NEW: Interface for communication ---
    interface UserSettingsListener {
        fun onRequestPermission()
    }

    private var _binding: DialogUserSettingsBinding? = null
    private val binding get() = _binding!!
    private var listener: UserSettingsListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is UserSettingsListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement UserSettingsListener")
        }
    }

   override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            if (bottomSheet != null) {
                val behavior = BottomSheetBehavior.from(bottomSheet)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true // Prevents it from collapsing to a peek height
            }
        }
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.closeBtn.setOnClickListener {
            dismiss()
        }

        binding.autoPlaybackSwitch.setOnCheckedChangeListener { _, isChecked ->
            val message = "Auto-playback " + if (isChecked) "ON" else "OFF"
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }

        binding.sendFeedbackBtn.setOnClickListener {
            Toast.makeText(requireContext(), "Feedback action triggered", Toast.LENGTH_SHORT).show()
            dismiss()
        }

        // --- NEW: Logic for the permission button ---
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            binding.requestPermissionBtn.visibility = View.VISIBLE
            binding.requestPermissionBtn.setOnClickListener {
                listener?.onRequestPermission()
                dismiss()
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
