package com.example.ichiba.utils

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class SortDialogFragment : DialogFragment() {

    interface SortOptionListener {
        fun onSortOptionSelected(option: String)
    }

    private var listener: SortOptionListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val options = arrayOf("Price: Low to High", "Price: High to Low", "Date: Newest First", "Date: Oldest First")

            builder.setTitle("Sort By")
                .setSingleChoiceItems(options, -1) { dialog, which ->
                    listener?.onSortOptionSelected(options[which])
                    dialog.dismiss()
                }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = parentFragment as SortOptionListener
        } catch (e: ClassCastException) {
            throw ClassCastException((parentFragment.toString() +
                    " must implement SortOptionListener"))
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }
}