package uk.ac.open.salsabeacons;

import android.app.Application;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.util.SimpleArrayMap;
import android.util.Log;

import org.xmlpull.v1.XmlPullParserException;

import java.sql.SQLDataException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by rmg29 on 22/01/2015.
 */
public class SalsaBeacon implements Parcelable {
  private static final String TAG = "SalsaBeaconBeacon";

  private static final String REGION_ID_PREFIX = "region_";
  private static final String BEACON_ID_PREFIX = "beacon_";

  public static final String[] sOccurenceProjection = {
      Salsa.BeaconOccurrence._ID,
      Salsa.BeaconOccurrence.COLUMN_NAME_FIRST_LOGGED,
      Salsa.BeaconOccurrence.COLUMN_NAME_LAST_LOGGED
  };
  public static final String[] sOccurenceViewedProjection = {
      Salsa.BeaconViewed._ID,
      Salsa.BeaconViewed.COLUMN_NAME_BEACON_OCCURRENCE_ID,
      Salsa.BeaconViewed.COLUMN_NAME_TIMESTAMP
  };

  private static final int DEFAULT_VALID_DISTANCE = 10;
  private static final long DEFAULT_VALID_PERIOD = 43200000; // 12 hours in milliseconds

  private static HashMap<String, SalsaBeacon> loadedBeacons = new HashMap<String, SalsaBeacon>();

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
  private double mLastProximity;
  private String mTitle;


  static SalsaBeacon getInstance(int areaId, int individualId, double proximity) {
    String beaconId = SalsaBeacon.BEACON_ID_PREFIX +  areaId + "_" + individualId;
    Long now = System.currentTimeMillis();
    if(loadedBeacons.containsKey(beaconId)) {
      SalsaBeacon b = loadedBeacons.get(beaconId);
      Log.d(TAG+" b", Double.toString(proximity)+" <= "+Double.toString(b.mValidityDistance));
      if(proximity <= b.mValidityDistance) {
        b.mLastProximity = proximity;
        b.updateOccurrence(now);
        Log.d(TAG+" cached", b.toString());
        return b;
      } else {
        Log.d(TAG+" null", "null");
        return null;
      }
    } else {
      SalsaBeacon b = new SalsaBeacon(areaId, individualId, proximity);
      Log.d(TAG+" create", b.toString());
      Log.d(TAG+" load latest", String.valueOf(b.loadLatestOccurrence()));
      Log.d(TAG+" update latest", String.valueOf(b.updateOccurrence(now)));
      loadedBeacons.put(b.getId(), b);
      return b;
    }
  }

  static public void updateOccurrencesToDb() {
    Collection values = loadedBeacons.values();
    Iterator i = values.iterator();
    Application application = (Application) BeaconReferenceApplication.getContext();
    ContentResolver cr = application.getContentResolver();
    ContentValues cv;
    while(i.hasNext()) {
      SalsaBeacon b = (SalsaBeacon) i.next();
      cv = new ContentValues();
      cv.put(Salsa.BeaconOccurrence.COLUMN_NAME_LAST_LOGGED, b.mLastLogged);
      cr.update(b.mDbUri, cv, null, null);
    }
  }

  private SalsaBeacon(int areaId, int individualId, double proximity) throws Resources.NotFoundException {
    mRegionId = SalsaBeacon.REGION_ID_PREFIX + areaId;
    mBeaconId = SalsaBeacon.BEACON_ID_PREFIX +  areaId + "_" + individualId;
    mLastProximity = proximity;
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
    return mLastProximity;
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

  private int getOccurrenceDbId() {
    try {
      return Integer.parseInt(mDbUri.getLastPathSegment());
    } catch(NullPointerException e) {
      return 0;
    }
  }

  private boolean loadLatestOccurrence() {
    Application application = (Application) BeaconReferenceApplication.getContext();
    ContentResolver cr = application.getContentResolver();
    String selection = Salsa.BeaconOccurrence.COLUMN_NAME_BEACON_NAME + " = ?";
    String[] selectionArgs = {getId()};
    Cursor occurrences = cr.query(
        Salsa.BeaconOccurrence.CONTENT_URI, SalsaBeacon.sOccurenceProjection, selection, selectionArgs, null
    );
    if(occurrences == null || !occurrences.moveToFirst()) {
      return false;
    }
    try {
      mDbUri = Salsa.BeaconOccurrence.CONTENT_ID_URI_BASE.buildUpon().appendPath(
          occurrences.getString(occurrences.getColumnIndexOrThrow(Salsa.BeaconOccurrence._ID))
      ).build();
      mFirstLogged = occurrences.getLong(
          occurrences.getColumnIndexOrThrow(Salsa.BeaconOccurrence.COLUMN_NAME_FIRST_LOGGED)
      );
      mLastLogged = occurrences.getLong(
          occurrences.getColumnIndexOrThrow(Salsa.BeaconOccurrence.COLUMN_NAME_LAST_LOGGED)
      );
      occurrences.close();
      loadOccurrenceViewed();
    } catch(IllegalArgumentException e) {
      mDbUri = null;
      mFirstLogged = 0;
      mLastLogged = 0;
      return false;
    }
    return true;
  }

  private boolean insertOccurrence(Long timeStamp) {
    Application application = (Application) BeaconReferenceApplication.getContext();
    ContentResolver cr = application.getContentResolver();
    ContentValues values = new ContentValues();
    values.put(Salsa.BeaconOccurrence.COLUMN_NAME_BEACON_NAME, getId());
    values.put(Salsa.BeaconOccurrence.COLUMN_NAME_FIRST_LOGGED, timeStamp);
    values.put(Salsa.BeaconOccurrence.COLUMN_NAME_LAST_LOGGED, timeStamp);
    try {
      mDbUri = cr.insert(Salsa.BeaconOccurrence.CONTENT_URI, values);
      Log.d(TAG+" dbURI", mDbUri.toString());
      mFirstLogged = timeStamp;
      mLastLogged = timeStamp;
      return true;
    } catch(SQLException e) {
      mDbUri = null;
      mFirstLogged = 0;
      mLastLogged = 0;
      return false;
    }
  }

  private boolean updateOccurrence(Long timeStamp) {
    Long test = mLastLogged + mValidityPeriod;
    Boolean bool = test < timeStamp;
    Log.d(TAG+" update test", Long.toString(test)+" < "+timeStamp+" = "+bool.toString());
    if((mLastLogged + mValidityPeriod) < timeStamp) {
      Log.d(TAG+" update mLastLogged", Long.toString(mLastLogged));
      Log.d(TAG+" update mValidityPeriod", Long.toString(mValidityPeriod));
      Log.d(TAG+" update", test.toString()+" < "+Long.toString(timeStamp));
      return insertOccurrence(timeStamp);
    } else {
      mLastLogged = timeStamp;
      return true;
    }
  }

  private void loadOccurrenceViewed() {
    int occurenceId = getOccurrenceDbId();
    if(occurenceId > 0) {
      Application application = (Application) BeaconReferenceApplication.getContext();
      ContentResolver cr = application.getContentResolver();
      String selection = Salsa.BeaconViewed.COLUMN_NAME_BEACON_OCCURRENCE_ID + " = ?";
      String[] selectionArgs = {Integer.toString(getOccurrenceDbId())};
      Cursor viewed = cr.query(
          Salsa.BeaconViewed.CONTENT_URI, SalsaBeacon.sOccurenceViewedProjection, selection, selectionArgs, null
      );
      if(viewed == null || !viewed.moveToFirst()) {
        return;
      }
      try {
        mUserViewed = true;
        mUserViewedTimestamp = viewed.getLong(
            viewed.getColumnIndexOrThrow(Salsa.BeaconViewed.COLUMN_NAME_TIMESTAMP)
        );
        viewed.close();
      } catch(IllegalArgumentException e) {
        mUserViewedTimestamp = 0;
      }
    }
  }

  public boolean insertOccurrenceViewed() {
    if(!mUserViewed) {
      Long now = System.currentTimeMillis();
      Application application = (Application) BeaconReferenceApplication.getContext();
      ContentResolver cr = application.getContentResolver();
      ContentValues values = new ContentValues();
      values.put(Salsa.BeaconViewed.COLUMN_NAME_BEACON_OCCURRENCE_ID, getOccurrenceDbId());
      values.put(Salsa.BeaconViewed.COLUMN_NAME_TIMESTAMP, now);
      try {
        Uri viewedId = cr.insert(Salsa.BeaconViewed.CONTENT_URI, values);
        Log.d(TAG+" dbURI", viewedId.toString());
        mUserViewedTimestamp = now;
        mUserViewed = true;
        return true;
      } catch(SQLException e) {
        return false;
      }
    }
    return false;
  }

  private void parseContent(XmlResourceParser parser) {
    Boolean validRoot = false;
    String contentType = "";
    try {
      while(XmlResourceParser.END_DOCUMENT != parser.next()) {
        Log.d(TAG+" EVENT", String.valueOf(parser.getEventType()));
        switch (parser.getEventType()) {

          case XmlResourceParser.START_DOCUMENT:
            continue;

          case XmlResourceParser.START_TAG:
            Log.d(TAG+" START_TAG", "'"+parser.getName()+"'");
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
            Log.d(TAG+" TEXT", parser.getText());
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
    Application application = (Application) BeaconReferenceApplication.getContext();
    String name;
    if(mUri == null) {
      name = mTitle;
    } else {
      name = application.getResources().getString(application.getResources().getIdentifier(mRegionId, "string", "uk.ac.open.salsabeacons"));
      //name += " " + application.getResources().getString(application.getResources().getIdentifier(mBeaconId, "string", "uk.ac.open.salsabeacons"));
      name += " " + mTitle;
    }
    return name;
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
