package uk.ac.open.salsabeacons;

import android.app.ListActivity;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;

/**
 *
 * @author dyoung
 * @author Matt Tyler
 */
public class MonitoringActivity extends ListActivity {
  protected static final String TAG = "MonitoringActivity";
  private BeaconManager beaconManager;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    Log.d(TAG, "onCreate");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_monitoring);
    //ListView listView = (ListView) findViewById(R.id.listView);
    setListAdapter(((BeaconReferenceApplication) this.getApplicationContext()).getBeaconAdapter());
    verifyBluetooth();
    //logToDisplay("Application just launched" );
  }

  /*public void onRangingClicked(View view) {
    Intent myIntent = new Intent(this, RangingActivity.class);
    this.startActivity(myIntent);
  }*/

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
    if(beacon.isUri()) {
      Intent viewIntent = new Intent("android.intent.action.VIEW", beacon.getUri());
      startActivity(viewIntent);
    } else {
      Intent intent = new Intent(this, BeaconInfoActivity.class);
      intent.putExtra(BeaconReferenceApplication.SALSA_BEACON_ID, beacon.getId());
      startActivity(intent);
    }

    //CharSequence text = v.toString();
    //int duration = Toast.LENGTH_LONG;

    //Toast.makeText(getApplicationContext(), text, duration).show();
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

  /*public void logToDisplay(final String line) {
    runOnUiThread(new Runnable() {
      public void run() {
        EditText editText = (EditText)MonitoringActivity.this
            .findViewById(R.id.monitoringText);
        editText.append(line+"\n");
      }
    });
  }*/

}