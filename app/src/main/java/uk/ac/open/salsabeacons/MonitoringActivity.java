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
import android.bluetooth.BluetoothAdapter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import org.altbeacon.beacon.BeaconManager;

/**
 * Created by rmg29 on 22/01/2015.
 */
public class MonitoringActivity extends Activity implements BeaconListFragment.OnListFragmentInteractionListener {
  protected static final String TAG = "MonitoringActivity";
  static final String STATE_LAST_CLICKED = "lastClickedItem";
  private int mLastClickedListItem = 0;
  private boolean mIsDualPane;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    Log.d(TAG, "onCreate");
    super.onCreate(savedInstanceState);
    if (savedInstanceState != null) {
      mLastClickedListItem = savedInstanceState.getInt(STATE_LAST_CLICKED);
    }
    Resources res = getResources();
    String appName = res.getString(R.string.main_activity);
    String version = res.getString(R.string.salsa_app_version);
    setTitle(appName+" "+version);
    setContentView(R.layout.activity_monitoring);
    ContentWebViewFragment contentFragment = (ContentWebViewFragment) getFragmentManager()
        .findFragmentById(R.id.content_fragment);
    mIsDualPane = contentFragment != null && contentFragment.getView().getVisibility() == View.VISIBLE;
        verifyBluetooth();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle item selection
    switch (item.getItemId()) {
      case R.id.action_help:
        openHelp();
        return true;
      case R.id.action_about:
        openAbout();
        return true;
      case R.id.action_terms:
        openTerms();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  private void openHelp() {
    Intent intent = new Intent(this, ContentWebViewActivity.class);
    Resources resources = getResources();
    Uri aboutUri = Uri.parse("file:///android_asset/action-menu-content/dummy-content.html");
    intent.putExtra("title", resources.getString(R.string.action_help));
    intent.setData(aboutUri);
    startActivity(intent);
  }

  private void openAbout() {
    Intent intent = new Intent(this, ContentWebViewActivity.class);
    Resources resources = getResources();
    Uri aboutUri = Uri.parse("file:///android_asset/action-menu-content/dummy-content.html");
    intent.putExtra("title", resources.getString(R.string.action_about));
    intent.setData(aboutUri);
    startActivity(intent);
  }

  private void openTerms() {
    Intent intent = new Intent(this, ContentWebViewActivity.class);
    Resources resources = getResources();
    Uri aboutUri = Uri.parse("file:///android_asset/action-menu-content/dummy-content.html");
    intent.putExtra("title", resources.getString(R.string.action_terms));
    intent.setData(aboutUri);
    startActivity(intent);
  }

  @Override
  public void onResume() {
    super.onResume();
    Configuration config = getResources().getConfiguration();
    if(mIsDualPane) {
      onListFragmentInteraction(getLastClicked());
    }
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    outState.putInt(STATE_LAST_CLICKED, mLastClickedListItem);
    super.onSaveInstanceState(outState);
  }

  @Override
  public void onPause() {
    super.onPause();
  }

  private void verifyBluetooth() {
    try {
      if (!BeaconManager.getInstanceForApplication(this).checkAvailability()) {
        if(setBluetooth(true)) {
          Toast t = Toast.makeText(this, R.string.bluetooth_enabled, Toast.LENGTH_LONG);
          t.show();
        } else {
          final AlertDialog.Builder builder = new AlertDialog.Builder(this);
          builder.setTitle(R.string.bluetooth_not_enabled_title);
          builder.setMessage(R.string.bluetooth_not_enabled_message);
          builder.setPositiveButton(android.R.string.ok, null);
          builder.show();
        }
      }
    }
    catch (RuntimeException e) {
      final AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setTitle(R.string.bluetooth_LE_not_available_title);
      builder.setMessage(R.string.bluetooth_LE_not_available_message);
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

  private boolean setBluetooth(boolean enable) {
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    boolean isEnabled = bluetoothAdapter.isEnabled();
    if (enable && !isEnabled) {
      return bluetoothAdapter.enable();
    }
    else if(!enable && isEnabled) {
      return bluetoothAdapter.disable();
    }
    return true;
  }

  @Override
  public void onListFragmentInteraction(int position) {
    mLastClickedListItem = position;
    BeaconListFragment listFrag = (BeaconListFragment) getFragmentManager()
        .findFragmentById(R.id.beaconlisting);
    ListView list = listFrag.getListView();
    Cursor listItemCursor = (Cursor) list.getItemAtPosition(position);
    SalsaBeacon beacon;
    try {
      String beaconName = listItemCursor.getString(
          listItemCursor.getColumnIndexOrThrow(Salsa.BeaconOccurrence.COLUMN_NAME_BEACON_NAME)
      );
      beacon = SalsaBeacon.getInstance(beaconName);
    } catch (IllegalArgumentException | NullPointerException e) {
      return;
    }
    if(beacon.isUnassigned()) {
      return;
    }
    beacon.logOccurrenceViewed();
    if(!mIsDualPane) {
      if(!beacon.isResource()) {
        Intent viewIntent = new Intent("android.intent.action.VIEW", beacon.getUri());
        startActivity(viewIntent);
      } else {
        Intent intent = new Intent(this, ContentWebViewActivity.class);
        intent.putExtra("title", beacon.getTitle());
        intent.setData(beacon.getUri());
        startActivity(intent);
      }
    } else {
      ContentWebViewFragment contentFragment = (ContentWebViewFragment) getFragmentManager()
          .findFragmentById(R.id.content_fragment);
      contentFragment.updateContent(beacon.getUri().toString());
    }
  }

  public int getLastClicked() {
    return mLastClickedListItem;
  }
}