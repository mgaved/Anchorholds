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

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.text.Html;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

/**
 * Created by rmg29 on 17/04/2015.
 */
public class SalsaBeaconCursorAdapter extends ResourceCursorAdapter {

  private static final String TAG = "SalsaBeaconCursorAdp";

  public SalsaBeaconCursorAdapter(Context context, int layout, Cursor c, int flags) {
    super(context, layout, c, flags);
  }

  @Override
  public void bindView(View view, final Context context, Cursor cursor) {

    BeaconReferenceApplication app = (BeaconReferenceApplication) context.getApplicationContext();

    final String beaconName;
    final int occurrences;
    try {
      beaconName = cursor.getString(
          cursor.getColumnIndexOrThrow(Salsa.BeaconOccurrence.COLUMN_NAME_BEACON_NAME)
      );
      occurrences = cursor.getInt(
          cursor.getColumnIndexOrThrow(Salsa.BeaconOccurrence.AGG_COLUMN_NAME_OCCURRENCE_COUNT)
      );
      Log.d(TAG, "beaconName: " + beaconName);
    } catch (IllegalArgumentException e) {
      Log.d(TAG, "bindView failed: " + e.getMessage());
      return;
    }

    SalsaBeacon beacon = SalsaBeacon.getInstance(beaconName);

    TextView iconView = (TextView) view.findViewById(R.id.beacon_type_icon);
    TextView title = (TextView) view.findViewById(R.id.beacon_name);
    TextView timestamp = (TextView) view.findViewById(R.id.beacon_timestamp);
    TextView proximityText = (TextView) view.findViewById(R.id.beacon_proximity);
    title.setText(Html.fromHtml(beacon.toString()), TextView.BufferType.SPANNABLE);
    Integer iconResourceId = R.string.fa_globe;
    if(beacon.isUnassigned()) {
      iconResourceId = R.string.fa_question;
    } else if(beacon.isResource()) {
      iconResourceId = R.string.fa_file_text;
    }
    iconView.setTypeface(app.getIconFont());
    iconView.setText(iconResourceId);
    Resources resources =  BeaconReferenceApplication.getContext().getResources();
    java.text.DateFormat time = DateFormat.getTimeFormat(BeaconReferenceApplication.getContext());
    java.text.DateFormat date = DateFormat.getDateFormat(BeaconReferenceApplication.getContext());
    double proximity = (double) Math.round(beacon.getLastProximity() * 100) / 100;
    String viewedDetails = "times encountered: " + Integer.toString(occurrences);
    if(beacon.viewed()) {
      int grey = Color.parseColor("#9C9C9C");
      title.setTextColor(grey);
      iconView.setTextColor(grey);
      timestamp.setTextColor(grey);
      proximityText.setTextColor(grey);
      viewedDetails += " viewed at: "+ date.format(beacon.getViewedAt()) + " "
          + time.format(beacon.getViewedAt());
    } else {
      int black = Color.parseColor("#000000");
      title.setTextColor(black);
      iconView.setTextColor(black);
      timestamp.setTextColor(black);
      proximityText.setTextColor(black);
    }
    timestamp.setText(
        date.format(beacon.getLastLogged()) + " "
            + time.format(beacon.getLastLogged())
            + viewedDetails
    );
    proximityText.setText(resources.getString(R.string.region) + ": " +beacon.getRegion() + " | " +resources.getString(R.string.estimated_proximity)
        + ": " + proximity);
  }
}
