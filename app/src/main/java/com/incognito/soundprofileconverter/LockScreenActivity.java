package com.incognito.soundprofileconverter;

import androidx.appcompat.app.AppCompatActivity;

import android.app.KeyguardManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class LockScreenActivity extends AppCompatActivity {

    private DevicePolicyManager devicePolicyManager;
    private ComponentName compName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_screen);

        getSupportActionBar().hide();

        TextView messageTextView = findViewById(R.id.lockScreenText);
        Button unlockBtn = findViewById(R.id.unlockBtn);
        unlockBtn.setOnClickListener(arg0 -> {
            finish();
        });

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String messageText = extras.getString("messageText");
            if (messageText.length() > 0)
                messageTextView.setText(messageText);
            else
                messageTextView.setText("Device is locked by owner");
        }

        devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        compName = new ComponentName(this, LockScreenActivity.class);

        setShowWhenLocked(true);
        setTurnScreenOn(true);
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        if(keyguardManager!=null)
            keyguardManager.requestDismissKeyguard(this, null);

        boolean active = devicePolicyManager.isAdminActive(compName);

        if (active) {
            devicePolicyManager.lockNow();
        } else {
            Toast.makeText(this, "You need to enable the Admin Device Features", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed(){
        moveTaskToBack(false);
    }
}