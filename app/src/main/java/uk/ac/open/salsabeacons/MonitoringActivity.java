package uk.ac.open.salsabeacons;

import android.app.ListActivity;
import android.os.Bundle;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import org.altbeacon.beacon.BeaconManager;

/**
 * Created by rmg29 on 22/01/2015.
 */
public class MonitoringActivity extends ListActivity {
  protected static final String TAG = "MonitoringActivity";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    Log.d(TAG, "onCreate");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_monitoring);
    setListAdapter(((BeaconReferenceApplication) this.getApplicationContext()).getBeaconAdapter());
    verifyBluetooth();
  }

  @Override
  public void onResume() {
    super.onResume();
    ((BeaconReferenceApplication) this.getApplicationContext()).setMonitoringActivity(this);
  }

  @Override
  public void onPause() {
    super.onPause();
    ((BeaconReferenceApplication) this.getApplicationContext()).setMonitoringActivity(null);
  }

  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
    SalsaBeacon beacon = (SalsaBeacon) getListView().getItemAtPosition(position);
    if(!beacon.isResource()) {
      Intent viewIntent = new Intent("android.intent.action.VIEW", beacon.getUri());
      startActivity(viewIntent);
    } else {
      Intent intent = new Intent(this, BeaconInfoActivity.class);
      intent.putExtra(BeaconReferenceApplication.SALSA_BEACON_ID, beacon.getId());
      startActivity(intent);
    }
  }

  private void verifyBluetooth() {

    try {
      if (!BeaconManager.getInstanceForApplication(this).checkAvailability()) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Bluetooth not enabled");
        builder.setMessage("Please enable bluetooth in settings and restart this application.");
        builder.setPositiveButton(android.R.string.ok, null);
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
          @Override
          public void onDismiss(DialogInterface dialog) {
            finish();
            System.exit(0);
          }
        });
        builder.show();
      }
    }
    catch (RuntimeException e) {
      final AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setTitle("Bluetooth LE not available");
      builder.setMessage("Sorry, this device does not support Bluetooth LE.");
      builder.setPositiveButton(android.R.string.ok, null);
      builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

        @Override
        public void onDismiss(DialogInterface dialog) {
          finish();
          System.exit(0);
        }

      });
      builder.show();

    }

  }

}