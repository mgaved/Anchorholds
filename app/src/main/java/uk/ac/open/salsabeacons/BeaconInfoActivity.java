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

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.webkit.WebSettings;
import android.webkit.WebView;


public class BeaconInfoActivity extends Activity {
  private final String TAG = "BeaconInfoActivity";
  private final int sdkVersion = Build.VERSION.SDK_INT;

  private WebView mWebView;
  private WebAppInterface mWebAppInterface;
  private String mUrl;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Intent intent = getIntent();
    setTitle(Html.fromHtml(intent.getStringExtra("SalsaBeaconTitle")));
    setContentView(R.layout.activity_beacon_info);
    mUrl = intent.getData().toString();
    mWebView = (WebView) findViewById(R.id.webview);
    mWebView.loadUrl(mUrl);
    if (sdkVersion < Build.VERSION_CODES.LOLLIPOP) {
      WebSettings webSettings = mWebView.getSettings();
      webSettings.setJavaScriptEnabled(true);
      mWebAppInterface = new WebAppInterface(this);
      mWebView.addJavascriptInterface(mWebAppInterface, "Android");
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    mWebView.onResume();
  }

  @Override
  public void onPause() {
    super.onPause();
    if(mWebAppInterface != null) {
      mWebAppInterface.pauseAudio();
    }
    mWebView.reload();
    mWebView.onPause();
  }

}

