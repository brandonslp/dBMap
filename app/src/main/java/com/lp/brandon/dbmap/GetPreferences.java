package com.lp.brandon.dbmap;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by brand on 31/10/2016.
 */
public class GetPreferences {
    Context context;
    SharedPreferences preferences;

    public GetPreferences(Context context) {
        this.context = context;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public String getStartTime(){
        return preferences.getString("edtsettings_starttime","3");
    }

    public String gettTmelap(){
        return preferences.getString("edtsettings_timelap","500");
    }

    public String getdB(){
        return preferences.getString("edtsettings_db","-20");
    }

    public String getAmplitudeReference(){
        return preferences.getString("edtsettings_amp","32768.0");
    }

    public String getRadiusMap(){
        return preferences.getString("edtsettings_radio","100");
    }
}
