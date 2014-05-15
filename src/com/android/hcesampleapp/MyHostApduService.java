package com.android.hcesampleapp;

import android.app.Service;
import android.content.Intent;
import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class MyHostApduService extends HostApduService {

	private final String TAG = getClass().getName();
	private static final String CARD_INFO = "com.android.hcesampleapp.CARD_INFO";
	private String data = null;
	private static byte[] AID={
        (byte)0x00
      , (byte)0xA4
      , (byte)0x04
      , (byte)0x00
      , (byte)0x07
      , (byte)0xF0, (byte)0x39, (byte)0x41, (byte)0x48, (byte)0x14, (byte)0x81, (byte)0x00
      , (byte)0x00
};
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
	}



	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		//super.onStartCommand(intent, flags, startId);
		
		data = intent.getStringExtra(CARD_INFO);
		Log.d(TAG,"onStartCommand : " + data);
		
		return Service.START_STICKY;
	}



	@Override
	public byte[] processCommandApdu(byte[] apdu, Bundle extras) {
		
		Log.d(TAG,"processCommandApdu()");
		
		byte CMD = apdu[0];
		byte[] response = null;
		
		if (CMD == 0x00) {
			
			response = data.getBytes();			
		}
		
		Log.d(TAG,"HCE processCommandApdu received APDU data: " + response);
		return response;
	}

	
	
	@Override
	public void onDeactivated(int reason) {
		/*
		 * Android will keep forwarding new APDUs from the reader to your
		 * service, until either:
		 * 
		 * 1. The NFC reader sends another "SELECT AID" APDU, which the OS resolves
		 * to a different service; 
		 * 2. The NFC link between the NFC reader and your
		 * device is broken.
		 */
		
		Log.d(TAG,"HCE Deactivated with reason: " + reason);
		Toast.makeText(this, "Transaction Complete", Toast.LENGTH_LONG).show();
	}

}
