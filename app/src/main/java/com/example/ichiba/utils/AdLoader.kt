package com.example.ichiba.utils

import android.content.Context
import com.example.ichiba.activites.AdLoader

open class AdLoader {

    companion object {
        private var adLoaderDialog: AdLoader? = null

        fun showDialog(
            context: Context?,
            isCancelable: Boolean
        ) {
            hideDialog()
            if (context != null) {
                try {
                    adLoaderDialog = AdLoader(context)
                    adLoaderDialog?.let { loader ->
                        loader.setCanceledOnTouchOutside(true)
                        loader.setCancelable(isCancelable)
                        loader.show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        fun hideDialog() {
            try {
                adLoaderDialog?.let { loader ->
                    if (loader.isShowing) {
                        loader.dismiss()
                    }
                    adLoaderDialog = null
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
