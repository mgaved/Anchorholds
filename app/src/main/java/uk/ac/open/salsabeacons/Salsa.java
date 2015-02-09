package uk.ac.open.salsabeacons;

/**
 * Created by rmg29 on 22/01/2015.
 */

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Defines a contract between the Salsa content provider and its clients. A contract defines the
 * information that a client needs to access the provider as one or more data tables. A contract
 * is a public, non-extendable (final) class that contains constants defining column names and
 * URIs. A well-written client depends only on the constants in the contract.
 */
public final class Salsa {
  public static final String AUTHORITY = "uk.ac.open.salsabeacons.Salsa";

  // This class cannot be instantiated
  private Salsa() {
  }

  /**
   * Region table contract
   */
  public static final class Region implements BaseColumns {
    /**
     * The table name offered by this provider
     */
    public static final String TABLE_NAME = "region";

    /*
     * URI definitions
     */

    /**
     * The scheme part for this provider's URI
     */
    private static final String SCHEME = "content://";

    /**
     * Path parts for the URIs
     */

    /**
     * Path part for the Region URI
     */
    private static final String PATH_REGION = "/region";

    /**
     * Path part for the Region ID URI
     */
    private static final String PATH_REGION_ID = "/region/";

    /**
     * 0-relative position of a region ID segment in the path part of a region ID URI
     */
    public static final int REGION_ID_PATH_POSITION = 1;

    /**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI =  Uri.parse(SCHEME + AUTHORITY + PATH_REGION);

    /**
     * The content URI base for a single region. Callers must
     * append a numeric region id to this Uri to retrieve a region
     */
    public static final Uri CONTENT_ID_URI_BASE
        = Uri.parse(SCHEME + AUTHORITY + PATH_REGION_ID);

    /**
     * The content URI match pattern for a single region, specified by its ID. Use this to match
     * incoming URIs or to construct an Intent.
     */
    public static final Uri CONTENT_ID_URI_PATTERN
        = Uri.parse(SCHEME + AUTHORITY + PATH_REGION_ID + "/#");

    /*
     * Column definitions
     */

    /**
     * Column name for the name of the region
     * <P>Type: TEXT</P>
     */
    public static final String COLUMN_NAME_NAME = "name";

    /**
     * Column name of the region description
     * <P>Type: TEXT</P>
     */
    public static final String COLUMN_NAME_DESCRIPTION = "description";

  }

  /**
   * Beacon table contract
   */
  public static final class Beacon implements BaseColumns {
    /**
     * The table name offered by this provider
     */
    public static final String TABLE_NAME = "beacon";

    /*
     * URI definitions
     */

    /**
     * The scheme part for this provider's URI
     */
    private static final String SCHEME = "content://";

    /**
     * Path parts for the URIs
     */

    /**
     * Path part for the Beacon URI
     */
    private static final String PATH_BEACON = "/beacon";

    /**
     * Path part for the Beacon ID URI
     */
    private static final String PATH_BEACON_ID = "/beacon/";

    /**
     * 0-relative position of a beacon ID segment in the path part of a beacon ID URI
     */
    public static final int BEACON_ID_PATH_POSITION = 1;

    /**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI =  Uri.parse(SCHEME + AUTHORITY + PATH_BEACON);

    /**
     * The content URI base for a single beacon. Callers must
     * append a numeric beacon id to this Uri to retrieve a beacon
     */
    public static final Uri CONTENT_ID_URI_BASE
        = Uri.parse(SCHEME + AUTHORITY + PATH_BEACON_ID);

    /**
     * The content URI match pattern for a single beacon, specified by its ID. Use this to match
     * incoming URIs or to construct an Intent.
     */
    public static final Uri CONTENT_ID_URI_PATTERN
        = Uri.parse(SCHEME + AUTHORITY + PATH_BEACON_ID + "/#");

    /*
     * Column definitions
     */

    /**
     * Column name for the region table foreign key
     * <P>Type: INTEGER (int from Region._ID)</P>
     */
    public static final String COLUMN_NAME_REGION_ID = "region_ID";

    /**
     * Column name for the name of the beacon
     * <P>Type: TEXT</P>
     */
    public static final String COLUMN_NAME_NAME = "name";

    /**
     * Column name of the beacon description
     * <P>Type: TEXT</P>
     */
    public static final String COLUMN_NAME_DESCRIPTION = "description";

    /**
     * Column name of the beacon URI
     * <P>Type: TEXT</P>
     */
    public static final String COLUMN_NAME_URI = "uri";

    /**
     * Column name of the beacon content
     * <P>Type: TEXT</P>
     */
    public static final String COLUMN_NAME_CONTENT = "content";

  }

  /**
   * LogBeacon table contract
   */
  public static final class LogBeacon implements BaseColumns {
    /**
     * The table name offered by this provider
     */
    public static final String TABLE_NAME = "log_beacon";

    /*
     * URI definitions
     */

    /**
     * The scheme part for this provider's URI
     */
    private static final String SCHEME = "content://";

    /**
     * Path parts for the URIs
     */

    /**
     * Path part for the LogBeacon URI
     */
    private static final String PATH_LOG_BEACON = "/log_beacon";

    /**
     * Path part for the LogBeacon ID URI
     */
    private static final String PATH_LOG_BEACON_ID = "/log_beacon/";

    /**
     * 0-relative position of a LogBeacon ID segment in the path part of a LogBeacon ID URI
     */
    public static final int BEACON_ID_PATH_POSITION = 1;

    /**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI =  Uri.parse(SCHEME + AUTHORITY + PATH_LOG_BEACON);

    /**
     * The content URI base for a single LogBeacon. Callers must
     * append a numeric beacon id to this Uri to retrieve a LogBeacon
     */
    public static final Uri CONTENT_ID_URI_BASE
        = Uri.parse(SCHEME + AUTHORITY + PATH_LOG_BEACON_ID);

    /**
     * The content URI match pattern for a single LogBeacon, specified by its ID. Use this to match
     * incoming URIs or to construct an Intent.
     */
    public static final Uri CONTENT_ID_URI_PATTERN
        = Uri.parse(SCHEME + AUTHORITY + PATH_LOG_BEACON_ID + "/#");

    /*
     * Column definitions
     */

    /**
     * Column name for the beacon table foreign key
     * <P>Type: INTEGER (int from Beacon._ID)</P>
     */
    public static final String COLUMN_NAME_BEACON_ID = "beacon_ID";

    /**
     * Column name for the first logging timestamp of the beacon
     * <P>Type: INTEGER (UTC timestamp)</P>
     */
    public static final String COLUMN_NAME_FIRST_SEEN = "first_seen";

    /**
     * Column name for the last logged timestamp of the beacon
     * <P>Type: INTEGER (UTC timestamp)</P>
     */
    public static final String COLUMN_NAME_LAST_SEEN = "last_seen";

    /**
     * Column name of a flag indicating whether the user has viewed the beacon content
     * <P>Type: INTEGER (Boolean)</P>
     */
    public static final String COLUMN_NAME_USER_VIEWED = "user_viewed";

  }
}
