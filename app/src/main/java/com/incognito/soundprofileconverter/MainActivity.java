package com.incognito.soundprofileconverter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.NotificationManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Logger;
import com.incognito.soundprofileconverter.adapter.RecyclerViewAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int
            ON_DO_NOT_DISTURB_CALLBACK_CODE = 1000,
            RESULT_PICK_CONTACT = 1001,
            ON_DEVICE_ADMIN_CALLBACK_CODE = 1002,
            ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 1003;

    private final static int REQUEST_CODE_ASK_PERMISSIONS = 1;
    private static final String[] REQUIRED_SDK_PERMISSIONS = new String[]{
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private RecyclerView recyclerView;
    private RecyclerViewAdapter recyclerViewAdapter;
    private ArrayList<WhitelistedContacts> whitelistedContactsArrayList = new ArrayList<>();

    private FloatingActionButton btnAddContact;

    private Switch ringerSwitch;
    private Switch sosSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        checkPermissions();

        recyclerView = findViewById(R.id.whitelistedContactsView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        btnAddContact = findViewById(R.id.addContact);
        btnAddContact.setOnClickListener(arg0 -> {
            Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
            startActivityForResult(contactPickerIntent, RESULT_PICK_CONTACT);
        });

        SharedPreferences sharedPrefs = getSharedPreferences("com.incognito.soundprofileconverter", MODE_PRIVATE);

        ringerSwitch = findViewById(R.id.ringerSwitch);
        ringerSwitch.setChecked(sharedPrefs.getBoolean("ringerSwitch", true));
        ringerSwitch.setOnClickListener(arg0 -> {
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putBoolean("ringerSwitch", ringerSwitch.isChecked());
            editor.apply();
        });

        sosSwitch = findViewById(R.id.sosSwitch);
        sosSwitch.setChecked(sharedPrefs.getBoolean("sosSwitch", true));
        sosSwitch.setOnClickListener(arg0 -> {
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putBoolean("sosSwitch", sosSwitch.isChecked());
            editor.apply();
        });

        final DBHandler dbHandler = new DBHandler(this);
        List<WhitelistedContacts> whitelistedContacts = dbHandler.getContacts();

        whitelistedContactsArrayList.addAll(whitelistedContacts);

        recyclerViewAdapter = new RecyclerViewAdapter(MainActivity.this, whitelistedContactsArrayList);
        recyclerView.setAdapter(recyclerViewAdapter);
    }

    protected void checkPermissions() {
        final List<String> missingPermissions = new ArrayList<>();
        // check all required dynamic permissions
        for (final String permission : REQUIRED_SDK_PERMISSIONS) {
            final int result = ContextCompat.checkSelfPermission(this, permission);
            if (result != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }
        if (!missingPermissions.isEmpty()) {
            // request all missing permissions
            final String[] permissions = missingPermissions
                    .toArray(new String[missingPermissions.size()]);
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_ASK_PERMISSIONS);
        } else {
            final int[] grantResults = new int[REQUIRED_SDK_PERMISSIONS.length];
            Arrays.fill(grantResults, PackageManager.PERMISSION_GRANTED);
            onRequestPermissionsResult(REQUEST_CODE_ASK_PERMISSIONS, REQUIRED_SDK_PERMISSIONS,
                    grantResults);
        }

        requestDNDPermission();
        requestDeviceAdminPermission();
        requestDrawOverOtherApps();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                for (int index = permissions.length - 1; index >= 0; --index) {
                    if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                        // exit the app if one permission is not granted
                        Toast.makeText(this,
                                "SMS Permission is required for this app to function",
                                Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }
                }
                break;
        }
    }

    private void requestDNDPermission() {
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if ( notificationManager.isNotificationPolicyAccessGranted()) {
            AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
            audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
        } else{
            // Ask the user to grant access
            Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            startActivityForResult(intent, MainActivity.ON_DO_NOT_DISTURB_CALLBACK_CODE );
        }
    }

    private void requestDeviceAdminPermission() {
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        ComponentName compName = new ComponentName(this, LockScreenActivity.class);
        Log.i("DA", String.valueOf(devicePolicyManager.isAdminActive(compName)));

        if (!devicePolicyManager.isAdminActive(compName)) {
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Additional text explaining why we need this permission");
            startActivityForResult(intent, ON_DEVICE_ADMIN_CALLBACK_CODE);
        }
    }

    private void requestDrawOverOtherApps() {
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case MainActivity.ON_DO_NOT_DISTURB_CALLBACK_CODE:
                this.requestDNDPermission();
                break;
            case RESULT_PICK_CONTACT:
                Cursor cursor = null;
                try {
                    Uri uri = data.getData();
                    cursor = getContentResolver().query(uri, null, null, null, null);
                    cursor.moveToFirst();
                    int phoneIndex = cursor.getColumnIndex(
                            ContactsContract.CommonDataKinds.Phone.NUMBER);
                    int nameIndex = cursor.getColumnIndex(
                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                    recyclerViewAdapter.addContact(new WhitelistedContacts(cursor.getString(nameIndex), cursor.getString(phoneIndex).replaceAll("\\s", "")));
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    assert cursor != null;
                    cursor.close();
                }
                break;
        }
    }
}