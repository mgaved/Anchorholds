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

    private static final String PATH_BEACON_OCCURRENCES_VIEWABLE_LIST_URI = "/beacon_occurrences_viewable_list";

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

    public static final Uri CONTENT_VIEWABLE_LIST_URI  = Uri.parse(SCHEME + AUTHORITY + PATH_BEACON_OCCURRENCES_VIEWABLE_LIST_URI);

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
     * Column name for the user viewed timestamp of the beacon
     * <P>Type: INTEGER (UTC timestamp)</P>
     */
    public static final String COLUMN_NAME_VIEWED = "viewed";

    /**
     * Column name for the user deleted from list timestamp of the beacon
     * <P>Type: INTEGER (UTC timestamp)</P>
     */
    public static final String COLUMN_NAME_DELETED = "deleted";

    /**
     * Column name for the user deleted from list timestamp of the beacon
     * <P>Type: INTEGER (UTC timestamp)</P>
     */
    public static final String COLUMN_NAME_LAST_VALID_PROXIMITY = "last_valid_proximity";

    /**
     * Column name for the aggregated count of occurrences fetched for the list view
     * <P>Type: INTEGER</P>
     */
    public static final String AGG_COLUMN_NAME_OCCURRENCE_COUNT = "occurrences";

    /**
     * The default sort order for this table
     */
    public static final String DEFAULT_SORT_ORDER = COLUMN_NAME_LAST_LOGGED + " DESC";

  }
}
