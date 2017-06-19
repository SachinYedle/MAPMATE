package com.riktam.mapmate.locationsharing.customviews;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.CheckBox;

/**
 * Created by admin1 on 31/1/17.
 */

public class CustomFontCheckBox extends android.support.v7.widget.AppCompatCheckBox {
    public CustomFontCheckBox(Context context) {
        super(context);
        init();
    }

    public CustomFontCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomFontCheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/ROBOTO-LIGHT_2.TTF");
        setTypeface(typeface);
    }
}
