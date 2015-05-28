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

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewFragment;



public class ContentWebViewFragment extends WebViewFragment {

  private static final String ARG_CONTENT_URL = "content_url";

  private String mContentUrl;
  private WebAppInterface mWebAppInterface;

  /**
   * Use this factory method to create a new instance of
   * this fragment using the provided parameters.
   *
   * @param contentUrl url to find the content to load.
   * @return A new instance of fragment contentWebViewFragment.
   */
  public static ContentWebViewFragment newInstance(String contentUrl) {
    ContentWebViewFragment fragment = new ContentWebViewFragment();
    Bundle args = new Bundle();
    args.putString(ARG_CONTENT_URL, contentUrl);
    fragment.setArguments(args);
    return fragment;
  }

  public ContentWebViewFragment() {
    // Required empty public constructor
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
      mContentUrl = getArguments().getString(ARG_CONTENT_URL);
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = super.onCreateView(inflater, container, savedInstanceState);
    updateContent(null);
    return view;
    // Inflate the layout for this fragment
    //return inflater.inflate(R.layout.fragment_content_web_view, container, false);
  }

  public void updateContent(String newUrl) {
    if(newUrl != null) {
      mContentUrl = newUrl;
    }
    WebView view = getWebView();
    view.loadUrl(mContentUrl);
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
      WebSettings webSettings = view.getSettings();
      webSettings.setJavaScriptEnabled(true);
      mWebAppInterface = new WebAppInterface(view.getContext());
      view.addJavascriptInterface(mWebAppInterface, "Android");
    }
  }

  public boolean goBack() {
    WebView view = getWebView();

    if (view.canGoBack()) {
      view.goBack();
      return true;
    }
    return false;
  }

}
