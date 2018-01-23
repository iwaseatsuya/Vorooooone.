package vorooooone.vorooooone.vorooooonMain


import android.os.Bundle
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Button
import android.widget.TextView
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import vorooooone.vorooooone.R
import vorooooone.felhr.usbserial.UsbSerialDevice
import vorooooone.felhr.usbserial.UsbSerialInterface
//import Kotlinx.android.synthetic.main.activity_vorooooone

import java.nio.ByteBuffer
import android.app.PendingIntent
import android.media.Image
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ImageView
import java.util.*


class VorooooonActivity : Activity() {
    companion object {
        private val REQUEST_CODE = 1000
        private val orderList = listOf<String>("○", "離陸", "上", "後ろ", "前", "右", "左", "10万ボルト",
                "ブーメラン", "着", "時計回り", "半時計周り", "強制終了")
    }

    private var textView: TextView? = null
    private var mTextView: TextView? = null

    //private var buttonStart: ImageView? = null

    private var mUsbManager: UsbManager? = null
    private var mUsbDevice: UsbDevice? = null

    /*
    private var upButton: Button? =null
    private var frontButton: Button? =null
    private var l_turnButton: Button? =null
    private var r_turnButton: Button? =null
    private var leftButton: Button? =null
    private var rightButton: Button? =null
    private var downButton: Button? =null
    private var rearButton: Button? =null
    private var stopButton: Button? =null
    */

    private var orderNumber: Int = 0
    private var lang: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vorooooon)




        // 認識結果を表示させる
        textView = findViewById(R.id.text_view)

        //
        mTextView = findViewById(R.id.test_view)

        //ボタンの設定
        val buttonStart = findViewById<ImageView>(R.id.button_start)
        val upButton = findViewById<ImageView>(R.id.UpButton)
        val frontButton = findViewById<ImageView>(R.id.FrontButton)
        val r_turnButton = findViewById<ImageView>(R.id.R_turnButton)
        val l_turnButton = findViewById<ImageView>(R.id.L_turnButton)
        val leftButton = findViewById<ImageView>(R.id.LeftButton)
        val rightButton = findViewById<ImageView>(R.id.RightButton)
        val downButton = findViewById<ImageView>(R.id.DownButton)
        val rearButton = findViewById<ImageView>(R.id.RearButton)
        val stopButton = findViewById<ImageView>(R.id.StopButton)

        mUsbManager = getSystemService(AppCompatActivity.USB_SERVICE) as UsbManager

        // Arduinoの端末を認識させる
        updateList()
        permission()

        //ボタンの処理の記述
        buttonStart!!.setOnClickListener {
            speech()
        }
        upButton!!.setOnClickListener {
            orderNumber = 2
            connectDevice()
        }
        frontButton!!.setOnClickListener {
            orderNumber = 4
            connectDevice()
        }
        r_turnButton!!.setOnClickListener {
            orderNumber = 10
            connectDevice()
        }
        l_turnButton!!.setOnClickListener {
            orderNumber = 11
            connectDevice()
        }
        leftButton!!.setOnClickListener {
            orderNumber = 6
            connectDevice()
        }
        rightButton!!.setOnClickListener {
            orderNumber = 5
            connectDevice()
        }
        downButton!!.setOnClickListener {
            orderNumber = 2
            connectDevice()
        }
        rearButton!!.setOnClickListener {
            orderNumber = 3
            connectDevice()
        }
        stopButton!!.setOnClickListener {
            orderNumber = 12
            connectDevice()
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
                if (OrderCheck(candidates[0])) {
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
            mTextView!!.text = "no device found"
        } else {
            var string = ""

            for (name in deviceList.keys) {

                string += name
                if (deviceList[name]!!.getVendorId() == 4292) {
                    string += " (Arduino)\n"
                    mUsbDevice = deviceList[name]
                } else {
                    string += "\n"
                }
            }
            mTextView!!.text = string
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
                /*// 英語
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH.toString())*/

                // Off line mode
                intent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            /*} else if (lang == 1) {
                // Off line mode
                intent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)*/
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
    private fun OrderCheck(checkstr: String): Boolean {
        var orderResult: Boolean = false

        for (i: Int in 1 until orderList.size) {
            //命令音声かの判定
            if (checkstr.contains(orderList.get(i))) {
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
        val bytes = ByteBuffer.allocate(4).putInt(orderNumber).array()

        usb.write(bytes)
        usb.close()

    }
}

