package com.cordova.m2w.plugin.rtmp;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import com.github.faucamp.simplertmp.RtmpHandler;
import java.io.IOException;
import java.net.SocketException;
import net.ossrs.yasea.SrsEncodeHandler;
import net.ossrs.yasea.SrsPublisher;
import net.ossrs.yasea.SrsRecordHandler;
import org.apache.cordova.CallbackContext;

public class RtmpActivity
  extends Activity
  implements
    RtmpHandler.RtmpListener,
    SrsRecordHandler.SrsRecordListener,
    SrsEncodeHandler.SrsEncodeListener {
  public static final int SAMPLE_AUDIO_RATE_IN_HZ = 44100;
  public static boolean running = false;
  String TAG = "RtmpActivity";

  public String streamId;
  public Boolean isPublishing = true;
  private SrsPublisher mPublisher;
  private CallbackContext callbackContext;

  // Creates instance of the manager.

  RtmpActivity() {
    mPublisher = new SrsPublisher();
    mPublisher.setSendAudioOnly(true);
    mPublisher.setEncodeHandler(new SrsEncodeHandler(this));
    mPublisher.setRtmpHandler(new RtmpHandler(this));
    mPublisher.setRecordHandler(new SrsRecordHandler(this));
    //  mPublisher.setPreviewResolution(mWidth, mHeight);
    // mPublisher.setOutputResolution(mHeight, mWidth);
  }

  public void startPublish(
    String url,
    String streamID,
    CallbackContext callbackContext
  ) {
    mPublisher.startPublish(url + "/" + streamID);
    this.callbackContext = callbackContext;
  }

  public void stopPublish() {
    mPublisher.stopPublish();
  }

  public void mute() {
    mPublisher.pauseEncode();
  }

  public void unmute() {
    mPublisher.resumeEncode();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    //  this.mute();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mPublisher.stopPublish();
  }

  @Override
  public void onRtmpConnecting(String msg) {}

  @Override
  public void onRtmpConnected(String msg) {
    this.callbackContext.success();
   // this.mute();
  }

  @Override
  public void onRtmpVideoStreaming() {}

  @Override
  public void onRtmpAudioStreaming() {}

  @Override
  public void onRtmpStopped() {}

  @Override
  public void onRtmpDisconnected() {}

  @Override
  public void onRtmpVideoFpsChanged(double fps) {}

  @Override
  public void onRtmpVideoBitrateChanged(double bitrate) {}

  @Override
  public void onRtmpAudioBitrateChanged(double bitrate) {}

  @Override
  public void onRtmpSocketException(SocketException e) {}

  @Override
  public void onRtmpIOException(IOException e) {}

  @Override
  public void onRtmpIllegalArgumentException(IllegalArgumentException e) {}

  @Override
  public void onRtmpIllegalStateException(IllegalStateException e) {}

  @Override
  public void onNetworkWeak() {}

  @Override
  public void onNetworkResume() {}

  @Override
  public void onEncodeIllegalArgumentException(IllegalArgumentException e) {}

  @Override
  public void onRecordPause() {}

  @Override
  public void onRecordResume() {}

  @Override
  public void onRecordStarted(String msg) {}

  @Override
  public void onRecordFinished(String msg) {}

  @Override
  public void onRecordIllegalArgumentException(IllegalArgumentException e) {}

  @Override
  public void onRecordIOException(IOException e) {}
}
