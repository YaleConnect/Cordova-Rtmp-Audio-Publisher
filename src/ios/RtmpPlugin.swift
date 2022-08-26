import AVFoundation
import Photos
import HaishinKit

final class ExampleRecorderDelegate: DefaultAVRecorderDelegate {
    static let `default` = ExampleRecorderDelegate()

    override func didFinishWriting(_ recorder: AVRecorder) {
        guard let writer: AVAssetWriter = recorder.writer else {
            return
        }
        PHPhotoLibrary.shared().performChanges({() -> Void in
            PHAssetChangeRequest.creationRequestForAssetFromVideo(atFileURL: writer.outputURL)
        }, completionHandler: { _, error -> Void in
            do {
                try FileManager.default.removeItem(at: writer.outputURL)
            } catch {
                print(error)
            }
        })
    }
}
@objc(RtmpPlugin) class RtmpPlugin : CDVPlugin {
    private var maxRetryCount: Int = 5
    private var streamId: String  = ""
    private var streamUrl: String  = ""
    private var retryCount = 0
    private var rtmpConnection = RTMPConnection()
    private var rtmpStream: RTMPStream!
    private var callbackId: String = ""
  
    @objc(init:)
    func `init`(_ command: CDVInvokedUrlCommand) {
      var pluginResult = CDVPluginResult(
        status: CDVCommandStatus_ERROR
      )
        print("INITTTTTT")
            
        DispatchQueue.global(qos: .background).async {
                 print("In background")
            
          // self.startMicSession()
            
            self.maxRetryCount = 5
            self.streamUrl  = ""
            self.streamId = ""
            self.retryCount = 0
            self.rtmpConnection = RTMPConnection()
            self.rtmpStream = RTMPStream(connection: self.rtmpConnection)
            self.rtmpStream.receiveVideo = false;
            self.rtmpStream.audioSettings = [
                .sampleRate: 22050, .actualBitrate: 48 * 1000, .bitrate: 48 * 1000]
            self.rtmpStream.recorderSettings = [
                AVMediaType.audio: [
                    AVFormatIDKey: Int(kAudioFormatMPEG4AAC),
                    AVSampleRateKey: 0,
                    AVNumberOfChannelsKey: 01
                   // AVEncoderBitRateKey: 128000
                ]
            ]
            self.rtmpStream.attachAudio(AVCaptureDevice.default(for: AVMediaType.audio), automaticallyConfiguresApplicationAudioSession: false){ error in
                print(error)
            }
      
     
             }
      
      pluginResult = CDVPluginResult(
          status: CDVCommandStatus_OK,
          messageAs: "Init"
      )
        
      self.commandDelegate!.send(
          pluginResult,
          callbackId: command.callbackId
      )
    }
    func startMicSession(){
        print("starting mic session")
        let session = AVAudioSession.sharedInstance()
         do {
             // https://stackoverflow.com/questions/51010390/avaudiosession-setcategory-swift-4-2-ios-12-play-sound-on-silent
           /*  if #available(iOS 10.0, *) {
                 try session.setCategory(.record, mode: .voiceChat, options: [])
             } else {*/
             session.perform(NSSelectorFromString("setCategory:withOptions:error:"), with: AVAudioSession.Category.playAndRecord, with: [])
             try session.setMode(.voiceChat)
           //  }
             try session.setPreferredSampleRate(22050)
           //  try session.setPreferredInputNumberOfChannels(1)
    
             try session.setActive(true)
         } catch {
             print(error)
         }
    }
    
    @objc(stopPublish:)
    func stopPublish(_ command: CDVInvokedUrlCommand) {
      var pluginResult = CDVPluginResult(
        status: CDVCommandStatus_ERROR
      )
        print("STOPPP \(self.streamId)")
        if(!rtmpStream.paused){
            rtmpStream.paused.toggle()
        }
        rtmpConnection.close()
        rtmpConnection.removeEventListener(.rtmpStatus, selector: #selector(rtmpStatusHandler), observer: self)
        rtmpConnection.removeEventListener(.ioError, selector: #selector(rtmpErrorHandler), observer: self)
        rtmpStream.close()
      pluginResult = CDVPluginResult(
          status: CDVCommandStatus_OK,
          messageAs: "Stop streaming"
      )

      self.commandDelegate!.send(
          pluginResult,
          callbackId: command.callbackId
      )
    }
    
  @objc(startPublish:)
  func startPublish(_ command: CDVInvokedUrlCommand) {
   /* var pluginResult = CDVPluginResult(
      status: CDVCommandStatus_ERROR
    )*/
    let uri = command.arguments[0] as? String ?? ""
    let streamId = command.arguments[1] as? String ?? ""
      print(uri)
      print(streamId)
      //urlStream = "\(uri)/\(streamId)"
  
      //rtmpConnection.addEventListener(.rtmpStatus, selector: #selector(rtmpStatusHandler), observer: self)
     // rtmpConnection.addEventListener(.ioError, selector: #selector(rtmpErrorHandler), observer: self)
      //print("START PUBLISH  \(uri)")
     // rtmpConnection.connect(uri)
      DispatchQueue.global(qos: .background).async {
               print("In background")
          self.streamId = streamId
          self.streamUrl = uri
          self.rtmpConnection.addEventListener(.rtmpStatus, selector: #selector(self.rtmpStatusHandler), observer: self)
          self.rtmpConnection.addEventListener(.ioError, selector: #selector(self.rtmpErrorHandler), observer: self)
          print("START PUBLISH  \(uri)")
          self.rtmpConnection.connect(uri)
      }
      self.callbackId = command.callbackId
    /*pluginResult = CDVPluginResult(
        status: CDVCommandStatus_OK,
        messageAs: "Streaming to \(uri)"
    )

    self.commandDelegate!.send(
        pluginResult,
        callbackId: command.callbackId
    )*/
  }
    
    @objc(mute:)
    func mute(_ command: CDVInvokedUrlCommand) {
      var pluginResult = CDVPluginResult(
        status: CDVCommandStatus_ERROR
      )
      pluginResult = CDVPluginResult(
          status: CDVCommandStatus_OK,
          messageAs: "mute"
      )
        rtmpStream.paused = true
      self.commandDelegate!.send(
          pluginResult,
          callbackId: command.callbackId
      )
    }
    
    @objc(unmute:)
    func unmute(_ command: CDVInvokedUrlCommand) {
      var pluginResult = CDVPluginResult(
        status: CDVCommandStatus_ERROR
      )
      pluginResult = CDVPluginResult(
          status: CDVCommandStatus_OK,
          messageAs: "unmute"
      )
        rtmpStream.paused = false
      self.commandDelegate!.send(
          pluginResult,
          callbackId: command.callbackId
      )
    }
    
    @objc
    private func rtmpStatusHandler(_ notification: Notification) {
        let e = Event.from(notification)
        guard let data: ASObject = e.data as? ASObject, let code: String = data["code"] as? String else {
            return
        }
        print(code)
        switch code {
        case RTMPConnection.Code.connectSuccess.rawValue:
            retryCount = 0
            rtmpStream.publish(streamId)
       
        case RTMPConnection.Code.connectFailed.rawValue, RTMPConnection.Code.connectClosed.rawValue:
            guard retryCount <= self.maxRetryCount else {
                return
            }
            Thread.sleep(forTimeInterval: pow(2.0, Double(retryCount)))
            rtmpConnection.connect(streamUrl)
            retryCount += 1
        case "NetStream.Publish.Start":
            rtmpStream.paused = true
           let pluginResult = CDVPluginResult(
                status: CDVCommandStatus_OK,
                messageAs: "NetStream.Publish.Start"
            )
            print("netstream start")
            print(self.callbackId)
            self.commandDelegate!.send(
                pluginResult,
                callbackId: self.callbackId
            )
        default:
            break
        }
    }

    @objc
    private func rtmpErrorHandler(_ notification: Notification) {
        print("ERRRORRRRR")
        print(notification)
        rtmpConnection.connect(streamUrl)
    }
}

