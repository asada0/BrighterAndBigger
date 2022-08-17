//
//  SettingActivity.kt
//  Brighter and Bigger
//
//  Created by Kazunori Asada, Masataka Matsuda and Hirofumi Ukawa on 2019/08/20.
//  Copyright 2010-2019 Kazunori Asada. All rights reserved.
//

package asada0.android.brighterbigger

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import kotlin.math.round

class SettingsActivity : AppCompatActivity() {
    companion object {
        const val REQUEST_CODE_MONO_COLOR = 1
        const val REQUEST_CODE_DUO_COLOR1 = 2
        const val REQUEST_CODE_DUO_COLOR2 = 3
        const val RESULT_OK = 1
        const val RESULT_CANCEL = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.settingsContainer, SettingPreferenceFragment())
                .commit()
    }

    class SettingPreferenceFragment : PreferenceFragmentCompat() {
        private lateinit var mPref: BBPreference
        private lateinit var mMonoModeCategory: PreferenceCategory

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.pref_settings, rootKey)
            mPref = BBPreference(this.context!!)
            mMonoModeCategory = findPreference(getString(R.string.pref_key_mono_mode_category)) as PreferenceCategory

            // Setup Discard Color
            setupDiscardColor()

            // Setup Reset Button
            setupResetButton()

            // Setup Manual Button
            setupManualButton()

            // Setup Privacy Policy Button
            setupPrivacyButton()

            // Disable Cont. Auto Focus OFF - Setting
            if (mPref.isOccurredTrouble(BBPreference.TROUBLE_NO_TAP_FOCUS_ANYWAY)) {
                val prefContAutoFocusOff = findPreference(getString(R.string.pref_key_cont_autofocus_off))
                prefContAutoFocusOff.isEnabled = false
            }
        }

        private fun setupDiscardColor() {
            val prefDiscardColorInfo = findPreference(getString(R.string.pref_key_discard_color_info))
            prefDiscardColorInfo.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                if (newValue as Boolean) {
                    showUsedColor()
                    mPref.mMonoMode = true
                } else {
                    hideUsedColor()
                    mPref.mMonoMode = false
                }
                mPref.mDiscardColorInfo = newValue
                true
            }

            // Init
            if (mPref.mDiscardColorInfo) {
                showUsedColor()
            }
        }

        private fun setupResetButton() {
            val reset = findPreference(getString(R.string.pref_key_reset_all))
            reset.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                AlertDialog.Builder(this.context!!)
                        .setTitle(getString(R.string.pref_title_reset_message))
                        .setPositiveButton(getString(R.string.pref_title_reset_ok)){ _, _ ->
                            BBPreference(this.context!!).resetAll()
                            this.activity!!.recreate()
                        }.setNegativeButton(getString(R.string.pref_title_reset_cancel)){ _, _ ->
                        }.show()
                true
            }
        }

        private fun setupManualButton() {
            val manual = findPreference(getString(R.string.pref_key_manual))
            manual.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                startActivity(Intent(activity, ManualActivity::class.java))
                true
            }
        }

        private fun setupPrivacyButton() {
            val privacy = findPreference(getString(R.string.pref_key_privacy))
            privacy.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                startActivity(Intent(activity, PrivacyActivity::class.java))
                true
            }
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)

            // Return from color picker
            if (resultCode != RESULT_OK) return
            if (requestCode == REQUEST_CODE_MONO_COLOR){
                setCustomColorSummary(1)
            }
            if (requestCode == REQUEST_CODE_DUO_COLOR1){
                setCustomColorSummary(2)
            }
            if (requestCode == REQUEST_CODE_DUO_COLOR2){
                setCustomColorSummary(3)
            }
        }

        private fun showUsedColor() {
            var target = findPreference(getString(R.string.pref_key_color_used))
            if (target == null){
                // Init
                when (mPref.mUsedColor) {
                    7 -> {
                        mPref.mMonoMode = true
                        showCustomColor(1)
                    }
                    8 -> {
                        mPref.mMonoMode = true
                        showCustomColor(2)
                        showCustomColor(3)
                    }
                }

                mMonoModeCategory.addPreference(ListPreference(this.activity).apply {
                    entries = resources.getStringArray(R.array.pref_color_used_list_titles)
                    entryValues = resources.getStringArray(R.array.pref_color_used_list_values)
                    layoutResource = R.layout.style_preference_info
                    title = getString(R.string.pref_title_color_used)
                    key = getString(R.string.pref_key_color_used)
                    summary = entries[mPref.mUsedColor]
                    order = 1
                })

                target = findPreference(getString(R.string.pref_key_color_used))
                target.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                    mPref.mUsedColor = newValue.toString().toInt()
                    target.summary = resources.getStringArray(R.array.pref_color_used_list_titles)[mPref.mUsedColor]
                    mPref.mMonoMode = true
                    when(mPref.mUsedColor) {
                        7 -> {
                            showCustomColor(1)
                            hideCustomColor(2)
                            hideCustomColor(3)
                        }
                        8 -> {
                            hideCustomColor(1)
                            showCustomColor(2)
                            showCustomColor(3)
                        }
                        else -> {
                            hideCustomColor(1)
                            hideCustomColor(2)
                            hideCustomColor(3)
                        }
                    }

                    true
                }
            }
        }

        private fun hideUsedColor() {
            val target = findPreference(getString(R.string.pref_key_color_used))
            if (target != null) {
                mMonoModeCategory.removePreference(target)
            }
            hideCustomColor(1)
            hideCustomColor(2)
            hideCustomColor(3)
        }

        private fun showCustomColor(type: Int) {
            val keyStr = when(type) {
                1 -> getString(R.string.pref_key_custom_mono_color)
                2 -> getString(R.string.pref_key_custom_duo_color1)
                else -> getString(R.string.pref_key_custom_duo_color2)
            }
            val reqCode = when(type) {
                1 -> REQUEST_CODE_MONO_COLOR
                2 -> REQUEST_CODE_DUO_COLOR1
                else -> REQUEST_CODE_DUO_COLOR2
            }

            var colorPreference = findPreference(keyStr)
            if (colorPreference != null) return // Found (Already exists)

            mMonoModeCategory.addPreference(Preference(this.activity).apply {
                title = when(type) {
                    1 -> getString(R.string.pref_title_custom_color)
                    2 -> getString(R.string.pref_title_custom_color_light)
                    else -> getString(R.string.pref_title_custom_color_dark)
                }
                layoutResource = R.layout.style_preference_info
                key = keyStr
                order = type + 1
            })

            setCustomColorSummary(type)

            colorPreference = findPreference(keyStr)
            colorPreference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                val prevColor = when(type) {
                    1 -> mPref.mCustomMonoColor
                    2 -> mPref.mCustomDuoColor1
                    else -> mPref.mCustomDuoColor2
                }

                // Display the color picker
                val intent = Intent(this.activity, ColorPickerActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.putExtra("prevColor", prevColor)
                intent.putExtra("reqCode", reqCode)
                startActivityForResult(intent, reqCode)
                true
            }
        }

        private fun hideCustomColor(type: Int) {
            val keyStr = when(type) {
                1 -> getString(R.string.pref_key_custom_mono_color)
                2 -> getString(R.string.pref_key_custom_duo_color1)
                else -> getString(R.string.pref_key_custom_duo_color2)
            }

            val target = findPreference(keyStr)
            if (target != null) {
                mMonoModeCategory.removePreference(target)
            }
        }

        private fun setCustomColorSummary(type: Int) {
            val keyStr = when(type) {
                1 -> getString(R.string.pref_key_custom_mono_color)
                2 -> getString(R.string.pref_key_custom_duo_color1)
                else -> getString(R.string.pref_key_custom_duo_color2)
            }

            val color = when(type) {
                1 -> mPref.mCustomMonoColor
                2 -> mPref.mCustomDuoColor1
                else -> mPref.mCustomDuoColor2
            }
            val colorPreference = findPreference(keyStr)
            colorPreference ?: return

            val r = round(Color.red(color) / 255.0f * 100.0f).toInt()
            val g = round(Color.green(color) / 255.0f * 100.0f).toInt()
            val b = round(Color.blue(color) / 255.0f * 100.0f).toInt()
            colorPreference.summary = String.format("R: %3d%%  G: %3d%%  B: %3d%%", r, g, b)
        }
    }
}