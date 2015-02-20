package uk.ac.open.salsabeacons;

import android.content.Context;
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

}
