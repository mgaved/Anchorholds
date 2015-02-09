package uk.ac.open.salsabeacons;

import android.app.Application;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.net.Uri;
import android.util.Log;

import org.xmlpull.v1.XmlPullParserException;

/**
 * Created by rmg29 on 22/01/2015.
 */
public class SalsaBeacon {
  private static final String TAG = "SalsaBeaconBeacon";

  private static final String REGION_ID_PREFIX = "region_";
  private static final String BEACON_ID_PREFIX = "beacon_";
  private String mRegionId;
  private String mBeaconId;
  private XmlResourceParser mParser;
  private Boolean mIsUri = null;
  private Uri mUri;
  private String mContentType;

  protected SalsaBeacon(int areaId, int individualId) throws Resources.NotFoundException {
    mRegionId = SalsaBeacon.REGION_ID_PREFIX + areaId;
    mBeaconId = SalsaBeacon.BEACON_ID_PREFIX +  areaId + "_" + individualId;
    Application application = (Application) BeaconReferenceApplication.getContext();
    mParser = application.getResources().getXml(application.getResources().getIdentifier(mBeaconId, "xml", "uk.ac.open.salsabeacons"));
  }

  protected SalsaBeacon(String beaconId) {
    String[] parts = beaconId.split("_");
    int areaId = Integer.parseInt(parts[1]);
    int individualId = Integer.parseInt(parts[2]);
    mRegionId = SalsaBeacon.REGION_ID_PREFIX + areaId;
    mBeaconId = SalsaBeacon.BEACON_ID_PREFIX +  areaId + "_" + individualId;
    Application application = (Application) BeaconReferenceApplication.getContext();
    mParser = application.getResources().getXml(application.getResources().getIdentifier(mBeaconId, "xml", "uk.ac.open.salsabeacons"));
  }

  private void parseContent() {
    Boolean validRoot = false;
    mIsUri = false;
    try {
      while(XmlResourceParser.END_DOCUMENT != mParser.next()) {
        Log.d(TAG+" EVENT", String.valueOf(mParser.getEventType()));
        switch (mParser.getEventType()) {

          case XmlResourceParser.START_DOCUMENT:
            continue;

          case XmlResourceParser.START_TAG:
            Log.d(TAG+" START_TAG", "'"+mParser.getName()+"'");
            if (!mParser.getName().equals("beacon") && !validRoot) {
              throw new XmlPullParserException("Invalid root element");
            }
            if (mParser.getName().equals("beacon")) {
              validRoot = true;
            }
            else if (mParser.getName().equals("uri")) {
              if(mParser.getDepth() == 2) {
                mIsUri = true;
                mContentType = mParser.getName();
              }
            }
            continue;

          case XmlResourceParser.TEXT:
            Log.d(TAG+" TEXT", mParser.getText());
            if(mIsUri) {
              mUri = Uri.parse(mParser.getText());
            }
            continue;

          case XmlResourceParser.END_TAG:
            if (mIsUri && mParser.getName().equals("uri")) {
              return;
            }
            continue;
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
    if(mUri == null) {
      parseContent();
    }
    if(mUri.getScheme().equals("file")) {
      return true;
    }
    return false;
  }

  public Uri getUri() {
    if(mUri == null) {
      parseContent();
    }
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
    String stringRegion = application.getResources().getString(application.getResources().getIdentifier(mRegionId, "string", "uk.ac.open.salsabeacons"));
    String stringBeacon = application.getResources().getString(application.getResources().getIdentifier(mBeaconId, "string", "uk.ac.open.salsabeacons"));
    return stringRegion + " " + stringBeacon;
  }

}
