package uk.ac.open.salsabeacons;

import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.app.Notification;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.util.Log;
import android.widget.ArrayAdapter;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.startup.RegionBootstrap;
import org.altbeacon.beacon.startup.BootstrapNotifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by dyoung on 12/13/13.
 */
public class BeaconReferenceApplication extends Application implements BootstrapNotifier, RangeNotifier {
  private static final String TAG = "SALSA APP";
  private static final String SALSABEACONSID = "46A88354-06AC-4743-A762-901C0717596E";
  private static Context mContext;
  public final static String SALSA_BEACON_ID = "uk.ac.open.salsabeacons.SALSA_BEACON_ID";
  private RegionBootstrap regionBootstrap;
  private BackgroundPowerSaver backgroundPowerSaver;
  private boolean haveDetectedBeaconsSinceBoot = false;
  private MonitoringActivity monitoringActivity = null;
  private BeaconManager beaconManager = null;
  private ArrayAdapter beaconAdapter = null;
  private List<SalsaBeacon> currentBeacons = new ArrayList<SalsaBeacon>();//new HashMap();
  private LifecycleHandler mLifecycleHandler = null;

  public void onCreate() {
    super.onCreate();
    mContext = this;
    mLifecycleHandler = new LifecycleHandler();
    registerActivityLifecycleCallbacks(mLifecycleHandler);
    beaconManager = org.altbeacon.beacon.BeaconManager.getInstanceForApplication(this);
    beaconManager.setForegroundScanPeriod(2000);
    beaconAdapter = new ArrayAdapter<SalsaBeacon>(this, android.R.layout.simple_expandable_list_item_1);
    // By default the AndroidBeaconLibrary will only find AltBeacons.  If you wish to make it
    // find a different type of beacon, you must specify the byte layout for that beacon's
    // advertisement with a line like below.  The example shows how to find a beacon with the
    // same byte layout as AltBeacon but with a beaconTypeCode of 0xaabb.  To find the proper
    // layout expression for other beacon types, do a web search for "setBeaconLayout"
    // including the quotes.
    //
    // beaconManager.getBeaconParsers().add(new BeaconParser().
    //        setBeaconLayout("m:2-3=aabb,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
    //

    beaconManager.getBeaconParsers().add(new BeaconParser().
        setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));

    Log.d(TAG, "setting up background monitoring for beacons and power saving");
    // wake up the app when a beacon is seen

    Region region = new Region("salsaRegion", Identifier.parse(this.SALSABEACONSID), null, null);
    regionBootstrap = new RegionBootstrap(this, region);
    BeaconManager.setDebug(true);

    // simply constructing this class and holding a reference to it in your custom Application
    // class will automatically cause the BeaconLibrary to save battery whenever the application
    // is not visible.  This reduces bluetooth power usage by about 60%
    backgroundPowerSaver = new BackgroundPowerSaver(this);

    // If you wish to test beacon detection in the Android Emulator, you can use code like this:
    // BeaconManager.setBeaconSimulator(new TimedBeaconSimulator() );
    // ((TimedBeaconSimulator) BeaconManager.getBeaconSimulator()).createTimedSimulatedBeacons();
  }

  public static Context getContext(){
    return mContext;
  }

  @Override
  public void didEnterRegion(Region region) {
    Log.d(TAG, "ENTERED a Region: " + region);
    beaconManager.setRangeNotifier(this);

    try {
      beaconManager.startRangingBeaconsInRegion(region);
    }
    catch (RemoteException e) {

    }


    // In this example, this class sends a notification to the user whenever a Beacon
    // matching a Region (defined above) are first seen.
    //Log.d(TAG, "did enter region.");
    //if (!haveDetectedBeaconsSinceBoot) {
      //Log.d(TAG, "auto launching MainActivity");

      // The very first time since boot that we detect an beacon, we launch the
      // MainActivity
      //Intent intent = new Intent(this, MonitoringActivity.class);
      //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      // Important:  make sure to add android:launchMode="singleInstance" in the manifest
      // to keep multiple copies of this activity from getting created if the user has
      // already manually launched the app.
      //this.startActivity(intent);
      //haveDetectedBeaconsSinceBoot = true;
    //} else {
      //if (monitoringActivity != null) {
        // If the Monitoring Activity is visible, we log info about the beacons we have
        // seen on its display
        //monitoringActivity.logToDisplay("I see a beacon again" );
      //} else {
        // If we have already seen beacons before, but the monitoring activity is not in
        // the foreground, we send a notification to the user on subsequent detections.
        //Log.d(TAG, "Sending notification.");
        //sendNotification();
      //}
    //}


  }

  @Override
  public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
    Log.d(TAG, "Amount of Beacons in region on this scan: " + beacons.size());
    if (beacons.size() > 0) {
      currentBeacons.clear();
      //Object[] tmp = beacons.toArray();
      Iterator<Beacon> itr = beacons.iterator();
      while(itr.hasNext()) {
        Beacon beacon = itr.next();
        currentBeacons.add((SalsaBeacon) new SalsaBeacon(beacon.getId2().toInt(), beacon.getId3().toInt()));
      }
      //currentBeacons = Arrays. copyOf(tmp, tmp.length, Beacon[].class);
      /*Beacon foundBeacon = beacons.iterator().next();
      for (Beacon beacon : currentBeacons) {
        if (beacon.getDistance() < 1)
          currentBeacons.put(beacon, new Date());
      }*/
      //if(!currentBeacons.containsKey(foundBeacon)) {
        //currentBeacons.put(foundBeacon, new Date());
        //if(foundBeacon.getDistance() < 1) {
          if(mLifecycleHandler.isApplicationVisible()) {
            Log.d(TAG, "App visible and Beacons in range");
            new UpdateListTask().execute(currentBeacons);
          } else {
            Log.d(TAG, "App NOT visible and Beacons in range");
            sendNotification(currentBeacons.get(0));
          }
        //}
      //}
    }
    Log.d(TAG, "Ranging for region: " + region);




      //EditText editText = (EditText)RangingActivity.this
          //.findViewById(R.id.rangingText);

      //Beacon firstBeacon = beacons.iterator().next();
      //if(firstBeacon.getDistance() < 1) {
      //  sendNotification(firstBeacon);
      //}

      //logToDisplay("The first beacon "+firstBeacon.toString()+" is about "+firstBeacon.getDistance()+" meters away.");
    //}
  }

  @Override
  public void didExitRegion(Region region) {
    Log.d(TAG, "EXITED a Region");
    /*if (monitoringActivity != null) {
      monitoringActivity.logToDisplay("I no longer see a beacon.");
    }*/
  }

  @Override
  public void didDetermineStateForRegion(int state, Region region) {
    Log.d(TAG, "did Determine State For Region");
    /*if (monitoringActivity != null) {
      monitoringActivity.logToDisplay("I have just switched from seeing/not seeing beacons: " + state);
    }*/
  }

  private void sendNotification(SalsaBeacon beacon) {
    Notification.Builder builder =
        new Notification.Builder(this)
            .setContentTitle("Beacon Reference Application")
            .setContentText("An beacon is nearby.")
            .setSmallIcon(R.drawable.ic_launcher)
            .setAutoCancel(true);

    TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
    Intent intent = new Intent(this, BeaconInfoActivity.class);
    intent.putExtra(SALSA_BEACON_ID, beacon.getId());
    stackBuilder.addParentStack(BeaconInfoActivity.class);
    stackBuilder.addNextIntent(intent);
    PendingIntent resultPendingIntent =
        stackBuilder.getPendingIntent(
            0,
            PendingIntent.FLAG_UPDATE_CURRENT
        );
    builder.setContentIntent(resultPendingIntent);
    NotificationManager notificationManager =
        (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
    notificationManager.notify(1, builder.build());
  }

  public ArrayAdapter getBeaconAdapter() {
    return beaconAdapter;
  }

  public List<SalsaBeacon> getCurrentBeacons() {
    return currentBeacons;
  }

  public void setMonitoringActivity(MonitoringActivity activity) {
    this.monitoringActivity = activity;
  }

  private class UpdateListTask extends AsyncTask<List<SalsaBeacon>, Void,List<SalsaBeacon>> {
    protected List<SalsaBeacon> doInBackground(List<SalsaBeacon>... beacons) {
      return beacons[0];
    }

    /*protected void onProgressUpdate(Integer... progress) {
      setProgressPercent(progress[0]);
    }*/

    protected void onPostExecute(List<SalsaBeacon> beacons) {
      beaconAdapter.clear();
      Log.d(TAG, "UPDATING BEACON LIST. Number of Beacons: " + beacons.size());
      beaconAdapter.addAll(beacons);
      beaconAdapter.notifyDataSetChanged();
    }
  }

}