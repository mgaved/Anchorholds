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

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.webkit.JavascriptInterface;

import java.io.IOException;

public class WebAppInterface {
  private final String TAG = "WebAppInterface";
  Context mContext;
  MediaPlayer mMediaPlayer;


  /** Instantiate the interface and set the context */
  WebAppInterface(Context c) {
    mContext = c;
  }

  /** play audio from the web page */
  @JavascriptInterface
  public void playAudio(String uri) {
    Uri myUri = Uri.parse(uri);
    String filePath = uri.replace("file:///android_asset/", "");
    Log.d(TAG, uri+" "+filePath);
    mMediaPlayer = new MediaPlayer();
    mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    try {
      AssetFileDescriptor afd = mContext.getAssets().openFd(filePath);
      Log.d(TAG, afd.toString());
      mMediaPlayer.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
      //mMediaPlayer.setDataSource(mContext, myUri);
      mMediaPlayer.prepare();
      mMediaPlayer.start();
    } catch(IOException e) {
      Log.e(TAG, "Failed to play " + myUri.getLastPathSegment());
      Log.e(TAG, "message " + e.getMessage());
    }
  }

  /** pause audio from the web page */
  @JavascriptInterface
  public void pauseAudio() {
    if(mMediaPlayer != null) {
      mMediaPlayer.pause();
      mMediaPlayer.release();
      mMediaPlayer = null;
    }
  }

  /** returns the app version number */
  @JavascriptInterface
  public String getVersion() {
    PackageManager packageManager = mContext.getPackageManager();
    String versionName = "";
    try {
      PackageInfo packageInfo = packageManager.getPackageInfo(mContext.getPackageName(), 0);
      versionName = packageInfo.versionName;
    } catch (PackageManager.NameNotFoundException e) {
      versionName = "";
    }
    Log.d(TAG, versionName);
    return versionName;
  }
}
