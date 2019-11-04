package com.example.acrcloudsdkdemokotlin

import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle

import com.acrcloud.rec.*
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import com.acrcloud.rec.utils.ACRCloudLogger
import com.acrcloud.rec.ACRCloudClient
import com.acrcloud.rec.ACRCloudConfig
import android.support.v4.app.ActivityCompat
import android.content.pm.PackageManager
import android.util.Log


class MainActivity : AppCompatActivity(),IACRCloudListener {

    private val TAG = "MainActivity"

    private var mProcessing = false
    private var mAutoRecognizing = false
    private var initState = false

    private var path = ""

    private var startTime: Long = 0
    private val stopTime: Long = 0

    private val PRINT_MSG = 1001

    private var mConfig: ACRCloudConfig = ACRCloudConfig()
    private var mClient: ACRCloudClient = ACRCloudClient()

    private var mVolume: TextView? = null
    private var mResult: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mVolume = findViewById<TextView>(R.id.volume)
        mResult = findViewById<TextView>(R.id.result)

        verifyPermissions()

        findViewById<Button>(R.id.start).setOnClickListener(object : View.OnClickListener {

            override fun onClick(arg0: View) {
                start()
            }
        })

        findViewById<Button>(R.id.cancel).setOnClickListener(
            object : View.OnClickListener {

                override fun onClick(v: View) {
                    cancel()
                }
            })


        this.mConfig.acrcloudListener = this
        this.mConfig.context = this
        // Please create project in "http://console.acrcloud.cn/service/avr".
        this.mConfig.host = "XXXXXX"
        this.mConfig.accessKey = "XXXXXX"
        this.mConfig.accessSecret = "XXXXXX"

        // If you do not need volume callback, you set it false.
        this.mConfig.recorderConfig.isVolumeCallback = true

        this.mClient = ACRCloudClient()
        this.initState = this.mClient.initWithConfig(this.mConfig)

//        ACRCloudLogger.setLog(true)
    }

    fun start() {
        if (!this.initState) {
            Toast.makeText(this, "init error", Toast.LENGTH_SHORT).show()
            return
        }

        if (!mProcessing) {
            mProcessing = true
            mVolume?.text = ""
            mResult?.text = ""
            if (this.mClient == null || !this.mClient.startRecognize()) {
                mProcessing = false
                result.text = "start error!"
            }
            startTime = System.currentTimeMillis()
        }
    }

    fun cancel() {
        if (mProcessing && this.mClient != null) {
            this.mClient.cancel()
        }

        this.reset()
    }

    fun reset() {
        time.text = ""
        result.text = ""
        mProcessing = false
    }

    override fun onResult(results: ACRCloudResult?) {
        this.reset()

        val result = results?.getResult()

        mResult?.text = result
        startTime = System.currentTimeMillis()
    }

    override fun onVolumeChanged(curVolume: Double) {
        val time = (System.currentTimeMillis() - startTime) / 1000
        volume.text = getString(R.string.volume) + ":" + curVolume.toString() + "\n\nRecord：" + time + " s"
    }

    private val REQUEST_EXTERNAL_STORAGE = 1
    private val PERMISSIONS = arrayOf<String>(
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.INTERNET,
        Manifest.permission.RECORD_AUDIO
    )

    fun verifyPermissions() {
        for (i in PERMISSIONS.indices) {
            val permission = ActivityCompat.checkSelfPermission(this, PERMISSIONS[i])
            if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this, PERMISSIONS,
                    REQUEST_EXTERNAL_STORAGE
                )
                break
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        Log.e("MainActivity", "release")
        if (this.mClient != null) {
            this.mClient.release()
            this.initState = false
        }
    }
}
