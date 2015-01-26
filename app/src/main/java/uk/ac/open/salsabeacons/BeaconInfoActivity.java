package uk.ac.open.salsabeacons;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


public class BeaconInfoActivity extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Intent intent = getIntent();
    SalsaBeacon beacon = new SalsaBeacon(intent.getStringExtra(BeaconReferenceApplication.SALSA_BEACON_ID));
    setContentView(R.layout.activity_beacon_info);
    TextView mText = (TextView) findViewById(R.id.beaconInfo);
    mText.setText(beacon.getHtml());
    mText.setMovementMethod(LinkMovementMethod.getInstance());
    //ActionBar actionBar = getActionBar();
    //actionBar.setDisplayHomeAsUpEnabled(true);
  }

}
