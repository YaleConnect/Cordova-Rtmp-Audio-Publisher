package com.cordova.m2w.plugin.rtmp;

import android.os.AsyncTask;
import android.util.Log;
import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RtmpPlugin extends CordovaPlugin {
  public static final String START_PUBLISH = "startPublish";
  public static final String STOP_PUBLISH = "stopPublish";
  public static final String MUTE = "mute";
  public static final String UNMUTE = "unmute";
  public static final String INIT = "init";
  RtmpActivity rtmp;

  @Override
  public boolean execute(
    String action,
    JSONArray args,
    CallbackContext callbackContext
  )
    throws JSONException {
    try {
      Log.d("TAG", "ACTION");
      Log.d("TAG", action);
      if (INIT.equals(action)) {
        rtmp = new RtmpActivity();
        callbackContext.success();
        return true;
      }
      if (START_PUBLISH.equals(action)) {
        String url;
        String streamID;
        try {
          //  JSONObject options = args.getJSONObject(0);
          //  url = options.getString("url");
          //  streamID = options.getString("streamID");
          // JSONArray options = args.getString(0);
          url = args.getString(0);
          streamID = args.getString(1);
        } catch (JSONException e) {
          callbackContext.error("Error encountered: " + e.getMessage());
          return false;
        }
        rtmp.startPublish(url, streamID, callbackContext);
        // callbackContext.success();
        return true;
      } else if (STOP_PUBLISH.equals(action)) {
        rtmp.stopPublish();
        callbackContext.success();
        return true;
      } else if (MUTE.equals(action)) {
        rtmp.mute();
        callbackContext.success();
        return true;
      } else if (UNMUTE.equals(action)) {
        rtmp.unmute();
        callbackContext.success();
        return true;
      }
      callbackContext.error("Invalid action");
      return false;
    } catch (Exception e) {
      System.err.println("Exception: " + e.getMessage());
      callbackContext.error(e.getMessage());
      return false;
    }
  }
}
