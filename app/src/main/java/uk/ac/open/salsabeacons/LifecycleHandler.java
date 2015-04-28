/*
 * This file is part of Salsa Beacons
 *
 * Salsa Beacons is a Bluetooth LE aware Android app that enables location dependant learning
 * author:  Richard Greenwood <richard.greenwood@open.ac.uk>
 * Copyright (C) 2015 The Open University
 *
 * Salsa Beacons is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * Salsa Beacons is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with Salsa Beacons.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.open.salsabeacons;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by rmg29 on 16/01/2015.
 */
public class LifecycleHandler implements Application.ActivityLifecycleCallbacks {
  // I use four separate variables here. You can, of course, just use two and
  // increment/decrement them instead of using four and incrementing them all.
  private final String TAG = "LifecycleHandler";
  private int resumed;
  private int paused;
  private int started;
  private int stopped;

  public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
  }

  public void onActivityDestroyed(Activity activity) {
  }

  public void onActivityResumed(Activity activity) {
    ++resumed;
    Log.d(TAG, "onActivityResumed");
    BeaconReferenceApplication application =
        (BeaconReferenceApplication) BeaconReferenceApplication.getContext();
    //application.clearBuffer();
    application.cancelNotification();
  }

  public void onActivityPaused(Activity activity) {
    ++paused;
    Log.d(TAG, "onActivityPaused");
    //BeaconReferenceApplication application =
    //    (BeaconReferenceApplication) BeaconReferenceApplication.getContext();
    //application.populateScreenOffBuffer();
    SalsaBeacon.logOccurrencesToDb();
  }

  public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
  }

  public void onActivityStarted(Activity activity) {
    ++started;
  }

  public void onActivityStopped(Activity activity) {
    ++stopped;
    //android.util.Log.w("test", "application is visible: " + (started > stopped));
  }

  // And these two public static functions
  public boolean isApplicationVisible() {
      return started > stopped;
  }

  public boolean isApplicationInForeground() {
      return resumed > paused;
  }
}
