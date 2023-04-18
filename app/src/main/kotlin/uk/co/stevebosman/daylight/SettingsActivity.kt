package uk.co.stevebosman.daylight

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import uk.co.stevebosman.timepreference.TimeDialog
import uk.co.stevebosman.timepreference.TimePreference

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }
        private val dialogFragmentTag = "CustomPreference"

        override fun onDisplayPreferenceDialog(preference: Preference) {
            if (parentFragmentManager.findFragmentByTag(dialogFragmentTag) != null) {
                return
            }
            if (preference is TimePreference) {
                val f: DialogFragment = TimeDialog(preference.getKey())
                f.setTargetFragment(this, 0)
                f.show(parentFragmentManager, dialogFragmentTag)
            } else {
                super.onDisplayPreferenceDialog(preference)
            }
        }    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}