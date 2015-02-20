package uk.ac.open.salsabeacons;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;
import android.util.Log;

/**
 * Created by rmg29 on 13/02/2015.
 */
public class SalsaProviderTest extends ProviderTestCase2<SalsaProvider> {

  private String mBeaconId;
  private long mNow;
  private MockContentResolver cr;

  /**
   * Constructor.
   *
   */
  public SalsaProviderTest() {
    super(SalsaProvider.class, Salsa.AUTHORITY);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    mBeaconId = "beacon_3_4";
    mNow = System.currentTimeMillis();
    cr = getMockContentResolver();
  }

  /*public void testPreConditions() {

  }*/

  public void testProviderOccurrence() {
    ContentValues values = new ContentValues();
    values.put(Salsa.BeaconOccurrence.COLUMN_NAME_BEACON_NAME, mBeaconId);
    values.put(Salsa.BeaconOccurrence.COLUMN_NAME_FIRST_LOGGED, mNow);
    values.put(Salsa.BeaconOccurrence.COLUMN_NAME_LAST_LOGGED, mNow);

    Uri dbUri = cr.insert(Salsa.BeaconOccurrence.CONTENT_URI, values);

    //assertNotNull("A successful beacon insert should return a valid Uri", dbUri);

    String[] occurenceProjection = {
        Salsa.BeaconOccurrence._ID,
        Salsa.BeaconOccurrence.COLUMN_NAME_BEACON_NAME,
        Salsa.BeaconOccurrence.COLUMN_NAME_FIRST_LOGGED,
        Salsa.BeaconOccurrence.COLUMN_NAME_LAST_LOGGED
    };

    Cursor occurrences = cr.query(
        Salsa.BeaconOccurrence.CONTENT_URI, occurenceProjection, null, null, null
    );

    assertTrue("The cursor should contain one entry", occurrences.moveToFirst());

    Integer dbUriId = Integer.valueOf(
        occurrences.getInt(occurrences.getColumnIndexOrThrow(Salsa.BeaconOccurrence._ID))
    );

    assertEquals(
        "The created Id should equal the Uri id",
        Integer.valueOf(dbUri.getLastPathSegment()),
        dbUriId
    );
    assertEquals(
        "The input beacon name should equal the output",
        mBeaconId,
        occurrences.getString(
            occurrences.getColumnIndexOrThrow(Salsa.BeaconOccurrence.COLUMN_NAME_BEACON_NAME)
        )
    );
    assertEquals(
        "The input first logged should equal the output",
        Long.valueOf(mNow),
        Long.valueOf(occurrences.getLong(
          occurrences.getColumnIndexOrThrow(Salsa.BeaconOccurrence.COLUMN_NAME_FIRST_LOGGED)
        ))
    );
    assertEquals(
        "The input last logged should equal the output",
        Long.valueOf(mNow),
        Long.valueOf(occurrences.getLong(
            occurrences.getColumnIndexOrThrow(Salsa.BeaconOccurrence.COLUMN_NAME_LAST_LOGGED)
        ))
    );

    occurrences.close();

    values = new ContentValues();
    Long updateValue = System.currentTimeMillis() + 10000;
    values.put(Salsa.BeaconOccurrence.COLUMN_NAME_LAST_LOGGED, updateValue);
    String[] where = {dbUriId.toString()};
    assertEquals("Expecting 1 update", 1, cr.update(Salsa.BeaconOccurrence.CONTENT_URI, values, Salsa.BeaconOccurrence._ID+"=?", where));

    String selection = Salsa.BeaconOccurrence.COLUMN_NAME_BEACON_NAME + " = ?";
    String[] selectionArgs = {mBeaconId};

    occurrences = cr.query(
        Salsa.BeaconOccurrence.CONTENT_URI, occurenceProjection, selection, selectionArgs, null
    );

    assertEquals("The cursor should contain one entry", 1, occurrences.getCount());

    occurrences.moveToFirst();

    assertEquals(
        "The input last logged should equal the output",
        updateValue,
        Long.valueOf(occurrences.getLong(
            occurrences.getColumnIndexOrThrow(Salsa.BeaconOccurrence.COLUMN_NAME_LAST_LOGGED)
        ))
    );

    occurrences.close();

  }

  public void testSalsaBeacon() {

  }
}
