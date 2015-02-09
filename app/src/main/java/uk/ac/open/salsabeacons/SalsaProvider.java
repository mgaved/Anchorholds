package uk.ac.open.salsabeacons;

import android.content.ContentProvider;
import android.content.Context;
import android.content.UriMatcher;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

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
  private static final int DATABASE_VERSION = 1;

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
     * NotePad class.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
      db.execSQL("CREATE TABLE " + Salsa.Region.TABLE_NAME + " ("
          + Salsa.Region._ID + " INTEGER PRIMARY KEY,"
          + Salsa.Region.COLUMN_NAME_NAME + " TEXT,"
          + Salsa.Region.COLUMN_NAME_DESCRIPTION + " TEXT"
          + ");");
      db.execSQL("CREATE TABLE " + Salsa.Beacon.TABLE_NAME + " ("
          + Salsa.Beacon._ID + " INTEGER PRIMARY KEY,"
          + Salsa.Beacon.COLUMN_NAME_REGION_ID + " INTEGER,"
          + Salsa.Beacon.COLUMN_NAME_NAME + " TEXT,"
          + Salsa.Beacon.COLUMN_NAME_DESCRIPTION + " TEXT,"
          + Salsa.Beacon.COLUMN_NAME_URI + " TEXT,"
          + Salsa.Beacon.COLUMN_NAME_CONTENT + " TEXT"
          + ");");
      db.execSQL("CREATE TABLE " + Salsa.LogBeacon.TABLE_NAME + " ("
          + Salsa.LogBeacon._ID + " INTEGER PRIMARY KEY,"
          + Salsa.LogBeacon.COLUMN_NAME_BEACON_ID + " INTEGER,"
          + Salsa.LogBeacon.COLUMN_NAME_FIRST_SEEN + " INTEGER,"
          + Salsa.LogBeacon.COLUMN_NAME_LAST_SEEN + " INTEGER,"
          + Salsa.LogBeacon.COLUMN_NAME_USER_VIEWED + " INTEGER"
          + ");");
    }

    private String insertVersion1Data() {
      String regionData = "INSERT INTO " + Salsa.Region.TABLE_NAME + " ("
          + Salsa.Region.COLUMN_NAME_NAME + ","
          + Salsa.Region.COLUMN_NAME_DESCRIPTION + ") VALUES ("
          +
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
          + newVersion + ", which will destroy all old data");

      // Kills the table and existing data
      db.execSQL("DROP TABLE IF EXISTS notes");

      // Recreates the database with a new version
      onCreate(db);
    }
  }

}
