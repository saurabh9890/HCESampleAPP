package com.android.hcesampleapp;

import io.card.payment.CardIOActivity;
import io.card.payment.CreditCard;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

	// You MUST register with card.io to get an app token. Go to
	// https://card.io/apps/new/
	private static final String MY_CARDIO_APP_TOKEN = "e2605699bfc24555adc71d2ab1e28979"; // My Token number
	private static final String CARD_INFO = "com.android.hcesampleapp.CARD_INFO";
	private static int MY_SCAN_REQUEST_CODE = 100; // arbitrary int
	
	private final String TAG = getClass().getName();

	private Button scanButton;
	private TextView resultTextView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		resultTextView = (TextView) findViewById(R.id.resultTextView);
		scanButton = (Button) findViewById(R.id.scanButton);

		resultTextView.setText("card.io library version: "
				+ CardIOActivity.sdkVersion() + "\nBuilt: "
				+ CardIOActivity.sdkBuildDate());
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (CardIOActivity.canReadCardWithCamera(this)) {
			scanButton.setText("Scan a credit card with card.io");
		} else {
			scanButton.setText("Enter credit card information");
		}
	}

	public void onScanPress(View v) {
		// This method is set up as an onClick handler in the layout xml
		// e.g. android:onClick="onScanPress"

		Intent scanIntent = new Intent(this, CardIOActivity.class);

		// required for authentication with card.io
		scanIntent.putExtra(CardIOActivity.EXTRA_APP_TOKEN, MY_CARDIO_APP_TOKEN);

		// customize these values to suit your needs.
		scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, true); // default:
																		// true
		scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_CVV, false); // default:
																		// false
		scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_ZIP, false); // default:
																		// false

		// hides the manual entry button
		// if set, developers should provide their own manual entry mechanism in
		// the app
		scanIntent.putExtra(CardIOActivity.EXTRA_SUPPRESS_MANUAL_ENTRY, false); // default:
																				// false
		// MY_SCAN_REQUEST_CODE is arbitrary and is only used within this
		// activity.
		startActivityForResult(scanIntent, MY_SCAN_REQUEST_CODE);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == MY_SCAN_REQUEST_CODE) {
			String resultStr;
			if (data != null && data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
				CreditCard scanResult = data
						.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT);

				// Never log a raw card number. Avoid displaying it, but if
				// necessary use getFormattedCardNumber()
				resultStr = "Card Number: "
						+ scanResult.getRedactedCardNumber() + "\n";
				Log.d(TAG, "Card Number: " + scanResult.getFormattedCardNumber());
				
				
				if (scanResult.isExpiryValid()) {
					resultStr += "Expiration Date: " + scanResult.expiryMonth
							+ "/" + scanResult.expiryYear + "\n";
				}

				if (scanResult.cvv != null) {
					// Never log or display a CVV
					resultStr += "CVV has " + scanResult.cvv.length()
							+ " digits.\n";
				}

				if (scanResult.postalCode != null) {
					resultStr += "postalCode : " + scanResult.postalCode + "\n";
				}
			} else {
				resultStr = "Scan was canceled.";
			}

			// do something with resultDisplayStr, maybe display it in a textView
			resultTextView.setText(resultStr);
			
			
			// start HCE service with Card Info.
			Intent hceService = new Intent(this, MyHostApduService.class);
			Bundle extra = new Bundle();
			extra.putString(CARD_INFO, resultStr);
			hceService.putExtras(extra);
			startService(hceService);
		}
	}

	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
