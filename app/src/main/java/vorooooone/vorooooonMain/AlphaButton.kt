package vorooooone.vorooooonMain

import android.content.Context
import android.util.AttributeSet
import android.widget.Button

//半透明になるエフェクトのボタン
class AlphaButton : Button {
    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    override fun setPressed(pressed: Boolean) {
        if (pressed) {
            this.alpha = 0.75f
        } else {
            this.alpha = 1.0f
        }
        super.setPressed(pressed)
    }

}