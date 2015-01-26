package uk.ac.open.salsabeacons;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

/**
 * Created by rmg29 on 16/01/2015.
 */
public class LifecycleHandler implements Application.ActivityLifecycleCallbacks {
  // I use four separate variables here. You can, of course, just use two and
  // increment/decrement them instead of using four and incrementing them all.
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
  }

  public void onActivityPaused(Activity activity) {
    ++paused;
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
