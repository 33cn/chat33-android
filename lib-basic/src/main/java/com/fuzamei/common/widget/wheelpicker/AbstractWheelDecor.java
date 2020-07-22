package com.fuzamei.common.widget.wheelpicker;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public abstract class AbstractWheelDecor {
    public abstract void drawDecor(Canvas canvas, Rect rectLast, Rect rectNext, Paint paint);
}