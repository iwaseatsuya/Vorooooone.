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
        // タイトルを非表示にします。
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        // splash.xmlをViewに指定します。
        setContentView(R.layout.splash)

        mUsbManager = getSystemService(AppCompatActivity.USB_SERVICE) as UsbManager

        val splashView = findViewById<ImageView>(R.id.splashview)

        AlertDialog.Builder(this)
                .setTitle("Androidとコントローラを接続してください。")
                .setPositiveButton("ok"){ dialog, which ->
                }.show()

        splashView!!.setOnClickListener{ run() }

        permission()

    }

    fun run() {
        // スプラッシュ完了後に実行するActivityを指定します
        val deviceList = mUsbManager!!.deviceList

        if (deviceList == null || deviceList.isEmpty()) {

            // ここにアラート表示して、OKボタン押したら下のアクティビティ遷移処理が走ればOKなはずだが、OK押す前に画面が遷移してしまうので
            // onCreatにのみ書いている
//            AlertDialog.Builder(this)
//                    .setTitle("Androidとコントローラを接続してください。")
//                    .setPositiveButton("ok"){ dialog, which ->
//                    }.show()

            startActivity(Intent(this@SplashActivity, SplashActivity::class.java))

        } else {
            for (name in deviceList.keys) {
                if (deviceList[name]!!.getVendorId() == 4292) {
                    mUsbDevice = deviceList[name]
                } else {

                }
            }
            startActivity(Intent(this@SplashActivity, VorooooonActivity::class.java))
            // SplashActivityを終了させます。
            this@SplashActivity.finish()
        }
    }

    private fun permission() {
        if (mUsbDevice == null) {
            return
        }
        // シリアル通信用のパーミッションを取得
        if (!mUsbManager!!.hasPermission(mUsbDevice)) {
            mUsbManager!!.requestPermission(mUsbDevice,
                    PendingIntent.getBroadcast(this@SplashActivity, 0, Intent("あ"), 0))
            return
        }
    }
}

