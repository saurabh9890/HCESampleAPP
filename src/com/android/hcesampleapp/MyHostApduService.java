package com.android.hcesampleapp;

import java.util.Arrays;

import android.app.Service;
import android.content.Intent;
import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class MyHostApduService extends HostApduService {

	private final String TAG = getClass().getName();
	private static final String CARD_INFO = "com.android.hcesampleapp.CARD_INFO";
	
	// AID for our loyalty card service.
	private static final String AID = "F222222222";
	// ISO-DEP command HEADER for selecting an AID.
	// Format: [Class | Instruction | Parameter 1 | Parameter 2]
	private static final String SELECT_APDU_HEADER = "00A40400";
	// "OK" status word sent in response to SELECT AID command (0x9000)
	private static final byte[] SELECT_OK_SW = HexStringToByteArray("9000");
	// "UNKNOWN" status word sent in response to invalid APDU command (0x0000)
	private static final byte[] UNKNOWN_CMD_SW = HexStringToByteArray("0000");
	private static final byte[] SELECT_APDU = BuildSelectApdu(AID);
	
	private String data = null;


	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		data = intent.getStringExtra(CARD_INFO);
		Log.d(TAG, "onStartCommand : " + data);

		return Service.START_STICKY;
	}

	@Override
	public byte[] processCommandApdu(byte[] apdu, Bundle extras) {

		Log.d(TAG, "Received APDU: " + ByteArrayToHexString(apdu));

		if (Arrays.equals(SELECT_APDU, apdu)) {

			byte[] dataBytes = data.getBytes();
			Log.d(TAG, "Sending card number: " + data);
			return ConcatArrays(dataBytes, SELECT_OK_SW);
		} else {
			return UNKNOWN_CMD_SW;
		}

	}

	@Override
	public void onDeactivated(int reason) {

		Log.d(TAG, "HCE Deactivated with reason: " + reason);
		Toast.makeText(this, "Transaction Complete", Toast.LENGTH_LONG).show();
	}

	
	public static byte[] BuildSelectApdu(String aid) {
		// Format: [CLASS | INSTRUCTION | PARAMETER 1 | PARAMETER 2 | LENGTH |
		// DATA]
		return HexStringToByteArray(SELECT_APDU_HEADER
				+ String.format("%02X", aid.length() / 2) + aid);
	}

	
	
	public static String ByteArrayToHexString(byte[] bytes) {
		final char[] hexArray = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
				'9', 'A', 'B', 'C', 'D', 'E', 'F' };
		char[] hexChars = new char[bytes.length * 2]; // Each byte has two hex
														// characters (nibbles)
		int v;
		for (int j = 0; j < bytes.length; j++) {
			v = bytes[j] & 0xFF; // Cast bytes[j] to int, treating as unsigned
									// value
			hexChars[j * 2] = hexArray[v >>> 4]; // Select hex character from
													// upper nibble
			hexChars[j * 2 + 1] = hexArray[v & 0x0F]; // Select hex character
														// from lower nibble
		}
		return new String(hexChars);
	}

	public static byte[] HexStringToByteArray(String s)
			throws IllegalArgumentException {
		int len = s.length();
		if (len % 2 == 1) {
			throw new IllegalArgumentException(
					"Hex string must have even number of characters");
		}
		byte[] data = new byte[len / 2]; // Allocate 1 byte per 2 hex characters
		for (int i = 0; i < len; i += 2) {
			// Convert each character into a integer (base-16), then bit-shift
			// into place
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character
					.digit(s.charAt(i + 1), 16));
		}
		return data;
	}

	
	public static byte[] ConcatArrays(byte[] first, byte[]... rest) {
		int totalLength = first.length;
		for (byte[] array : rest) {
			totalLength += array.length;
		}
		byte[] result = Arrays.copyOf(first, totalLength);
		int offset = first.length;
		for (byte[] array : rest) {
			System.arraycopy(array, 0, result, offset, array.length);
			offset += array.length;
		}
		return result;
	}

}
