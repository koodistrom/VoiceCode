package com.jaakkomantyla.voicecode;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;


public class CodeEditText extends androidx.appcompat.widget.AppCompatEditText {

        private Rect rect;
        private Paint paint;

        public CodeEditText(Context context, AttributeSet attrs) {
            super(context, attrs);
            rect = new Rect();
            paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.LTGRAY);
            paint.setTextSize(15);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            int baseline = getBaseline();
            for (int i = 0; i < getLineCount(); i++) {
                canvas.drawText("" + (i+1), rect.left, baseline, paint);
                baseline += getLineHeight();
            }
            super.onDraw(canvas);
        }
    }

