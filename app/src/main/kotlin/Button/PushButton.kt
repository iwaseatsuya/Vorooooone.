package Button

import android.content.Context
import android.util.AttributeSet
import android.widget.Button

//プッシュするエフェクトのボタン
class PushButton : Button {
    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    override fun setPressed(pressed: Boolean) {
        if (pressed) {
            this.scaleY = 0.92f
            this.scaleX = 0.96f
            this.alpha = 0.75f
        } else {
            //ボタンを離したとき
            this.scaleY = 1.0f
            this.scaleX = 1.0f
            this.alpha = 1.0f
        }
        super.setPressed(pressed)
    }

}