package vorooooone.vorooooone.vorooooonMain


import android.os.Bundle
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.TextView
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import vorooooone.vorooooone.R
import vorooooone.felhr.usbserial.UsbSerialDevice
import vorooooone.felhr.usbserial.UsbSerialInterface

import java.nio.ByteBuffer
import android.app.PendingIntent
import android.support.v7.app.AppCompatActivity
import android.widget.ImageView
import vorooooone.Button.PushButton


class VorooooonActivity : Activity() {
    companion object {
        private val REQUEST_CODE = 1000
        private val orderList = listOf<String>("○", "離陸", "上","下", "前", "後ろ", "右", "左", "着",
                                "時計回り", "半時計周り", "強制終了", "光","ブーメラン","な音","高速移動","雷","かめはめ波")
    }

    private var textView: TextView? = null
    private var connection_view: TextView? = null

    private var mUsbManager: UsbManager? = null
    private var mUsbDevice: UsbDevice? = null

    private var orderNumber: Int = 0
    private var lang: Int = 0
    private val droneID: Int = 4292

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vorooooon)

        val iv = ImageView(this)
        iv.setImageResource(R.drawable.manual)


        // 認識結果を表示させる
        textView = findViewById(R.id.text_view)

        //接続状態の表示
        connection_view = findViewById(R.id.Connection_Status)

        mUsbManager = getSystemService(AppCompatActivity.USB_SERVICE) as UsbManager

        // Arduinoの端末を認識させる
        updateList()
        permission()

        val buttonList =listOf<PushButton>(
                findViewById<PushButton>(R.id.button_start),findViewById<PushButton>(R.id.TakeOffButton),
                findViewById<PushButton>(R.id.UpButton),findViewById<PushButton>(R.id.DownButton),
                findViewById<PushButton>(R.id.FrontButton),findViewById<PushButton>(R.id.RearButton),
                findViewById<PushButton>(R.id.RightButton), findViewById<PushButton>(R.id.LeftButton),
                findViewById<PushButton>(R.id.LandingButton), findViewById<PushButton>(R.id.R_turnButton),
                findViewById<PushButton>(R.id.L_turnButton), findViewById<PushButton>(R.id.StopButton),
                findViewById<PushButton>(R.id.HelpButton))

        //ボタンの設定
        buttonList[0].setOnClickListener { speech() }

        for (i: Int in 1 until(buttonList.size-1) ){
            buttonList[i].setOnClickListener {
                    orderNumber = i
                    connectDevice()
            }
        }

        buttonList[12]!!.setOnClickListener {
            setContentView(iv)
            iv!!.setOnClickListener {
                startActivity(Intent(this@VorooooonActivity, VorooooonActivity::class.java))
            }
        }
    }

    // 結果を受け取るために onActivityResult を設置
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK) {
            // 認識結果を ArrayList で取得
            val candidates = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)

            if (candidates.size > 0) {
                // 認識結果候補で一番有力なものを表示
                textView!!.text = candidates[0]

                //操作可能な命令であれば接続開始
                if (orderCheck(candidates[0])) {
                    connectDevice()
                }
            }
        }
    }

    //ーーーーーーーーーーーーーーーーここから下はメソッドーーーーーーーーーーーーーーーーーーーーーー

    // AndroidとArduinoを接続したときに端末情報を呼び込む
    private fun updateList() {
        val deviceList = mUsbManager!!.deviceList

        if (deviceList == null || deviceList.isEmpty()) {
            connection_view!!.text = "no device found"
        } else {
            var string = ""

            for (name in deviceList.keys) {

                string += name
                if (deviceList[name]!!.getVendorId() == droneID) {
                    string += " (Arduino)\n"
                    mUsbDevice = deviceList[name]
                } else {
                    string += "\n"
                }

            }
            connection_view!!.text = string
        }
    }

    //パーミッション設定
    private fun permission() {
        if (mUsbDevice == null) {
            return
        }
        // シリアル通信用のパーミッションを取得
        if (!mUsbManager!!.hasPermission(mUsbDevice)) {
            mUsbManager!!.requestPermission(mUsbDevice,
                    PendingIntent.getBroadcast(this@VorooooonActivity, 0, Intent("あ"), 0))
            return
        }
    }

    private fun speech() {
        // 音声認識が使えるか確認する
        try {
            // 音声認識の　Intent インスタンス
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

            if (lang == 0) {
                intent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            } else {
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            }

            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 100)
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "音声を入力")
            // インテント発行(google制のダイアログ表示)
            startActivityForResult(intent, REQUEST_CODE)
        } catch (e: ActivityNotFoundException) {
            textView!!.text = "No Activity "
        }
    }

    //命令一覧との照合
    private fun orderCheck(checkstr: String): Boolean {
        var orderResult = false

        for (i: Int in 1 until orderList.size) {
            //命令音声かの判定
            if (checkstr.contains(orderList[i])) {
                orderNumber = i
                orderResult = true
                break
            }
        }
        return orderResult
    }

    // Arduinoの命令を呼び出す
    private fun connectDevice() {
        Thread(Runnable {
            val connection = mUsbManager!!.openDevice(mUsbDevice)
            val usb = UsbSerialDevice.createUsbSerialDevice(mUsbDevice, connection)
            val USB = usb.open()

            // USBが接続されているなら
            if (USB) {
                try {
                    // 例外処理が行われないよう、1秒間中断する
                    Thread.sleep(1000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }

                usb.setDTR(false)
                usb.setBaudRate(9600)
                usb.setDataBits(8)
                usb.setParity(UsbSerialInterface.PARITY_NONE)
                usb.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF)

                // 音声をArduinoに飛ばす
                send(usb)
            }
            connection.close()
        }).start()
    }

    //ドローンコントローラに命令を送信
    private fun send(usb: UsbSerialDevice) {
        var bytes = ByteBuffer.allocate(4).putInt(orderNumber).array()
        //var bytes = ByteBuffer.allocate(4).putInt(1).array()

        usb.write(bytes)
        usb.close()
    }
}

