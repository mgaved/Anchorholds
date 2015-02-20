package uk.ac.open.salsabeacons;

import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.app.Notification;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.Vibrator;
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
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by rmg29 on 22/01/2015.
 */
public class BeaconReferenceApplication extends Application implements BootstrapNotifier, RangeNotifier {
  private static final String TAG = "BeaconReferenceApplication";
  private static final String SALSABEACONSID = "46A88354-06AC-4743-A762-901C0717596E";
  private static Context mContext;
  //public final static String SALSA_BEACON_ID = "uk.ac.open.salsabeacons.SALSA_BEACON_ID";
  private Typeface fontAwesome;
  private RegionBootstrap regionBootstrap;
  private BackgroundPowerSaver backgroundPowerSaver;
  private MonitoringActivity monitoringActivity = null;
  private BeaconManager beaconManager = null;
  private ArrayAdapter beaconAdapter = null;
  private List<SalsaBeacon> currentBeacons = new ArrayList<SalsaBeacon>();
  private Hashtable<SalsaBeacon, Long> mBeaconBuffer = new Hashtable<SalsaBeacon, Long>();
  private ArrayList<SalsaBeacon> mBeaconScreenOffBuffer = new ArrayList<SalsaBeacon>();
  private LifecycleHandler mLifecycleHandler = null;

  public void onCreate() {
    super.onCreate();
    mContext = this;
    fontAwesome = Typeface.createFromAsset(getAssets(), "font-awesome-4.3.0/fonts/fontawesome-webfont.ttf");
    mLifecycleHandler = new LifecycleHandler();
    registerActivityLifecycleCallbacks(mLifecycleHandler);
    beaconManager = org.altbeacon.beacon.BeaconManager.getInstanceForApplication(this);
    beaconManager.setForegroundScanPeriod(1800);
    beaconManager.setForegroundBetweenScanPeriod(300);
    beaconManager.setBackgroundScanPeriod(2200);
    beaconManager.setBackgroundBetweenScanPeriod(10000);
    beaconAdapter = new BeaconArrayAdapter<SalsaBeacon>(this, R.layout.beacon_list);
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
  }

  public Typeface getIconFont() {
    return fontAwesome;
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
  }

  @Override
  public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
    Log.d(TAG, "Amount of Beacons in region on this scan: " + beacons.size());
    Long now = System.currentTimeMillis();
    int defaultExpires = calculateDefaultExpiresLength();
    Boolean added = false;
    Boolean removed = false;
    if(beacons.size() > 0) {
      for (Beacon beacon : beacons) {
        Long expires = now + calculateExpires(beacon);
        SalsaBeacon sb = SalsaBeacon.getInstance(beacon.getId2().toInt(), beacon.getId3().toInt(), beacon.getDistance());
        if(sb != null) {
          boolean thisAdded = addToBuffer(sb, expires);
          added = added || thisAdded;
        }
      }
    }

    Iterator bufferItr = mBeaconBuffer.entrySet().iterator();

    while(bufferItr.hasNext()) {
      Map.Entry entry = (Map.Entry) bufferItr.next();
      Long expires = (Long) entry.getValue();
      if(now > expires) {
        bufferItr.remove();
        removed = true;
      }
    }

    PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
    if(mLifecycleHandler.isApplicationVisible() && (added || removed)) {
      Log.d(TAG, "added: " + added + " removed: " + removed);
      new UpdateListTask().execute(mBeaconBuffer.keySet());
    } else if(!pm.isScreenOn() && added) {
      Log.d(TAG, "App NOT visible and Beacons in range");
      sendNotification();
      Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
      v.vibrate(500); // Vibrate for 500 milliseconds
    }
    Log.d(TAG, "Ranging for region: " + region);
  }

  private Long calculateExpires(Beacon beacon) {
    Long expireTime = new Long(0);
    double distance = beacon.getDistance();
    if(distance > 10) {
      expireTime = expireTime - 1000;
    }
    else if (distance > 1) {
      expireTime = expireTime + 3000;
    } else {
      expireTime = expireTime + 10000;
    }
    return expireTime;
  }

  private boolean addToBuffer(SalsaBeacon sb, Long expiresStamp) {
    boolean newBeacon = false;
    PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
    if(!pm.isScreenOn()) {
      Log.d(TAG, "screen not on");
      if(!mBeaconScreenOffBuffer.contains(sb)) {
        mBeaconScreenOffBuffer.add(sb);
        newBeacon = true;
      }
    } else {
      if(!mBeaconBuffer.containsKey(sb)) {
        Log.d(TAG, "Buffer doesn't contain Key"+sb.toString());
        newBeacon = true;
      }
      mBeaconBuffer.put(sb, expiresStamp);
    }

    return newBeacon;
  }

  public void clearBuffer() {
    mBeaconScreenOffBuffer.clear();
    mBeaconBuffer.clear();
  }

  public void populateScreenOffBuffer() {
    Set<SalsaBeacon> values = mBeaconBuffer.keySet();
    Iterator i = values.iterator();
    while(i.hasNext()) {
      mBeaconScreenOffBuffer.add((SalsaBeacon) i.next());
    }
    mBeaconBuffer.clear();
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

  private int calculateDefaultExpiresLength() {
    PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
    if(!pm.isScreenOn()) {
      return 300000;
    }
    return 10000;
  }

  private void sendNotification() {
    Notification.Builder builder =
        new Notification.Builder(this)
            .setContentTitle(getResources().getString(R.string.app_name))
            .setContentText(getResources().getString(R.string.notification))
            .setSmallIcon(R.drawable.ic_white_salsa)
            .setAutoCancel(true);

    TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
    Intent intent = new Intent(this, MonitoringActivity.class);
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

  public void cancelNotification() {
    NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
    notificationManager.cancel(1);
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

  private class UpdateListTask extends AsyncTask<Set<SalsaBeacon>, Void, Set<SalsaBeacon>> {
    protected Set<SalsaBeacon> doInBackground(Set<SalsaBeacon>... beacons) {
      return beacons[0];
    }

    /*protected void onProgressUpdate(Integer... progress) {
      setProgressPercent(progress[0]);
    }*/

    protected void onPostExecute(Set<SalsaBeacon> beacons) {
      beaconAdapter.clear();
      Log.d(TAG, "UPDATING BEACON LIST. Number of Beacons: " + beacons.size());
      Iterator itr = beacons.iterator();
      while(itr.hasNext()) {
        SalsaBeacon beacon = (SalsaBeacon) itr.next();
        beaconAdapter.add(beacon);
      }
      beaconAdapter.notifyDataSetChanged();
    }
  }

}