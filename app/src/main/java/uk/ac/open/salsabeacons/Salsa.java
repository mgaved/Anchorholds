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
  public static final String AUTHORITY = "uk.ac.open.salsabeacons.salsa";

  // This class cannot be instantiated
  private Salsa() {
  }

  /**
   * BeaconOccurrence table contract
   */
  public static final class BeaconOccurrence implements BaseColumns {
    /**
     * The table name offered by this provider
     */
    public static final String TABLE_NAME = "beacon_occurrence";

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
    private static final String PATH_BEACON_OCCURRENCE = "/beacon_occurrence";

    /**
     * Path part for the LogBeacon ID URI
     */
    private static final String PATH_BEACON_OCCURRENCE_ID = "/beacon_occurrence/";

    /**
     * 0-relative position of a LogBeacon ID segment in the path part of a LogBeacon ID URI
     */
    public static final int BEACON_OCCURRENCE_ID_PATH_POSITION = 1;

    /**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI =  Uri.parse(SCHEME + AUTHORITY + PATH_BEACON_OCCURRENCE);

    /**
     * The content URI base for a single LogBeacon. Callers must
     * append a numeric beacon id to this Uri to retrieve a LogBeacon
     */
    public static final Uri CONTENT_ID_URI_BASE
        = Uri.parse(SCHEME + AUTHORITY + PATH_BEACON_OCCURRENCE_ID);

    /**
     * The content URI match pattern for a single LogBeacon, specified by its ID. Use this to match
     * incoming URIs or to construct an Intent.
     */
    public static final Uri CONTENT_ID_URI_PATTERN
        = Uri.parse(SCHEME + AUTHORITY + PATH_BEACON_OCCURRENCE_ID + "/#");

    /*
         * MIME type definitions
         */

    /**
     * The MIME type of {@link #CONTENT_URI} providing a directory of beacon occurrences.
     */
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/uk.ac.open.salsabeacons.occurrence";

    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single
     * beacon occurrence.
     */
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/uk.ac.open.salsabeacons.occurrence";

    /*
     * Column definitions
     */

    /**
     * Column name for the beacon string name
     * <P>Type: TEXT</P>
     */
    public static final String COLUMN_NAME_BEACON_NAME = "beacon_name";

    /**
     * Column name for the first logging timestamp of the beacon
     * <P>Type: INTEGER (UTC timestamp)</P>
     */
    public static final String COLUMN_NAME_FIRST_LOGGED = "first_logged";

    /**
     * Column name for the last logged timestamp of the beacon
     * <P>Type: INTEGER (UTC timestamp)</P>
     */
    public static final String COLUMN_NAME_LAST_LOGGED = "last_logged";

    /**
     * The default sort order for this table
     */
    public static final String DEFAULT_SORT_ORDER = COLUMN_NAME_LAST_LOGGED + " DESC";

  }

  /**
   * BeaconViewed table contract
   */
  public static final class BeaconViewed implements BaseColumns {
    /**
     * The table name offered by this provider
     */
    public static final String TABLE_NAME = "beacon_viewed";

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
    private static final String PATH_BEACON_VIEWED = "/beacon_viewed";

    /**
     * Path part for the LogBeacon ID URI
     */
    private static final String PATH_BEACON_VIEWED_ID = "/beacon_viewed/";

    /**
     * 0-relative position of a LogBeacon ID segment in the path part of a LogBeacon ID URI
     */
    public static final int BEACON_VIEWED_ID_PATH_POSITION = 1;

    /**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI =  Uri.parse(SCHEME + AUTHORITY + PATH_BEACON_VIEWED);

    /**
     * The content URI base for a single LogBeacon. Callers must
     * append a numeric beacon id to this Uri to retrieve a LogBeacon
     */
    public static final Uri CONTENT_ID_URI_BASE
        = Uri.parse(SCHEME + AUTHORITY + PATH_BEACON_VIEWED_ID);

    /**
     * The content URI match pattern for a single LogBeacon, specified by its ID. Use this to match
     * incoming URIs or to construct an Intent.
     */
    public static final Uri CONTENT_ID_URI_PATTERN
        = Uri.parse(SCHEME + AUTHORITY + PATH_BEACON_VIEWED_ID + "/#");

    /*
         * MIME type definitions
         */

    /**
     * The MIME type of {@link #CONTENT_URI} providing a directory of beacon occurrences.
     */
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/uk.ac.open.salsabeacons.viewed";

    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single
     * beacon occurrence.
     */
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/uk.ac.open.salsabeacons.viewed";

    /*
     * Column definitions
     */

    /**
     * Column name for the beacon table foreign key
     * <P>Type: INTEGER (int from Beacon._ID)</P>
     */
    public static final String COLUMN_NAME_BEACON_OCCURRENCE_ID = "beacon_occurrence_id";

    /**
     * Column name for the timestamp of when the user views the beacons associated contents
     * <P>Type: INTEGER (UTC timestamp)</P>
     */
    public static final String COLUMN_NAME_TIMESTAMP = "timestamp";

    /**
     * The default sort order for this table
     */
    public static final String DEFAULT_SORT_ORDER = COLUMN_NAME_TIMESTAMP + " DESC";

  }
}
