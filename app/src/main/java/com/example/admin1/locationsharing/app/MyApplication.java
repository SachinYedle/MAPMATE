package com.example.admin1.locationsharing.app;

import android.app.AlertDialog;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;

import com.example.admin1.locationsharing.BuildConfig;
import com.example.admin1.locationsharing.R;
import com.example.admin1.locationsharing.services.LocationDataService;
import com.example.admin1.locationsharing.services.UserDataService;
import com.example.admin1.locationsharing.db.dao.DaoMaster;
import com.example.admin1.locationsharing.db.dao.DaoSession;
import com.example.admin1.locationsharing.interfaces.PositiveClick;
import com.example.admin1.locationsharing.utils.Constants;
import com.example.admin1.locationsharing.utils.CustomLog;
import com.example.admin1.locationsharing.utils.SharedPreferencesData;
import com.google.android.gms.maps.GoogleMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by admin1 on 30/11/16.
 */

public class MyApplication extends Application {
    private static SharedPreferencesData sharedPreferencesData;
    private Retrofit retrofit;
    private static Context applicationContext;
    private static MyApplication instance;
    private static Context context;
    private GoogleMap googleMap;
    private DaoMaster.DevOpenHelper devOpenHelper;
    private ProgressDialog mProgressDialog;


    @Override
    public void onCreate() {
        super.onCreate();
        initializeVariables();
        buildRetrofitClient();
        initializeDatabase(this);
        if (!BuildConfig.IS_PRODUCTION) {
            saveDataDb();
        }
    }

    public void saveDataDb() {
        CustomLog.d("MyApplication","SaveDB");
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();
            if (sd.canWrite()) {
                String currentDBPath = "//data//com.example.admin1.locationsharing//databases//" +
                        Constants.DB_NAME;
                String backupDBPath = "location_sharing";
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);
                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB)
                            .getChannel();
                    FileChannel dst = new FileOutputStream(backupDB)
                            .getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
            }
        } catch (Exception e) {
            CustomLog.e("MyApplication : ",e.getMessage());
        }
    }
    public void initializeVariables(){
        instance = this;
        sharedPreferencesData = new SharedPreferencesData(this);
        applicationContext = this.getApplicationContext();

    }

    private void initializeDatabase(Context context) {
        devOpenHelper = new DaoMaster.DevOpenHelper(context, Constants.DB_NAME, null);
    }


    public DaoSession getWritableDaoSession(Context context) {
        if(devOpenHelper == null){
            initializeDatabase(context);
        }
        SQLiteDatabase database = devOpenHelper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(database);
        return daoMaster.newSession();
    }

    /** Returns the Redable Dao Session*/
    public DaoSession getReadableDaoSession(Context context){
        if(devOpenHelper == null){
            initializeDatabase(context);
        }
        SQLiteDatabase database = devOpenHelper.getReadableDatabase();
        DaoMaster daoMaster = new DaoMaster(database);
        return daoMaster.newSession();
    }

    public UserDataService getUserDataService(){
        if(retrofit == null){
            buildRetrofitClient();
        }
        return retrofit.create(UserDataService.class);
    }
    private void buildRetrofitClient() {
        retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BuildConfig.HOST).build();
    }
    public LocationDataService getLocationDataService(final String token) {
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request request = chain.request();
                        request = request.newBuilder()
                                .addHeader("auth_token", token)
                                .build();
                        Response response = chain.proceed(request);
                        return response;
                    }
                })
                .build();

        retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient)
                .baseUrl(BuildConfig.HOST).build();

        return retrofit.create(LocationDataService.class);
    }
    public boolean isConnectedToInterNet() {
        ConnectivityManager connectivity = (ConnectivityManager) getCurrentActivityContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
        }
        return false;
    }
    public static Context getCurrentActivityContext() {
        if (context == null) {
            return applicationContext;
        } else {
            return context;
        }
    }
    public void setCurrentActivityContext(Context mContext) {
        context = mContext;
    }

    public static synchronized MyApplication getInstance() {
        if (instance == null) {
            instance = new MyApplication();
        }
        return instance;
    }

    public void showProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        mProgressDialog = new ProgressDialog(getCurrentActivityContext(), R.style
                .ProgressDialogStyle);
        mProgressDialog.setMessage(getString(R.string.loading));
        mProgressDialog.setCancelable(false);
        if (!mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }
    }

    /**
     * Hides progress bar
     */
    public void hideProgressDialog() {
        CustomLog.d("Progress","hide");
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    /**
     * Shows progress bar
     */
    public void showProgressDialog(String title, String description) {

        CustomLog.d("Progress","Show");
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        mProgressDialog = new ProgressDialog(getCurrentActivityContext(), R.style
                .ProgressDialogStyle);
        mProgressDialog.setTitle(title);
        mProgressDialog.setMessage(description);
        mProgressDialog.setCancelable(false);
        if (!mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }
    }
    public void updateTextInProgressDialog(String text){
        try {
            if (mProgressDialog.isShowing()) {
                mProgressDialog.setMessage(text);
            }
        } catch (Exception e) {
            CustomLog.e("Update Message Error",e.getMessage());
        }
    }

    public void showAlert(String alertMessage) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getCurrentActivityContext(), R
                .style.AlertDialogStyle);
        // Setting Dialog Title
        alertDialog.setTitle(getString(R.string.alert));
        // Setting Dialog Message
        alertDialog.setMessage(alertMessage);
        alertDialog.setCancelable(false);
        // Setting Positive "Yes" Button
        alertDialog.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener
                () {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();
    }


    public void showAlertWithPositiveButton(String alertMessage, String positiveText,
                                            PositiveClick positiveClick) {
        showAlertWithPositiveButton(null, alertMessage, positiveText, positiveClick);
    }


    public void showAlertWithPositiveNegativeButton(String title, String alertMessage, String
            negativeText, String positiveText, final PositiveClick positiveClick) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getCurrentActivityContext(), R
                .style.AlertDialogStyle);
        if (title != null) {
            alertDialog.setTitle(title);
        } else {
            alertDialog.setTitle(getString(R.string.alert));
        }
        alertDialog.setMessage(alertMessage);
        alertDialog.setCancelable(false);
        alertDialog.setNegativeButton(negativeText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.setPositiveButton(positiveText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                positiveClick.onClick();
            }
        });
        alertDialog.show();
    }

    /**
     * @param title         (String) : text in title
     * @param alertMessage  (String) : alertMessage
     * @param positiveText  (String) : text in positive button
     * @param positiveClick (PositiveClick) : positiveClick
     */
    public void showAlertWithPositiveButton(String title, String alertMessage, String positiveText,
                                            final PositiveClick positiveClick) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getCurrentActivityContext
                (), R.style.AlertDialogStyle);
        if (title != null) {
            alertDialog.setTitle(title);
        } else {
            alertDialog.setTitle(getString(R.string.alert));
        }
        alertDialog.setMessage(alertMessage);
        alertDialog.setCancelable(false);
        if (!positiveText.equals(getString(R.string.ok))) {
            alertDialog.setNegativeButton(getString(R.string.ok), new DialogInterface
                    .OnClickListener
                    () {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
        }
        alertDialog.setPositiveButton(positiveText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                positiveClick.onClick();
            }
        });
        alertDialog.show();
    }
}
