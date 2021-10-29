package com.incognito.soundprofileconverter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

import java.util.ArrayList;

public class IncomingSms extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        // Commands
        final String SET_RINGER = "SET RINGER";
        final String LOCK_DEVICE = "LOCK";
        final String GET_LOCATION = "GET LOCATION";

        final Bundle bundle = intent.getExtras();

        final DBHandler dbHandler = new DBHandler(context);
         ArrayList<WhitelistedContacts> whitelistedContacts = dbHandler.getContacts();

        try {

            if (bundle != null) {

                final Object[] pdusObj = (Object[]) bundle.get("pdus");

                for (int i = 0; i < pdusObj.length; i++) {

                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);

                    String senderNum = currentMessage.getDisplayOriginatingAddress();
                    String message = currentMessage.getDisplayMessageBody();
                    boolean isWhitelisted = false;

                    Log.i("SmsReceiver", "senderNum: "+ senderNum + "; message: " + message);

//                    if (!message.equals(SET_RINGER))
//                        return;

                    // Check if contact is whitelisted
                    for (WhitelistedContacts contact: whitelistedContacts) {
                        if (contact.getContactPhoneNumber().equals(senderNum)) {
                            isWhitelisted = true;
                            break;
                        }
                    }

                    if (!isWhitelisted)
                        return;

                    SharedPreferences sharedPrefs = context.getSharedPreferences("com.incognito.soundprofileconverter", Context.MODE_PRIVATE);

                    if (message.startsWith(SET_RINGER) && sharedPrefs.getBoolean("ringerSwitch", true)) {
                        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                        Log.i("SmsReceiver", "Original RingerMode" + audioManager.getRingerMode());
                        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
                        audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                        Log.i("SmsReceiver", "Now RingerMode" + audioManager.getRingerMode());
                        audioManager.setStreamVolume(AudioManager.STREAM_RING, maxVolume, AudioManager.FLAG_SHOW_UI + AudioManager.FLAG_PLAY_SOUND);
                    } else if (message.startsWith(LOCK_DEVICE) && sharedPrefs.getBoolean("sosSwitch", true)) {
                        Intent myIntent = new Intent(context, LockScreenActivity.class);
                        myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        String messageText;
                        try {
                            messageText = message.substring(5);
                        } catch (Exception e) {
                            messageText = "Device is locked by owner";
                        }

                        myIntent.putExtra("messageText", messageText);
                        context.startActivity(myIntent);
                    } else if (message.startsWith(GET_LOCATION) && sharedPrefs.getBoolean("sosSwitch", true)) {
                        context.startService(new Intent(context, LocationHandler.class));
                        try {
                            SmsManager smsManager = SmsManager.getDefault();
                            String text = "Track Live Location on \n https://soundprofileconverter.web.app/";
                            smsManager.sendTextMessage(senderNum, null, text, null, null);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e("SmsReceiver", "Exception smsReceiver" +e);
        }
    }
}