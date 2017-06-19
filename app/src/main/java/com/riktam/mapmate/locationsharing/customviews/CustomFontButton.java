package com.riktam.mapmate.locationsharing.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;
import com.riktam.mapmate.locationsharing.R;

/**
 * Created by admin1 on 31/1/17.
 */

public class CustomFontButton extends android.support.v7.widget.AppCompatButton {
    public CustomFontButton(Context context) {
        super(context);
        init(context,null);
    }

    public CustomFontButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs);
    }

    public CustomFontButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs);
    }

    private void init(Context context, AttributeSet attrs){
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
