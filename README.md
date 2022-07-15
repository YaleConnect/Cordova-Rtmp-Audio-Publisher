# Cordova Rtmp Audio Publisher

Plugin that publish audio via rtmp for android and iOS

## Android/iOS Permissions

For this plugin work correctly you must provide the following permissions:

 Android
 
 iOS

## Installation  
    
Install the plugin

    $ cordova plugin add https://github.com/lolblacklistm2w/Cordova-Rtmp-Audio-Publisher.git

## Usage

Initialize plugin

    (<any>window).RtmpPlugin.init(
        (response) => {
          console.log("INIT RTMPPLUGIN SUCCESS" + response);
        },
        (error) => {
          console.error("INIT RTMPPLUGIN Error" + error);
        });

 Start publishing
    
    (<any>window).RtmpPlugin.startPublish(
        serverUrl, //The server url example: rtmp.com:1935
        publishName, //The rtmp publisher name
        (response) => {
          console.log("PUBLISH RTMPPLUGIN SUCCESS", response);
        },
        (error) => {
          console.error("PUBLISH RTMPPLUGIN ERROR" + error);
        });
    
 Stop publishing
 
     (<any>window).RtmpPlugin.stopPublish(
        (response) => {         
           console.log("UNPUBLISH RTMPPLUGIN SUCCESS", response);
        },
        (error) => {
           console.error("UNPUBLISH RTMPPLUGIN ERROR" + error);
        });
      
  Mute
  
      (<any>window).RtmpPlugin.mute(
        (response) => {
           console.log("MUTE RTMPPLUGIN SUCCESS", response);
        },
        (error) => { 
           console.error("MUTE RTMPPLUGIN ERROR" + error);
        });
      
  Unmute
    
      (<any>window).RtmpPlugin.unmute(
        (responce) => {
           console.log("UNMUTE RTMPPLUGIN SUCCESS", response);
        },
        (error) => {
           console.error("UNMUTE RTMPPLUGIN ERROR" + error);
        });
