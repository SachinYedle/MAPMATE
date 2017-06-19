package com.riktam.mapmate.locationsharing.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.riktam.mapmate.locationsharing.R;

/**
 * Created by Sachin on 30/1/17.
 */

public class CustomFontTextView extends android.support.v7.widget.AppCompatTextView {

    public CustomFontTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context,attrs);
    }

    public CustomFontTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs);
    }

    public CustomFontTextView(Context context) {
        super(context);
        init(context,null);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray attributeArray = context.obtainStyledAttributes(attrs, R.styleable.CustomFontTextView);
        int textStyle = attributeArray.getInt(R.styleable.CustomFontTextView_textStyle, 0);

        if (textStyle == 1) {
            Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/ROBOTO-REGULAR_8.TTF");
            setTypeface(typeface);
        }else {
            Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/ROBOTO-LIGHT_2.TTF");
            setTypeface(typeface);
        }
    }
}