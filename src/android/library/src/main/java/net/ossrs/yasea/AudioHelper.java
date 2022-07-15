package net.ossrs.yasea;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

public class AudioHelper {
    private static final String TAG = "AudioHelper";

    /**
     * @param context
     * @return true if mute
     */
    public static boolean switchMute(Context context) {
        AudioManager audioManager;
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        int oldMode = audioManager.getMode();
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        boolean isMute = !audioManager.isMicrophoneMute();
        audioManager.setMicrophoneMute(isMute);
        audioManager.setMode(oldMode);

        return isMute;
    }


    private static int[] SAMPLE_RATES = new int[]{22050};
    private static short[] ENCODINGS = new short[]{AudioFormat.ENCODING_PCM_16BIT};
    private static short[] CHANNELS = new short[]{AudioFormat.CHANNEL_IN_MONO};

    public static AudioRecord findAudioRecord() {

        for (int rate : SAMPLE_RATES) {
            for (short encoding : ENCODINGS) {
                for (short channelConfig : CHANNELS) {
                    try {
                        Log.d(TAG, "Attempting rate " + rate + "Hz, bits: " + encoding + ", channel: "
                                + channelConfig);
                        int bufferSize = AudioRecord.getMinBufferSize(rate, channelConfig, encoding);

                        if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {
                            // check if we can instantiate and have a success
                            AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, rate, channelConfig, encoding, bufferSize);

                            if (recorder.getState() == AudioRecord.STATE_INITIALIZED)
                                return recorder;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, rate + "Exception, keep trying.", e);
                    }
                }
            }
        }
        return null;
    }
}