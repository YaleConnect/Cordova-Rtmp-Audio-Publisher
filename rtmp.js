/*global cordova, module*/
var exec = require("cordova/exec");

var RtmpPlugin = {
  startPublish: function (url, streamID, callback) {
    var options = {};
    options.url = url;
    options.streamID = streamID;
    exec(
      function (out) {
        callback(out == "true");
      },
      function (error) {
        console.log("RtmpPlugin.startPublish failed: " + error);
      },
      "RtmpPlugin",
      "startPublish",
      [url, streamID]
    );
  },
  stopPublish: function (callback) {
    exec(
      function (out) {
        callback(out == "true");
      },
      function (error) {
        console.log("RtmpPlugin.stopPublish failed: " + error);
      },
      "RtmpPlugin",
      "stopPublish",
      []
    );
  },
  init: function (callback) {
    exec(
      function (out) {
        callback(out == "true");
      },
      function (error) {
        console.log("RtmpPlugin.init failed: " + error);
      },
      "RtmpPlugin",
      "init",
      []
    );
  },
  mute: function (callback) {
    exec(
      function (out) {
        callback(out == "true");
      },
      function (error) {
        console.log("RtmpPlugin.mute failed: " + error);
      },
      "RtmpPlugin",
      "mute",
      []
    );
  },
  unmute: function (callback) {
    exec(
      function (out) {
        callback(out == "true");
      },
      function (error) {
        console.log("RtmpPlugin.unmute failed: " + error);
      },
      "RtmpPlugin",
      "unmute",
      []
    );
  },
  status: function (callback) {
    exec(
      function (out) {
        console.log("RTMP PLUGIN STATUS ", out);
        callback(out);
      },
      function (error) {
        console.log("RtmpPlugin.status failed: " + error);
      },
      "RtmpPlugin",
      "status",
      []
    );
  }
};

module.exports = RtmpPlugin;
