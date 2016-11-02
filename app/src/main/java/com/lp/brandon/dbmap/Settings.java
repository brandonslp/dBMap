package com.lp.brandon.dbmap;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by brand on 31/10/2016.
 */
public class Settings extends PreferenceActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }
}
