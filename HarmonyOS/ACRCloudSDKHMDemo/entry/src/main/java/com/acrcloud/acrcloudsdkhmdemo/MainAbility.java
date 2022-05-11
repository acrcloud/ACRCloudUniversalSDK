package com.acrcloud.acrcloudsdkhmdemo;

import com.acrcloud.acrcloudsdkhmdemo.slice.MainAbilitySlice;
import com.acrcloud.rec.ACRCloudClient;
import com.acrcloud.rec.ACRCloudConfig;
import com.acrcloud.rec.ACRCloudResult;
import com.acrcloud.rec.IACRCloudListener;
import com.acrcloud.rec.utils.ACRCloudLogger;

import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;
import ohos.agp.components.Button;
import ohos.agp.components.Component;
import ohos.agp.components.Text;
import ohos.agp.window.dialog.ToastDialog;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.zson.ZSONArray;
import ohos.utils.zson.ZSONException;
import ohos.utils.zson.ZSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainAbility extends Ability implements IACRCloudListener {
    private final static HiLogLabel TAG = new HiLogLabel(HiLog.LOG_APP, 0x1234, "MainActivity");

    private Text mVolume, mResult;

    private boolean mProcessing = false;
    private boolean mAutoRecognizing = false;
    private boolean initState = false;

    private long startTime = 0;
    private long stopTime = 0;

    private ACRCloudConfig mConfig = null;
    private ACRCloudClient mClient = null;

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setMainRoute(MainAbilitySlice.class.getName());
        super.setUIContent(ResourceTable.Layout_ability_main);

        this.requestPermission();

        mVolume = (Text) findComponentById(ResourceTable.Id_volume);
        mResult = (Text) findComponentById(ResourceTable.Id_result);

        Button startBtn = (Button) findComponentById(ResourceTable.Id_start);
        startBtn.setClickedListener(new Component.ClickedListener() {
            @Override
            public void onClick(Component component) {
                start();
            }
        });

        Button stopBtn = (Button) findComponentById(ResourceTable.Id_stop);
        if (stopBtn ==null) {
            HiLog.error(TAG, "stop btn null");
        }
        stopBtn.setClickedListener(new Component.ClickedListener() {
            @Override
            public void onClick(Component component) {
                stop();
            }
        });

        Button cancelBtn = (Button) findComponentById(ResourceTable.Id_cancel);
        if (cancelBtn ==null) {
            HiLog.error(TAG, "cancel btn null");
        }
        cancelBtn.setClickedListener(new Component.ClickedListener() {
            @Override
            public void onClick(Component component) {
                cancel();
            }
        });

        ACRCloudLogger.setLog(true);

        this.mConfig = new ACRCloudConfig();

        this.mConfig.acrcloudListener = this;
        this.mConfig.context = this;

        this.mConfig.host = "XXXXXXXX";
        this.mConfig.accessKey = "XXXXXXXX";
        this.mConfig.accessSecret = "XXXXXXXX";

        this.mConfig.recorderConfig.isVolumeCallback = true;

        this.mClient = new ACRCloudClient();

        this.initState = this.mClient.initWithConfig(this.mConfig);
    }

    @Override
    public void onResult(ACRCloudResult results) {
        this.reset();

        String result = results.getResult();

        String tres = "\n";
        try {
            ZSONObject j = ZSONObject.stringToZSON(result);
            ZSONObject j1 = j.getZSONObject("status");
            int j2 = j1.getInteger("code");
            if (j2 == 0) {
                ZSONObject metadata = j.getZSONObject("metadata");
                //
                if (metadata.containsKey("humming")) {
                    ZSONArray hummings = metadata.getZSONArray("humming");
                    for (int i = 0; i < hummings.size(); i++) {
                        ZSONObject tt = hummings.getZSONObject(i);
                        String title = tt.getString("title");
                        double score = tt.getDouble("score");
                        ZSONArray artistt = tt.getZSONArray("artists");
                        ZSONObject art = artistt.getZSONObject(0);
                        String artist = art.getString("name");
                        tres = tres + (i + 1) + ".  " + title + ";    score:" + score + "\n";
                    }
                }
                if (metadata.containsKey("music")) {
                    ZSONArray musics = metadata.getZSONArray("music");
                    for (int i = 0; i < musics.size(); i++) {
                        ZSONObject tt = musics.getZSONObject(i);
                        String title = tt.getString("title");
                        ZSONArray artistt = tt.getZSONArray("artists");
                        ZSONObject art = artistt.getZSONObject(0);
                        String artist = art.getString("name");
                        tres = tres + (i + 1) + ".  Title: " + title + "    Artist: " + artist + "\n";
                    }
                }
                if (metadata.containsKey("streams")) {
                    ZSONArray musics = metadata.getZSONArray("streams");
                    for (int i = 0; i < musics.size(); i++) {
                        ZSONObject tt = musics.getZSONObject(i);
                        String title = tt.getString("title");
                        String channelId = tt.getString("channel_id");
                        tres = tres + (i + 1) + ".  Title: " + title + "    Channel Id: " + channelId + "\n";
                    }
                }
                if (metadata.containsKey("custom_files")) {
                    ZSONArray musics = metadata.getZSONArray("custom_files");
                    for (int i = 0; i < musics.size(); i++) {
                        ZSONObject tt = musics.getZSONObject(i);
                        String title = tt.getString("title");
                        long playOffset = tt.getLong("play_offset_ms");
                        String playTime = secondToTime(playOffset / 1000);
                        tres = tres + (i + 1) + ".  Title: " + title + "  " + playTime + "\n";
                    }
                }
                tres = tres + "\n\n" + result;
            } else {
                tres = result;
            }
        } catch (ZSONException e) {
            tres = result;
            e.printStackTrace();
        }

        mResult.setText(tres);
        startTime = System.currentTimeMillis();
    }

    @Override
    public void onVolumeChanged(double volume) {
        long time = (System.currentTimeMillis() - startTime) / 1000;
        mVolume.setText("Volume: " + volume + "\n\nTime: " + time + " s");
    }

    public void start() {
        HiLog.debug(TAG, "start");
        if (!this.initState) {
            new ToastDialog(getContext())
                    .setText("init error")
                    .show();
            return;
        }

        mResult.setText("starting...");
        if (!mProcessing) {
            mProcessing = true;
            if (this.mClient == null || !this.mClient.startRecognize()) {
                mProcessing = false;
                mResult.setText("start error!");
            }
            startTime = System.currentTimeMillis();
        }
    }

    public void stop() {
        this.mClient.stopRecordToRecognize();
    }

    public void reset() {
        mResult.setText("");
//        mVolume.setText("");
        mProcessing = false;
    }

    public void cancel() {
        if (mProcessing && this.mClient != null) {
            this.mClient.cancel();
        }
        mProcessing = false;
        this.reset();
    }

    public static String secondToTime(long second) {
        long hours = second / 3600;
        long minutes = (second % 3600) / 60;
        long seconds = second % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private void requestPermission() {
        String[] permission = {
                "ohos.permission.MICROPHONE",
                "ohos.permission.INTERNET",
        };
        List<String> applyPermissions = new ArrayList<>();
        for (String element : permission) {
            if (verifySelfPermission(element) != 0) {
                if (canRequestPermission(element)) {
                    applyPermissions.add(element);
                }
            }
        }
        requestPermissionsFromUser(applyPermissions.toArray(new String[0]), 0);
    }
}
