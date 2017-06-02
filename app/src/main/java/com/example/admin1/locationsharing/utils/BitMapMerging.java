package com.example.admin1.locationsharing.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.example.admin1.locationsharing.R;
import com.example.admin1.locationsharing.app.MyApplication;
import com.squareup.picasso.Picasso;

/**
 * Created by admin1 on 20/2/17.
 */

public class BitMapMerging {

    private static BitMapMerging instance = null;
    public static BitMapMerging getInstance() {
        if (instance == null) {
            instance = new BitMapMerging();
        }
        return instance;
    }

    public Bitmap inflateViewGetBitmap(String name) {

        ColorGenerator generator = ColorGenerator.MATERIAL;
        int color = generator.getRandomColor();
        CustomLog.i("Color","color:"+color);
        String nameFirstLetter = name.charAt(0) + "".toUpperCase();
        TextDrawable drawable = TextDrawable.builder()
                .buildRect(nameFirstLetter, color);
        View view = ((Activity) MyApplication.getCurrentActivityContext()).getLayoutInflater().inflate(R.layout.map_activity_marker_layout, null);
        ImageView image = (ImageView) view.findViewById(R.id.map_marker_imageView);
        image.setImageBitmap(drawableToBitmap(drawable));
        //Picasso.with(MyApplication.getCurrentActivityContext()).load("https://lh4.googleusercontent.com/-aOkut36hUIQ/AAAAAAAAAAI/AAAAAAAAAAA/ADPlhfLcuV2NVxv1FHD_O8y11siX5_5tFQ/s96-c/photo.jpg").into(image);
        //image.setImageBitmap(profile);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) MyApplication.getCurrentActivityContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }

    public Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        int width = drawable.getIntrinsicWidth();
        width = width > 0 ? width : 96; // Replaced the 1 by a 96
        int height = drawable.getIntrinsicHeight();
        height = height > 0 ? height : 96; // Replaced the 1 by a 96

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public Bitmap mergeBitmap(Bitmap bmp2, Bitmap bmp1) {
        Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(bmp1, new Matrix(), null);
        canvas.drawBitmap(bmp2, new Matrix(), null);
        return bmOverlay;
    }
}
