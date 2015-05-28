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
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Html;


public class ContentWebViewActivity extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Intent intent = getIntent();
    String title = intent.getStringExtra("title");
    String contentUrl = intent.getData().toString();
    setTitle(Html.fromHtml(title));
    setContentView(R.layout.activity_content_web_view);
    if (savedInstanceState == null) {
      getFragmentManager().beginTransaction()
          .add(R.id.container, ContentWebViewFragment.newInstance(contentUrl))
          .commit();
    }
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
      finish();
    }
  }

  @Override
  public void onBackPressed() {
    ContentWebViewFragment contentFragment = (ContentWebViewFragment) getFragmentManager()
        .findFragmentById(R.id.container);
    if(contentFragment!= null && contentFragment.goBack()) {
      return;
    }
    // Otherwise defer to system default behavior.
    super.onBackPressed();
  }
}
