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

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.util.HashMap;

/**
 * Created by rmg29 on 22/01/2015.
 */
public class SalsaProvider extends ContentProvider {
  // Used for debugging and logging
  private static final String TAG = "SalsaProvider";

  /**
   * The database that the provider uses as its underlying data store
   */
  private static final String DATABASE_NAME = "salsa.db";

  /**
   * The database version
   */
  private static final int DATABASE_VERSION = 2;

  /**
   * A projection map used to select columns from the database
   */
  private static HashMap<String, String> sOccurrenceProjectionMap;

  /**
   * A projection map used to select columns from the database
   */
  //private static HashMap<String, String> sViewProjectionMap;

  /**
   * A projection map used to select columns from the database
   */
  private static HashMap<String, String> sListProjectionMap;

  /*
     * Constants used by the Uri matcher to choose an action based on the pattern
     * of the incoming URI
     */
  // The incoming URI matches the Beacons URI pattern
  private static final int BEACON_OCCURRENCES = 1;

  // The incoming URI matches the Beacon ID URI pattern
  private static final int BEACON_OCCURRENCE_ID = 2;

  // The incoming URI matches the Beacon viewed URI pattern
  //private static final int BEACON_VIEWS = 3;

  // The incoming URI matches the Beacon viewed ID URI pattern
  //private static final int BEACON_VIEW_ID = 4;

  // The incoming URI matches the Beacon list URI pattern
  private static final int BEACON_OCCURRENCE_VIEWABLE_LIST = 5;

  // The incoming URI matches the Beacon list ID URI pattern
  //private static final int BEACON_LIST_ID = 6;

  /**
   * A UriMatcher instance
   */
  private static final UriMatcher sUriMatcher;

  // Handle to a new DatabaseHelper.
  private DatabaseHelper mOpenHelper;

  /**
   * A block that instantiates and sets static objects
   */
  static {

    /*
     * Creates and initializes the URI matcher
     */
    // Create a new instance
    sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);


    // Add a pattern that routes URIs terminated with "beacon_occurrence" to a BEACON_OCCURRENCES
    // operation
    sUriMatcher.addURI(Salsa.AUTHORITY, "beacon_occurrence", BEACON_OCCURRENCES);

    // Add a pattern that routes URIs terminated with "beacon_occurrence" plus an integer
    // to a beacon_occurrence ID operation
    sUriMatcher.addURI(Salsa.AUTHORITY, "beacon_occurrence/#", BEACON_OCCURRENCE_ID);


    // Add a pattern that routes URIs terminated with "beacon_list" to a beacon_list
    // operation
    sUriMatcher.addURI(Salsa.AUTHORITY, "beacon_occurrences_viewable_list", BEACON_OCCURRENCE_VIEWABLE_LIST);

    // Add a pattern that routes URIs terminated with "beacon_list" plus an integer
    // to a beacon_list ID operation
    //sUriMatcher.addURI(Salsa.AUTHORITY, "beacon_list/#", BEACON_LIST_ID);

    /*
     * Creates and initializes a projection map that returns all columns
     */

    // Creates a new projection map instance. The map returns a column name
    // given a string. The two are usually equal.
    sOccurrenceProjectionMap = new HashMap<String, String>();

    // Maps the string "_ID" to the column name "_ID"
    sOccurrenceProjectionMap.put(Salsa.BeaconOccurrence._ID, Salsa.BeaconOccurrence._ID);

    // Maps "beacon_name" to "beacon_name"
    sOccurrenceProjectionMap.put(Salsa.BeaconOccurrence.COLUMN_NAME_BEACON_NAME,
        Salsa.BeaconOccurrence.COLUMN_NAME_BEACON_NAME);

    // Maps "first_logged" to "first_logged"
    sOccurrenceProjectionMap.put(Salsa.BeaconOccurrence.COLUMN_NAME_FIRST_LOGGED,
        Salsa.BeaconOccurrence.COLUMN_NAME_FIRST_LOGGED);

    // Maps "last_logged" to "last_logged"
    sOccurrenceProjectionMap.put(Salsa.BeaconOccurrence.COLUMN_NAME_LAST_LOGGED,
        Salsa.BeaconOccurrence.COLUMN_NAME_LAST_LOGGED);

    // Maps "viewed" to "viewed"
    sOccurrenceProjectionMap.put(Salsa.BeaconOccurrence.COLUMN_NAME_VIEWED,
        Salsa.BeaconOccurrence.COLUMN_NAME_VIEWED);

    // Maps "deleted" to "deleted"
    sOccurrenceProjectionMap.put(Salsa.BeaconOccurrence.COLUMN_NAME_DELETED,
        Salsa.BeaconOccurrence.COLUMN_NAME_DELETED);

    // Maps "last_valid_proximity" to "last_valid_proximity"
    sOccurrenceProjectionMap.put(Salsa.BeaconOccurrence.COLUMN_NAME_LAST_VALID_PROXIMITY,
        Salsa.BeaconOccurrence.COLUMN_NAME_LAST_VALID_PROXIMITY);

    //sListProjectionMap = (HashMap) sOccurrenceProjectionMap.clone();

    sListProjectionMap = new HashMap<String, String>();

    // Maps the string "_ID" to the column name "_ID"
    sListProjectionMap.put(Salsa.BeaconOccurrence._ID, Salsa.BeaconOccurrence._ID);

    // Maps "beacon_name" to "beacon_name"
    sListProjectionMap.put(Salsa.BeaconOccurrence.COLUMN_NAME_BEACON_NAME,
        Salsa.BeaconOccurrence.COLUMN_NAME_BEACON_NAME);

    // Maps "first_logged" to "first_logged"
    sListProjectionMap.put(Salsa.BeaconOccurrence.COLUMN_NAME_FIRST_LOGGED,
        Salsa.BeaconOccurrence.COLUMN_NAME_FIRST_LOGGED);

    // Maps "last_logged" to "last_logged"
    sListProjectionMap.put(Salsa.BeaconOccurrence.COLUMN_NAME_LAST_LOGGED,
        Salsa.BeaconOccurrence.COLUMN_NAME_LAST_LOGGED);

    // Maps "viewed" to "viewed"
    sListProjectionMap.put(Salsa.BeaconOccurrence.COLUMN_NAME_VIEWED,
        Salsa.BeaconOccurrence.COLUMN_NAME_VIEWED);

    // Maps "deleted" to "deleted"
    sListProjectionMap.put(Salsa.BeaconOccurrence.COLUMN_NAME_DELETED,
        Salsa.BeaconOccurrence.COLUMN_NAME_DELETED);

    // Maps "last_valid_proximity" to "last_valid_proximity"
    sListProjectionMap.put(Salsa.BeaconOccurrence.COLUMN_NAME_LAST_VALID_PROXIMITY,
        Salsa.BeaconOccurrence.COLUMN_NAME_LAST_VALID_PROXIMITY);

    sListProjectionMap.put(Salsa.BeaconOccurrence.AGG_COLUMN_NAME_OCCURRENCE_COUNT,
        Salsa.BeaconOccurrence.AGG_COLUMN_NAME_OCCURRENCE_COUNT);
    // Creates a new projection map instance. The map returns a column name
    // given a string. The two are usually equal.
    /*sListProjectionMap = new HashMap<String, String>();

    // Maps the string "_ID" to the column name "beacon_list._ID"
    sListProjectionMap.put(Salsa.BeaconList._ID, Salsa.BeaconList.TABLE_NAME + "." + Salsa.BeaconList._ID);

    // Maps "occurrence_id" to "occurrence_id"
    sListProjectionMap.put(Salsa.BeaconList.COLUMN_NAME_BEACON_OCCURRENCE_ID,
        Salsa.BeaconList.COLUMN_NAME_BEACON_OCCURRENCE_ID);

    // Maps "beacon_name" to "beacon_name"
    sListProjectionMap.put(Salsa.BeaconOccurrence.COLUMN_NAME_BEACON_NAME,
        Salsa.BeaconOccurrence.COLUMN_NAME_BEACON_NAME);

    // Maps "first_logged" to "first_logged"
    sListProjectionMap.put(Salsa.BeaconOccurrence.COLUMN_NAME_FIRST_LOGGED,
        Salsa.BeaconOccurrence.COLUMN_NAME_FIRST_LOGGED);

    // Maps "viewed_timestamp" to "viewed_timestamp"
    sListProjectionMap.put(Salsa.BeaconOccurrence.COLUMN_NAME_VIEWED,
        Salsa.BeaconOccurrence.COLUMN_NAME_VIEWED);*/
  }

  /**
   *
   * This class helps open, create, and upgrade the database file. Set to package visibility
   * for testing purposes.
   */
  static class DatabaseHelper extends SQLiteOpenHelper {

    DatabaseHelper(Context context) {
      // calls the super constructor, requesting the default cursor factory.
      super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     *
     * Creates the underlying database with table name and column names taken from the
     * Salsa class.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {

      // Create the beacon_occurrence table
      db.execSQL("CREATE TABLE " + Salsa.BeaconOccurrence.TABLE_NAME + " ("
          + Salsa.BeaconOccurrence._ID + " INTEGER PRIMARY KEY,"
          + Salsa.BeaconOccurrence.COLUMN_NAME_BEACON_NAME + " TEXT,"
          + Salsa.BeaconOccurrence.COLUMN_NAME_FIRST_LOGGED + " INTEGER,"
          + Salsa.BeaconOccurrence.COLUMN_NAME_LAST_LOGGED + " INTEGER, "
          + Salsa.BeaconOccurrence.COLUMN_NAME_VIEWED + " INTEGER, "
          + Salsa.BeaconOccurrence.COLUMN_NAME_DELETED + " INTEGER, "
          + Salsa.BeaconOccurrence.COLUMN_NAME_LAST_VALID_PROXIMITY + " REAL "
          + ");"
      );

      // Create the beacon_list table
      /*db.execSQL("CREATE TABLE " + Salsa.BeaconList.TABLE_NAME + " ("
          + Salsa.BeaconList._ID + " INTEGER PRIMARY KEY,"
          + Salsa.BeaconList.COLUMN_NAME_BEACON_OCCURRENCE_ID + " INTEGER"
          + ");"
      );*/
    }

    /**
     *
     * Demonstrates that the provider must consider what happens when the
     * underlying datastore is changed. In this sample, the database is upgraded the database
     * by destroying the existing data.
     * A real application should upgrade the database in place.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

      // Logs that the database is being upgraded
      Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
          + newVersion);
      switch(oldVersion) {

        case 1:
          upgradeTo2(db);
        //case 2:
          //upgradeTo3(db);
          break;

        default:
          throw new IllegalStateException(
              "onUpgrade() with unknown newVersion" + newVersion);
      }
    }

    private void upgradeTo2(SQLiteDatabase db) {
      // Add viewed and deleted columns to beacon_occurrence table
      db.execSQL("ALTER TABLE " + Salsa.BeaconOccurrence.TABLE_NAME
              + " ADD COLUMN " + Salsa.BeaconOccurrence.COLUMN_NAME_VIEWED + " INTEGER;"
      );
      db.execSQL("ALTER TABLE " + Salsa.BeaconOccurrence.TABLE_NAME
              + " ADD COLUMN " + Salsa.BeaconOccurrence.COLUMN_NAME_DELETED + " INTEGER;"
      );
      db.execSQL("ALTER TABLE " + Salsa.BeaconOccurrence.TABLE_NAME
              + " ADD COLUMN " + Salsa.BeaconOccurrence.COLUMN_NAME_LAST_VALID_PROXIMITY + " REAL;"
      );
      // remove beacon_viewed table - using string because defining has been removed
      db.execSQL("DROP TABLE IF EXISTS beacon_viewed;");
    }
  }

  /**
   *
   * Initializes the provider by creating a new DatabaseHelper. onCreate() is called
   * automatically when Android creates the provider in response to a resolver request from a
   * client.
   */
  @Override
  public boolean onCreate() {

    // Creates a new helper object. Note that the database itself isn't opened until
    // something tries to access it, and it's only created if it doesn't already exist.
    mOpenHelper = new DatabaseHelper(getContext());

    // Assumes that any failures will be reported by a thrown exception.
    return true;
  }

  /**
   * This is called when a client calls {@link android.content.ContentResolver#getType(Uri)}.
   * Returns the MIME data type of the URI given as a parameter.
   *
   * @param uri The URI whose MIME type is desired.
   * @return The MIME type of the URI.
   * @throws IllegalArgumentException if the incoming URI pattern is invalid.
   */
  @Override
  public String getType(Uri uri) {

    /**
     * Chooses the MIME type based on the incoming URI pattern
     */
    switch (sUriMatcher.match(uri)) {

      // If the pattern is for beacon occurrences, returns the general content type.
      case BEACON_OCCURRENCES:
      case BEACON_OCCURRENCE_VIEWABLE_LIST:
        return Salsa.BeaconOccurrence.CONTENT_TYPE;

      // If the pattern is for beacon occurrence IDs, returns the note ID content type.
      case BEACON_OCCURRENCE_ID:
        return Salsa.BeaconOccurrence.CONTENT_ITEM_TYPE;

      //case BEACON_LIST:
      //  return Salsa.BeaconList.CONTENT_TYPE;

      //case BEACON_LIST_ID:
      //  return Salsa.BeaconList.CONTENT_ITEM_TYPE;

      // If the URI pattern doesn't match any permitted patterns, throws an exception.
      default:
        throw new IllegalArgumentException("Unknown URI " + uri);
    }
  }

  /**
   * This method is called when a client calls
   * {@link android.content.ContentResolver#query(Uri, String[], String, String[], String)}.
   * Queries the database and returns a cursor containing the results.
   *
   * @return A cursor containing the results of the query. The cursor exists but is empty if
   * the query returns no results or an exception occurs.
   * @throws IllegalArgumentException if the incoming URI pattern is invalid.
   */
  @Override
  public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                      String sortOrder) {

    // Opens the database object in "read" mode, since no writes need to be done.
    SQLiteDatabase db = mOpenHelper.getReadableDatabase();

    // Constructs a new query builder
    SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

    String groupBy = null;
    String having = null;

    /**
     * Choose the projection and adjust the "where" clause based on URI pattern-matching.
     */
    int uriMatch = sUriMatcher.match(uri);
    switch (uriMatch) {

      // If the incoming URI is for beacon occurrences, chooses the occurrence projection
      case BEACON_OCCURRENCES:
        qb.setTables(Salsa.BeaconOccurrence.TABLE_NAME);
        qb.setProjectionMap(sOccurrenceProjectionMap);
        break;

      /* If the incoming URI is for a single beacon occurrence identified by its ID, chooses
       * the occurrence projection, and appends "_ID = <beacon_occurrence_ID>" to the where
       * clause, so that it selects that single beacon occurrence
       */
      case BEACON_OCCURRENCE_ID:
        qb.setTables(Salsa.BeaconOccurrence.TABLE_NAME);
        qb.setProjectionMap(sOccurrenceProjectionMap);
        qb.appendWhere(
            Salsa.BeaconOccurrence._ID +    // the name of the ID column
                "=" +
                // the position of the beacon occurrence ID itself in the incoming URI
                uri.getPathSegments().get(Salsa.BeaconOccurrence.BEACON_OCCURRENCE_ID_PATH_POSITION));
        break;

      case BEACON_OCCURRENCE_VIEWABLE_LIST:
        String subQuery = SQLiteQueryBuilder.buildQueryString(
            false,
            Salsa.BeaconOccurrence.TABLE_NAME,
            new String[] {Salsa.BeaconOccurrence._ID,
                Salsa.BeaconOccurrence.COLUMN_NAME_BEACON_NAME,
                Salsa.BeaconOccurrence.COLUMN_NAME_FIRST_LOGGED,
                Salsa.BeaconOccurrence.COLUMN_NAME_LAST_LOGGED,
                Salsa.BeaconOccurrence.COLUMN_NAME_VIEWED,
                Salsa.BeaconOccurrence.COLUMN_NAME_DELETED,
                Salsa.BeaconOccurrence.COLUMN_NAME_LAST_VALID_PROXIMITY,
                "COUNT(_id) AS occurrences"},
            null,
            Salsa.BeaconOccurrence.COLUMN_NAME_BEACON_NAME,
            "MAX(" + Salsa.BeaconOccurrence.COLUMN_NAME_LAST_LOGGED + ")",
            null,
            null
        );
        qb.setTables("(" + subQuery + ")");
        qb.setProjectionMap(sListProjectionMap);
        qb.appendWhere(Salsa.BeaconOccurrence.COLUMN_NAME_DELETED + " = 0");
        //groupBy = Salsa.BeaconOccurrence.COLUMN_NAME_BEACON_NAME;
        //having = "MAX(" + Salsa.BeaconOccurrence.COLUMN_NAME_LAST_LOGGED + ")";
        break;

      /*case BEACON_LIST_ID:
        qb.setTables(Salsa.BeaconList.TABLE_NAME + "," + Salsa.BeaconOccurrence.TABLE_NAME);
        qb.setProjectionMap(sListProjectionMap);
        qb.appendWhere(
            Salsa.BeaconList.COLUMN_NAME_BEACON_OCCURRENCE_ID
            + "="
            + Salsa.BeaconOccurrence.TABLE_NAME + "." + Salsa.BeaconOccurrence._ID
            + " AND "
            + Salsa.BeaconList._ID    // the name of the ID column
            + "="
            // the position of the beacon view ID itself in the incoming URI
            +   uri.getPathSegments().get(Salsa.BeaconList.BEACON_LIST_ID_PATH_POSITION)
        );
        break;*/

      default:
        // If the URI doesn't match any of the known patterns, throw an exception.
        throw new IllegalArgumentException("Unknown URI " + uri);
    }


    String orderBy;
    if (TextUtils.isEmpty(sortOrder)) {
      switch (uriMatch) {
        case BEACON_OCCURRENCES:
        case BEACON_OCCURRENCE_ID:
        case BEACON_OCCURRENCE_VIEWABLE_LIST:
          orderBy = Salsa.BeaconOccurrence.DEFAULT_SORT_ORDER;
          break;
        /*case BEACON_LIST:
        case BEACON_LIST_ID:
          orderBy = Salsa.BeaconList.DEFAULT_SORT_ORDER;
          break;*/
        default:
          orderBy = sortOrder;
      }
    } else {
      // otherwise, uses the incoming sort order
      orderBy = sortOrder;
    }

       /*
        * Performs the query. If no problems occur trying to read the database, then a Cursor
        * object is returned; otherwise, the cursor variable contains null. If no records were
        * selected, then the Cursor object is empty, and Cursor.getCount() returns 0.
        */
    Log.d(TAG, "about to run query: " + qb.buildQuery(projection,  selection, null, null, orderBy, null));
    Cursor c = qb.query(
        db,            // The database to query
        projection,    // The columns to return from the query
        selection,     // The columns for the where clause
        selectionArgs, // The values for the where clause
        null,       // group the rows for the list
        null,        // filtering row groups for list
        orderBy        // The sort order
    );

    // Tells the Cursor what URI to watch, so it knows when its source data changes
    c.setNotificationUri(getContext().getContentResolver(), uri);
    return c;
  }

  /**
   * This is called when a client calls
   * {@link android.content.ContentResolver#insert(Uri, ContentValues)}.
   * Inserts a new row into the database. This method sets up default values for any
   * columns that are not included in the incoming map.
   * If rows were inserted, then listeners are notified of the change.
   * @return The row ID of the inserted row.
   * @throws SQLException if the insertion fails.
   */
  @Override
  public Uri insert(Uri uri, ContentValues initialValues) {
    // Validates the incoming URI.
    // Only the full provider URI is allowed for inserts.
    // Hands to specific method for easier reading!
    switch (sUriMatcher.match(uri)) {
      case BEACON_OCCURRENCES:
        return insertBeaconOccurrence(uri, initialValues);

      default:
        throw new IllegalArgumentException("Unknown URI " + uri);
    }
  }

  private Uri insertBeaconOccurrence(Uri uri, ContentValues initialValues) {
    // A map to hold the new record's values.
    ContentValues values;

    // If the incoming values map is not null, uses it for the new values.
    if (initialValues != null) {
      values = new ContentValues(initialValues);

    } else {
      // Otherwise, create a new value map
      values = new ContentValues();
    }

    // Gets the current system time in milliseconds
    Long now = System.currentTimeMillis();

    // If the values map doesn't contain the first seen date, sets the value to the current time.
    if (!values.containsKey(Salsa.BeaconOccurrence.COLUMN_NAME_FIRST_LOGGED)) {
      values.put(Salsa.BeaconOccurrence.COLUMN_NAME_FIRST_LOGGED, now);
    }

    // If the values map doesn't contain the last seen date, sets the value to the current
    // time.
    if (!values.containsKey(Salsa.BeaconOccurrence.COLUMN_NAME_LAST_LOGGED)) {
      values.put(Salsa.BeaconOccurrence.COLUMN_NAME_LAST_LOGGED, now);
    }

    // If the values map doesn't contain the viewed timestamp, sets the value to 0.
    if (!values.containsKey(Salsa.BeaconOccurrence.COLUMN_NAME_VIEWED)) {
      values.put(Salsa.BeaconOccurrence.COLUMN_NAME_VIEWED, 0);
    }

    // If the values map doesn't contain the deleted flag, sets the value to 0.
    if (!values.containsKey(Salsa.BeaconOccurrence.COLUMN_NAME_DELETED)) {
      values.put(Salsa.BeaconOccurrence.COLUMN_NAME_DELETED, 0);
    }

    // Opens the database object in "write" mode.
    SQLiteDatabase db = mOpenHelper.getWritableDatabase();

    // Performs the insert and returns the ID of the new BeaconOccurrence.
    long rowId = db.insert(
        Salsa.BeaconOccurrence.TABLE_NAME, // The table to insert into.
        null,                              //
        values                             // A map of column names, and the values to insert
                                           // into the columns.
    );

    // If the insert succeeded, the row ID exists.
    if (rowId > 0) {
      // Creates a URI with the note ID pattern and the new row ID appended to it.
      Uri beaconOccurrenceUri = ContentUris.withAppendedId(
          Salsa.BeaconOccurrence.CONTENT_ID_URI_BASE, rowId
      );

      // Notifies observers registered against this provider that the data changed.
      getContext().getContentResolver().notifyChange(beaconOccurrenceUri, null);
      return beaconOccurrenceUri;
    }

    // If the insert didn't succeed, then the rowID is <= 0. Throws an exception.
    throw new SQLException("Failed to insert row into " + uri);
  }

  /*private Uri insertBeaconList(Uri uri, ContentValues value) {

    if (value == null || !value.containsKey(Salsa.BeaconList.COLUMN_NAME_BEACON_OCCURRENCE_ID)) {
      throw new SQLException("Failed to insert row into " + uri);
    }

    // Opens the database object in "write" mode.
    SQLiteDatabase db = mOpenHelper.getWritableDatabase();

    // Performs the insert and returns the ID of the new BeaconOccurrence.
    long rowId = db.insert(
        Salsa.BeaconList.TABLE_NAME, // The table to insert into.
        null,                        //
        value                        // A map of column name, and the value to insert into the columns.
    );

    // If the insert succeeded, the row ID exists.
    if (rowId > 0) {
      // Creates a URI with the note ID pattern and the new row ID appended to it.
      Uri beaconListUri = ContentUris.withAppendedId(
          Salsa.BeaconList.CONTENT_ID_URI_BASE, rowId
      );

      // Notifies observers registered against this provider that the data changed.
      getContext().getContentResolver().notifyChange(beaconListUri, null);
      return beaconListUri;
    }

    // If the insert didn't succeed, then the rowID is <= 0. Throws an exception.
    throw new SQLException("Failed to insert row into " + uri);
  }*/
  /**
   * This is called when a client calls
   * {@link android.content.ContentResolver#delete(Uri, String, String[])}.
   * Deletes records from the database. If the incoming URI matches the note ID URI pattern,
   * this method deletes the one record specified by the ID in the URI. Otherwise, it deletes a
   * a set of records. The record or records must also match the input selection criteria
   * specified by where and whereArgs.
   *
   * If rows were deleted, then listeners are notified of the change.
   * @return If a "where" clause is used, the number of rows affected is returned, otherwise
   * 0 is returned. To delete all rows and get a row count, use "1" as the where clause.
   * @throws IllegalArgumentException if the incoming URI pattern is invalid.
   */
  @Override
  public int delete(Uri uri, String where, String[] whereArgs) {

    // Opens the database object in "write" mode.
    SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    String finalWhere;

    int count;

    // Does the delete based on the incoming URI pattern.
    switch (sUriMatcher.match(uri)) {

      // If the incoming pattern matches the general pattern for beacon_occurrence, does a delete
      // based on the incoming "where" columns and arguments.
      case BEACON_OCCURRENCES:
        count = db.delete(
            Salsa.BeaconOccurrence.TABLE_NAME,  // The database table name
            where,                              // The incoming where clause column names
            whereArgs                           // The incoming where clause values
        );
        break;

      // If the incoming URI matches a single beacon_occurrence ID, does the delete based on the
      // incoming data, but modifies the where clause to restrict it to the
      // particular beacon_occurrence ID.
      case BEACON_OCCURRENCE_ID:
                /*
                 * Starts a final WHERE clause by restricting it to the
                 * desired note ID.
                 */
        finalWhere =
            Salsa.BeaconOccurrence._ID +                         // The ID column name
                " = " +                                          // test for equality
                uri.getPathSegments().                           // the incoming note ID
                    get(Salsa.BeaconOccurrence.BEACON_OCCURRENCE_ID_PATH_POSITION)
        ;

        // If there were additional selection criteria, append them to the final
        // WHERE clause
        if (where != null) {
          finalWhere = finalWhere + " AND " + where;
        }

        // Performs the delete.
        count = db.delete(
            Salsa.BeaconOccurrence.TABLE_NAME,  // The database table name.
            finalWhere,                         // The final WHERE clause
            whereArgs                           // The incoming where clause values.
        );
        break;

      // If the incoming pattern matches the general pattern for beacon_list, does a delete
      // based on the incoming "where" columns and arguments.
      /*case BEACON_LIST:
        count = db.delete(
            Salsa.BeaconList.TABLE_NAME,  // The database table name
            where,                              // The incoming where clause column names
            whereArgs                           // The incoming where clause values
        );
        break;

      // If the incoming URI matches a single beacon_list ID, does the delete based on the
      // incoming data, but modifies the where clause to restrict it to the
      // particular beacon_list ID.
      case BEACON_LIST_ID:
        finalWhere =
            Salsa.BeaconList._ID +                         // The ID column name
                " = " +                                    // test for equality
                uri.getPathSegments().                     // the incoming note ID
                    get(Salsa.BeaconList.BEACON_LIST_ID_PATH_POSITION)
        ;

        // If there were additional selection criteria, append them to the final
        // WHERE clause
        if (where != null) {
          finalWhere = finalWhere + " AND " + where;
        }

        // Performs the delete.
        count = db.delete(
            Salsa.BeaconList.TABLE_NAME,  // The database table name.
            finalWhere,                   // The final WHERE clause
            whereArgs                     // The incoming where clause values.
        );
        break;*/

      // If the incoming pattern is invalid, throws an exception.
      default:
        throw new IllegalArgumentException("Unknown URI " + uri);
    }

        /*Gets a handle to the content resolver object for the current context, and notifies it
         * that the incoming URI changed. The object passes this along to the resolver framework,
         * and observers that have registered themselves for the provider are notified.
         */
    getContext().getContentResolver().notifyChange(uri, null);

    // Returns the number of rows deleted.
    return count;
  }

  /**
   * This is called when a client calls
   * {@link android.content.ContentResolver#update(Uri,ContentValues,String,String[])}
   * Updates records in the database. The column names specified by the keys in the values map
   * are updated with new data specified by the values in the map. If the incoming URI matches the
   * beacon occurence ID URI pattern, then the method updates the one record specified by the ID
   * in the URI;
   * otherwise, it updates a set of records. The record or records must match the input
   * selection criteria specified by where and whereArgs.
   * If rows were updated, then listeners are notified of the change.
   *
   * @param uri The URI pattern to match and update.
   * @param values A map of column names (keys) and new values (values).
   * @param where An SQL "WHERE" clause that selects records based on their column values. If this
   * is null, then all records that match the URI pattern are selected.
   * @param whereArgs An array of selection criteria. If the "where" param contains value
   * placeholders ("?"), then each placeholder is replaced by the corresponding element in the
   * array.
   * @return The number of rows updated.
   * @throws IllegalArgumentException if the incoming URI pattern is invalid.
   */
  @Override
  public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {

    // Opens the database object in "write" mode.
    SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    int count;
    String finalWhere;

    // Does the update based on the incoming URI pattern
    switch (sUriMatcher.match(uri)) {

      // If the incoming URI matches the general beacon_occurrence pattern, does the update based on
      // the incoming data.
      case BEACON_OCCURRENCES:
        // Does the update and returns the number of rows updated.
        count = db.update(
            Salsa.BeaconOccurrence.TABLE_NAME, // The database table name.
            values,                            // A map of column names and new values to use.
            where,                             // The where clause column names.
            whereArgs                          // The where clause column values to select on.
        );
        break;

      // If the incoming URI matches a single beacon_occurrence ID, does the update based on the incoming
      // data, but modifies the where clause to restrict it to the particular beacon_occurrence ID.
      case BEACON_OCCURRENCE_ID:
        /*
         * Starts creating the final WHERE clause by restricting it to the incoming
         * beacon occurrence ID.
         */
        finalWhere =
            Salsa.BeaconOccurrence._ID +                         // The ID column name
                " = " +                                          // test for equality
                uri.getPathSegments().                           // the incoming note ID
                    get(Salsa.BeaconOccurrence.BEACON_OCCURRENCE_ID_PATH_POSITION)
        ;

        // If there were additional selection criteria, append them to the final WHERE
        // clause
        if (where !=null) {
          finalWhere = finalWhere + " AND " + where;
        }


        // Does the update and returns the number of rows updated.
        count = db.update(
            Salsa.BeaconOccurrence.TABLE_NAME, // The database table name.
            values,                            // A map of column names and new values to use.
            finalWhere,                        // The final WHERE clause to use
                                               // placeholders for whereArgs
            whereArgs                          // The where clause column values to select on, or
                                               // null if the values are in the where argument.
        );
        break;

      // If the incoming URI matches the general beacon_occurrence pattern, does the update based on
      // the incoming data.
      /*case BEACON_LIST:
        // Does the update and returns the number of rows updated.
        count = db.update(
            Salsa.BeaconList.TABLE_NAME, // The database table name.
            values,                      // A map of column names and new values to use.
            where,                       // The where clause column names.
            whereArgs                    // The where clause column values to select on.
        );
        break;

      // If the incoming URI matches a single beacon_occurrence ID, does the update based on the incoming
      // data, but modifies the where clause to restrict it to the particular beacon_occurrence ID.
      case BEACON_LIST_ID:
        finalWhere =
            Salsa.BeaconList._ID +                         // The ID column name
                " = " +                                    // test for equality
                uri.getPathSegments().                     // the incoming note ID
                    get(Salsa.BeaconList.BEACON_LIST_ID_PATH_POSITION)
        ;

        // If there were additional selection criteria, append them to the final WHERE
        // clause
        if (where !=null) {
          finalWhere = finalWhere + " AND " + where;
        }
        // Does the update and returns the number of rows updated.
        count = db.update(
            Salsa.BeaconList.TABLE_NAME, // The database table name.
            values,                      // A map of column names and new values to use.
            finalWhere,                  // The final WHERE clause to use
            // placeholders for whereArgs
            whereArgs                    // The where clause column values to select on, or
            // null if the values are in the where argument.
        );
        break;*/

      // If the incoming pattern is invalid, throws an exception.
      default:
        throw new IllegalArgumentException("Unknown URI " + uri);
    }

        /*Gets a handle to the content resolver object for the current context, and notifies it
         * that the incoming URI changed. The object passes this along to the resolver framework,
         * and observers that have registered themselves for the provider are notified.
         */
    getContext().getContentResolver().notifyChange(uri, null);

    // Returns the number of rows updated.
    return count;
  }

}
