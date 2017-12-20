package vorooooone.vorooooone.vorooooonMain


import android.os.Bundle
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import android.view.View
import android.widget.Button
import android.widget.TextView
import java.util.Locale
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import vorooooone.vorooooone.R
import vorooooone.felhr.usbserial.UsbSerialDevice
import vorooooone.felhr.usbserial.UsbSerialInterface

import java.nio.ByteBuffer
import java.util.HashMap
import android.app.PendingIntent
import android.support.v7.app.AppCompatActivity


class  VorooooonActivity : Activity() {
    companion object {
        private val REQUEST_CODE = 1000
        private val orderList = listOf<String>("上","右","左","下","前","後ろ","10万ボルト")
    }

    private var textView: TextView? = null
    private var languageView: TextView? = null
    private var mTextView: TextView? = null
    private var lang: Int = 2 //オフラインをデフォルトに

    private var buttonStart: Button? = null
    private var buttonJapanese: Button? = null
    private var buttonEnglish: Button? = null
    //var mButton: Button? = null

    private var mUsbManager: UsbManager? = null
    private var mUsbDevice: UsbDevice? = null
    private var device: UsbDevice? = null

    //private var num: Byte = 0
    private var num: Int = 0
    private var droneOrder: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vorooooon)

        //オフラインをデフォルトに
        lang = 2

        // 認識結果を表示させる
        textView = findViewById(R.id.text_view)

        //言語の実行モードを表示させる
        languageView = findViewById(R.id.language_view)

        //各ボタンの設定
        buttonStart = findViewById(R.id.button_start)
        buttonJapanese = findViewById(R.id.button_japanese)
        buttonEnglish = findViewById(R.id.button_english)

        mTextView = findViewById(R.id.test_view)
        //mButton = findViewById(R.id.button)
        mUsbManager = getSystemService(AppCompatActivity.USB_SERVICE) as UsbManager

        // パーミッション設定
        permission()

        // Arduinoの端末を認識させる
        updateList()

        //各ボタンの処理の記述
        buttonJapanese!!.setOnClickListener {
            lang = 0
            languageView!!.text = "モード：日本語での実行"
        }
        buttonEnglish!!.setOnClickListener {
            lang = 1
            languageView!!.text = "モード：英語での実行"
        }
        buttonStart!!.setOnClickListener {
            // 音声認識を開始
            speech()
        }

//        mButton!!.setOnClickListener {
//         permission()
//        }
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
                if(OrderCheck( candidates[0] )){
                    connectDevice(droneOrder)
                    //connectDevice(num)
                }
            }
        }
    }


//ーーーーーーーーーーーーーーーーここから下はメソッドーーーーーーーーーーーーーーーーーーーーーーー

    private fun speech() {
        // 音声認識が使えるか確認する
        try {
            // 音声認識の　Intent インスタンス
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

            if (lang == 0) {
                // 日本語
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.JAPAN.toString())
            } else if (lang == 1) {
                // 英語
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH.toString())
            } else if (lang == 2) {
                // Off line mode
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

    // Arduinoの命令を呼び出す
    private fun connectDevice(droneOrder: String) {
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
                send(droneOrder, usb)
            }
            connection.close()
        }).start()
    }

    // パーミッション設定
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

    //ドローンコントローラに命令を送信
    private fun send(str: String, usb: UsbSerialDevice) {
        if (str === "上") {
            num = 1

            /*多分これ意味ない
            val bytes = ByteBuffer.allocate(4).putInt(num.toInt()).array()
             */
            val bytes = ByteBuffer.allocate(4).putInt(num).array()

            usb.write(bytes)
            usb.close()
        }
    }

    //命令一覧との照合
    fun OrderCheck(checkstr: String): Boolean{
        var orderResult: Boolean = false

        for(i in 0 until orderList.size) {
            //命令音声かの判定
            if (checkstr.contains(orderList.get(0))) {
                droneOrder = orderList.get(0)
                //num = i
                orderResult = true //ここでコネクトでもよさげ
                break
            }
        }
        return orderResult
    }
}

/*todo
命令リストの命令の格納場所と番地をコントローラ側の命令番号と合わせる（例：１で上なら　1番目に"上"）
 */