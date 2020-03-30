package uk.co.stevebosman.daylight

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TimePicker
import androidx.preference.PreferenceDialogFragmentCompat

class TimeDialog(val preferenceKey: String) : PreferenceDialogFragmentCompat() {
    private lateinit var picker: TimePicker

    init {
        val b  =  Bundle(1);
        b.putString(ARG_KEY, preferenceKey);
        setArguments(b);
    }

    override fun onCreateDialogView(context: Context?): View {
        picker = TimePicker(context)
        picker.setIs24HourView(true)
        picker.hour = timePreference().hour
        picker.minute = timePreference().minute
        return picker
    }

    override fun onBindDialogView(v: View?) {
        super.onBindDialogView(v)

        picker.hour = timePreference().hour
        picker.minute = timePreference().minute
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult) {
            timePreference().setValue(picker.hour, picker.minute)
        }
    }

    private fun timePreference() = (preference as TimePreference)

}