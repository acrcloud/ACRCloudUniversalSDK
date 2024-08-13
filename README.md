# ACRCloud Android SDK

## Overview
  [ACRCloud](https://www.acrcloud.com/) provides services such as **[Music Recognition](https://www.acrcloud.com/music-recognition)**, **[Broadcast Monitoring](https://www.acrcloud.com/broadcast-monitoring/)**, **[Custom Audio Recognition](https://www.acrcloud.com/second-screen-synchronization%e2%80%8b/)**, **[Copyright Compliance & Data Deduplication](https://www.acrcloud.com/copyright-compliance-data-deduplication/)**, **[Live Channel Detection](https://www.acrcloud.com/live-channel-detection/)**, and **[Offline Recognition](https://www.acrcloud.com/offline-recognition/)** etc.<br>

## Requirements                                                                                                                             
Follow one of the tutorials to create a project and get your host, access_key, and access_secret.

 * [Recognize Music](https://docs.acrcloud.com/tutorials/recognize-music)
 * [Recognize Custom Content](https://docs.acrcloud.com/tutorials/recognize-custom-content)
 * [Broadcast Monitoring for Music](https://docs.acrcloud.com/tutorials/broadcast-monitoring-for-music)
 * [Broadcast Monitoring for Custom Content](https://docs.acrcloud.com/tutorials/broadcast-monitoring-for-custom-content)
 * [Detect Live & Timeshift TV Channels](https://docs.acrcloud.com/tutorials/detect-live-and-timeshift-tv-channels)
 * [Recognize Custom Content Offline](https://docs.acrcloud.com/tutorials/recognize-custom-content-offline)
 * [Recognize Live Channels and Custom Content](https://docs.acrcloud.com/tutorials/recognize-tv-channels-and-custom-content)
 * 


## Identify Music or TV
This demo shows how to identify music ( songs ) or detect live TV channels by recorded sound with ACRCloud SDK. Contact us if you have any questions or special requirements about the SDK: support@acrcloud.com

## Preparation
* The newest ACRCloud SDK which contains both ObjectC and Swift demo projects.
* If you want to recognize music, you need an Audio Recognition project. ( See [How to Recognize Music](https://docs.acrcloud.com/tutorials/recognize-music) )
* If you want to detect tv channels, you need a Live Channel Detection project. ( See [How to Detect Live TV Channels](https://docs.acrcloud.com/tutorials/detect-live-and-timeshift-tv-channels) )
* Save the information of “host”, “access_key”, “access_secret” of your project.
* Make sure you have Xcode installed.

## Directory Description:

* ACRCloudUniversalSDKDemo
	demo project
	
* libs
	all librarys of sdk
		(1) acrcloud-universal-sdk-(version).jar: java library
		(2) libACRCloudUniversalEngine.so: JNI library
		
* docs: https://docs.acrcloud.com/

* libs-so-tinyalsa
    If you can not use tinyalsa of libs, you can try this share library.

* libs-c
    C/C++ library.

* If you want to use Offline Version, please use this SDK[offline_version].

## Test the demo
[ACRCloudUniversalSDKDemo] is an Android Studio Project, you can add your "host", "access_key" and "access_secret" to recognize music or other contents.

## How to use the SDK
* demo code
```java
public class MainActivity implements IACRCloudListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.mConfig = new ACRCloudConfig();
        this.mConfig.acrcloudListener = this;
        this.mConfig.context = this;
        // Please create project in "http://console.acrcloud.cn/service/avr".
        this.mConfig.host = "XXXXXX";
        this.mConfig.accessKey = "XXXXXX";
        this.mConfig.accessSecret = "XXXXXX";
        
        // If you do not need volume callback, you set it false.
        this.mConfig.recorderConfig.isVolumeCallback = true;

        // By default, pre-recording is enabled and 3s audio is retained. 
        // If you don't need pre-recording, you can set reservedRecordBufferMS = 0
        this.mConfig.recorderConfig.reservedRecordBufferMS = 3000;

        this.mClient = new ACRCloudClient();
        this.initState = this.mClient.initWithConfig(this.mConfig);
    }

    public void start() {
        if (!this.initState) {
            Toast.makeText(this, "init error", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!mProcessing) {
            mProcessing = true;
            mVolume.setText("");
            mResult.setText("");

            // startRecognize: turn on the recording and identify it.
            if (this.mClient == null || !this.mClient.startRecognize()) {
                mProcessing = false;
                mResult.setText("start error!");
            }
            startTime = System.currentTimeMillis();
        }
    }

    public void cancel() {
        if (mProcessing && this.mClient != null) {
            // cancel: cancel the current identify session.
            this.mClient.cancel();
        }

        this.reset();
    }

    @Override
    public void onVolumeChanged(double volume) {
        long time = (System.currentTimeMillis() - startTime) / 1000;
        mVolume.setText(getResources().getString(R.string.volume) + volume + "\n\nTime: " + time + " s");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.e("MainActivity", "release");
        if (this.mClient != null) {
            this.mClient.release();
            this.initState = false;
            this.mClient = null;
        }
    }

    // result callback
    @Override
    public void onResult(ACRCloudResult results) {
        String result = results.getResult();

        String tres = "\n";

        try {
            JSONObject j = new JSONObject(result);
            JSONObject j1 = j.getJSONObject("status");
            int j2 = j1.getInt("code");
            if(j2 == 0){
                JSONObject metadata = j.getJSONObject("metadata");
                //
                if (metadata.has("music")) {
                    JSONArray musics = metadata.getJSONArray("music");
                    for(int i=0; i<musics.length(); i++) {
                        JSONObject tt = (JSONObject) musics.get(i);
                        String title = tt.getString("title");
                        JSONArray artistt = tt.getJSONArray("artists");
                        JSONObject art = (JSONObject) artistt.get(0);
                        String artist = art.getString("name");
                        tres = tres + (i+1) + ".  Title: " + title + "    Artist: " + artist + "\n";
                    }
                }

                tres = tres + "\n\n" + result;
            }else{
                tres = result;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
```

### Low-Level Function
#### File/PCM/Fingerprint Recognition
if you recognize audio data or get the audio fingerprint, the audio format should be  RIFF (little-endian) data, WAVE audio, Microsoft PCM, 16-bit,  you also can use the resample function to convert the audio data to what we need.
```java
public ACRCloudClient() {
    /*
        create audio fingerprint from audio buffer.
        
        buffer: audio buffer (format: RIFF (little-endian) data, WAVE audio, Microsoft PCM, 16-bit)
        bufferLen: the lenght of buffer
        sampleRate: sample rate of buffer
        nChannels: channel number of buffer
    */
    public static byte[] createClientFingerprint(byte[] buffer, int bufferLen, int sampleRate, int nChannels);

    /*
        create humming fingerprint from audio buffer.
        
        buffer: audio buffer (format: RIFF (little-endian) data, WAVE audio, Microsoft PCM, 16-bit)
        bufferLen: the lenght of buffer
        sampleRate: sample rate of buffer
        nChannels: channel number of buffer
    */
    public static byte[] createHummingClientFingerprint(byte[] buffer, int bufferLen, int sampleRate, int nChannels)

    /*
        recognize audio buffer and return the final result.
        
        buffer: audio buffer (format: RIFF (little-endian) data, WAVE audio, Microsoft PCM, 16-bit)
        bufferLen: the lenght of buffer
        sampleRate: sample rate of buffer
        nChannels: channel number of buffer
    */
    public String recognize(byte[] buffer, int bufferLen, int sampleRate, int nChannels);

    /*
        recognize fingerprint buffer and return the final result.
        
        buffer: acrcloud fingerprint buffer
        bufferLen: the lenght of buffer
        recType: AUDIO (audio fingerprint), HUMMING (humming fingerprint)
        userParams: Some user-defined parameters.
    */
    public String recognizeFingerprint(byte[] buffer, int bufferLen, ACRCloudConfig.RecognizerType recType, Map<String, String> userParams)
}
```
