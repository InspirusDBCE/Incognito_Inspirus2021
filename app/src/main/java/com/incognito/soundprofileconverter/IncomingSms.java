package com.incognito.soundprofileconverter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import java.util.ArrayList;

public class IncomingSms extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        // Commands
        final String SET_RINGER = "SET RINGER";

        final Bundle bundle = intent.getExtras();

        final DBHandler dbHandler = new DBHandler(context);
         ArrayList<WhitelistedContacts> whitelistedContacts = dbHandler.getContacts();

         /// To delete
        if (whitelistedContacts.size() == 0) {
            dbHandler.addNewContact("Saish Naik", "+919527767505");
            dbHandler.addNewContact("Rajdick Kerkar", "+919822975781");
            whitelistedContacts = dbHandler.getContacts();
        }

        try {

            if (bundle != null) {

                final Object[] pdusObj = (Object[]) bundle.get("pdus");

                for (int i = 0; i < pdusObj.length; i++) {

                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);

                    String senderNum = currentMessage.getDisplayOriginatingAddress();
                    String message = currentMessage.getDisplayMessageBody();
                    boolean isWhitelisted = false;

                    Log.i("SmsReceiver", "senderNum: "+ senderNum + "; message: " + message);

                    if (!message.equals(SET_RINGER))
                        return;

                    // Check if contact is whitelisted
                    for (WhitelistedContacts contact: whitelistedContacts) {
                        if (contact.getContactPhoneNumber().equals(senderNum)) {
                            isWhitelisted = true;
                            break;
                        }
                    }

                    if (!isWhitelisted)
                        return;

                    if (message.equals(SET_RINGER)) {
                        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                        Log.i("SmsReceiver", "Original RingerMode" + audioManager.getRingerMode());
                        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
                        audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                        Log.i("SmsReceiver", "Now RingerMode" + audioManager.getRingerMode());
                        audioManager.setStreamVolume(AudioManager.STREAM_RING, maxVolume, AudioManager.FLAG_SHOW_UI + AudioManager.FLAG_PLAY_SOUND);
                    }
                }
            }
        } catch (Exception e) {
            Log.e("SmsReceiver", "Exception smsReceiver" +e);
        }
    }
}