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
import com.example.admin1.locationsharing.db.dao.DaoMaster;
import com.example.admin1.locationsharing.db.dao.DaoSession;
import com.example.admin1.locationsharing.interfaces.PositiveClick;
import com.example.admin1.locationsharing.services.UserDataService;
import com.example.admin1.locationsharing.utils.Constants;
import com.example.admin1.locationsharing.utils.SharedPreferencesData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
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
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();
            if (sd.canWrite()) {
                String currentDBPath = "//data//com.example.admin1.locationsharing.temp//databases//" +
                        Constants.DB_NAME;
                String backupDBPath = Constants.DB_NAME;
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
            e.printStackTrace();
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
    public Context getCurrentActivityContext() {
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
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    /**
     * Shows progress bar
     */
    public void showProgressDialog(String title, String description) {
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
