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

import android.app.Application;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.xmlpull.v1.XmlPullParserException;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by rmg29 on 22/01/2015.
 */
public class SalsaBeacon implements Parcelable {
  private static final String TAG = "SalsaBeaconClass";

  private static final String REGION_ID_PREFIX = "region_";
  private static final String BEACON_ID_PREFIX = "salsa_";

  public static final String[] sOccurenceNameProjection = {
      Salsa.BeaconOccurrence._ID,
      Salsa.BeaconOccurrence.COLUMN_NAME_BEACON_NAME
  };

  public static final String[] sOccurenceProjection = {
      Salsa.BeaconOccurrence._ID,
      Salsa.BeaconOccurrence.COLUMN_NAME_FIRST_LOGGED,
      Salsa.BeaconOccurrence.COLUMN_NAME_LAST_LOGGED,
      Salsa.BeaconOccurrence.COLUMN_NAME_VIEWED,
      Salsa.BeaconOccurrence.COLUMN_NAME_DELETED,
      Salsa.BeaconOccurrence.COLUMN_NAME_LAST_VALID_PROXIMITY
  };

  private static final int DEFAULT_VALID_DISTANCE = 10;
  private static final long DEFAULT_VALID_PERIOD = 43200000; // 12 hours in milliseconds

  private static HashMap<String, SalsaBeacon> loadedBeacons = new HashMap<String, SalsaBeacon>();
  private int mAreaId;
  private int mIndividualId;
  private String mRegionId;
  private String mBeaconId;
  private Uri mUri;
  private int mValidityDistance;
  private long mValidityPeriod;
  private Uri mDbUri;
  private long mFirstLogged;
  private long mLastLogged;
  private boolean mUserViewed = false;
  private long mUserViewedTimestamp;
  private boolean mUserDeletedFromList = false;
  private double mLastProximity;
  private double mLastValidProximity;
  private String mTitle;

  static public SalsaBeacon getInstance(long id) {
    Application application = (Application) BeaconReferenceApplication.getContext();
    ContentResolver cr = application.getContentResolver();
    Uri idPath = Uri.withAppendedPath(Salsa.BeaconOccurrence.CONTENT_ID_URI_BASE, Long.toString(id));
    Cursor occurrence = cr.query(idPath, sOccurenceNameProjection, null, null, null);
    if(occurrence == null || !occurrence.moveToFirst()) {
      return null;
    }
    SalsaBeacon beacon = getInstance(
        occurrence.getString(
            occurrence.getColumnIndexOrThrow(Salsa.BeaconOccurrence.COLUMN_NAME_BEACON_NAME)
        )
    );
    occurrence.close();
    return beacon;
  }

  static public SalsaBeacon getInstance(String beaconName) {
    String[] components = beaconName.split("_");
    if(components.length == 3) {
      return getInstance(Integer.parseInt(components[1]), Integer.parseInt(components[2]));
    }
    return null;
  }

  static public SalsaBeacon getInstance(int areaId, int individualId) {
    String beaconId = SalsaBeacon.BEACON_ID_PREFIX +  areaId + "_" + individualId;
    if(!loadedBeacons.containsKey(beaconId)) {
      SalsaBeacon b = new SalsaBeacon(areaId, individualId);
      b.loadLatestOccurrence();
      loadedBeacons.put(b.getId(), b);
    }
    return loadedBeacons.get(beaconId);
  }

  static public void handleFoundBeacon(int areaId, int individualId, double proximity) {
    String beaconId = SalsaBeacon.BEACON_ID_PREFIX +  areaId + "_" + individualId;
    Long now = System.currentTimeMillis();
    if(!loadedBeacons.containsKey(beaconId)) {
      SalsaBeacon b = new SalsaBeacon(areaId, individualId);
      b.loadLatestOccurrence();
      loadedBeacons.put(b.getId(), b);
    }
    SalsaBeacon b = loadedBeacons.get(beaconId);
    b.mLastProximity = proximity;
    if(b.mLastProximity <= b.mValidityDistance) { // only action beacon surfacing to user if within valid distance
      b.mLastValidProximity = b.mLastProximity;
      b.logOccurrence(now, false);
    }
  }

  static public void logOccurrencesToDb() {
    Collection values = loadedBeacons.values();
    for (Object value : values) {
      if(((SalsaBeacon) value).mLastValidProximity > 0) {
        ((SalsaBeacon) value).logOccurrence();
      }
    }
  }

  private SalsaBeacon(int areaId, int individualId) throws Resources.NotFoundException {
    mAreaId = areaId;
    mIndividualId = individualId;
    mRegionId = SalsaBeacon.REGION_ID_PREFIX + mAreaId;
    mBeaconId = SalsaBeacon.BEACON_ID_PREFIX +  mAreaId + "_" + mIndividualId;
    mDbUri = null;
    mFirstLogged = 0;
    mLastLogged = 0;
    mUserViewed = false;
    mUserViewedTimestamp = 0;
    mLastValidProximity = 0;
    Application application = (Application) BeaconReferenceApplication.getContext();
    try {
      XmlResourceParser parser = application.getResources().getXml(
          application.getResources().getIdentifier(mBeaconId, "xml", "uk.ac.open.salsabeacons")
      );
      parseContent(parser);
    } catch(Resources.NotFoundException e) {
      Log.i(TAG, e.getMessage());
      mTitle = mBeaconId;
      mValidityDistance = SalsaBeacon.DEFAULT_VALID_DISTANCE;
      mValidityPeriod = SalsaBeacon.DEFAULT_VALID_PERIOD;
    }
  }

  public double getLastProximity() {
    return mLastValidProximity;
  }

  public Date getLastLogged() {
    return new Date(mLastLogged);
  }

  public boolean viewed() {
    return mUserViewed;
  }

  public Date getViewedAt() {
    return new Date(mUserViewedTimestamp);
  }

  public String getTitle() {
    return mTitle;
  }

  public String getRegion() {
    Application application = (Application) BeaconReferenceApplication.getContext();
    String name;
    try {
      name = application.getResources().getString(application.getResources().getIdentifier(mRegionId, "string", "uk.ac.open.salsabeacons"));
    } catch (Exception e) {
      name = mRegionId;
    }
    return name;
  }

  private int getOccurrenceDbId() {
    try {
      return Integer.parseInt(mDbUri.getLastPathSegment());
    } catch(NullPointerException e) {
      return 0;
    }
  }

  private void loadLatestOccurrence() {
    Application application = (Application) BeaconReferenceApplication.getContext();
    ContentResolver cr = application.getContentResolver();
    String selection = Salsa.BeaconOccurrence.COLUMN_NAME_BEACON_NAME + " = ?";
    String[] selectionArgs = {getId()};
    Cursor occurrences = cr.query(
        Salsa.BeaconOccurrence.CONTENT_URI, sOccurenceProjection, selection, selectionArgs, null
    );
    if(occurrences == null || !occurrences.moveToFirst()) {
      return;
    }
    try {
      mDbUri = Uri.withAppendedPath(
          Salsa.BeaconOccurrence.CONTENT_ID_URI_BASE,
          occurrences.getString(occurrences.getColumnIndexOrThrow(Salsa.BeaconOccurrence._ID))
      );
      mFirstLogged = occurrences.getLong(
          occurrences.getColumnIndexOrThrow(Salsa.BeaconOccurrence.COLUMN_NAME_FIRST_LOGGED)
      );
      mLastLogged = occurrences.getLong(
          occurrences.getColumnIndexOrThrow(Salsa.BeaconOccurrence.COLUMN_NAME_LAST_LOGGED)
      );
      mUserViewedTimestamp = occurrences.getLong(
          occurrences.getColumnIndexOrThrow(Salsa.BeaconOccurrence.COLUMN_NAME_VIEWED)
      );
      mUserDeletedFromList = occurrences.getInt(
          occurrences.getColumnIndexOrThrow(Salsa.BeaconOccurrence.COLUMN_NAME_DELETED)
      ) == 1;
      mLastValidProximity = occurrences.getDouble(
          occurrences.getColumnIndexOrThrow(Salsa.BeaconOccurrence.COLUMN_NAME_LAST_VALID_PROXIMITY)
      );
      if(mUserViewedTimestamp > 0) {
        mUserViewed = true;
      }
      occurrences.close();
    } catch(IllegalArgumentException e) {
      mDbUri = null;
      mFirstLogged = 0;
      mLastLogged = 0;
      mUserViewedTimestamp = 0;
      mUserViewed = false;
      mUserDeletedFromList = false;
    }
  }

  private void logOccurrence() {
    logOccurrence(mLastLogged, true);
  }

  private void logOccurrence(Long timeStamp, boolean writeToDb) {
    Application application = (Application) BeaconReferenceApplication.getContext();
    ContentResolver cr = application.getContentResolver();
    ContentValues values = new ContentValues();

    if ((mLastLogged + mValidityPeriod) < timeStamp) { // always write to DB if a new occurence if needed
      // a new occurence will be placed if either one doesn't exist or (mLastLogged = 0) or
      // the current occurence is out of date!
      SalsaBeacon newOccurrence = new SalsaBeacon(mAreaId, mIndividualId);
      newOccurrence.mLastLogged = timeStamp;
      newOccurrence.mLastProximity = mLastProximity;
      newOccurrence.mLastValidProximity = newOccurrence.mLastProximity;

      values.put(Salsa.BeaconOccurrence.COLUMN_NAME_BEACON_NAME, newOccurrence.getId());
      values.put(Salsa.BeaconOccurrence.COLUMN_NAME_FIRST_LOGGED, newOccurrence.mLastLogged);
      values.put(Salsa.BeaconOccurrence.COLUMN_NAME_LAST_LOGGED, newOccurrence.mLastLogged);
      values.put(Salsa.BeaconOccurrence.COLUMN_NAME_VIEWED, newOccurrence.mUserViewedTimestamp);
      values.put(Salsa.BeaconOccurrence.COLUMN_NAME_DELETED, newOccurrence.mUserDeletedFromList);
      values.put(Salsa.BeaconOccurrence.COLUMN_NAME_LAST_VALID_PROXIMITY, newOccurrence.mLastValidProximity);
      mDbUri = cr.insert(Salsa.BeaconOccurrence.CONTENT_URI, values);
      cr.notifyChange(Salsa.BeaconOccurrence.CONTENT_VIEWABLE_LIST_URI, null);

      ((BeaconReferenceApplication) application).sendNotification();
      SalsaBeacon.loadedBeacons.remove(getId());
    } else if (writeToDb) { // must be a current valid occurence ((mLastLogged + mValidityPeriod) >= timeStamp) and explicit call to write to DB
      mLastLogged = timeStamp;

      values.put(Salsa.BeaconOccurrence.COLUMN_NAME_LAST_LOGGED, mLastLogged);
      values.put(Salsa.BeaconOccurrence.COLUMN_NAME_VIEWED, mUserViewedTimestamp);
      values.put(Salsa.BeaconOccurrence.COLUMN_NAME_DELETED, mUserDeletedFromList);
      values.put(Salsa.BeaconOccurrence.COLUMN_NAME_LAST_VALID_PROXIMITY, mLastValidProximity);
      cr.update(mDbUri, values, null, null);
      cr.notifyChange(Salsa.BeaconOccurrence.CONTENT_VIEWABLE_LIST_URI, null);

    } else { // must be a current valid occurence ((mLastLogged + mValidityPeriod) >= timeStamp) so update in memory instance
      mLastLogged = timeStamp;
    }
  }

  public void logOccurrenceViewed() {
    if(!mUserViewed) {
      mUserViewedTimestamp = System.currentTimeMillis();
      mUserViewed = true;
      logOccurrence();
      //Application application = (Application) BeaconReferenceApplication.getContext();
      //ContentResolver cr = application.getContentResolver();
      //cr.notifyChange(Salsa.BeaconOccurrence.CONTENT_VIEWABLE_LIST_URI, null);
    }
  }

  public void setDeletedFlag() {
    mUserDeletedFromList = true;
    logOccurrence();
    //Application application = (Application) BeaconReferenceApplication.getContext();
    //ContentResolver cr = application.getContentResolver();
    //cr.notifyChange(Salsa.BeaconOccurrence.CONTENT_VIEWABLE_LIST_URI, null);
  }

  private void parseContent(XmlResourceParser parser) {
    Boolean validRoot = false;
    String contentType = "";
    try {
      while(XmlResourceParser.END_DOCUMENT != parser.next()) {
        Log.d(TAG, "EVENT " + String.valueOf(parser.getEventType()));
        switch (parser.getEventType()) {

          case XmlResourceParser.START_DOCUMENT:
            continue;

          case XmlResourceParser.START_TAG:
            Log.d(TAG, "START_TAG '" + parser.getName() + "'");
            if (!parser.getName().equals("beacon") && !validRoot) {
              throw new XmlPullParserException("Invalid root element");
            }
            if (parser.getName().equals("beacon")) {
              validRoot = true;
            }
            else {
              contentType = parser.getName();
            }
            continue;

          case XmlResourceParser.TEXT:
            Log.d(TAG, "TEXT " + parser.getText());
            switch (contentType) {
              case "title":
                mTitle = parser.getText();
                continue;
              case "uri":
                mUri = Uri.parse(parser.getText());
                continue;

              case "occurrence_validity_distance":
                mValidityDistance = Integer.parseInt(parser.getText());
                continue;

              case "occurrence_validity_period":
                mValidityPeriod = Long.parseLong(parser.getText()) * 1000; // seconds converted to milliseconds
            }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public String getId() {
    return mBeaconId;
  }

  public boolean isResource() {
    if(mUri != null && mUri.getScheme().equals("file")) {
      return true;
    }
    return false;
  }

  public boolean isUnassigned() {
    return mUri == null;
  }

  public Uri getUri() {
    return mUri;
  }

  @Override
  public int hashCode() {
    int result = 17;
    result = 31 * result + mRegionId.hashCode();
    result = 31 * result + mBeaconId.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if(o instanceof SalsaBeacon) {
      SalsaBeacon sb = (SalsaBeacon) o;
      if (sb.getId().equals(this.getId())) return true;
    }
    return false;
  }

  @Override
  public String toString() {
    //Application application = (Application) BeaconReferenceApplication.getContext();
    //String name;
    //if(mUri == null) {
    //  name = mTitle;
    //} else {
    //  name = application.getResources().getString(application.getResources().getIdentifier(mRegionId, "string", "uk.ac.open.salsabeacons"));
    //  //name += " " + application.getResources().getString(application.getResources().getIdentifier(mBeaconId, "string", "uk.ac.open.salsabeacons"));
    //  name += " " + mTitle;
    //}
    //return name;
    return mTitle;
  }

  /*
   * Implementation of the Parcelable interface
   * To allow a SalsaBeacon instance to be passed via an intent
   */

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel out, int flags) {
    out.writeString(mRegionId);
    out.writeString(mBeaconId);
    out.writeString(mUri.toString());
    out.writeInt(mValidityDistance);
    out.writeLong(mValidityPeriod);
    out.writeString(mDbUri.toString());
    out.writeLong(mFirstLogged);
    out.writeLong(mLastLogged);
    out.writeInt(mUserViewed ? 1 : 0);
    out.writeLong(mUserViewedTimestamp);
    out.writeDouble(mLastProximity);
    out.writeString(mTitle);
  }

  public static final Parcelable.Creator<SalsaBeacon> CREATOR
      = new Parcelable.Creator<SalsaBeacon>() {
    public SalsaBeacon createFromParcel(Parcel in) {
      return new SalsaBeacon(in);
    }

    public SalsaBeacon[] newArray(int size) {
      return new SalsaBeacon[size];
    }
  };

  private SalsaBeacon(Parcel in) {
    mRegionId = in.readString();
    mBeaconId = in.readString();
    mUri = Uri.parse(in.readString());
    mValidityDistance = in.readInt();
    mValidityPeriod = in.readLong();
    mDbUri = Uri.parse(in.readString());
    mFirstLogged = in.readLong();
    mLastLogged = in.readLong();
    mUserViewed = in.readInt() == 0;
    mUserViewedTimestamp = in.readLong();
    mLastProximity = in.readDouble();
    mTitle = in.readString();
  }
}
