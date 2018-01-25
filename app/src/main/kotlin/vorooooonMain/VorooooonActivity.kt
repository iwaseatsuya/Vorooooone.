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
import felhr.usbserial.UsbSerialDevice
import felhr.usbserial.UsbSerialInterface

import java.nio.ByteBuffer
import android.app.PendingIntent
import android.support.v7.app.AppCompatActivity
import android.widget.ImageView
import Button.PushButton

class VorooooonActivity : Activity() {
    companion object {
        private val REQUEST_CODE = 1000
        private val orderList = listOf<String>("○", "離陸", "上", "前", "後ろ", "右", "左", "光",
                "ブーメラン", "着", "時計回り", "半時計周り", "強制終了","な音","高速移動","雷","かめはめ波")
    }

    private var speechView: TextView? = null
    private var connectionView: TextView? = null

    private var mUsbManager: UsbManager? = null
    private var mUsbDevice: UsbDevice? = null

    private var orderNumber: Int = 0
    private var lang: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vorooooon)

        val iv = ImageView(this)
        iv.setImageResource(R.drawable.manual3)


        // 認識結果を表示させる
        speechView = findViewById(R.id.text_view)

        //接続状態の表示
        connectionView = findViewById(R.id.Connection_Status)

        //各ボタンの設定
        val buttonStart = findViewById<PushButton>(R.id.button_start)
        val upButton = findViewById<PushButton>(R.id.UpButton)
        val frontButton = findViewById<PushButton>(R.id.FrontButton)
        val r_turnButton = findViewById<PushButton>(R.id.R_turnButton)
        val l_turnButton = findViewById<PushButton>(R.id.L_turnButton)
        val leftButton = findViewById<PushButton>(R.id.LeftButton)
        val rightButton = findViewById<PushButton>(R.id.RightButton)
        val downButton = findViewById<PushButton>(R.id.DownButton)
        val rearButton = findViewById<PushButton>(R.id.RearButton)
        val stopButton = findViewById<PushButton>(R.id.StopButton)
        val takeofButton = findViewById<PushButton>(R.id.TakeOffButton)
        val landingButton = findViewById<PushButton>(R.id.LandingButton)

        mUsbManager = getSystemService(AppCompatActivity.USB_SERVICE) as UsbManager

        // Arduinoの端末を認識させる
        updateList()
        permission()

        //ボタンの処理の記述
        buttonStart!!.setOnClickListener {
            speech()
        }
        upButton!!.setOnClickListener {
            orderNumber = orderList.indexOf("上")
            connectDevice()
        }
        frontButton!!.setOnClickListener {
            orderNumber = orderList.indexOf("前")
            connectDevice()
        }
        r_turnButton!!.setOnClickListener {
            orderNumber = orderList.indexOf("時計回り")
            connectDevice()
        }
        l_turnButton!!.setOnClickListener {
            orderNumber = orderList.indexOf("反時計回り")
            connectDevice()
        }
        leftButton!!.setOnClickListener {
            orderNumber = orderList.indexOf("左")
            connectDevice()
        }
        rightButton!!.setOnClickListener {
            orderNumber =  orderList.indexOf("右")
            connectDevice()
        }
        downButton!!.setOnClickListener {
            orderNumber = orderList.indexOf("下")
            connectDevice()
        }
        rearButton!!.setOnClickListener {
            orderNumber = orderList.indexOf("後ろ")
            connectDevice()
        }
        stopButton!!.setOnClickListener {
            orderNumber = orderList.indexOf("強制停止")
            connectDevice()
        }
        takeofButton!!.setOnClickListener{
            orderNumber = orderList.indexOf("離陸")
            connectDevice()
        }
        landingButton!!.setOnClickListener{
            orderNumber = orderList.indexOf("着")
            connectDevice()
        }
        //てす
        takeofButton!!.setOnLongClickListener{
            //setContentView(iv)
            return@setOnLongClickListener true
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
                speechView!!.text = candidates[0]

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
            connectionView!!.text = "no device found"
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
            connectionView!!.text = string
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
            speechView!!.text = "No Activity "
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
            val usbconnection = UsbSerialDevice.createUsbSerialDevice(mUsbDevice, connection)
            val USB = usbconnection.open()

            // USBが接続されているなら
            if (USB) {
                try {
                    // 例外処理が行われないよう、1秒間中断する
                    Thread.sleep(1000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }

                usbconnection.setDTR(false)
                usbconnection.setBaudRate(9600)
                usbconnection.setDataBits(8)
                usbconnection.setParity(UsbSerialInterface.PARITY_NONE)
                usbconnection.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF)

                // 音声をArduinoに飛ばす
                send(usbconnection)
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

