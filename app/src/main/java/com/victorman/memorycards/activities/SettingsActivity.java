package com.victorman.memorycards.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.victorman.memorycards.App;
import com.victorman.memorycards.AppPreferences;
import com.victorman.memorycards.R;

public class SettingsActivity extends AppCompatActivity {

    public static final int RESULT_CODE_OK = 1;

    private int notifySendingIntervalMinutes;
    private int cardsMixMethod;
    private int cardsStackSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.DarkTheme);
        } else {
            setTheme(R.style.LightTheme);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Intent intent = getIntent();
        notifySendingIntervalMinutes = intent.getIntExtra(MainActivity.EXTRA_NOTIFY_SENDING_INTERVAL_MINUTES, 5);
        cardsMixMethod = intent.getIntExtra(MainActivity.EXTRA_CARDS_MIX_METHOD, MainActivity.CARDS_MIX_METHOD_RANDOM);
        cardsStackSize = intent.getIntExtra(MainActivity.EXTRA_CARDS_STACK_SIZE, 10);

        setToolbar();
        setOtherViews();
    }

    protected void setToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.activitySettingsToolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveChanges();
                goBack();
            }
        });
    }

    protected void saveChanges() {
        AppPreferences.setNotifySendingIntervalMinutes(notifySendingIntervalMinutes);
        AppPreferences.setCardsMixMethod(cardsMixMethod);
        AppPreferences.setCardsStackSize(cardsStackSize);
        AppPreferences.setDarkThemeEnabled(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES);
    }

    protected void goBack() {
        Intent intent = new Intent();
        intent.putExtra(MainActivity.EXTRA_NOTIFY_SENDING_INTERVAL_MINUTES, notifySendingIntervalMinutes);
        intent.putExtra(MainActivity.EXTRA_CARDS_MIX_METHOD, cardsMixMethod);
        intent.putExtra(MainActivity.EXTRA_CARDS_STACK_SIZE, cardsStackSize);
        setResult(RESULT_CODE_OK, intent);
        finish();
    }

    protected void setOtherViews() {
        MaterialButtonToggleGroup mixGroup = findViewById(R.id.activitySettingsToggleGroupMix);
        if (cardsMixMethod == MainActivity.CARDS_MIX_METHOD_ORDER) {
            mixGroup.check(R.id.activitySettingsToggleButtonOrder);
        } else if (cardsMixMethod == MainActivity.CARDS_MIX_METHOD_RANDOM) {
            mixGroup.check(R.id.activitySettingsToggleButtonRandom);
        } else if (cardsMixMethod == MainActivity.CARDS_MIX_METHOD_SMART) {
            mixGroup.check(R.id.activitySettingsToggleButtonSmart);
        }
        mixGroup.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
        @Override
        public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                if (!isChecked) return;
                if (checkedId == R.id.activitySettingsToggleButtonOrder) {
                    cardsMixMethod = MainActivity.CARDS_MIX_METHOD_ORDER;
                } else if (checkedId == R.id.activitySettingsToggleButtonRandom) {
                    cardsMixMethod = MainActivity.CARDS_MIX_METHOD_RANDOM;
                } else if (checkedId == R.id.activitySettingsToggleButtonSmart) {
                    cardsMixMethod = MainActivity.CARDS_MIX_METHOD_SMART;
                }
            }
        });

        MaterialButtonToggleGroup themeGroup = findViewById(R.id.activitySettingsToggleGroupTheme);
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            themeGroup.check(R.id.activitySettingsToggleButtonDark);
        } else {
            themeGroup.check(R.id.activitySettingsToggleButtonLight);
        }
        themeGroup.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                if (!isChecked) return;
                if (checkedId == R.id.activitySettingsToggleButtonDark) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    setTheme(R.style.DarkTheme);
                } else if (checkedId == R.id.activitySettingsToggleButtonLight) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    setTheme(R.style.LightTheme);
                }
            }
        });

        SeekBar seekBarStackSize = findViewById(R.id.activitySettingsSeekBarStackSize);
        seekBarStackSize.setProgress(cardsStackSize - 10);
        final TextView textViewStackSize = findViewById(R.id.activitySettingsTextViewStackSize);
        textViewStackSize.setText(String.valueOf(cardsStackSize));
        seekBarStackSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textViewStackSize.setText(String.valueOf(progress + 10));
                cardsStackSize = progress + 10;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        SeekBar seekBarSendingInterval = findViewById(R.id.activitySettingsSeekBarSendingInterval);
        seekBarSendingInterval.setProgress(notifySendingIntervalMinutes - 1);
        final TextView textViewSendingInterval = findViewById(R.id.activitySettingsTextViewSendingInterval);
        textViewSendingInterval.setText(String.valueOf(notifySendingIntervalMinutes));
        seekBarSendingInterval.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textViewSendingInterval.setText(String.valueOf(progress + 1));
                notifySendingIntervalMinutes = progress + 1;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
}