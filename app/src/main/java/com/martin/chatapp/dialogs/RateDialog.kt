package com.martin.chatapp.dialogs

//android.support.v7 && android.support.v4 helps supporting sdk<23
import android.support.v7.app.AlertDialog
import android.support.v4.app.DialogFragment

import android.app.Dialog
import android.os.Bundle
import com.martin.chatapp.R
import com.martin.chatapp.extensions.toast

class RateDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        AlertDialog.Builder(context!!)
                .setTitle(getString(R.string.dialog_title))
                .setView(R.layout.dialog_rate)
                .setPositiveButton(getString(R.string.dialog_ok)) { dialog, which ->
                    activity!!.toast("Pressed OK")
                }
                .setNegativeButton(getString(R.string.dialog_cancel)) { dialog, which ->
                    activity!!.toast("Pressed Cancel")
                }
                .create()
    }
}