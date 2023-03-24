package uk.co.stevebosman.daylight

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import uk.co.stevebosman.daylight.databinding.DaylightViewBinding


/**
 * Custom View to display date, alongside sunrise and sunset times.
 */
class DaylightView : ConstraintLayout {
    private val binding: DaylightViewBinding
    init {
        val layoutInflater: LayoutInflater = LayoutInflater.from(context)
        binding = DaylightViewBinding.inflate(layoutInflater, this)
    }

    private var _sunrise: String? = null
    private var _sunset: String? = null
    private var _date: String? = null

    /**
     * The text to draw
     */
    var sunrise: String?
        get() = _sunrise
        set(value) {
            _sunrise = value
            binding.sunriseView.text = value
        }
    var sunset: String?
        get() = _sunset
        set(value) {
            _sunset = value
            binding.sunsetView.text = value
        }
    var date: String?
        get() = _date
        set(value) {
            _date = value
            binding.dateView.text = value
        }

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        inflate(context, R.layout.daylight_view, this)
        // Load attributes
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.DaylightView, defStyle, 0
        )

        _sunrise = a.getString(
            R.styleable.DaylightView_sunrise
        )
        _sunset = a.getString(
            R.styleable.DaylightView_sunset
        )
        _date = a.getString(
            R.styleable.DaylightView_date
        )

        a.recycle()
    }
}
