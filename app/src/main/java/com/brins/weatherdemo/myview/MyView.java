package com.brins.weatherdemo.myview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import java.util.Random;

public class MyView extends LinearLayout {


    public MyView(Context context) {
        super(context);
    }

    public MyView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MyView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MyView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


             Shader shader = new LinearGradient(0, 300, 1200, 250, Color.parseColor("#6173bf")
                    , Color.parseColor("#FFB1BFFA"), Shader.TileMode.CLAMP);
                Paint paint = new Paint();
                paint.setShader(shader);
                /*canvas.drawCircle(400,400,500,paint);*/
                canvas.drawRect(0, 0, 2000, 2000, paint);


    }
}
