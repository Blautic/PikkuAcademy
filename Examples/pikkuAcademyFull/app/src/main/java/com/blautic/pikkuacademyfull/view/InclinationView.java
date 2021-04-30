package com.blautic.pikkuacademyfull.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.ArcShape;
import android.graphics.drawable.shapes.OvalShape;
import android.util.AttributeSet;
import android.widget.ImageView;
import androidx.core.content.ContextCompat;
import com.blautic.pikkuacademyfull.R;

public class InclinationView extends androidx.appcompat.widget.AppCompatImageView {

    private ShapeDrawable smallerCircle;

    public InclinationView(Context context) {
        super(context);
        init(null, 0);
    }

    public InclinationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public InclinationView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        initInclination();
    }

    private void initInclination(){
        smallerCircle= new ShapeDrawable( new OvalShape());
        smallerCircle.getPaint().setColor(Color.WHITE);
        smallerCircle.getPaint().setStyle(Paint.Style.STROKE);
        smallerCircle.getPaint().setStrokeWidth(3);
        drawArc(0);
    }

    public void drawArc(float angle) {
        drawArc(angle, 0);
    }

    public void drawArc(float angle, int offset) {
        int start = 270+offset;
        start %= 360;
        ArcShape arcShape = new ArcShape(start,angle);
        ShapeDrawable shapeDrawable = new ShapeDrawable(arcShape);
        shapeDrawable.setIntrinsicHeight(300);
        shapeDrawable.setIntrinsicWidth(300);
        Drawable[] d = {smallerCircle,shapeDrawable};
        LayerDrawable composite1 = new LayerDrawable(d);
        shapeDrawable.getPaint().setColor(ContextCompat.getColor(getContext(), R.color.pikku_green));
        setImageDrawable(composite1);
    }

}