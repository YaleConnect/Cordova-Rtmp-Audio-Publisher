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
import org.json.JSONException;
import org.json.JSONObject;
import android.util.Log;
public class RtmpActivity extends Activity
  implements RtmpHandler.RtmpListener,
             SrsRecordHandler.SrsRecordListener,
             SrsEncodeHandler.SrsEncodeListener {
  public static final int SAMPLE_AUDIO_RATE_IN_HZ = 44100; // (no se usa)
  public static boolean running = false;
  private static final String TAG = "RtmpActivity";
  public String streamId;
  public Boolean isPublishing = true;
  private SrsPublisher mPublisher;
  private CallbackContext callbackContext;
  // control de notificación/tiempos
  private volatile boolean notifiedReady = false;
  private final Handler mainHandler = new Handler(Looper.getMainLooper());
  private Runnable readyRunnable;
  private boolean callbackHandled = false;
  private static final String ERROR_CODE_TWOWAY_ALREADY_ACTIVE = "TWOWAY_ALREADY_ACTIVE";
  private static final String ERROR_CODE_RTMP_PUBLISH = "RTMP_PUBLISH_ERROR";
  private static final String ERROR_CODE_RTMP_DISCONNECTED = "RTMP_DISCONNECTED";
  private static final String STREAM_ALREADY_EXISTS_MESSAGE = "Current stream object has existed";
  public RtmpActivity() {
    mPublisher = new SrsPublisher();
    mPublisher.setSendAudioOnly(true);
    mPublisher.setEncodeHandler(new SrsEncodeHandler(this));
    mPublisher.setRtmpHandler(new RtmpHandler(this));
    mPublisher.setRecordHandler(new SrsRecordHandler(this));
    // Arranca muteado por si acaso (startEncode también lo hace)
    mPublisher.setSendVideoOnly(true);
  }
  public void startPublish(String url, String streamID, CallbackContext callbackContext) {
    this.callbackContext = callbackContext;
    this.streamId = streamID;
    this.notifiedReady = false;
    this.callbackHandled = false;
    cancelReadyTimer();
    Log.d(TAG, "startPublish request: streamId=" + streamID + " url=" + url);
    try {
      mPublisher.startPublish(url + "/" + streamID);
    } catch (Exception e) {
      notifyCallbackError(ERROR_CODE_RTMP_PUBLISH, e != null ? e.getMessage() : null);
    }
    // startPublish -> startEncode -> setMuted(true) -> queda muteado
  }
  private void stopStreaming() {
    if (mPublisher != null) {
      try {
        mPublisher.stopPublish();
      } catch (Exception e) {
        Log.w(TAG, "stopStreaming failed", e);
      }
    }
  }

  public void stopPublish() {
    cancelReadyTimer();
    stopStreaming();
    this.callbackContext = null;
    this.notifiedReady = false;
    Log.d(TAG, "stopPublish called");
  }
  /** MUTE desde el front */
  public void mute() {
    if (mPublisher != null) {
      // Mutear = no enviar audio real: silencio con timing estable
      mPublisher.setSendVideoOnly(true);
      // opcional:
      // mPublisher.pauseEncode();
    }
  }
  /** UNMUTE desde el front */
  public void unmute() {
    if (mPublisher != null) {
      // Desmutear = volver a leer del mic y mandar audio real
      mPublisher.setSendVideoOnly(false);
      // opcional si pausaste:
      // mPublisher.resumeEncode();
    }
  }
  private void notifyReadyOnce() {
    if (callbackContext != null && !notifiedReady) {
      notifiedReady = true;
      Log.d(TAG, "notifyReadyOnce - ready");
      notifyCallbackSuccess();
    }
  }

  private void notifyCallbackSuccess() {
    if (callbackContext != null && !callbackHandled) {
      callbackHandled = true;
      Log.d(TAG, "notifyCallbackSuccess");
      callbackContext.success();
      callbackContext = null;
    }
  }

  private void notifyCallbackError(String code, String message) {
    if (callbackContext != null && !callbackHandled) {
      callbackHandled = true;
      Log.d(TAG, "notifyCallbackError code=" + code + " message=" + message);
      try {
        JSONObject error = new JSONObject();
        error.put("code", code);
        error.put("message", message != null ? message : "");
        callbackContext.error(error);
      } catch (JSONException e) {
        callbackContext.error(message != null ? message : "");
      } finally {
        callbackContext = null;
      }
    }
  }

  private boolean isTwoWayBusyError(String message) {
    return message != null && message.contains(STREAM_ALREADY_EXISTS_MESSAGE);
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
    // nada más
  }
  @Override
  protected void onDestroy() {
    super.onDestroy();
    stopPublish();
  }
  // ───────────── RTMP callbacks ─────────────
  @Override public void onRtmpConnecting(String msg) {
    Log.d(TAG, "onRtmpConnecting msg=" + msg);
  }
  @Override public void onRtmpConnected(String msg) {
    Log.d(TAG, "onRtmpConnected msg=" + msg);
    // Hacemos “warm-up” corto y notificamos, salvo que llegue antes el audio.
    scheduleReadyTimer(300); // afiná 200–400ms si querés
  }
  @Override public void onRtmpVideoStreaming() {
    Log.d(TAG, "onRtmpVideoStreaming");
    // Por si tu fork dispara primero el evento de video
    cancelReadyTimer();
    notifyReadyOnce();
  }
  @Override public void onRtmpAudioStreaming() {
    Log.d(TAG, "onRtmpAudioStreaming");
    // ✅ audio ya está saliendo → listo para habilitar UI / mandar two_way
    cancelReadyTimer();
    notifyReadyOnce();
  }
  @Override public void onRtmpStopped() {
    Log.d(TAG, "onRtmpStopped");
    handleDisconnect("El servidor detuvo la transmisión");
  }
  @Override public void onRtmpDisconnected() {
    Log.d(TAG, "onRtmpDisconnected");
    handleDisconnect("El servidor cerró la conexión sin responder");
  }
  @Override public void onRtmpVideoFpsChanged(double fps) {}
  @Override public void onRtmpVideoBitrateChanged(double bitrate) {}
  @Override public void onRtmpAudioBitrateChanged(double bitrate) {}
  @Override public void onRtmpSocketException(SocketException e) {
    cancelReadyTimer();
    notifyCallbackError(ERROR_CODE_RTMP_PUBLISH, e != null ? e.getMessage() : "");
  }
  @Override public void onRtmpIOException(IOException e) {
    cancelReadyTimer();
    notifyCallbackError(ERROR_CODE_RTMP_PUBLISH, e != null ? e.getMessage() : "");
  }
  @Override public void onRtmpIllegalArgumentException(IllegalArgumentException e) {
    cancelReadyTimer();
    notifyCallbackError(ERROR_CODE_RTMP_PUBLISH, e != null ? e.getMessage() : "");
  }
  @Override public void onRtmpIllegalStateException(IllegalStateException e) {
    cancelReadyTimer();
    final String message = e != null ? e.getMessage() : "";
    final String code = isTwoWayBusyError(message) ? ERROR_CODE_TWOWAY_ALREADY_ACTIVE : ERROR_CODE_RTMP_PUBLISH;
    Log.d(TAG, "onRtmpIllegalStateException code=" + code + " message=" + message);
    notifyCallbackError(code, message);
  }
  private void handleDisconnect(String reason) {
    cancelReadyTimer();
    stopStreaming();
    if (callbackContext != null && !callbackHandled) {
      Log.d(TAG, "handleDisconnect reason=" + reason);
      notifyCallbackError(ERROR_CODE_RTMP_DISCONNECTED, reason);
    }
  }
  // ───────────── Encoder / Record callbacks ─────────────
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
