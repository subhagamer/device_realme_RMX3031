/*
 * Copyright (C) 2016 The OmniROM Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.realmeparts;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.Display;

import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;
import androidx.preference.TwoStatePreference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;

public class DeviceSettings extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    public static final String KEY_SRGB_SWITCH = "srgb";
    public static final String KEY_HBM_SWITCH = "hbm";
    public static final String KEY_DC_SWITCH = "dc";
    public static final String KEY_OTG_SWITCH = "otg";
    public static final String KEY_PERF_PROFILE = "perf_profile";
	public static final String KEY_VIBRATION_STRENGTH = "vibration_strength";
	public static final String VIB_STRENGTH_SYSTEM_PROPERTY = "persist.vib_strength";
    public static final String PERF_PROFILE_SYSTEM_PROPERTY = "persist.perf_profile";
    public static final String KEY_GAME_SWITCH = "game";
    public static final String KEY_CHARGING_SWITCH = "smart_charging";
    public static final String KEY_CHARGING_SPEED = "charging_speed";
    public static final String KEY_RESET_STATS = "reset_stats";
	public static final String KEY_DT2W_SWITCH = "dt2w";
    public static final String KEY_DND_SWITCH = "dnd";
    public static final String KEY_CABC = "cabc";
    public static final String CABC_SYSTEM_PROPERTY = "persist.cabc_profile";
    public static final String KEY_SETTINGS_PREFIX = "device_setting_";
    public static final String TP_LIMIT_ENABLE = "/proc/touchpanel/oplus_tp_limit_enable";
    public static final String TP_DIRECTION = "/proc/touchpanel/oplus_tp_direction";
    private static final String ProductName = Utils.ProductName();
    private static final String KEY_CATEGORY_CHARGING = "charging";
    private static final String KEY_CATEGORY_GRAPHICS = "graphics";
    private static final String KEY_CATEGORY_REFRESH_RATE = "refresh_rate";
    public static SecureSettingListPreference mChargingSpeed;
    public static TwoStatePreference mResetStats;
    public static TwoStatePreference mRefreshRate120Forced;
	private static TwoStatePreference mDT2WModeSwitch;
    public static RadioButtonPreference mRefreshRate120;
    public static RadioButtonPreference mRefreshRate60;
    public static SeekBarPreference mSeekBarPreference;
    public static DisplayManager mDisplayManager;
    private static NotificationManager mNotificationManager;
    public TwoStatePreference mDNDSwitch;
    public PreferenceCategory mPreferenceCategory;
	private Vibrator mVibrator;
	private SecureSettingListPreference mVibStrength;
    private TwoStatePreference mDCModeSwitch;
    private TwoStatePreference mSRGBModeSwitch;
    private TwoStatePreference mHBMModeSwitch;
    private TwoStatePreference mOTGModeSwitch;
    private TwoStatePreference mGameModeSwitch;
    private TwoStatePreference mSmartChargingSwitch;
    private boolean CABC_DeviceMatched;
    private boolean DC_DeviceMatched;
    private boolean HBM_DeviceMatched;
    private boolean sRGB_DeviceMatched;
    private SecureSettingListPreference mCABC;
	private SecureSettingListPreference mPerfProfile;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        prefs.edit().putString("ProductName", ProductName).apply();

        addPreferencesFromResource(R.xml.main);

        mDCModeSwitch = findPreference(KEY_DC_SWITCH);
        mDCModeSwitch.setEnabled(DCModeSwitch.isSupported());
        mDCModeSwitch.setChecked(DCModeSwitch.isCurrentlyEnabled(this.getContext()));
        mDCModeSwitch.setOnPreferenceChangeListener(new DCModeSwitch());

        mSRGBModeSwitch = findPreference(KEY_SRGB_SWITCH);
        mSRGBModeSwitch.setEnabled(SRGBModeSwitch.isSupported());
        mSRGBModeSwitch.setChecked(SRGBModeSwitch.isCurrentlyEnabled(this.getContext()));
        mSRGBModeSwitch.setOnPreferenceChangeListener(new SRGBModeSwitch());

        mHBMModeSwitch = (TwoStatePreference) findPreference(KEY_HBM_SWITCH);
        mHBMModeSwitch.setEnabled(HBMModeSwitch.isSupported());
        mHBMModeSwitch.setChecked(HBMModeSwitch.isCurrentlyEnabled(this.getContext()));
        mHBMModeSwitch.setOnPreferenceChangeListener(new HBMModeSwitch(getContext()));

        mOTGModeSwitch = (TwoStatePreference) findPreference(KEY_OTG_SWITCH);
        mOTGModeSwitch.setEnabled(OTGModeSwitch.isSupported());
        mOTGModeSwitch.setChecked(OTGModeSwitch.isCurrentlyEnabled(this.getContext()));
        mOTGModeSwitch.setOnPreferenceChangeListener(new OTGModeSwitch());

        mGameModeSwitch = findPreference(KEY_GAME_SWITCH);
        mGameModeSwitch.setEnabled(GameModeSwitch.isSupported());
        mGameModeSwitch.setChecked(GameModeSwitch.isCurrentlyEnabled(this.getContext()));
        mGameModeSwitch.setOnPreferenceChangeListener(new GameModeSwitch(getContext()));

        mDNDSwitch = findPreference(KEY_DND_SWITCH);
        mDNDSwitch.setChecked(prefs.getBoolean(KEY_DND_SWITCH, false));
        mDNDSwitch.setOnPreferenceChangeListener(this);

        mSmartChargingSwitch = findPreference(KEY_CHARGING_SWITCH);
        mSmartChargingSwitch.setChecked(prefs.getBoolean(KEY_CHARGING_SWITCH, false));
        mSmartChargingSwitch.setOnPreferenceChangeListener(new SmartChargingSwitch(getContext()));

        mChargingSpeed = findPreference(KEY_CHARGING_SPEED);
        mChargingSpeed.setEnabled(mSmartChargingSwitch.isChecked());
        mChargingSpeed.setOnPreferenceChangeListener(this);

        mResetStats = findPreference(KEY_RESET_STATS);
        mResetStats.setChecked(prefs.getBoolean(KEY_RESET_STATS, false));
        mResetStats.setEnabled(mSmartChargingSwitch.isChecked());
        mResetStats.setOnPreferenceChangeListener(this);

        mSeekBarPreference = findPreference("seek_bar");
        mSeekBarPreference.setEnabled(mSmartChargingSwitch.isChecked());
        SeekBarPreference.mProgress = prefs.getInt("seek_bar", 95);

        mRefreshRate120Forced = findPreference("refresh_rate_120Forced");
        mRefreshRate120Forced.setChecked(prefs.getBoolean("refresh_rate_120Forced", false));
        mRefreshRate120Forced.setOnPreferenceChangeListener(new RefreshRateSwitch(getContext()));

        mRefreshRate120 = findPreference("refresh_rate_120");
        mRefreshRate120.setChecked(RefreshRateSwitch.isCurrentlyEnabled(this.getContext()));
        mRefreshRate120.setOnPreferenceChangeListener(new RefreshRateSwitch(getContext()));

        mRefreshRate60 = findPreference("refresh_rate_60");
        mRefreshRate60.setChecked(!RefreshRateSwitch.isCurrentlyEnabled(this.getContext()));
        mRefreshRate60.setOnPreferenceChangeListener(new RefreshRateSwitch(getContext()));

        mCABC = (SecureSettingListPreference) findPreference(KEY_CABC);
        mCABC.setValue(Utils.getStringProp(CABC_SYSTEM_PROPERTY, "0"));
        mCABC.setSummary(mCABC.getEntry());
        mCABC.setOnPreferenceChangeListener(this);
		
		mPerfProfile = (SecureSettingListPreference) findPreference(KEY_PERF_PROFILE);
        mPerfProfile.setValue(Utils.getStringProp(PERF_PROFILE_SYSTEM_PROPERTY, "0"));
        mPerfProfile.setSummary(mPerfProfile.getEntry());
        mPerfProfile.setOnPreferenceChangeListener(this);
		
		mDT2WModeSwitch = (TwoStatePreference) findPreference(KEY_DT2W_SWITCH);
        mDT2WModeSwitch.setEnabled(DT2WModeSwitch.isSupported());
        mDT2WModeSwitch.setChecked(DT2WModeSwitch.isCurrentlyEnabled(this.getContext()));
        mDT2WModeSwitch.setOnPreferenceChangeListener(new DT2WModeSwitch());

        mVibStrength = (SecureSettingListPreference) findPreference(KEY_VIBRATION_STRENGTH);
        mVibStrength.setValue(Utils.getStringProp(VIB_STRENGTH_SYSTEM_PROPERTY, "2500"));
        mVibStrength.setSummary(mVibStrength.getEntry());
        mVibStrength.setOnPreferenceChangeListener(this);

        mVibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);

        // Few checks to enable/disable options when activity is launched
        if ((prefs.getBoolean("refresh_rate_120", false) && prefs.getBoolean("refresh_rate_120Forced", false))) {
            mRefreshRate60.setEnabled(false);
            mRefreshRate120.setEnabled(false);
        } else if ((prefs.getBoolean("refresh_rate_60", false))) {
            mRefreshRate120Forced.setEnabled(false);
        }

        isCoolDownAvailable();
        DisplayRefreshRateModes();
        try {
            ParseJson();
        } catch (Exception e) {
            Log.d("DeviceSettings", e.toString());
        }
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mRefreshRate120) {
            mRefreshRate60.setChecked(false);
            mRefreshRate120.setChecked(true);
            mRefreshRate120Forced.setEnabled(true);
            return true;
        } else if (preference == mRefreshRate60) {
            mRefreshRate60.setChecked(true);
            mRefreshRate120.setChecked(false);
            mRefreshRate120Forced.setEnabled(false);
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        if (preference == mChargingSpeed) {
            mChargingSpeed.setValue((String) newValue);
            mChargingSpeed.setSummary(mChargingSpeed.getEntry());
        }

        if (preference == mCABC) {
            mCABC.setValue((String) newValue);
            mCABC.setSummary(mCABC.getEntry());
            Utils.setStringProp(CABC_SYSTEM_PROPERTY, (String) newValue);
        }
		if (preference == mPerfProfile) {
            mPerfProfile.setValue((String) newValue);
            mPerfProfile.setSummary(mPerfProfile.getEntry());
            Utils.setStringProp(PERF_PROFILE_SYSTEM_PROPERTY, (String) newValue);
        }
		if (preference == mVibStrength) {
            mVibStrength.setValue((String) newValue);
            mVibStrength.setSummary(mVibStrength.getEntry());
            Utils.setStringProp(VIB_STRENGTH_SYSTEM_PROPERTY, (String) newValue);
            mVibrator.vibrate(VibrationEffect.createOneShot(85, VibrationEffect.DEFAULT_AMPLITUDE));
        }
        return true;
    }

    // Remove Charging Speed preference if cool_down node is unavailable
    private void isCoolDownAvailable() {
        mPreferenceCategory = (PreferenceCategory) findPreference(KEY_CATEGORY_CHARGING);

        if (Utils.fileWritable(SmartChargingService.mmi_charging_enable)) {
            if (!Utils.fileWritable(SmartChargingService.cool_down)) {
                mPreferenceCategory.removePreference(findPreference(KEY_CHARGING_SPEED));
            }
        } else {
            getPreferenceScreen().removePreference(mPreferenceCategory);
        }
    }

    // Remove display refresh rate modes category if display doesn't support 120hz
    private void DisplayRefreshRateModes() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        String refreshRate = "";
        mDisplayManager = (DisplayManager) this.getContext().getSystemService(Context.DISPLAY_SERVICE);
        Display.Mode[] DisplayModes = mDisplayManager.getDisplay(Display.DEFAULT_DISPLAY).getSupportedModes();
        for (Display.Mode mDisplayMode : DisplayModes) {
            DecimalFormat df = new DecimalFormat("0.##");
            refreshRate += df.format(mDisplayMode.getRefreshRate()) + "Hz, ";
        }
        Log.d("DeviceSettings", "Device supports " + refreshRate + "refresh rate modes");

        if (!refreshRate.contains("120")) {
            prefs.edit().putBoolean("refresh_rate_120_device", false).apply();
            mPreferenceCategory = (PreferenceCategory) findPreference(KEY_CATEGORY_REFRESH_RATE);
            getPreferenceScreen().removePreference(mPreferenceCategory);
        } else prefs.edit().putBoolean("refresh_rate_120_device", true).apply();
    }

    private void ParseJson() throws JSONException {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        mPreferenceCategory = (PreferenceCategory) findPreference(KEY_CATEGORY_GRAPHICS);
        String features_json = Utils.InputStreamToString(getResources().openRawResource(R.raw.realmeparts_features));
        JSONObject jsonOB = new JSONObject(features_json);

        JSONArray CABC = jsonOB.getJSONArray(KEY_CABC);
        for (int i = 0; i < CABC.length(); i++) {
            if (ProductName.toUpperCase().contains(CABC.getString(i))) {
                {
                    CABC_DeviceMatched = true;
                }
            }
        }

        JSONArray DC = jsonOB.getJSONArray(KEY_DC_SWITCH);
        for (int i = 0; i < DC.length(); i++) {
            if (ProductName.toUpperCase().contains(DC.getString(i))) {
                {
                    DC_DeviceMatched = true;
                }
            }
        }

        JSONArray HBM = jsonOB.getJSONArray(KEY_HBM_SWITCH);
        for (int i = 0; i < HBM.length(); i++) {
            if (ProductName.toUpperCase().contains(HBM.getString(i))) {
                {
                    HBM_DeviceMatched = true;
                }
            }
        }

        JSONArray sRGB = jsonOB.getJSONArray(KEY_SRGB_SWITCH);
        for (int i = 0; i < sRGB.length(); i++) {
            if (ProductName.toUpperCase().contains(DC.getString(i))) {
                {
                    sRGB_DeviceMatched = true;
                }
            }
        }

        // Remove CABC preference if device is unsupported
        if (!CABC_DeviceMatched) {
            mPreferenceCategory.removePreference(findPreference(KEY_CABC));
            prefs.edit().putBoolean("CABC_DeviceMatched", false).apply();
        } else prefs.edit().putBoolean("CABC_DeviceMatched", true).apply();

        // Remove DC-Dimming preference if device is unsupported
        if (!DC_DeviceMatched) {
            mPreferenceCategory.removePreference(findPreference(KEY_DC_SWITCH));
            prefs.edit().putBoolean("DC_DeviceMatched", false).apply();
        } else prefs.edit().putBoolean("DC_DeviceMatched", true).apply();

        // Remove HBM preference if device is unsupported
        if (!HBM_DeviceMatched) {
            mPreferenceCategory.removePreference(findPreference(KEY_HBM_SWITCH));
            prefs.edit().putBoolean("HBM_DeviceMatched", false).apply();
        } else prefs.edit().putBoolean("HBM_DeviceMatched", true).apply();

        // Remove sRGB preference if device is unsupported
        if (!sRGB_DeviceMatched) {
            mPreferenceCategory.removePreference(findPreference(KEY_SRGB_SWITCH));
            prefs.edit().putBoolean("sRGB_DeviceMatched", false).apply();
        } else prefs.edit().putBoolean("sRGB_DeviceMatched", true).apply();
    }
}
