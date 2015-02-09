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
import android.webkit.WebView;
import android.widget.TextView;


public class BeaconInfoActivity extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Intent intent = getIntent();
    SalsaBeacon beacon = new SalsaBeacon(intent.getStringExtra(BeaconReferenceApplication.SALSA_BEACON_ID));
    setTitle(getResources().getString(getResources().getIdentifier(beacon.getId(), "string", "uk.ac.open.salsabeacons")));
    setContentView(R.layout.activity_beacon_info);
    WebView mWebView = (WebView) findViewById(R.id.webview);
    mWebView.loadUrl(beacon.getUri().toString());
    //TextView mText = (TextView) findViewById(R.id.beaconInfo);
    //mText.setText(beacon.getHtml());
    //mText.setMovementMethod(LinkMovementMethod.getInstance());
  }

}
