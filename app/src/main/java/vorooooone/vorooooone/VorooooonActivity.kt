package vorooooone.vorooooone


import android.os.Bundle
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import android.view.View
import android.widget.Button
import android.widget.TextView
import java.util.ArrayList
import java.util.Locale

class  VorooooonActivity : Activity() {
    private var textView: TextView? = null
    private var languageView: TextView? = null
    private var lang: Int = 2 //オフラインをデフォルトに

    private var buttonStart: Button? = null
    private var buttonJapanese: Button? = null
    private var buttonEnglish: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vorooooon)

        // 認識結果を表示させる
        textView = findViewById<View>(R.id.text_view) as TextView

        //言語の実行モードを表示させる
        languageView = findViewById<View>(R.id.language_view) as TextView

        //各ボタンの設定
        buttonStart = findViewById<View>(R.id.button_start) as Button
        buttonJapanese = findViewById<View>(R.id.button_japanese) as Button
        buttonEnglish = findViewById<View>(R.id.button_english) as Button

        ButtonAction(buttonStart as Button)
        ButtonAction(buttonJapanese as Button)
        ButtonAction(buttonEnglish as Button)


    }

    //以下各処理メッソド

    fun speech() {
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

    //各ボタンの処理の記述
    fun ButtonAction(button: Button) {

        button!!.setOnClickListener {
            //Overrideさせるメッソド
        }
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
    }

    // 結果を受け取るために onActivityResult を設置
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // 認識結果を ArrayList で取得
            val candidates = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)

            if (candidates.size > 0) {
                //ここに特定の音声による分岐処理を記述
                textView!!.text = burizaeCheck(candidates[0])
            }
        }
    }

    //実験用クラス
    fun burizaeCheck(checkstr: String): String {
        var msg = if (checkstr == "ぶりぶりざえもん") "強いものの見方" else checkstr
        return msg
    }

    companion object {
        private val REQUEST_CODE = 1000
    }
}
