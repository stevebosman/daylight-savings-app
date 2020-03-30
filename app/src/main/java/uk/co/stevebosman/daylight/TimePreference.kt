package uk.co.stevebosman.daylight

import android.content.Context
import android.content.res.TypedArray
import android.icu.text.SimpleDateFormat
import android.util.AttributeSet
import androidx.preference.DialogPreference
import java.util.*


class TimePreference(ctxt: Context?, attrs: AttributeSet?, defStyle: Int) :
    DialogPreference(ctxt, attrs, defStyle) {
    var hour: Int = 0
    var minute: Int = 0

    constructor(ctxt: Context?) : this(ctxt, null)
    constructor(ctxt: Context?, attrs: AttributeSet?) : this(
        ctxt,
        attrs,
        R.attr.dialogPreferenceStyle
    )

    override fun onGetDefaultValue(a: TypedArray, index: Int): String? {
        return a.getString(index)
    }

    override fun onSetInitialValue(
        restoreValue: Boolean,
        defaultValue: Any?
    ) {
        var time = if (defaultValue == null) "00:00" else defaultValue as String
        if (restoreValue) {
            time = getPersistedString(time)
            summary = time
        }
        val format = SimpleDateFormat("HH:mm")
        val calendar = Calendar.getInstance()
        calendar.time = format.parse(time)
        hour = calendar.get(Calendar.HOUR_OF_DAY)
        minute = calendar.get(Calendar.MINUTE)
    }

    fun setValue(hour: Int, minute: Int) {
        val time = "${hour.toString().padStart(2, '0')}:${minute}"
        persistString(time)
        summary = time
    }

    init {
        setPositiveButtonText(android.R.string.ok)
        setNegativeButtonText(android.R.string.cancel)
    }
}