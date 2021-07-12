package sk.vyzyvacky.model

import android.content.Context
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatTextView

class FontFitTextView : AppCompatTextView {
    //Attributes
    private var mTestPaint: Paint? = null

    constructor(context: Context?) : super(context) {
        initialise()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initialise()
    }

    private fun initialise() {
        mTestPaint = Paint()
        mTestPaint!!.set(this.paint)
        //max size defaults to the initially specified text size unless it is too small
    }

    /* Re size the font so the specified text fits in the text box
     * assuming the text box is the specified width.
     */
    private fun refitText(text: String, textWidth: Int) {
        if (textWidth <= 0) return
        val targetWidth = textWidth - this.paddingLeft - this.paddingRight
        var hi = 100f
        var lo = 2f
        val threshold = 0.5f // How close we have to be
        mTestPaint!!.set(this.paint)
        while (hi - lo > threshold) {
            val size = (hi + lo) / 2
            mTestPaint!!.textSize = size
            if (mTestPaint!!.measureText(text) >= targetWidth) hi = size // too big
            else lo = size // too small
        }
        // Use lo so that we undershoot rather than overshoot
        this.setTextSize(TypedValue.COMPLEX_UNIT_PX, lo)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val parentWidth = MeasureSpec.getSize(widthMeasureSpec)
        val height = measuredHeight
        refitText(this.text.toString(), parentWidth)
        setMeasuredDimension(parentWidth, height)
    }

    override fun onTextChanged(text: CharSequence, start: Int, before: Int, after: Int) {
        refitText(text.toString(), this.width)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if (w != oldw) {
            refitText(this.text.toString(), w)
        }
    }
}