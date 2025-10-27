package com.cordova.m2w.plugin.rtmp;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.github.faucamp.simplertmp.RtmpHandler;

import java.io.IOException;
import java.net.SocketException;

import net.ossrs.yasea.SrsEncodeHandler;
import net.ossrs.yasea.SrsPublisher;
import net.ossrs.yasea.SrsRecordHandler;

import org.apache.cordova.CallbackContext;

public class RtmpActivity extends Activity
  implements RtmpHandler.RtmpListener,
  SrsRecordHandler.SrsRecordListener,
  SrsEncodeHandler.SrsEncodeListener {

  public static final int SAMPLE_AUDIO_RATE_IN_HZ = 44100;
  public static boolean running = false;

  private static final String TAG = "RtmpActivity";

  public String streamId;
  public Boolean isPublishing = true;

  private SrsPublisher mPublisher;
  private CallbackContext callbackContext;
  private volatile boolean notifiedReady = false;
  private final Handler mainHandler = new Handler(Looper.getMainLooper());
  private Runnable readyRunnable;

  public RtmpActivity() {
    mPublisher = new SrsPublisher();
    mPublisher.setSendAudioOnly(true);
    mPublisher.setEncodeHandler(new SrsEncodeHandler(this));
    mPublisher.setRtmpHandler(new RtmpHandler(this));
    mPublisher.setRecordHandler(new SrsRecordHandler(this));

    mPublisher.setSendVideoOnly(true);
  }

  public void startPublish(String url, String streamID, CallbackContext callbackContext) {
    this.callbackContext = callbackContext;
    this.streamId = streamID;
    this.notifiedReady = false;
    cancelReadyTimer();
    mPublisher.startPublish(url + "/" + streamID);
  }

  public void stopPublish() {
    cancelReadyTimer();
    if (mPublisher != null) {
      mPublisher.stopPublish();
    }
    this.callbackContext = null;
    this.notifiedReady = false;
  }

  public void mute() {
    if (mPublisher != null) {
      mPublisher.setSendVideoOnly(true);
    }
  }

  public void unmute() {
    if (mPublisher != null) {
      mPublisher.setSendVideoOnly(false);
    }
  }

  private void notifyReadyOnce() {
    if (callbackContext != null && !notifiedReady) {
      notifiedReady = true;
      callbackContext.success();
      callbackContext = null;
    }
  }

  private void scheduleReadyTimer(long delayMs) {
    cancelReadyTimer();
    readyRunnable = this::notifyReadyOnce;
    mainHandler.postDelayed(readyRunnable, delayMs);
  }

  private void cancelReadyTimer() {
    if (readyRunnable != null) {
      mainHandler.removeCallbacks(readyRunnable);
      readyRunnable = null;
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    stopPublish();
  }
  @Override public void onRtmpConnecting(String msg) {}

  @Override public void onRtmpConnected(String msg) {
    scheduleReadyTimer(300);
  }

  @Override public void onRtmpVideoStreaming() {
    cancelReadyTimer();
    notifyReadyOnce();
  }

  @Override public void onRtmpAudioStreaming() {
    cancelReadyTimer();
    notifyReadyOnce();
  }

  @Override public void onRtmpStopped() {}
  @Override public void onRtmpDisconnected() {}
  @Override public void onRtmpVideoFpsChanged(double fps) {}
  @Override public void onRtmpVideoBitrateChanged(double bitrate) {}
  @Override public void onRtmpAudioBitrateChanged(double bitrate) {}
  @Override public void onRtmpSocketException(SocketException e) {}
  @Override public void onRtmpIOException(IOException e) {}
  @Override public void onRtmpIllegalArgumentException(IllegalArgumentException e) {}
  @Override public void onRtmpIllegalStateException(IllegalStateException e) {}
  @Override public void onNetworkWeak() {}
  @Override public void onNetworkResume() {}
  @Override public void onEncodeIllegalArgumentException(IllegalArgumentException e) {}
  @Override public void onRecordPause() {}
  @Override public void onRecordResume() {}
  @Override public void onRecordStarted(String msg) {}
  @Override public void onRecordFinished(String msg) {}
  @Override public void onRecordIllegalArgumentException(IllegalArgumentException e) {}
  @Override public void onRecordIOException(IOException e) {}
}
