package uk.ac.open.salsabeacons;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


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

