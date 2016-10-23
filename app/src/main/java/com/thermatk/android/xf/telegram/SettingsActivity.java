package com.thermatk.android.xf.telegram;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import co.ceryle.radiorealbutton.library.RadioRealButtonGroup;

public class SettingsActivity extends AppCompatActivity {
    protected static final String prefDef = "emojiLib";
    protected static final String prefKey = "emojiLib";
    protected static final int defChoice = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        final RadioRealButtonGroup rrbg = (RadioRealButtonGroup) findViewById(R.id.RadioRealButtonGroup);

        rrbg.setPosition(readPref(this));

        rrbg.setOnClickedButtonPosition(new RadioRealButtonGroup.OnClickedButtonPosition() {
            @Override
            public void onClickedButtonPosition(int position) {
                changePref(position);
            }
        });
    }

    public static int readPref(Context context) {
        final SharedPreferences prefs = context.getSharedPreferences(prefDef, Context.MODE_WORLD_READABLE);
        return prefs.getInt(prefKey, defChoice);
    }

    private void changePref(int newPref) {
        SharedPreferences.Editor editor = this.getSharedPreferences(prefDef, Context.MODE_WORLD_READABLE).edit();
        editor.putInt(prefKey, newPref);
        editor.apply();
    }

}
