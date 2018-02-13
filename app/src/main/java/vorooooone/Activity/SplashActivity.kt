package vorooooone.vorooooone.vorooooonMain

import android.app.Activity
import android.app.PendingIntent
import android.content.DialogInterface
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.ImageView

import vorooooone.vorooooone.R

class SplashActivity : Activity() {

    private var mUsbManager: UsbManager? = null
    private var mUsbDevice: UsbDevice? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.splash)

        mUsbManager = getSystemService(AppCompatActivity.USB_SERVICE) as UsbManager

        val splashView = findViewById<ImageView>(R.id.splashview)

        splashView!!.setOnClickListener{
            //USB接続がされているか確認
            /*
            if (mUsbManager!!.deviceList == null || mUsbManager!!.deviceList.isEmpty()) {
                //接続を促す
                AlertDialog.Builder(this)
                        .setTitle("Androidとコントローラを接続してください。")
                        .setPositiveButton("ok") { dialog, which ->
                        }.show()
            }else { run() }
            */
            startActivity(Intent(this@SplashActivity, VorooooonActivity::class.java))
            this@SplashActivity.finish()
            //*/
        }
    }

    fun run() {
        // スプラッシュ完了後に実行するActivityを指定
        val deviceList = mUsbManager!!.deviceList

        if (deviceList == null || deviceList.isEmpty()) {
            startActivity(Intent(this@SplashActivity, SplashActivity::class.java))
        } else {
            for (name in deviceList.keys) {
                if (deviceList[name]!!.getVendorId() == 4292) {
                    mUsbDevice = deviceList[name]
                }
            }
            startActivity(Intent(this@SplashActivity, VorooooonActivity::class.java))
            this@SplashActivity.finish()
        }
    }
}

