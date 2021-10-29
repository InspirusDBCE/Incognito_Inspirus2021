package com.incognito.soundprofileconverter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.incognito.soundprofileconverter.adapter.RecyclerViewAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int
            ON_DO_NOT_DISTURB_CALLBACK_CODE = 1000,
            RESULT_PICK_CONTACT = 1001;

    private final static int REQUEST_CODE_ASK_PERMISSIONS = 1;
    private static final String[] REQUIRED_SDK_PERMISSIONS = new String[] {
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS
    };

//    private WhitelistedContactsAdapter adapter;
//    private RecyclerView whitelistedContactsView;
//    ArrayList<WhitelistedContacts> whitelistedContacts;

    private RecyclerView recyclerView;
    private RecyclerViewAdapter recyclerViewAdapter;
    private ArrayList<WhitelistedContacts> whitelistedContactsArrayList = new ArrayList<>();
    private ArrayAdapter<String> arrayAdapter;

    private FloatingActionButton btnAddContact;

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
        btnAddContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                startActivityForResult(contactPickerIntent, RESULT_PICK_CONTACT);
            }
        });

        final DBHandler dbHandler = new DBHandler(this);
        List<WhitelistedContacts> whitelistedContacts = dbHandler.getContacts();

        for (WhitelistedContacts contact: whitelistedContacts) {
            whitelistedContactsArrayList.add(contact);
        }

        recyclerViewAdapter = new RecyclerViewAdapter(MainActivity.this, whitelistedContactsArrayList);
        recyclerView.setAdapter(recyclerViewAdapter);


//        adapter = new WhitelistedContactsAdapter(whitelistedContacts, this);
//        whitelistedContactsView = findViewById(R.id.whitelistedContactsView);
//        whitelistedContactsView.setAdapter(adapter);
//        whitelistedContactsView.setLayoutManager(new LinearLayoutManager(this));
////        whitelistedContactsView.setHasFixedSize(true);
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
                // all permissions were granted
                //initialize();
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
                    recyclerViewAdapter.addContact(new WhitelistedContacts(cursor.getString(nameIndex), cursor.getString(phoneIndex)));
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